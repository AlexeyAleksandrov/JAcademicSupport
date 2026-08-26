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
    private final DstSettingsService            settingsService;
    private final DstLevelMetaFactory           metaFactory;

    @Transactional(readOnly = true)
    public DstL1Response analyzeLevel1(Long curriculumId, String domain) {
        return analyzeLevel1(curriculumId, domain, DstCalcOptions.defaults(settingsService.get()));
    }

    @Transactional(readOnly = true)
    public DstL1Response analyzeLevel1(Long curriculumId, String domain, DstCalcOptions options) {
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

        Set<Long> touchedDisciplineIds = new LinkedHashSet<>();
        Map<String, Double> familyProportionalHours = aggregateFamilyHours(
                allDiscs, allCoverage, discHoursMap, canonicalMeta, domain, options, touchedDisciplineIds);

        Set<String> vacFamilies = new LinkedHashSet<>();
        for (ProfessionWeight pw : profs) {
            dstQueryService.getFamiliesForDomain(pw.professionCode(), domain).stream()
                    .map(FamilyInfo::techFamily).forEach(vacFamilies::add);
        }

        Set<String> allFamilies = new LinkedHashSet<>(vacFamilies);
        familyProportionalHours.keySet().stream()
                .filter(f -> f != null && !f.isEmpty())
                .forEach(allFamilies::add);

        int nFamilies = combinationService.isNClustersAuto()
                ? Math.max(1, allFamilies.size())
                : Math.max(1, vacFamilies.size());

        double coveredHoursD = familyProportionalHours.values().stream()
                .mapToDouble(Double::doubleValue).sum();
        int coveredHours = (int) Math.round(coveredHoursD);
        int independentHours = coveredHours;
        if (options.explicitFamilies()) {
            DstCalcOptions derivedOptions = new DstCalcOptions(
                    options.domainMode(), DstCalcOptions.CoverageMode.DERIVED, options.skillMode(),
                    options.hoursBase(), options.budgetMode(), options.budgetHours(), options.disciplineId());
            Map<String, Double> derivedHours = aggregateFamilyHours(
                    allDiscs, allCoverage, discHoursMap, canonicalMeta, domain,
                    derivedOptions, new LinkedHashSet<>());
            independentHours = (int) Math.round(
                    derivedHours.values().stream().mapToDouble(Double::doubleValue).sum());
        }
        int budgetHours = resolveBudget(options, independentHours, touchedDisciplineIds, discHoursMap);

        resp.setTotalDomainHours(budgetHours);
        resp.setNFamilies(nFamilies);

        String primaryProfCode = profs.get(0).professionCode();
        List<DstL1FamilyResult> results = new ArrayList<>();
        for (String family : allFamilies) {
            double supplyHoursD = familyProportionalHours.getOrDefault(family, 0.0);
            int    supplyHours  = (int) Math.round(supplyHoursD);
            double supply       = DstHoursPolicy.supply(supplyHoursD, budgetHours);

            DstContext ctx = new DstContext(primaryProfCode, domain, family, null);
            DstTraceResponse trace = combinationService.computeWeightedFamily(
                    ctx, profs, supply, dstQueryService, nFamilies);

            results.add(buildFamilyResult(family, supply, supplyHours, trace));
        }

        results.sort(Comparator.comparingDouble(DstL1FamilyResult::getBetp).reversed());
        resp.setFamilies(results);
        resp.setMeta(metaFactory.build(options, budgetHours, coveredHours,
                budgetLabel(options, touchedDisciplineIds.size(), domain), nFamilies));
        return resp;
    }

    @Transactional(readOnly = true)
    public DstL1DisciplineResponse analyzeLevel1ForDiscipline(Long disciplineId) {
        return analyzeLevel1ForDiscipline(disciplineId, DstCalcOptions.defaults(settingsService.get()));
    }

    @Transactional(readOnly = true)
    public DstL1DisciplineResponse analyzeLevel1ForDiscipline(Long disciplineId, DstCalcOptions options) {
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

        if (covList.isEmpty() || totalHours == 0) {
            resp.setError("Нет данных о покрытии для дисциплины.");
            return resp;
        }

        Map<String, Map<String, Double>> domainFamilyHours = new LinkedHashMap<>();
        Set<String> domainsInDisc = new LinkedHashSet<>();
        for (DisciplineCoverage cov : covList) {
            String dom = getEffectiveDomain(cov, canonicalMeta);
            if (dom != null && !dom.isEmpty()) domainsInDisc.add(dom);
        }
        for (String dom : domainsInDisc) {
            Map<String, Integer> famEff = computeEffectiveFamilyHours(covList, dom, canonicalMeta, options);
            int sumFamEff = famEff.values().stream().mapToInt(i -> i).sum();
            if (sumFamEff == 0) continue;
            double scale = DstHoursPolicy.coverageScale(
                    options.explicitFamilies(), totalHours, sumFamEff);
            for (Map.Entry<String, Integer> fe : famEff.entrySet()) {
                domainFamilyHours
                        .computeIfAbsent(dom, k -> new LinkedHashMap<>())
                        .merge(fe.getKey(), fe.getValue() * scale, Double::sum);
            }
        }

        String primaryProfCode = profs.get(0).professionCode();
        List<DstL1DomainSection> sections = new ArrayList<>();

        for (Map.Entry<String, Map<String, Double>> domEntry : domainFamilyHours.entrySet()) {
            String domain = domEntry.getKey();
            Map<String, Double> familyHours = domEntry.getValue();

            Integer inherited = options.inheritedBudget();
            int domainHours = inherited != null ? inherited : totalHours;

            Set<String> vacFamilies = new LinkedHashSet<>();
            for (ProfessionWeight pw : profs) {
                dstQueryService.getFamiliesForDomain(pw.professionCode(), domain).stream()
                        .map(FamilyInfo::techFamily).forEach(vacFamilies::add);
            }
            Set<String> allFamilies = new LinkedHashSet<>(vacFamilies);
            allFamilies.addAll(familyHours.keySet());

            int nFamilies = combinationService.isNClustersAuto()
                    ? Math.max(1, allFamilies.size())
                    : Math.max(1, vacFamilies.size());

            List<DstL1FamilyResult> familyResults = new ArrayList<>();
            for (String family : allFamilies) {
                double supplyHoursD = familyHours.getOrDefault(family, 0.0);
                int    supplyHours  = (int) Math.round(supplyHoursD);
                double supply       = DstHoursPolicy.supply(supplyHoursD, domainHours);

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
        int allCoveredHours = sections.stream()
                .flatMap(section -> section.getFamilies().stream())
                .mapToInt(DstL1FamilyResult::getSupplyHours)
                .sum();
        resp.setMeta(metaFactory.build(options, totalHours, allCoveredHours,
                "часы дисциплины (" + totalHours + " ч.)"));
        return resp;
    }

    /** Resolves T for L1: inherited parent budget, Σ touched disciplines or the aggregated coverage. */
    private int resolveBudget(DstCalcOptions options, int independentHours,
                              Set<Long> touchedDisciplineIds, Map<Long, Integer> discHoursMap) {
        return DstHoursPolicy.resolveBudget(options, independentHours, touchedDisciplineIds, discHoursMap);
    }

    private String budgetLabel(DstCalcOptions options, int touchedCount, String domain) {
        Integer inherited = options.inheritedBudget();
        if (inherited != null) {
            return "бюджет домена «" + domain + "», унаследованный с L0";
        }
        return switch (options.hoursBase()) {
            case TOUCHED_DISCIPLINES ->
                    "Σ часов дисциплин домена (" + touchedCount + " дисц.)";
            case SINGLE_DISCIPLINE ->
                    "часы выбранной дисциплины #" + options.disciplineId();
            case CURRICULUM -> "условный объём дисциплин, связанных с доменом «" + domain + "»";
        };
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
                                                       String targetDomain,
                                                       DstCalcOptions options,
                                                       Set<Long> touchedDisciplineIds) {
        Map<Long, List<DisciplineCoverage>> byDisc = allCoverage.stream()
                .collect(Collectors.groupingBy(DisciplineCoverage::getDisciplineId));

        Map<String, Double> familyHours = new HashMap<>();
        for (Discipline disc : allDiscs) {
            int discTotalHours = discHoursMap.getOrDefault(disc.getId(), 0);
            if (discTotalHours == 0) continue;
            List<DisciplineCoverage> covList = byDisc.getOrDefault(disc.getId(), List.of());
            if (covList.isEmpty()) continue;

            Map<String, Integer> famEff = computeEffectiveFamilyHours(covList, targetDomain, meta, options);
            int sumFamEff = famEff.values().stream().mapToInt(i -> i).sum();
            if (sumFamEff == 0) continue;
            touchedDisciplineIds.add(disc.getId());

            double scale = DstHoursPolicy.coverageScale(
                    options.explicitFamilies(), discTotalHours, sumFamEff);
            for (Map.Entry<String, Integer> e : famEff.entrySet()) {
                familyHours.merge(e.getKey(), e.getValue() * scale, Double::sum);
            }
        }
        return familyHours;
    }

    private String getEffectiveDomain(DisciplineCoverage cov, CanonicalMeta meta) {
        if (cov.getDomain() != null && !cov.getDomain().isEmpty()) return cov.getDomain();
        if (cov.getCanonicalId() != null) return meta.domainMap.get(cov.getCanonicalId());
        if (cov.getTechFamily() != null) return meta.techFamilyDomainMap.get(cov.getTechFamily());
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
        Set<String> techFamilies = coverage.stream()
                .filter(c -> c.getDomain() == null && c.getCanonicalId() == null)
                .map(DisciplineCoverage::getTechFamily)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, String> techFamilyDomainMap = techFamilies.isEmpty() ? Map.<String, String>of() :
                skillCanonicalRepository.findDomainsByTechFamilies(techFamilies).stream()
                        .collect(Collectors.toMap(
                                r -> (String) r[0], r -> (String) r[1], (a, b) -> a));
        return new CanonicalMeta(domainMap, familyMap, techFamilyDomainMap);
    }

    /**
     * Hours each tech family contributes inside one discipline for the target domain.
     *
     * EXPLICIT mode counts only rows that name the family directly (no canonical_id);
     * DERIVED mode keeps the FAMILY ▶ SKILL priority used historically.
     */
    private Map<String, Integer> computeEffectiveFamilyHours(
            List<DisciplineCoverage> covList, String targetDomain, CanonicalMeta meta,
            DstCalcOptions options) {

        if (options.explicitFamilies()) {
            Map<String, Integer> explicit = new HashMap<>();
            for (DisciplineCoverage cov : covList) {
                if (cov.getCanonicalId() != null) continue;
                if (cov.getTechFamily() == null || cov.getTechFamily().isEmpty()) continue;
                if (!targetDomain.equals(getEffectiveDomain(cov, meta))) continue;
                explicit.merge(cov.getTechFamily(), cov.getHours() != null ? cov.getHours() : 0, Integer::sum);
            }
            return explicit;
        }

        Map<String, Map<String, Integer>> byFamilyAndLevel = new HashMap<>();
        for (DisciplineCoverage cov : covList) {
            String dom = getEffectiveDomain(cov, meta);
            if (!targetDomain.equals(dom)) continue;
            String family = getEffectiveTechFamily(cov, meta);
            if (family == null || family.isEmpty()) continue;
            int hours = cov.getHours() != null ? cov.getHours() : 0;
            String level = cov.getCanonicalId() != null ? "SKILL" : "FAMILY";
            byFamilyAndLevel
                    .computeIfAbsent(family, k -> new HashMap<>())
                    .merge(level, hours, Integer::sum);
        }

        Map<String, Integer> effective = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> e : byFamilyAndLevel.entrySet()) {
            Map<String, Integer> levels = e.getValue();
            int eff = levels.containsKey("FAMILY") ? levels.get("FAMILY")
                    : levels.getOrDefault("SKILL", 0);
            effective.put(e.getKey(), eff);
        }
        return effective;
    }

    private record CanonicalMeta(Map<Long, String> domainMap, Map<Long, String> familyMap,
                                  Map<String, String> techFamilyDomainMap) {}
}
