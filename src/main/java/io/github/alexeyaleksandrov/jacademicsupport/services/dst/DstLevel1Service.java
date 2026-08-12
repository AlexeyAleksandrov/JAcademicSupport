package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level1.DstL1DisciplineResponse;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level1.DstL1DomainSection;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level1.DstL1FamilyResult;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level1.DstL1Response;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstTraceResponse;
import io.github.alexeyaleksandrov.jacademicsupport.models.Curriculum;
import io.github.alexeyaleksandrov.jacademicsupport.models.CurriculumProfession;
import io.github.alexeyaleksandrov.jacademicsupport.models.Discipline;
import io.github.alexeyaleksandrov.jacademicsupport.models.DisciplineCoverage;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CurriculumProfessionRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CurriculumRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DisciplineCoverageRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DisciplineRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillCanonicalRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService.FamilyInfo;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService.ProfessionWeight;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.DstContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DstLevel1Service {

    private final CurriculumRepository          curriculumRepository;
    private final CurriculumProfessionRepository professionRepository;
    private final DisciplineRepository          disciplineRepository;
    private final DisciplineCoverageRepository  coverageRepository;
    private final SkillCanonicalRepository      skillCanonicalRepository;
    private final DstQueryService               dstQueryService;
    private final DstCombinationService         combinationService;

    @Transactional(readOnly = true)
    public DstL1Response analyzeLevel1(Long curriculumId, String domain) {
        DstL1Response resp = new DstL1Response();
        resp.setCurriculumId(curriculumId);
        resp.setDomain(domain);

        Optional<Curriculum> optCurr = curriculumRepository.findById(curriculumId);
        if (optCurr.isEmpty()) {
            resp.setError("Учебный план не найден: " + curriculumId);
            return resp;
        }
        resp.setCurriculumName(optCurr.get().getName());

        List<CurriculumProfession> cpList = professionRepository.findByCurriculumId(curriculumId);
        if (cpList.isEmpty()) {
            resp.setError("В учебном плане не указаны профессии.");
            return resp;
        }
        double totalWeight = cpList.stream().mapToDouble(CurriculumProfession::getWeight).sum();
        List<ProfessionWeight> profs = cpList.stream()
                .map(cp -> new ProfessionWeight(
                        cp.getProfessionCode(),
                        cp.getProfessionName(),
                        totalWeight > 0 ? cp.getWeight() / totalWeight : 1.0 / cpList.size()))
                .sorted(Comparator.comparingDouble(ProfessionWeight::weight).reversed())
                .collect(Collectors.toList());
        resp.setProfessions(profs);

        List<Discipline> allDiscs = disciplineRepository.findByCurriculumId(curriculumId);
        List<Long> allDiscIds = allDiscs.stream().map(Discipline::getId).collect(Collectors.toList());

        List<DisciplineCoverage> allCoverage = allDiscIds.isEmpty()
                ? List.of()
                : coverageRepository.findByDisciplineIdIn(allDiscIds);

        CanonicalMeta canonicalMeta = loadCanonicalMeta(allCoverage);

        Map<Long, Integer> discHoursMap = allDiscs.stream()
                .collect(Collectors.toMap(Discipline::getId,
                        d -> d.getTotalHours() != null ? d.getTotalHours() : 0));

        Map<String, Double> familyProportionalHours = aggregateFamilyHours(
                allDiscs, allCoverage, discHoursMap, canonicalMeta, domain);

        Set<String> vacFamilies = new LinkedHashSet<>();
        for (ProfessionWeight pw : profs) {
            dstQueryService.getFamiliesForDomain(pw.professionCode(), domain).stream()
                    .map(FamilyInfo::techFamily).forEach(vacFamilies::add);
        }

        Set<String> allFamilies = new LinkedHashSet<>(vacFamilies);
        familyProportionalHours.keySet().stream()
                .filter(f -> f != null && !f.isEmpty())
                .forEach(allFamilies::add);

        int nFamilies = Math.max(1, vacFamilies.size());
        int totalDomainHours = (int) Math.round(
                familyProportionalHours.values().stream().mapToDouble(Double::doubleValue).sum());
        resp.setTotalDomainHours(totalDomainHours);
        resp.setNFamilies(nFamilies);

        String primaryProfCode = profs.get(0).professionCode();
        List<DstL1FamilyResult> results = new ArrayList<>();
        for (String family : allFamilies) {
            double supplyHoursD = familyProportionalHours.getOrDefault(family, 0.0);
            int    supplyHours  = (int) Math.round(supplyHoursD);
            double supply       = totalDomainHours > 0 ? Math.min(1.0, supplyHoursD / totalDomainHours) : 0.0;

            DstContext ctx = new DstContext(primaryProfCode, domain, family, null);
            DstTraceResponse trace = combinationService.computeWeightedFamily(
                    ctx, profs, supply, dstQueryService, nFamilies);

            results.add(buildFamilyResult(family, supply, supplyHours, trace));
        }

        results.sort(Comparator.comparingDouble(DstL1FamilyResult::getBetp).reversed());
        resp.setFamilies(results);
        return resp;
    }

    @Transactional(readOnly = true)
    public DstL1DisciplineResponse analyzeLevel1ForDiscipline(Long disciplineId) {
        DstL1DisciplineResponse resp = new DstL1DisciplineResponse();
        resp.setDisciplineId(disciplineId);

        Optional<Discipline> optDisc = disciplineRepository.findById(disciplineId);
        if (optDisc.isEmpty()) {
            resp.setError("Дисциплина не найдена: " + disciplineId);
            return resp;
        }
        Discipline disc = optDisc.get();
        resp.setDisciplineName(disc.getName());
        int totalHours = disc.getTotalHours() != null ? disc.getTotalHours() : 0;
        resp.setTotalHours(totalHours);

        List<CurriculumProfession> cpList = professionRepository.findByCurriculumId(disc.getCurriculumId());
        if (cpList.isEmpty()) {
            resp.setError("В учебном плане дисциплины не указаны профессии.");
            return resp;
        }
        double totalWeight = cpList.stream().mapToDouble(CurriculumProfession::getWeight).sum();
        List<ProfessionWeight> profs = cpList.stream()
                .map(cp -> new ProfessionWeight(
                        cp.getProfessionCode(),
                        cp.getProfessionName(),
                        totalWeight > 0 ? cp.getWeight() / totalWeight : 1.0 / cpList.size()))
                .sorted(Comparator.comparingDouble(ProfessionWeight::weight).reversed())
                .collect(Collectors.toList());
        resp.setProfessions(profs);

        List<DisciplineCoverage> covList = coverageRepository.findByDisciplineId(disciplineId);
        CanonicalMeta canonicalMeta = loadCanonicalMeta(covList);

        int sumAllCoverage = covList.stream()
                .mapToInt(c -> c.getHours() != null ? c.getHours() : 0).sum();
        if (sumAllCoverage == 0 || totalHours == 0) {
            resp.setError("Нет данных о покрытии для дисциплины.");
            return resp;
        }

        Map<String, Map<String, Double>> domainFamilyHours = new LinkedHashMap<>();
        for (DisciplineCoverage cov : covList) {
            String dom    = getEffectiveDomain(cov, canonicalMeta);
            String family = getEffectiveTechFamily(cov, canonicalMeta);
            if (dom == null || dom.isEmpty() || family == null || family.isEmpty()) continue;
            int hours = cov.getHours() != null ? cov.getHours() : 0;
            double proportional = (double) hours / sumAllCoverage * totalHours;
            domainFamilyHours
                    .computeIfAbsent(dom, k -> new LinkedHashMap<>())
                    .merge(family, proportional, Double::sum);
        }

        String primaryProfCode = profs.get(0).professionCode();
        List<DstL1DomainSection> sections = new ArrayList<>();

        for (Map.Entry<String, Map<String, Double>> domEntry : domainFamilyHours.entrySet()) {
            String domain = domEntry.getKey();
            Map<String, Double> familyHours = domEntry.getValue();

            int domainHours = (int) Math.round(
                    familyHours.values().stream().mapToDouble(Double::doubleValue).sum());

            Set<String> vacFamilies = new LinkedHashSet<>();
            for (ProfessionWeight pw : profs) {
                dstQueryService.getFamiliesForDomain(pw.professionCode(), domain).stream()
                        .map(FamilyInfo::techFamily).forEach(vacFamilies::add);
            }
            Set<String> allFamilies = new LinkedHashSet<>(vacFamilies);
            allFamilies.addAll(familyHours.keySet());

            int nFamilies = Math.max(1, vacFamilies.size());

            List<DstL1FamilyResult> familyResults = new ArrayList<>();
            for (String family : allFamilies) {
                double supplyHoursD = familyHours.getOrDefault(family, 0.0);
                int    supplyHours  = (int) Math.round(supplyHoursD);
                double supply       = domainHours > 0 ? Math.min(1.0, supplyHoursD / domainHours) : 0.0;

                DstContext ctx = new DstContext(primaryProfCode, domain, family, null);
                DstTraceResponse trace = combinationService.computeWeightedFamily(
                        ctx, profs, supply, dstQueryService, nFamilies);

                familyResults.add(buildFamilyResult(family, supply, supplyHours, trace));
            }
            familyResults.sort(Comparator.comparingDouble(DstL1FamilyResult::getBetp).reversed());

            DstL1DomainSection section = new DstL1DomainSection();
            section.setDomain(domain);
            section.setDomainHours(domainHours);
            section.setNFamilies(nFamilies);
            section.setFamilies(familyResults);
            sections.add(section);
        }

        sections.sort(Comparator.comparingInt(DstL1DomainSection::getDomainHours).reversed());
        resp.setDomains(sections);
        return resp;
    }

    private DstL1FamilyResult buildFamilyResult(String family, double supply,
                                                 int supplyHours, DstTraceResponse trace) {
        DstL1FamilyResult r = new DstL1FamilyResult();
        r.setTechFamily(family);
        r.setMT(trace.getMT());
        r.setMU(trace.getMU());
        r.setMF(trace.getMF());
        r.setK(trace.getK());
        r.setBetp(trace.getBetp());
        r.setSupply(supply);
        r.setSupplyHours(supplyHours);
        r.setDelta(trace.getDelta());
        r.setUsedYager(trace.isUsedYager());
        r.setRecommendation(trace.getRecommendation());
        r.setSources(trace.getSources());
        r.setCombinations(trace.getCombinations());
        return r;
    }

    private Map<String, Double> aggregateFamilyHours(List<Discipline> allDiscs,
                                                       List<DisciplineCoverage> allCoverage,
                                                       Map<Long, Integer> discHoursMap,
                                                       CanonicalMeta meta,
                                                       String targetDomain) {
        Map<Long, List<DisciplineCoverage>> byDisc = allCoverage.stream()
                .collect(Collectors.groupingBy(DisciplineCoverage::getDisciplineId));

        Map<String, Double> familyHours = new HashMap<>();
        for (Discipline disc : allDiscs) {
            int discTotalHours = discHoursMap.getOrDefault(disc.getId(), 0);
            if (discTotalHours == 0) continue;
            List<DisciplineCoverage> covList = byDisc.getOrDefault(disc.getId(), List.of());
            if (covList.isEmpty()) continue;

            int sumAllCoverage = covList.stream()
                    .mapToInt(c -> c.getHours() != null ? c.getHours() : 0).sum();
            if (sumAllCoverage == 0) continue;

            Map<String, Integer> familyHoursInDisc = new HashMap<>();
            for (DisciplineCoverage cov : covList) {
                String dom    = getEffectiveDomain(cov, meta);
                String family = getEffectiveTechFamily(cov, meta);
                if (dom == null || !dom.equals(targetDomain)) continue;
                if (family == null || family.isEmpty()) continue;
                int hours = cov.getHours() != null ? cov.getHours() : 0;
                familyHoursInDisc.merge(family, hours, Integer::sum);
            }
            for (Map.Entry<String, Integer> e : familyHoursInDisc.entrySet()) {
                double fraction = (double) e.getValue() / sumAllCoverage;
                familyHours.merge(e.getKey(), fraction * discTotalHours, Double::sum);
            }
        }
        return familyHours;
    }

    private String getEffectiveDomain(DisciplineCoverage cov, CanonicalMeta meta) {
        if (cov.getDomain() != null && !cov.getDomain().isEmpty()) return cov.getDomain();
        if (cov.getCanonicalId() != null) return meta.domainMap.get(cov.getCanonicalId());
        return null;
    }

    private String getEffectiveTechFamily(DisciplineCoverage cov, CanonicalMeta meta) {
        if (cov.getTechFamily() != null && !cov.getTechFamily().isEmpty()) return cov.getTechFamily();
        if (cov.getCanonicalId() != null) {
            String family = meta.familyMap.get(cov.getCanonicalId());
            return family != null ? family : "Прочее";
        }
        return null;
    }

    private CanonicalMeta loadCanonicalMeta(List<DisciplineCoverage> coverage) {
        Set<Long> canonicalIds = coverage.stream()
                .map(DisciplineCoverage::getCanonicalId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> domainMap  = new HashMap<>();
        Map<Long, String> familyMap  = new HashMap<>();
        if (!canonicalIds.isEmpty()) {
            for (SkillCanonical sc : skillCanonicalRepository.findAllById(canonicalIds)) {
                if (sc.getDomain()     != null) domainMap.put(sc.getId(), sc.getDomain());
                if (sc.getTechFamily() != null) familyMap.put(sc.getId(), sc.getTechFamily());
            }
        }
        return new CanonicalMeta(domainMap, familyMap);
    }

    private record CanonicalMeta(Map<Long, String> domainMap, Map<Long, String> familyMap) {}
}
