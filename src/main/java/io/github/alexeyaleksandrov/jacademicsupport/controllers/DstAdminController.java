package io.github.alexeyaleksandrov.jacademicsupport.controllers;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
public class DstAdminController {

    private final SkillNormalizationService  normalizationService;
    private final VacancyProfessionService   professionService;
    private final SkillDependencyService     dependencyService;
    private final ProfessionClusterService   professionClusterService;
    private final VacancyClusterScoreService scoreService;

    /**
     * @deprecated Skill normalization is now handled by the Python LLM pipeline.
     *             Run: python skill-atomize-test/test_atomize.py --all-skills --save
     */
    @SuppressWarnings("deprecation")
    @PostMapping("/normalize-skills")
    public ResponseEntity<Map<String, Object>> normalizeSkills() {
        log.warn("POST /normalize-skills called — endpoint is deprecated (410 Gone). Use Python LLM pipeline.");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "gone");
        body.put("message", "Regex normalization is deprecated. " +
                "Use the Python LLM pipeline: python skill-atomize-test/test_atomize.py --all-skills --save");
        return ResponseEntity.status(HttpStatus.GONE).body(body);
    }

    @PostMapping("/classify-professions")
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
    public ResponseEntity<Map<String, Object>> computeProfessionWeights() {
        log.info("Admin: computing profession-cluster weights");
        ProfessionClusterService.WeightReport report = professionClusterService.computeWeights();
        return ResponseEntity.ok(reportMap("compute-profession-weights", Map.of(
                "rowsUpserted", report.rowsUpserted()
        )));
    }

    @PostMapping("/compute-cluster-scores")
    public ResponseEntity<Map<String, Object>> computeClusterScores() {
        log.info("Admin: computing vacancy-cluster scores");
        VacancyClusterScoreService.ScoreReport report = scoreService.computeScores();
        return ResponseEntity.ok(reportMap("compute-cluster-scores", Map.of(
                "rowsUpserted", report.rowsUpserted()
        )));
    }

    @PostMapping("/run-full-pipeline")
    public ResponseEntity<Map<String, Object>> runFullPipeline(
            @RequestParam(defaultValue = "0.30") double dependencyThreshold) {
        log.info("Admin: running full DST pipeline");

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("phase2_normalize", Map.of(
                "status", "skipped",
                "reason", "Normalization handled by Python LLM pipeline (test_atomize.py)"));

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
