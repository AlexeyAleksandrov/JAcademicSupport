package io.github.alexeyaleksandrov.jacademicsupport.controllers;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * DST algorithm query endpoints.
 * Provides profiled data for the three-level DST algorithm.
 *
 * Usage flow:
 *   GET /dst/professions                                  → Level 0: all professions
 *   GET /dst/professions/{code}/clusters                  → Level 1: clusters for profession
 *   GET /dst/professions/{code}/clusters/{id}/vacancies   → Level 1 detail: vacancies with scores
 *   GET /dst/professions/{code}/clusters/{id}/skills      → Level 2: skill frequency in cluster
 *
 * Canonical skill analytics (LLM data):
 *   GET /dst/skills/{canonicalId}/related   → top co-occurring skills
 *   GET /dst/vacancies/{vacancyId}/domain   → primary domain for a vacancy
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
            @PathVariable Long clusterId,
            @RequestParam(defaultValue = "all") String mode) {
        List<DstQueryService.SkillInfo> result = "strict".equals(mode)
                ? dstQueryService.getStrictSkillsForCluster(profCode, clusterId)
                : dstQueryService.getSkillsForProfessionAndCluster(profCode, clusterId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/professions/{profCode}/domain-clusters")
    public ResponseEntity<List<DstQueryService.DomainClusterInfo>> getDomainClusters(
            @PathVariable String profCode) {
        return ResponseEntity.ok(dstQueryService.getDomainsForProfession(profCode));
    }

    @GetMapping("/professions/{profCode}/domain-clusters/{domain}/skills")
    public ResponseEntity<List<DstQueryService.SkillInfo>> getSkillsByDomain(
            @PathVariable String profCode,
            @PathVariable String domain) {
        return ResponseEntity.ok(dstQueryService.getSkillsForProfessionAndDomain(profCode, domain));
    }

    @GetMapping("/skills/{canonicalId}/related")
    public ResponseEntity<List<DstQueryService.RelatedSkillInfo>> getRelatedSkills(
            @PathVariable Long canonicalId) {
        try {
            return ResponseEntity.ok(dstQueryService.getRelatedSkills(canonicalId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/skills/{canonicalId}/versions")
    public ResponseEntity<List<DstQueryService.VersionInfo>> getSkillVersions(
            @PathVariable Long canonicalId) {
        return ResponseEntity.ok(dstQueryService.getVersionsForSkill(canonicalId));
    }

    @GetMapping("/vacancies/{vacancyId}/domain")
    public ResponseEntity<DstQueryService.VacancyDomainInfo> getVacancyDomain(
            @PathVariable Long vacancyId) {
        return dstQueryService.getVacancyDomain(vacancyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
