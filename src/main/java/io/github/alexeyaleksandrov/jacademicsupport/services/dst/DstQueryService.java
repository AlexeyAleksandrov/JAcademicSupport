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
    private final SkillVersionRepository        versionRepository;
    private final ExpertOpinionRepository       expertOpinionRepository;
    private final ForesightRepository           foresightRepository;
    private final VacancyEntityRepository       vacancyEntityRepository;

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
                    topCooc,
                    sc.getTechType(),
                    sc.getVersionGroup()
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
            String techType    = (String) r[5];
            String versionGrp  = (String) r[6];
            return new SkillInfo(canonicalId, description, canonicalId, relFreq, absCount, false, domain, List.of(), techType, versionGrp);
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
            List<Map<String, Object>> topCooccurrences,
            String techType,
            String versionGroup
    ) {}

    public record VersionInfo(
            Long    id,
            String  rawString,
            String  versionMin,
            String  versionMax,
            boolean isPlus,
            long    absoluteCount
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
     * BPA result for a single DST source (EXP or FC) at a given level.
     * mT = m(T) = κ × averageScore (support for hypothesis "relevant")
     * mTheta = m(Θ) = 1 - mT (ignorance mass)
     */
    public record BpaResult(
            long   relevantCount,
            long   totalCount,
            double lambda,
            double averageScore,
            double mT,
            double mTheta
    ) {
        public static BpaResult empty() {
            return new BpaResult(0, 0, 0.0, 0.0, 0.0, 1.0);
        }
    }

    private static final double LAMBDA_EXP         = 5.0;
    private static final double LAMBDA_FC          = 2.0;
    private static final double LAMBDA_EXP_DOMAIN  = 1.0;  // domain-level: cross-prof entries shouldn't saturate
    private static final double LAMBDA_FC_DOMAIN   = 0.5;  // domain-level: 4/4 cross-prof AI tools → κ≈0.39, not 0.86
    private static final long   TOTAL_EXPERTS  = 12L;
    private static final long   TOTAL_SOURCES  = 4L;

    private BpaResult computeBpa(long relevantCount, double avgScore, long total, double lambda) {
        if (total == 0 || relevantCount == 0) return BpaResult.empty();
        double kappa = 1.0 - Math.exp(-lambda * (double) relevantCount / total);
        double mT    = kappa * avgScore;
        return new BpaResult(relevantCount, total, lambda, avgScore, mT, 1.0 - mT);
    }

    private BpaResult extractBpa(List<Object[]> rows, long total, double lambda) {
        if (rows.isEmpty()) return BpaResult.empty();
        Object[] row = rows.get(0);
        if (row[0] == null) return BpaResult.empty();
        long   cnt = ((Number) row[0]).longValue();
        double avg = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
        return computeBpa(cnt, avg, total, lambda);
    }

    @Transactional(readOnly = true)
    public BpaResult getExpBpaByDomain(String profCode, String domain) {
        return extractBpa(expertOpinionRepository.aggregateByDomain(domain, profCode), TOTAL_EXPERTS, LAMBDA_EXP_DOMAIN);
    }

    @Transactional(readOnly = true)
    public BpaResult getExpBpaByFamily(String profCode, String domain, String techFamily) {
        return extractBpa(expertOpinionRepository.aggregateByDomainAndFamily(domain, techFamily, profCode), TOTAL_EXPERTS, LAMBDA_EXP);
    }

    @Transactional(readOnly = true)
    public BpaResult getExpBpaByCanonical(String profCode, Long canonicalId) {
        return extractBpa(expertOpinionRepository.aggregateByCanonical(canonicalId, profCode), TOTAL_EXPERTS, LAMBDA_EXP);
    }

    @Transactional(readOnly = true)
    public BpaResult getFcBpaByDomain(String profCode, String domain) {
        return extractBpa(foresightRepository.aggregateByDomain(domain, profCode), TOTAL_SOURCES, LAMBDA_FC_DOMAIN);
    }

    public record ProfessionWeight(String professionCode, String professionName, double weight) {}

    @Transactional(readOnly = true)
    public BpaResult getWeightedVacBpaByDomain(List<ProfessionWeight> profs, String domain) {
        return weightedAverage(profs, p -> getVacBpaByDomain(p.professionCode(), domain));
    }

    @Transactional(readOnly = true)
    public BpaResult getWeightedExpBpaByDomain(List<ProfessionWeight> profs, String domain) {
        return weightedAverage(profs, p -> getExpBpaByDomain(p.professionCode(), domain));
    }

    @Transactional(readOnly = true)
    public BpaResult getWeightedFcBpaByDomain(List<ProfessionWeight> profs, String domain) {
        return weightedAverage(profs, p -> getFcBpaByDomain(p.professionCode(), domain));
    }

    private BpaResult weightedAverage(List<ProfessionWeight> profs,
                                      java.util.function.Function<ProfessionWeight, BpaResult> fn) {
        double sumW = 0.0, sumMT = 0.0, sumCnt = 0.0, sumTotal = 0.0;
        double lambda = 0.0;
        for (ProfessionWeight pw : profs) {
            BpaResult r = fn.apply(pw);
            if (r.relevantCount() > 0) {
                sumMT    += pw.weight() * r.mT();
                sumCnt   += pw.weight() * r.relevantCount();
                sumTotal += pw.weight() * r.totalCount();
                lambda    = r.lambda();
                sumW     += pw.weight();
            }
        }
        if (sumW == 0) return BpaResult.empty();
        double mT = sumMT / sumW;
        long cnt  = (long) (sumCnt / sumW);
        long tot  = (long) (sumTotal / sumW);
        return new BpaResult(cnt, tot, lambda, mT, mT, 1.0 - mT);
    }

    @Transactional(readOnly = true)
    public BpaResult getFcBpaByFamily(String profCode, String domain, String techFamily) {
        return extractBpa(foresightRepository.aggregateByDomainAndFamily(domain, techFamily, profCode), TOTAL_SOURCES, LAMBDA_FC);
    }

    @Transactional(readOnly = true)
    public BpaResult getFcBpaByCanonical(String profCode, Long canonicalId) {
        return extractBpa(foresightRepository.aggregateByCanonical(canonicalId, profCode), TOTAL_SOURCES, LAMBDA_FC);
    }

    public record FamilyInfo(
            String techFamily,
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
                        List.of(),
                        (String) r[5],
                        (String) r[6]
                ))
                .collect(Collectors.toList());
    }

    /**
     * Level 2.5: tech families within a domain for a given profession.
     * Returns families sorted by vacancy count descending.
     * Only families with at least one tagged skill are returned.
     */
    @Transactional(readOnly = true)
    public List<FamilyInfo> getFamiliesForDomain(String profCode, String domain) {
        return canonicalRepository.findFamilyDistributionByDomainAndProfession(profCode, domain)
                .stream()
                .map(r -> new FamilyInfo(
                        (String) r[0],
                        ((Number) r[1]).longValue(),
                        r[2] != null ? ((Number) r[2]).doubleValue() : 0.0
                ))
                .collect(Collectors.toList());
    }

    /**
     * Level 3: canonical skills for a profession within a specific domain + tech family.
     */
    @Transactional(readOnly = true)
    public List<SkillInfo> getSkillsByDomainAndFamily(String profCode, String domain, String techFamily) {
        return canonicalRepository.findSkillsByDomainAndFamilyAndProfession(profCode, domain, techFamily)
                .stream()
                .map(r -> new SkillInfo(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        ((Number) r[0]).longValue(),
                        r[4] != null ? ((Number) r[4]).doubleValue() : 0.0,
                        ((Number) r[3]).longValue(),
                        false,
                        (String) r[2],
                        List.of(),
                        (String) r[5],
                        (String) r[6]
                ))
                .collect(Collectors.toList());
    }

    private static final double LAMBDA_VAC        = 15.0;
    private static final double LAMBDA_VAC_DOMAIN = 2.0;   // domain ratios are 0.5-0.9; λ=15 saturates

    @Transactional(readOnly = true)
    public BpaResult getVacBpaByDomain(String profCode, String domain) {
        if (profCode == null) return BpaResult.empty();
        long total    = canonicalRepository.countTotalVacanciesForProfession(profCode);
        long relevant = canonicalRepository.countVacanciesByProfessionAndDomain(profCode, domain);
        if (total == 0 || relevant == 0) return BpaResult.empty();
        return computeBpa(relevant, 1.0, total, LAMBDA_VAC_DOMAIN);
    }

    @Transactional(readOnly = true)
    public BpaResult getVacBpaByFamily(String profCode, String domain, String techFamily) {
        if (profCode == null || domain == null || techFamily == null) return BpaResult.empty();
        long total    = canonicalRepository.countTotalVacanciesForProfessionAndDomain(profCode, domain);
        long relevant = canonicalRepository.countVacanciesByTechFamilyAndDomainAndProfession(profCode, domain, techFamily);
        if (total == 0 || relevant == 0) return BpaResult.empty();
        return computeBpa(relevant, 1.0, total, LAMBDA_VAC);
    }

    @Transactional(readOnly = true)
    public BpaResult getWeightedVacBpaByFamily(List<ProfessionWeight> profs, String domain, String techFamily) {
        return weightedAverage(profs, p -> getVacBpaByFamily(p.professionCode(), domain, techFamily));
    }

    @Transactional(readOnly = true)
    public BpaResult getWeightedExpBpaByFamily(List<ProfessionWeight> profs, String domain, String techFamily) {
        return weightedAverage(profs, p -> getExpBpaByFamily(p.professionCode(), domain, techFamily));
    }

    @Transactional(readOnly = true)
    public BpaResult getWeightedFcBpaByFamily(List<ProfessionWeight> profs, String domain, String techFamily) {
        return weightedAverage(profs, p -> getFcBpaByFamily(p.professionCode(), domain, techFamily));
    }

    @Transactional(readOnly = true)
    public BpaResult getVacBpaByCanonical(String profCode, Long canonicalId) {
        long total = vacancyEntityRepository.count();
        SkillDomainStats stats = domainStatsRepository.findByCanonicalId(canonicalId).orElse(null);
        if (stats == null || total == 0) return BpaResult.empty();
        double avgScore = stats.getPctInDomain() != null ? stats.getPctInDomain().doubleValue() : 1.0;
        return computeBpa(stats.getVacancyCount(), avgScore, total, LAMBDA_VAC);
    }

    /**
     * Version variants for a canonical skill with global vacancy counts.
     * Uses skill_canonical siblings that share the same version_group family,
     * sorted by popularity (absoluteCount) descending.
     */
    @Transactional(readOnly = true)
    public List<VersionInfo> getVersionsForSkill(Long canonicalId) {
        List<Object[]> rows = canonicalRepository.findVersionSiblingsWithCounts(canonicalId);
        if (rows.isEmpty()) return List.of();

        SkillCanonical self = canonicalRepository.findById(canonicalId).orElse(null);
        String familyName = self != null
                ? (self.getVersionGroup() != null ? self.getVersionGroup() : self.getName())
                : "";
        String prefix = familyName + " ";

        return rows.stream()
                .map(r -> {
                    long   sibId = ((Number) r[0]).longValue();
                    String name  = (String)  r[1];
                    long   count = r[3] != null ? ((Number) r[3]).longValue() : 0L;
                    String versionStr = name.startsWith(prefix)
                            ? name.substring(prefix.length()).trim()
                            : name;
                    return new VersionInfo(sibId, name, versionStr, null, false, count);
                })
                .collect(Collectors.toList());
    }
}
