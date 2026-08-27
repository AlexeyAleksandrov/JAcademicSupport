package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DisciplineCoverageTreeDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstLevelMeta;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level0.DstL0DomainResult;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level0.DstL0Response;
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
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.DstContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DstLevel0Service {

    private final CurriculumRepository         curriculumRepository;
    private final CurriculumProfessionRepository professionRepository;
    private final DisciplineRepository         disciplineRepository;
    private final DisciplineCoverageRepository coverageRepository;
    private final SkillCanonicalRepository     skillCanonicalRepository;
    private final DstQueryService              dstQueryService;
    private final DstCombinationService        combinationService;
    private final DstSettingsService           settingsService;
    private final DstLevelMetaFactory          metaFactory;
    private final DstDisciplineTree            disciplineTree;

    @Transactional(readOnly = true)
    public DstL0Response analyzeLevel0(Long curriculumId) {
        return analyzeLevel0(curriculumId, DstCalcOptions.defaults(settingsService.get()));
    }

    @Transactional(readOnly = true)
    public DstL0Response analyzeLevel0(Long curriculumId, DstCalcOptions options) {
        DstL0Response resp = new DstL0Response();
        resp.setCurriculumId(curriculumId);

        Optional<Curriculum> optCurr = curriculumRepository.findById(curriculumId);
        if (optCurr.isEmpty()) {
            resp.setError("Учебный план не найден: " + curriculumId);
            return resp;
        }
        resp.setCurriculumName(optCurr.get().getName());

        List<CurriculumProfession> cpList = professionRepository.findByCurriculumId(curriculumId);
        if (cpList.isEmpty()) {
            resp.setError("В учебном плане не указаны профессии. Добавьте профессии с весами.");
            return resp;
        }

        double totalWeight = cpList.stream().mapToDouble(CurriculumProfession::getWeight).sum();
        List<ProfessionWeight> profs = cpList.stream()
                .map(cp -> new ProfessionWeight(
                        cp.getProfessionCode(),
                        cp.getProfessionName(),
                        totalWeight > 0 ? cp.getWeight() / totalWeight : 1.0 / cpList.size()))
                .collect(Collectors.toList());
        resp.setProfessions(profs);

        List<Discipline> allInCurriculum = disciplineRepository.findByCurriculumId(curriculumId);
        List<Long> allDiscIds = allInCurriculum.stream().map(Discipline::getId).collect(Collectors.toList());

        int totalHours = allInCurriculum.stream()
                .mapToInt(d -> d.getTotalHours() != null ? d.getTotalHours() : 0)
                .sum();
        resp.setTotalCurriculumHours(totalHours);

        // ── Load all coverage entries once ────────────────────────────────────
        List<DisciplineCoverage> allCoverage = allDiscIds.isEmpty()
                ? List.of()
                : coverageRepository.findByDisciplineIdIn(allDiscIds);

        // ── Build canonical → domain map ────────────────────────────────────
        Set<Long> canonicalIds = allCoverage.stream()
                .map(DisciplineCoverage::getCanonicalId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> canonicalDomainMap = canonicalIds.isEmpty() ? Map.of() :
                skillCanonicalRepository.findAllById(canonicalIds).stream()
                        .filter(sc -> sc.getDomain() != null && !sc.getDomain().isEmpty())
                        .collect(Collectors.toMap(SkillCanonical::getId, SkillCanonical::getDomain, (a, b) -> a));

        // ── Build techFamily → domain map (fallback for family-only coverage) ──────
        Set<String> techFamilies = allCoverage.stream()
                .filter(c -> c.getDomain() == null && c.getCanonicalId() == null)
                .map(DisciplineCoverage::getTechFamily)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, String> techFamilyDomainMap = techFamilies.isEmpty() ? Map.<String, String>of() :
                skillCanonicalRepository.findDomainsByTechFamilies(techFamilies).stream()
                        .collect(Collectors.toMap(
                                r -> (String) r[0], r -> (String) r[1], (a, b) -> a));

        // ── Per-discipline supply: hours of each domain inside the discipline ──
        Map<Long, List<DisciplineCoverage>> byDisc = allCoverage.stream()
                .collect(Collectors.groupingBy(DisciplineCoverage::getDisciplineId));
        Map<Long, Integer> discHoursMap = allInCurriculum.stream()
                .collect(Collectors.toMap(Discipline::getId,
                        d -> d.getTotalHours() != null ? d.getTotalHours() : 0));

        Map<String, Double> domainProportionalHours = new HashMap<>();
        Set<Long>           touchedDisciplineIds    = new LinkedHashSet<>();
        for (Discipline disc : allInCurriculum) {
            int discTotalHours = discHoursMap.getOrDefault(disc.getId(), 0);
            if (discTotalHours == 0) continue;
            List<DisciplineCoverage> covList = byDisc.getOrDefault(disc.getId(), List.of());
            if (covList.isEmpty()) continue;

            DisciplineCoverageTreeDto tree = disciplineTree.build(disc, covList);
            if (options.fullTree()) disciplineTree.validate(tree);
            int sumEff = 0;
            for (DisciplineCoverageTreeDto.Node domain : tree.getDomains()) {
                int hours = options.fullTree() ? domain.getTotalHours() : domain.getExplicitHours();
                if (hours <= 0) continue;
                sumEff += hours;
                domainProportionalHours.merge(domain.getLabel(), (double) hours, Double::sum);
            }
            if (sumEff > 0) touchedDisciplineIds.add(disc.getId());
        }

        // ── Normalisation base T ──────────────────────────────────────────────
        int budgetHours = resolveBudget(options, totalHours, touchedDisciplineIds, discHoursMap);
        int nClusters   = combinationService.isNClustersAuto() ? 0 : combinationService.defaultNClusters();

        // ── Collect all domains (market + curriculum) ─────────────────────────
        Set<String> vacDomains = new LinkedHashSet<>();
        for (ProfessionWeight pw : profs) {
            dstQueryService.getDomainsForProfession(pw.professionCode()).stream()
                    .map(DstQueryService.DomainClusterInfo::domain)
                    .filter(Objects::nonNull)
                    .forEach(vacDomains::add);
        }
        Set<String> rpdDomains = new LinkedHashSet<>(domainProportionalHours.keySet());

        Set<String> allDomains = new LinkedHashSet<>();
        allDomains.addAll(vacDomains);
        allDomains.addAll(rpdDomains);

        // Auto mode: BetP denominator = actual number of level objects
        int effectiveN = nClusters > 0 ? nClusters : Math.max(1, allDomains.size());

        List<DstL0DomainResult> results = new ArrayList<>();
        for (String domain : allDomains) {
            double supplyHoursD = domainProportionalHours.getOrDefault(domain, 0.0);
            int supplyHours = (int) Math.round(supplyHoursD);
            double supply = DstHoursPolicy.supply(supplyHoursD, budgetHours);

            String primaryProfCode = profs.get(0).professionCode();
            DstContext ctx = new DstContext(primaryProfCode, domain, null, null);
            DstTraceResponse trace = combinationService.computeWeighted(
                    ctx, profs, supply, dstQueryService, effectiveN);

            DstL0DomainResult dr = new DstL0DomainResult();
            dr.setDomain(domain);
            dr.setMT(trace.getMT());
            dr.setMU(trace.getMU());
            dr.setMF(trace.getMF());
            dr.setK(trace.getK());
            dr.setBetp(trace.getBetp());
            dr.setSupply(supply);
            dr.setSupplyHours(supplyHours);
            dr.setDelta(trace.getDelta());
            dr.setUsedYager(trace.isUsedYager());
            dr.setRecommendation(trace.getRecommendation());
            dr.setSources(trace.getSources());
            dr.setCombinations(trace.getCombinations());
            results.add(dr);
        }

        results.sort(Comparator.comparingDouble(DstL0DomainResult::getBetp).reversed());
        resp.setDomains(results);

        int coveredHours = (int) Math.round(
                domainProportionalHours.values().stream().mapToDouble(Double::doubleValue).sum());
        resp.setMeta(metaFactory.build(options, budgetHours, coveredHours,
                budgetLabel(options, totalHours, touchedDisciplineIds.size()), effectiveN));
        return resp;
    }

    /** Resolves T for L0 from the requested hours base / inherited parent budget. */
    private int resolveBudget(DstCalcOptions options, int totalHours,
                              Set<Long> touchedDisciplineIds, Map<Long, Integer> discHoursMap) {
        return DstHoursPolicy.resolveBudget(options, totalHours, touchedDisciplineIds, discHoursMap);
    }

    private String budgetLabel(DstCalcOptions options, int totalHours, int touchedCount) {
        Integer inherited = options.inheritedBudget();
        if (inherited != null) {
            return "наследуемый бюджет родительского уровня";
        }
        return switch (options.hoursBase()) {
            case TOUCHED_DISCIPLINES ->
                    "Σ часов дисциплин с покрытием (" + touchedCount + " дисц.)";
            case SINGLE_DISCIPLINE ->
                    "часы выбранной дисциплины #" + options.disciplineId();
            case CURRICULUM -> "весь учебный план (" + totalHours + " ч.)";
        };
    }

    /** True for a coverage row where the domain is stated explicitly (no family / skill). */
    private boolean isExplicitDomainRow(DisciplineCoverage cov) {
        return cov.getDomain() != null && !cov.getDomain().isEmpty()
                && cov.getTechFamily() == null
                && cov.getCanonicalId() == null;
    }

    private String getEffectiveDomain(DisciplineCoverage cov,
                                       Map<Long, String> canonicalDomainMap,
                                       Map<String, String> techFamilyDomainMap) {
        if (cov.getDomain() != null && !cov.getDomain().isEmpty()) {
            return cov.getDomain();
        }
        if (cov.getCanonicalId() != null) {
            return canonicalDomainMap.get(cov.getCanonicalId());
        }
        if (cov.getTechFamily() != null) {
            return techFamilyDomainMap.get(cov.getTechFamily());
        }
        return null;
    }

    /**
     * Hours each domain contributes inside one discipline.
     *
     * EXPLICIT mode counts only rows that name the domain directly.
     * DERIVED mode keeps the historical DOMAIN ▶ FAMILY ▶ SKILL priority, where
     * only the highest available level of every domain is counted.
     */
    private Map<String, Integer> computeEffectiveDomainHours(
            List<DisciplineCoverage> covList,
            Map<Long, String> canonicalDomainMap,
            Map<String, String> techFamilyDomainMap,
            DstCalcOptions options) {

        if (options.explicitDomains()) {
            Map<String, Integer> explicit = new HashMap<>();
            for (DisciplineCoverage cov : covList) {
                if (!isExplicitDomainRow(cov)) continue;
                explicit.merge(cov.getDomain(), cov.getHours() != null ? cov.getHours() : 0, Integer::sum);
            }
            return explicit;
        }

        Map<String, Map<String, Integer>> byDomainAndLevel = new HashMap<>();
        for (DisciplineCoverage cov : covList) {
            String dom = getEffectiveDomain(cov, canonicalDomainMap, techFamilyDomainMap);
            if (dom == null || dom.isEmpty()) continue;
            int hours = cov.getHours() != null ? cov.getHours() : 0;
            String level = cov.getCanonicalId() != null ? "SKILL"
                         : cov.getTechFamily() != null  ? "FAMILY"
                         : "DOMAIN";
            byDomainAndLevel
                    .computeIfAbsent(dom, k -> new HashMap<>())
                    .merge(level, hours, Integer::sum);
        }

        Map<String, Integer> effective = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> e : byDomainAndLevel.entrySet()) {
            Map<String, Integer> levels = e.getValue();
            int eff = levels.containsKey("DOMAIN") ? levels.get("DOMAIN")
                    : levels.containsKey("FAMILY") ? levels.get("FAMILY")
                    : levels.getOrDefault("SKILL", 0);
            effective.put(e.getKey(), eff);
        }
        return effective;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSupplyBreakdown(Long curriculumId, String domain) {
        List<Discipline> discs = disciplineRepository.findByCurriculumId(curriculumId);
        if (discs.isEmpty()) return List.of();
        List<Long> discIds = discs.stream().map(Discipline::getId).collect(Collectors.toList());
        List<Object[]> rows = coverageRepository.findDomainBreakdownByDisciplines(discIds, domain);
        return rows.stream().map(r -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("disciplineId", ((Number) r[0]).longValue());
            m.put("name",         r[1]);
            m.put("semester",     r[2]);
            m.put("hours",        ((Number) r[3]).longValue());
            return m;
        }).collect(Collectors.toList());
    }
}
