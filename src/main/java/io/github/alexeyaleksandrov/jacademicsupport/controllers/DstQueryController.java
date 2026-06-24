package io.github.alexeyaleksandrov.jacademicsupport.controllers;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService;
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
public class DstQueryController {

    private final DstQueryService dstQueryService;

    @GetMapping("/professions")
    public ResponseEntity<?> getProfessions() {
        return ResponseEntity.ok(dstQueryService.getAllProfessions());
    }

    @GetMapping("/professions/{profCode}/clusters")
    public ResponseEntity<List<DstQueryService.ClusterInfo>> getClustersForProfession(
            @PathVariable String profCode) {
        return ResponseEntity.ok(dstQueryService.getClustersForProfession(profCode));
    }

    @GetMapping("/professions/{profCode}/clusters/{clusterId}/vacancies")
    public ResponseEntity<List<DstQueryService.VacancyScoreInfo>> getVacanciesForCluster(
            @PathVariable String profCode,
            @PathVariable Long clusterId) {
        return ResponseEntity.ok(
                dstQueryService.getVacanciesForProfessionAndCluster(profCode, clusterId));
    }

    @GetMapping("/professions/{profCode}/clusters/{clusterId}/skills")
    public ResponseEntity<List<DstQueryService.SkillInfo>> getSkillsForCluster(
            @PathVariable String profCode,
            @PathVariable Long clusterId) {
        return ResponseEntity.ok(
                dstQueryService.getSkillsForProfessionAndCluster(profCode, clusterId));
    }
}
