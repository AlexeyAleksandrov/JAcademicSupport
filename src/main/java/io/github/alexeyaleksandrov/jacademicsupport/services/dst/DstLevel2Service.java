package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level2.DstL2DisciplineResponse;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level2.DstL2FamilySection;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level2.DstL2Response;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level2.DstL2SkillResult;
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
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService.ProfessionWeight;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService.SkillInfo;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.DstContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DstLevel2Service {

    private final CurriculumRepository           curriculumRepository;
    private final CurriculumProfessionRepository professionRepository;
    private final DisciplineRepository           disciplineRepository;
    private final DisciplineCoverageRepository   coverageRepository;
    private final SkillCanonicalRepository       skillCanonicalRepository;
    private final DstQueryService                dstQueryService;
    private final DstCombinationService          combinationService;

    @Transactional(readOnly = true)
    public DstL2Response analyzeLevel2(Long curriculumId, String domain, String techFamily) {
        DstL2Response resp = new DstL2Response();
        resp.setCurriculumId(curriculumId);
        resp.setDomain(domain);
        resp.setTechFamily(techFamily);

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

        CanonicalMeta meta = loadCanonicalMeta(allCoverage);

        Map<Long, Integer> discHoursMap = allDiscs.stream()
                .collect(Collectors.toMap(Discipline::getId,
                        d -> d.getTotalHours() != null ? d.getTotalHours() : 0));

        Map<Long, Double> skillProportionalHours = aggregateSkillHours(
                allDiscs, allCoverage, discHoursMap, meta, domain, techFamily);

        String primaryProfCode = profs.get(0).professionCode();

        Set<Long> vacSkillIds = new LinkedHashSet<>();
        for (ProfessionWeight pw : profs) {
            dstQueryService.getSkillsByDomainAndFamily(pw.professionCode(), domain, techFamily)
                    .stream().map(SkillInfo::canonicalId).forEach(vacSkillIds::add);
        }

        Set<Long> allSkillIds = new LinkedHashSet<>(vacSkillIds);
        allSkillIds.addAll(skillProportionalHours.keySet());

        int nSkills = Math.max(1, vacSkillIds.size());
        int totalFamilyHours = (int) Math.round(
                skillProportionalHours.values().stream().mapToDouble(Double::doubleValue).sum());
        resp.setNSkills(nSkills);
        resp.setTotalFamilyHours(totalFamilyHours);

        Map<Long, String> skillNameMap = buildSkillNameMap(allSkillIds, meta, allCoverage);

        List<DstL2SkillResult> results = new ArrayList<>();
        for (Long canonicalId : allSkillIds) {
            double supplyHoursD = skillProportionalHours.getOrDefault(canonicalId, 0.0);
            int    supplyHours  = (int) Math.round(supplyHoursD);
            double supply       = totalFamilyHours > 0 ? Math.min(1.0, supplyHoursD / totalFamilyHours) : 0.0;

            DstContext ctx = new DstContext(primaryProfCode, domain, techFamily, canonicalId);
            DstTraceResponse trace = combinationService.computeWeightedSkill(
                    ctx, profs, supply, dstQueryService, nSkills);

            results.add(buildSkillResult(canonicalId, skillNameMap.getOrDefault(canonicalId, "Навык #" + canonicalId),
                    supply, supplyHours, trace));
        }

        results.sort(Comparator.comparingDouble(DstL2SkillResult::getBetp).reversed());
        resp.setSkills(results);
        return resp;
    }

    @Transactional(readOnly = true)
    public DstL2DisciplineResponse analyzeLevel2ForDisciplineAndFamily(Long disciplineId,
                                                                        String domain,
                                                                        String techFamily) {
        DstL2DisciplineResponse resp = new DstL2DisciplineResponse();
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
        CanonicalMeta meta = loadCanonicalMeta(covList);

        int sumAllCoverage = covList.stream()
                .mapToInt(c -> c.getHours() != null ? c.getHours() : 0).sum();
        if (sumAllCoverage == 0 || totalHours == 0) {
            resp.setError("Нет данных о покрытии для дисциплины.");
            return resp;
        }

        Map<Long, Double> skillProportionalHours = new LinkedHashMap<>();
        for (DisciplineCoverage cov : covList) {
            String covDomain = getEffectiveDomain(cov, meta);
            String covFamily = getEffectiveTechFamily(cov, meta);
            Long   covCanonicalId = cov.getCanonicalId();
            if (!domain.equals(covDomain) || !techFamily.equals(covFamily)) continue;
            if (covCanonicalId == null) continue;
            int hours = cov.getHours() != null ? cov.getHours() : 0;
            double proportional = (double) hours / sumAllCoverage * totalHours;
            skillProportionalHours.merge(covCanonicalId, proportional, Double::sum);
        }

        String primaryProfCode = profs.get(0).professionCode();
        Set<Long> vacSkillIds = new LinkedHashSet<>();
        for (ProfessionWeight pw : profs) {
            dstQueryService.getSkillsByDomainAndFamily(pw.professionCode(), domain, techFamily)
                    .stream().map(SkillInfo::canonicalId).forEach(vacSkillIds::add);
        }

        Set<Long> allSkillIds = new LinkedHashSet<>(vacSkillIds);
        allSkillIds.addAll(skillProportionalHours.keySet());

        int nSkills = Math.max(1, vacSkillIds.size());
        double familyHoursNorm = skillProportionalHours.values().stream().mapToDouble(Double::doubleValue).sum();
        int familyHours = (int) Math.round(familyHoursNorm);

        Map<Long, String> skillNameMap = buildSkillNameMap(allSkillIds, meta, covList);

        List<DstL2SkillResult> results = new ArrayList<>();
        for (Long canonicalId : allSkillIds) {
            double supplyHoursD = skillProportionalHours.getOrDefault(canonicalId, 0.0);
            int    supplyHours  = (int) Math.round(supplyHoursD);
            double supply       = familyHoursNorm > 0 ? Math.min(1.0, supplyHoursD / familyHoursNorm) : 0.0;

            DstContext ctx = new DstContext(primaryProfCode, domain, techFamily, canonicalId);
            DstTraceResponse trace = combinationService.computeWeightedSkill(
                    ctx, profs, supply, dstQueryService, nSkills);

            results.add(buildSkillResult(canonicalId, skillNameMap.getOrDefault(canonicalId, "Навык #" + canonicalId),
                    supply, supplyHours, trace));
        }
        results.sort(Comparator.comparingDouble(DstL2SkillResult::getBetp).reversed());

        DstL2FamilySection section = new DstL2FamilySection();
        section.setDomain(domain);
        section.setTechFamily(techFamily);
        section.setFamilyHours(familyHours);
        section.setNSkills(nSkills);
        section.setSkills(results);

        resp.setSections(List.of(section));
        return resp;
    }

    private DstL2SkillResult buildSkillResult(Long canonicalId, String skillName, double supply,
                                               int supplyHours, DstTraceResponse trace) {
        DstL2SkillResult r = new DstL2SkillResult();
        r.setCanonicalId(canonicalId);
        r.setSkillName(skillName);
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

    private Map<Long, Double> aggregateSkillHours(List<Discipline> allDiscs,
                                                   List<DisciplineCoverage> allCoverage,
                                                   Map<Long, Integer> discHoursMap,
                                                   CanonicalMeta meta,
                                                   String targetDomain,
                                                   String targetFamily) {
        Map<Long, List<DisciplineCoverage>> byDisc = allCoverage.stream()
                .collect(Collectors.groupingBy(DisciplineCoverage::getDisciplineId));

        Map<Long, Double> skillHours = new LinkedHashMap<>();
        for (Discipline disc : allDiscs) {
            int discTotalHours = discHoursMap.getOrDefault(disc.getId(), 0);
            if (discTotalHours == 0) continue;
            List<DisciplineCoverage> covList = byDisc.getOrDefault(disc.getId(), List.of());
            if (covList.isEmpty()) continue;

            int sumAllCoverage = covList.stream()
                    .mapToInt(c -> c.getHours() != null ? c.getHours() : 0).sum();
            if (sumAllCoverage == 0) continue;

            for (DisciplineCoverage cov : covList) {
                String dom    = getEffectiveDomain(cov, meta);
                String family = getEffectiveTechFamily(cov, meta);
                Long   canonicalId = cov.getCanonicalId();
                if (!targetDomain.equals(dom) || !targetFamily.equals(family)) continue;
                if (canonicalId == null) continue;
                int hours = cov.getHours() != null ? cov.getHours() : 0;
                double fraction = (double) hours / sumAllCoverage;
                skillHours.merge(canonicalId, fraction * discTotalHours, Double::sum);
            }
        }
        return skillHours;
    }

    private Map<Long, String> buildSkillNameMap(Set<Long> canonicalIds,
                                                 CanonicalMeta meta,
                                                 List<DisciplineCoverage> coverage) {
        Map<Long, String> nameMap = new HashMap<>();
        for (Long id : canonicalIds) {
            String name = meta.nameMap().get(id);
            if (name != null) nameMap.put(id, name);
        }
        Set<Long> missing = canonicalIds.stream()
                .filter(id -> !nameMap.containsKey(id)).collect(Collectors.toSet());
        if (!missing.isEmpty()) {
            skillCanonicalRepository.findAllById(missing)
                    .forEach(sc -> nameMap.put(sc.getId(), sc.getName()));
        }
        return nameMap;
    }

    private String getEffectiveDomain(DisciplineCoverage cov, CanonicalMeta meta) {
        if (cov.getDomain() != null && !cov.getDomain().isEmpty()) return cov.getDomain();
        if (cov.getCanonicalId() != null) return meta.domainMap().get(cov.getCanonicalId());
        return null;
    }

    private String getEffectiveTechFamily(DisciplineCoverage cov, CanonicalMeta meta) {
        if (cov.getTechFamily() != null && !cov.getTechFamily().isEmpty()) return cov.getTechFamily();
        if (cov.getCanonicalId() != null) {
            String family = meta.familyMap().get(cov.getCanonicalId());
            return family != null ? family : "Прочее";
        }
        return null;
    }

    private CanonicalMeta loadCanonicalMeta(List<DisciplineCoverage> coverage) {
        Set<Long> ids = coverage.stream()
                .map(DisciplineCoverage::getCanonicalId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> domainMap = new HashMap<>();
        Map<Long, String> familyMap = new HashMap<>();
        Map<Long, String> nameMap   = new HashMap<>();
        if (!ids.isEmpty()) {
            for (SkillCanonical sc : skillCanonicalRepository.findAllById(ids)) {
                if (sc.getDomain()     != null) domainMap.put(sc.getId(), sc.getDomain());
                if (sc.getTechFamily() != null) familyMap.put(sc.getId(), sc.getTechFamily());
                if (sc.getName()       != null) nameMap.put(sc.getId(), sc.getName());
            }
        }
        return new CanonicalMeta(domainMap, familyMap, nameMap);
    }

    private record CanonicalMeta(Map<Long, String> domainMap,
                                  Map<Long, String> familyMap,
                                  Map<Long, String> nameMap) {}
}
