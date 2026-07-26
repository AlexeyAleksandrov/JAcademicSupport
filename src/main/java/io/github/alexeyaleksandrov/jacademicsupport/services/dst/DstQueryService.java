package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.models.*;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.*;
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

    private final ProfessionRepository          professionRepository;
    private final ProfessionClusterRepository   professionClusterRepository;
    private final VacancyClusterScoreRepository scoreRepository;
    private final WorkSkillRepository           workSkillRepository;
    private final SkillsGroupRepository         skillsGroupRepository;
    private final WorkSkillCanonicalRepository  workSkillCanonicalRepository;
    private final SkillCanonicalRepository      canonicalRepository;
    private final SkillDomainStatsRepository    domainStatsRepository;
    private final VacancyDomainRepository       vacancyDomainRepository;
    private final SkillDependencyRepository     dependencyRepository;

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
     * Uses work_skill_canonical M:N table populated by the Python LLM pipeline.
     * Each SkillInfo is enriched with domain and top co-occurrences.
     */
    @Transactional(readOnly = true)
    public List<SkillInfo> getSkillsForProfessionAndCluster(String profCode, Long clusterId) {
        // All vacancy scores for this profession + cluster
        List<VacancyClusterScore> relevantScores = scoreRepository
                .findByProfessionAndCluster(profCode, clusterId, MIN_SCORE);
        long totalVacancies = relevantScores.size();

        // Collect all work_skill IDs from relevant vacancies
        List<Long> workSkillIds = relevantScores.stream()
                .flatMap(vcs -> {
                    List<WorkSkill> skills = vcs.getVacancy().getSkills();
                    return skills == null ? java.util.stream.Stream.empty()
                                         : skills.stream().map(WorkSkill::getId);
                })
                .distinct()
                .collect(Collectors.toList());

        if (workSkillIds.isEmpty()) return List.of();

        // Pre-load M:N map for all relevant work_skills in one query
        Set<Long> allWorkSkillIds = new HashSet<>();
        for (VacancyClusterScore vcs : relevantScores) {
            List<WorkSkill> vacSkills = vcs.getVacancy().getSkills();
            if (vacSkills != null) vacSkills.forEach(ws -> allWorkSkillIds.add(ws.getId()));
        }
        Map<Long, Set<Long>> wsToCanonicals = new HashMap<>();
        workSkillCanonicalRepository.findByWorkSkillIdIn(new ArrayList<>(allWorkSkillIds))
                .forEach(wsc -> wsToCanonicals
                        .computeIfAbsent(wsc.getWorkSkillId(), k -> new HashSet<>())
                        .add(wsc.getCanonicalId()));

        // Count how often each canonical_id appears across relevant vacancies (via M:N cache)
        Map<Long, Long> canonicalFrequency = new HashMap<>();
        for (VacancyClusterScore vcs : relevantScores) {
            List<WorkSkill> vacSkills = vcs.getVacancy().getSkills();
            if (vacSkills == null) continue;
            Set<Long> vacancyCanonicals = new HashSet<>();
            for (WorkSkill ws : vacSkills) {
                Set<Long> cids = wsToCanonicals.get(ws.getId());
                if (cids != null) vacancyCanonicals.addAll(cids);
            }
            vacancyCanonicals.forEach(cid -> canonicalFrequency.merge(cid, 1L, Long::sum));
        }

        // Build SkillInfo for every canonical that appeared at least once
        List<SkillInfo> result = new ArrayList<>();
        canonicalFrequency.forEach((canonicalId, freq) -> {
            double relFreq = (double) freq / totalVacancies;
            SkillCanonical sc = canonicalRepository.findById(canonicalId).orElse(null);
            if (sc == null) return;

            SkillDomainStats stats = domainStatsRepository.findByCanonicalId(canonicalId).orElse(null);
            List<Map<String, Object>> topCooc = stats != null ? stats.getTopCooccurrences() : List.of();

            result.add(new SkillInfo(
                    canonicalId,
                    sc.getName(),
                    canonicalId,
                    relFreq,
                    freq,
                    false,
                    sc.getDomain(),
                    topCooc
            ));
        });

        result.sort(Comparator.comparingDouble(SkillInfo::relativeFrequency).reversed());
        return result;
    }

    /**
     * Level 2 (strict mode): canonical skills that actually belong to this cluster's skills_group,
     * filtered by profession. Uses native SQL for efficiency.
     */
    @Transactional(readOnly = true)
    public List<SkillInfo> getStrictSkillsForCluster(String profCode, Long clusterId) {
        List<Object[]> rows = canonicalRepository.findStrictClusterSkills(profCode, clusterId);
        return rows.stream().map(r -> {
            long   canonicalId = ((Number) r[0]).longValue();
            String description = (String) r[1];
            String domain      = (String) r[2];
            long   absCount    = ((Number) r[3]).longValue();
            double relFreq     = r[4] != null ? ((Number) r[4]).doubleValue() : 0.0;
            return new SkillInfo(canonicalId, description, canonicalId, relFreq, absCount, false, domain, List.of());
        }).collect(Collectors.toList());
    }

    /**
     * Returns top co-occurring skills for a given canonical skill.
     */
    @Transactional(readOnly = true)
    public List<RelatedSkillInfo> getRelatedSkills(Long canonicalId) {
        SkillCanonical skill = canonicalRepository.findById(canonicalId)
                .orElseThrow(() -> new NoSuchElementException("Canonical skill not found: " + canonicalId));

        List<SkillDependency> asParent = dependencyRepository.findByParent(skill);
        List<SkillDependency> asChild  = dependencyRepository.findByChild(skill);

        List<RelatedSkillInfo> result = new ArrayList<>();
        for (SkillDependency dep : asParent) {
            SkillCanonical other = dep.getChild();
            result.add(new RelatedSkillInfo(other.getId(), other.getName(), other.getDomain(), dep.getCoOccurrenceCnt()));
        }
        for (SkillDependency dep : asChild) {
            SkillCanonical other = dep.getParent();
            result.add(new RelatedSkillInfo(other.getId(), other.getName(), other.getDomain(), dep.getCoOccurrenceCnt()));
        }
        result.sort(Comparator.comparingInt(RelatedSkillInfo::coOccurrenceCount).reversed());
        return result;
    }

    /**
     * Returns domain info for a vacancy.
     */
    @Transactional(readOnly = true)
    public Optional<VacancyDomainInfo> getVacancyDomain(Long vacancyId) {
        return vacancyDomainRepository.findByVacancyId(vacancyId)
                .map(vd -> new VacancyDomainInfo(vd.getVacancyId(), vd.getPrimaryDomain(),
                        vd.getDomainScore(), vd.getComputedAt()));
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
            boolean isImplied,
            String domain,
            List<Map<String, Object>> topCooccurrences
    ) {}

    public record RelatedSkillInfo(
            Long   canonicalId,
            String name,
            String domain,
            int    coOccurrenceCount
    ) {}

    public record VacancyDomainInfo(
            Long       vacancyId,
            String     primaryDomain,
            java.math.BigDecimal domainScore,
            java.time.LocalDateTime computedAt
    ) {}

    public record DomainClusterInfo(
            String domain,
            long   vacancyCount,
            double weight
    ) {}

    /**
     * Domain-based Level 1: distribution of skill domains for a profession.
     * Uses skill_canonical.domain instead of skills_group.
     */
    @Transactional(readOnly = true)
    public List<DomainClusterInfo> getDomainsForProfession(String profCode) {
        return canonicalRepository.findDomainDistributionForProfession(profCode)
                .stream()
                .map(r -> new DomainClusterInfo(
                        (String) r[0],
                        ((Number) r[1]).longValue(),
                        r[2] != null ? ((Number) r[2]).doubleValue() : 0.0
                ))
                .collect(Collectors.toList());
    }

    /**
     * Domain-based Level 2: canonical skills for a profession within a specific domain.
     */
    @Transactional(readOnly = true)
    public List<SkillInfo> getSkillsForProfessionAndDomain(String profCode, String domain) {
        return canonicalRepository.findSkillsByDomainAndProfession(profCode, domain)
                .stream()
                .map(r -> new SkillInfo(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        ((Number) r[0]).longValue(),
                        r[4] != null ? ((Number) r[4]).doubleValue() : 0.0,
                        ((Number) r[3]).longValue(),
                        false,
                        (String) r[2],
                        List.of()
                ))
                .collect(Collectors.toList());
    }
}
