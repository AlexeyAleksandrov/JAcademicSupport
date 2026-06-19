package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.models.Profession;
import io.github.alexeyaleksandrov.jacademicsupport.models.ProfessionCluster;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyClusterScore;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ProfessionClusterRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ProfessionRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillsGroupRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.VacancyClusterScoreRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Query layer for the DST algorithm data.
 * Provides profiled data for Levels 0, 1, and 2.
 */
@Service
@RequiredArgsConstructor
public class DstQueryService {

    private final ProfessionRepository         professionRepository;
    private final ProfessionClusterRepository  professionClusterRepository;
    private final VacancyClusterScoreRepository scoreRepository;
    private final WorkSkillRepository          workSkillRepository;
    private final SkillsGroupRepository        skillsGroupRepository;

    private static final BigDecimal MIN_SCORE = new BigDecimal("0.01");

    /**
     * Level 0: list all professions.
     */
    public List<Profession> getAllProfessions() {
        return professionRepository.findAllByOrderByNameAsc();
    }

    /**
     * Level 1: clusters for a given profession, ordered by weight descending.
     * Includes avg market demand (avg vacancy_cluster_score) for the profession.
     */
    @Transactional(readOnly = true)
    public List<ClusterInfo> getClustersForProfession(String profCode) {
        List<ProfessionCluster> pcs = professionClusterRepository
                .findByProfessionCodeOrderByWeightDesc(profCode);

        return pcs.stream().map(pc -> {
            SkillsGroup cluster   = pc.getCluster();
            Double avgScore = scoreRepository
                    .avgScoreByProfessionAndCluster(profCode, cluster.getId());
            return new ClusterInfo(
                    cluster.getId(),
                    cluster.getDescription(),
                    pc.getWeight(),
                    avgScore != null ? avgScore : 0.0
            );
        }).collect(Collectors.toList());
    }

    /**
     * Level 1 (detail): vacancies for a given profession + cluster, ordered by score desc.
     * Includes vacancies matched directly and via skill dependencies.
     */
    @Transactional(readOnly = true)
    public List<VacancyScoreInfo> getVacanciesForProfessionAndCluster(String profCode, Long clusterId) {
        List<VacancyClusterScore> scores = scoreRepository
                .findByProfessionAndCluster(profCode, clusterId, MIN_SCORE);

        return scores.stream().map(vcs -> new VacancyScoreInfo(
                vcs.getVacancy().getId(),
                vcs.getVacancy().getName(),
                vcs.getVacancy().getPublishedAt(),
                vcs.getScore(),
                vcs.isFromTitle(),
                vcs.isFromSkills(),
                vcs.isFromDesc(),
                vcs.isViaDependency()
        )).collect(Collectors.toList());
    }

    /**
     * Level 2: skills for a given profession + cluster, with frequency and dependency info.
     * Returns direct skills (in cluster) + implied skills (via dependency graph).
     */
    @Transactional(readOnly = true)
    public List<SkillInfo> getSkillsForProfessionAndCluster(String profCode, Long clusterId) {
        SkillsGroup cluster = skillsGroupRepository.findById(clusterId).orElseThrow();
        List<WorkSkill> clusterSkills = workSkillRepository.findBySkillsGroupBySkillsGroupId(cluster);

        // All vacancy scores for this profession + cluster
        List<VacancyClusterScore> relevantScores = scoreRepository
                .findByProfessionAndCluster(profCode, clusterId, MIN_SCORE);
        long totalVacancies = relevantScores.size();

        // Count how often each canonical appears in the relevant vacancies
        Map<Long, Long> canonicalFrequency = new HashMap<>();
        for (VacancyClusterScore vcs : relevantScores) {
            List<WorkSkill> vacSkills = vcs.getVacancy().getSkills();
            if (vacSkills == null) continue;
            for (WorkSkill s : vacSkills) {
                if (s.getCanonicalId() != null) {
                    canonicalFrequency.merge(s.getCanonicalId(), 1L, Long::sum);
                }
            }
        }

        List<SkillInfo> result = new ArrayList<>();

        for (WorkSkill skill : clusterSkills) {
            if (skill.getCanonicalId() == null) continue;

            long freq      = canonicalFrequency.getOrDefault(skill.getCanonicalId(), 0L);
            double relFreq = totalVacancies > 0 ? (double) freq / totalVacancies : 0.0;

            result.add(new SkillInfo(
                    skill.getId(),
                    skill.getDescription(),
                    skill.getCanonicalId(),
                    relFreq,
                    freq,
                    false
            ));
        }

        result.sort(Comparator.comparingDouble(SkillInfo::relativeFrequency).reversed());
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Response records
    // ─────────────────────────────────────────────────────────────────────────

    public record ClusterInfo(
            long   clusterId,
            String clusterName,
            BigDecimal professionWeight,
            double marketDemandAvg
    ) {}

    public record VacancyScoreInfo(
            Long   vacancyId,
            String title,
            String publishedAt,
            BigDecimal score,
            boolean fromTitle,
            boolean fromSkills,
            boolean fromDesc,
            boolean viaDependency
    ) {}

    public record SkillInfo(
            long   skillId,
            String description,
            Long   canonicalId,
            double relativeFrequency,
            long   absoluteCount,
            boolean isImplied
    ) {}
}
