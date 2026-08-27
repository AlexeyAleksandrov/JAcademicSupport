package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DisciplineCoverageTreeDto;
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
    private final DstDecisionResolver            decisionResolver;
    private final DstSettingsService             settingsService;
    private final DstLevelMetaFactory            metaFactory;
    private final DstDisciplineTree              disciplineTree;

    @Transactional(readOnly = true)
    public DstL2Response analyzeLevel2(Long curriculumId, String domain, String techFamily) {
        return analyzeLevel2(curriculumId, domain, techFamily, DstCalcOptions.defaults(settingsService.get()));
    }

    @Transactional(readOnly = true)
    public DstL2Response analyzeLevel2(Long curriculumId, String domain, String techFamily,
                                        DstCalcOptions options) {
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

        Set<Long> touchedDisciplineIds = new LinkedHashSet<>();
        Map<Long, Double> skillProportionalHours = aggregateSkillHours(
                allDiscs, allCoverage, discHoursMap, meta, domain, techFamily, options, touchedDisciplineIds);

        String primaryProfCode = profs.get(0).professionCode();

        Set<Long> vacSkillIds = new LinkedHashSet<>();
        for (ProfessionWeight pw : profs) {
            dstQueryService.getSkillsByDomainAndFamily(pw.professionCode(), domain, techFamily)
                    .stream().map(SkillInfo::canonicalId).forEach(vacSkillIds::add);
        }

        Set<Long> allSkillIds = new LinkedHashSet<>(vacSkillIds);
        allSkillIds.addAll(skillProportionalHours.keySet());

        int nSkills = combinationService.isNClustersAuto()
                ? Math.max(1, allSkillIds.size())
                : Math.max(1, vacSkillIds.size());

        int coveredHours = (int) Math.round(
                skillProportionalHours.values().stream().mapToDouble(Double::doubleValue).sum());
        int independentHours = coveredHours;
        if (options.explicitSkills()) {
            DstCalcOptions derivedOptions = new DstCalcOptions(
                    options.domainMode(), options.familyMode(), DstCalcOptions.CoverageMode.DERIVED,
                    options.hoursBase(), options.budgetMode(), options.budgetHours(), options.disciplineId());
            Map<Long, Double> derivedHours = aggregateSkillHours(
                    allDiscs, allCoverage, discHoursMap, meta, domain, techFamily,
                    derivedOptions, new LinkedHashSet<>());
            independentHours = (int) Math.round(
                    derivedHours.values().stream().mapToDouble(Double::doubleValue).sum());
        }
        int budgetHours = resolveBudget(options, independentHours, touchedDisciplineIds, discHoursMap);

        resp.setNSkills(nSkills);
        resp.setTotalFamilyHours(budgetHours);

        Map<Long, String> skillNameMap = buildSkillNameMap(allSkillIds, meta, allCoverage);

        List<DstTraceResponse> traces = new ArrayList<>();
        List<DstL2SkillResult> results = new ArrayList<>();
        for (Long canonicalId : allSkillIds) {
            double supplyHoursD = skillProportionalHours.getOrDefault(canonicalId, 0.0);
            int    supplyHours  = (int) Math.round(supplyHoursD);
            double supply       = DstHoursPolicy.supply(supplyHoursD, budgetHours);

            DstContext ctx = new DstContext(primaryProfCode, domain, techFamily, canonicalId);
            DstTraceResponse trace = combinationService.computeWeightedSkill(
                    ctx, profs, supply, dstQueryService, nSkills);
            traces.add(trace);

            results.add(buildSkillResult(canonicalId, skillNameMap.getOrDefault(canonicalId, "Навык #" + canonicalId),
                    supply, supplyHours, trace));
        }

        double totalBetP = results.stream().mapToDouble(DstL2SkillResult::getBetp).sum();
        for (int i = 0; i < results.size(); i++) {
            DstTraceResponse trace = traces.get(i);
            decisionResolver.resolve(trace, results.get(i).getSupplyHours(), totalBetP);
            results.get(i).setRecommendation(trace.getRecommendation());
            results.get(i).setExpertiseRequired(trace.isExpertiseRequired());
        }

        results.sort(Comparator.comparingDouble(DstL2SkillResult::getBetp).reversed());
        resp.setSkills(results);
        resp.setMeta(metaFactory.build(options, budgetHours, coveredHours,
                budgetLabel(options, touchedDisciplineIds.size(), techFamily), nSkills));
        return resp;
    }

    @Transactional(readOnly = true)
    public DstL2DisciplineResponse analyzeLevel2ForDisciplineAndFamily(Long disciplineId,
                                                                        String domain,
                                                                        String techFamily) {
        return analyzeLevel2ForDisciplineAndFamily(disciplineId, domain, techFamily,
                DstCalcOptions.defaults(settingsService.get()));
    }

    @Transactional(readOnly = true)
    public DstL2DisciplineResponse analyzeLevel2ForDisciplineAndFamily(Long disciplineId,
                                                                        String domain,
                                                                        String techFamily,
                                                                        DstCalcOptions options) {
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

        // Sum of the family's skill hours inside this discipline
        int sumSkillsInFamily = covList.stream()
                .filter(c -> c.getCanonicalId() != null
                          && domain.equals(getEffectiveDomain(c, meta))
                          && techFamily.equals(getEffectiveTechFamily(c, meta)))
                .mapToInt(c -> c.getHours() != null ? c.getHours() : 0).sum();

        Map<Long, Double> skillProportionalHours = new LinkedHashMap<>();
        DisciplineCoverageTreeDto tree = disciplineTree.build(disc, covList);
        if (options.fullTree()) disciplineTree.validate(tree);
        DisciplineCoverageTreeDto.Node domainNode = tree.getDomains().stream()
                .filter(node -> domain.equals(node.getLabel())).findFirst().orElse(null);
        DisciplineCoverageTreeDto.Node familyNode = domainNode == null ? null : domainNode.getChildren().stream()
                .filter(node -> techFamily.equals(node.getLabel())).findFirst().orElse(null);
        if (familyNode != null) {
            for (DisciplineCoverageTreeDto.Node skill : familyNode.getChildren())
                skillProportionalHours.put(skill.getCanonicalId(), (double) skill.getTotalHours());
        }

        String primaryProfCode = profs.get(0).professionCode();
        Set<Long> vacSkillIds = new LinkedHashSet<>();
        for (ProfessionWeight pw : profs) {
            dstQueryService.getSkillsByDomainAndFamily(pw.professionCode(), domain, techFamily)
                    .stream().map(SkillInfo::canonicalId).forEach(vacSkillIds::add);
        }

        Set<Long> allSkillIds = new LinkedHashSet<>(vacSkillIds);
        allSkillIds.addAll(skillProportionalHours.keySet());

        int nSkills = combinationService.isNClustersAuto()
                ? Math.max(1, allSkillIds.size())
                : Math.max(1, vacSkillIds.size());

        int coveredHours = (int) Math.round(
                skillProportionalHours.values().stream().mapToDouble(Double::doubleValue).sum());
        Integer inherited = options.inheritedBudget();
        int derivedFamilyHours = sumAllCoverage > 0
                ? (int) Math.round((double) sumSkillsInFamily / sumAllCoverage * totalHours)
                : 0;
        int familyHours;
        if (inherited != null) {
            familyHours = inherited;
        } else {
            familyHours = switch (options.hoursBase()) {
                case SINGLE_DISCIPLINE, TOUCHED_DISCIPLINES -> totalHours;
                case CURRICULUM -> derivedFamilyHours;
            };
        }
        Map<Long, String> skillNameMap = buildSkillNameMap(allSkillIds, meta, covList);

        List<DstTraceResponse> traces = new ArrayList<>();
        List<DstL2SkillResult> results = new ArrayList<>();
        for (Long canonicalId : allSkillIds) {
            double supplyHoursD = skillProportionalHours.getOrDefault(canonicalId, 0.0);
            int    supplyHours  = (int) Math.round(supplyHoursD);
            double supply       = DstHoursPolicy.supply(supplyHoursD, familyHours);

            DstContext ctx = new DstContext(primaryProfCode, domain, techFamily, canonicalId);
            DstTraceResponse trace = combinationService.computeWeightedSkill(
                    ctx, profs, supply, dstQueryService, nSkills);
            traces.add(trace);

            results.add(buildSkillResult(canonicalId, skillNameMap.getOrDefault(canonicalId, "Навык #" + canonicalId),
                    supply, supplyHours, trace));
        }

        double totalBetP = results.stream().mapToDouble(DstL2SkillResult::getBetp).sum();
        for (int i = 0; i < results.size(); i++) {
            DstTraceResponse trace = traces.get(i);
            decisionResolver.resolve(trace, results.get(i).getSupplyHours(), totalBetP);
            results.get(i).setRecommendation(trace.getRecommendation());
            results.get(i).setExpertiseRequired(trace.isExpertiseRequired());
        }
        results.sort(Comparator.comparingDouble(DstL2SkillResult::getBetp).reversed());

        DstL2FamilySection section = new DstL2FamilySection();
        section.setDomain(domain);
        section.setTechFamily(techFamily);
        section.setFamilyHours(familyHours);
        section.setNSkills(nSkills);
        section.setSkills(results);

        resp.setSections(List.of(section));
        resp.setMeta(metaFactory.build(options, familyHours, coveredHours,
                inherited != null
                        ? "бюджет семейства «" + techFamily + "», унаследованный с L1"
                        : "часы семейства внутри дисциплины"));
        return resp;
    }

    /** Resolves T for L2: inherited parent budget, Σ touched disciplines or the aggregated coverage. */
    private int resolveBudget(DstCalcOptions options, int independentHours,
                              Set<Long> touchedDisciplineIds, Map<Long, Integer> discHoursMap) {
        return DstHoursPolicy.resolveBudget(options, independentHours, touchedDisciplineIds, discHoursMap);
    }

    private String budgetLabel(DstCalcOptions options, int touchedCount, String techFamily) {
        Integer inherited = options.inheritedBudget();
        if (inherited != null) {
            return "бюджет семейства «" + techFamily + "», унаследованный с L1";
        }
        return switch (options.hoursBase()) {
            case TOUCHED_DISCIPLINES ->
                    "Σ часов дисциплин семейства (" + touchedCount + " дисц.)";
            case SINGLE_DISCIPLINE ->
                    "часы выбранной дисциплины #" + options.disciplineId();
            case CURRICULUM -> "условный объём дисциплин, связанных с семейством «" + techFamily + "»";
        };
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
        r.setExpertiseRequired(trace.isExpertiseRequired());
        r.setSources(trace.getSources());
        r.setCombinations(trace.getCombinations());
        return r;
    }

    private Map<Long, Double> aggregateSkillHours(List<Discipline> allDiscs,
                                                   List<DisciplineCoverage> allCoverage,
                                                   Map<Long, Integer> discHoursMap,
                                                   CanonicalMeta meta,
                                                   String targetDomain,
                                                   String targetFamily,
                                                   DstCalcOptions options,
                                                   Set<Long> touchedDisciplineIds) {
        Map<Long, List<DisciplineCoverage>> byDisc = allCoverage.stream()
                .collect(Collectors.groupingBy(DisciplineCoverage::getDisciplineId));

        Map<Long, Double> skillHours = new LinkedHashMap<>();
        for (Discipline disc : allDiscs) {
            int discTotalHours = discHoursMap.getOrDefault(disc.getId(), 0);
            if (discTotalHours == 0) continue;
            List<DisciplineCoverage> covList = byDisc.getOrDefault(disc.getId(), List.of());
            if (covList.isEmpty()) continue;

            DisciplineCoverageTreeDto tree = disciplineTree.build(disc, covList);
            if (options.fullTree()) disciplineTree.validate(tree);
            DisciplineCoverageTreeDto.Node domainNode = tree.getDomains().stream()
                    .filter(node -> targetDomain.equals(node.getLabel())).findFirst().orElse(null);
            if (domainNode == null) continue;
            DisciplineCoverageTreeDto.Node familyNode = domainNode.getChildren().stream()
                    .filter(node -> targetFamily.equals(node.getLabel())).findFirst().orElse(null);
            if (familyNode == null || familyNode.getChildren().isEmpty()) continue;
            touchedDisciplineIds.add(disc.getId());
            for (DisciplineCoverageTreeDto.Node skill : familyNode.getChildren()) {
                skillHours.merge(skill.getCanonicalId(), (double) skill.getTotalHours(), Double::sum);
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
        if (cov.getTechFamily() != null) return meta.techFamilyDomainMap().get(cov.getTechFamily());
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
        Set<String> techFamilies = coverage.stream()
                .filter(c -> c.getDomain() == null && c.getCanonicalId() == null)
                .map(DisciplineCoverage::getTechFamily)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, String> techFamilyDomainMap = techFamilies.isEmpty() ? Map.<String, String>of() :
                skillCanonicalRepository.findDomainsByTechFamilies(techFamilies).stream()
                        .collect(Collectors.toMap(
                                r -> (String) r[0], r -> (String) r[1], (a, b) -> a));
        return new CanonicalMeta(domainMap, familyMap, nameMap, techFamilyDomainMap);
    }

    private record CanonicalMeta(Map<Long, String> domainMap,
                                  Map<Long, String> familyMap,
                                  Map<Long, String> nameMap,
                                  Map<String, String> techFamilyDomainMap) {}
}
