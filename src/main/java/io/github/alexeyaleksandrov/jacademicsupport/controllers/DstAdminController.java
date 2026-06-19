package io.github.alexeyaleksandrov.jacademicsupport.controllers;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Admin endpoints for running the DST data preparation pipeline.
 * Must be executed in order: normalize → classify → graph → clusters → scores.
 */
@RestController
@RequestMapping("/api/admin/dst")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "DST Admin Pipeline", description = "Data preparation pipeline for the DST algorithm")
public class DstAdminController {

    private final SkillNormalizationService  normalizationService;
    private final VacancyProfessionService   professionService;
    private final SkillDependencyService     dependencyService;
    private final ProfessionClusterService   professionClusterService;
    private final VacancyClusterScoreService scoreService;

    @PostMapping("/normalize-skills")
    @Operation(
        summary     = "Phase 2 — Normalise skills",
        description = "Runs the 3-pass regex pipeline: splits composite skills, extracts versions, "
                    + "creates skill_canonical entries, links work_skill.canonical_id."
    )
    public ResponseEntity<Map<String, Object>> normalizeSkills() {
        log.info("Admin: starting skill normalisation");
        SkillNormalizationService.NormalizationReport report = normalizationService.normalizeAll();
        return ResponseEntity.ok(reportMap("normalise-skills", Map.of(
                "processed",        report.processed(),
                "canonicalCreated", report.canonicalCreated(),
                "linked",           report.linked(),
                "unknown",          report.unknown()
        )));
    }

    @PostMapping("/classify-professions")
    @Operation(
        summary     = "Phase 3 — Classify vacancies into professions",
        description = "Keyword-matching on vacancy title. One vacancy may map to multiple "
                    + "professions with individual confidence scores."
    )
    public ResponseEntity<Map<String, Object>> classifyProfessions() {
        log.info("Admin: starting profession classification");
        VacancyProfessionService.ClassificationReport report = professionService.classifyAll();
        return ResponseEntity.ok(reportMap("classify-professions", Map.of(
                "classified", report.classified(),
                "other",      report.other(),
                "skipped",    report.skipped()
        )));
    }

    @PostMapping("/build-dependency-graph")
    @Operation(
        summary     = "Phase 4 — Build skill dependency graph",
        description = "Computes co-occurrence statistics from vacancy-skill data. "
                    + "Optional 'threshold' param (default 0.30) controls minimum P(parent|child)."
    )
    public ResponseEntity<Map<String, Object>> buildDependencyGraph(
            @RequestParam(defaultValue = "0.30") double threshold) {
        log.info("Admin: building dependency graph with threshold={}", threshold);
        SkillDependencyService.DependencyReport report = dependencyService.buildGraph(threshold);
        return ResponseEntity.ok(reportMap("build-dependency-graph", Map.of(
                "edgesSaved",   report.edgesSaved(),
                "edgesSkipped", report.edgesSkipped(),
                "threshold",    report.threshold()
        )));
    }

    @PostMapping("/compute-profession-weights")
    @Operation(
        summary     = "Phase 5 — Compute profession-cluster weights",
        description = "Fills profession_cluster.weight for every (profession, cluster) pair "
                    + "based on vacancy statistics."
    )
    public ResponseEntity<Map<String, Object>> computeProfessionWeights() {
        log.info("Admin: computing profession-cluster weights");
        ProfessionClusterService.WeightReport report = professionClusterService.computeWeights();
        return ResponseEntity.ok(reportMap("compute-profession-weights", Map.of(
                "rowsUpserted", report.rowsUpserted()
        )));
    }

    @PostMapping("/compute-cluster-scores")
    @Operation(
        summary     = "Phase 6 — Compute vacancy-cluster scores",
        description = "Pre-computes vacancy_cluster_score for every (vacancy, cluster) pair. "
                    + "Uses title=1.0, skills=0.8, description=0.5 weights with dependency expansion."
    )
    public ResponseEntity<Map<String, Object>> computeClusterScores() {
        log.info("Admin: computing vacancy-cluster scores");
        VacancyClusterScoreService.ScoreReport report = scoreService.computeScores();
        return ResponseEntity.ok(reportMap("compute-cluster-scores", Map.of(
                "rowsUpserted", report.rowsUpserted()
        )));
    }

    @PostMapping("/run-full-pipeline")
    @Operation(
        summary     = "Run full pipeline (phases 2–6)",
        description = "Runs all phases sequentially. Safe to re-run (idempotent — upserts, not inserts)."
    )
    public ResponseEntity<Map<String, Object>> runFullPipeline(
            @RequestParam(defaultValue = "0.30") double dependencyThreshold) {
        log.info("Admin: running full DST pipeline");

        Map<String, Object> results = new LinkedHashMap<>();

        SkillNormalizationService.NormalizationReport r1 = normalizationService.normalizeAll();
        results.put("phase2_normalize", Map.of(
                "processed", r1.processed(), "canonicalCreated", r1.canonicalCreated(),
                "linked", r1.linked(), "unknown", r1.unknown()));

        VacancyProfessionService.ClassificationReport r2 = professionService.classifyAll();
        results.put("phase3_classify", Map.of(
                "classified", r2.classified(), "other", r2.other(), "skipped", r2.skipped()));

        SkillDependencyService.DependencyReport r3 = dependencyService.buildGraph(dependencyThreshold);
        results.put("phase4_dependencies", Map.of(
                "edgesSaved", r3.edgesSaved(), "edgesSkipped", r3.edgesSkipped()));

        ProfessionClusterService.WeightReport r4 = professionClusterService.computeWeights();
        results.put("phase5_professionWeights", Map.of("rowsUpserted", r4.rowsUpserted()));

        VacancyClusterScoreService.ScoreReport r5 = scoreService.computeScores();
        results.put("phase6_clusterScores", Map.of("rowsUpserted", r5.rowsUpserted()));

        results.put("status", "completed");
        return ResponseEntity.ok(results);
    }

    private Map<String, Object> reportMap(String phase, Map<String, Object> data) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("phase", phase);
        result.put("status", "ok");
        result.putAll(data);
        return result;
    }
}
