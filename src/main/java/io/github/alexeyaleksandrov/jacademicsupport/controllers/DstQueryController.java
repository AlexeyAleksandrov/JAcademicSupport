package io.github.alexeyaleksandrov.jacademicsupport.controllers;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DST algorithm query endpoints.
 * Provides profiled data for the three-level DST algorithm.
 *
 * Usage flow:
 *   GET /dst/professions                              → Level 0: all professions
 *   GET /dst/professions/{code}/clusters              → Level 1: clusters for profession
 *   GET /dst/professions/{code}/clusters/{id}/vacancies → Level 1 detail: vacancies with scores
 *   GET /dst/professions/{code}/clusters/{id}/skills    → Level 2: skill frequency in cluster
 */
@RestController
@RequestMapping("/api/dst")
@RequiredArgsConstructor
@Tag(name = "DST Query", description = "Data query layer for the three-level DST algorithm")
public class DstQueryController {

    private final DstQueryService dstQueryService;

    @GetMapping("/professions")
    @Operation(
        summary     = "Level 0: profession catalogue",
        description = "Returns all IT professions available in the system with codes and descriptions."
    )
    public ResponseEntity<?> getProfessions() {
        return ResponseEntity.ok(dstQueryService.getAllProfessions());
    }

    @GetMapping("/professions/{profCode}/clusters")
    @Operation(
        summary     = "Level 1: clusters for a profession",
        description = "Returns clusters ranked by profession-specific weight. "
                    + "Each cluster includes average vacancy market-demand score."
    )
    public ResponseEntity<List<DstQueryService.ClusterInfo>> getClustersForProfession(
            @PathVariable String profCode) {
        return ResponseEntity.ok(dstQueryService.getClustersForProfession(profCode));
    }

    @GetMapping("/professions/{profCode}/clusters/{clusterId}/vacancies")
    @Operation(
        summary     = "Level 1 detail: vacancies for profession + cluster",
        description = "Returns vacancies that match the given profession and cluster, ordered by "
                    + "score descending. Includes vacancies matched indirectly via skill dependencies."
    )
    public ResponseEntity<List<DstQueryService.VacancyScoreInfo>> getVacanciesForCluster(
            @PathVariable String profCode,
            @PathVariable Long clusterId) {
        return ResponseEntity.ok(
                dstQueryService.getVacanciesForProfessionAndCluster(profCode, clusterId));
    }

    @GetMapping("/professions/{profCode}/clusters/{clusterId}/skills")
    @Operation(
        summary     = "Level 2: skills for profession + cluster",
        description = "Returns skills in the cluster with relative frequency across relevant vacancies, "
                    + "ordered by frequency descending. Used for Level 2 DST BPA."
    )
    public ResponseEntity<List<DstQueryService.SkillInfo>> getSkillsForCluster(
            @PathVariable String profCode,
            @PathVariable Long clusterId) {
        return ResponseEntity.ok(
                dstQueryService.getSkillsForProfessionAndCluster(profCode, clusterId));
    }
}
