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
    private final DstSettingsService            settingsService;

    /** Legacy cluster-score cut-off; editable via dst_settings.clusterMinScore. */
    private BigDecimal minScore() {
        Double v = settingsService.get().getClusterMinScore();
        return BigDecimal.valueOf(v != null ? v : DstSettingsDefaults.CLUSTER_MIN_SCORE);
    }

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
                .findByProfessionAndCluster(profCode, clusterId, minScore());

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
                .findByProfessionAndCluster(profCode, clusterId, minScore());
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
     * BPA result for one DST source (VAC, EXP or FC) at a given level.
     * A direct result uses the formulas below. A curriculum-weighted result is
     * a convex sum of direct results and exposes the exact terms in
     * {@code professionContributions}; its aggregate counts are informational.
     *
     * mT = m(T) = κ⁺ × averageScore          — support for hypothesis "relevant"
     * mF = m(F) = κ⁻ × averageNegativeScore  — support for hypothesis "not relevant"
     * mTheta = m(Θ) = 1 − mT − mF            — ignorance mass
     *
     * A non-zero mF is what makes the conflict coefficient
     * K = m1(T)·m2(F) + m1(F)·m2(T) non-zero and Yager's rule reachable.
     */
    public record BpaResult(
            long   relevantCount,
            long   totalCount,
            double lambda,
            double kappa,
            double averageScore,
            double mT,
            double mTheta,
            long   negativeCount,
            double negativeKappa,
            double averageNegativeScore,
            double mF,
            double massNormalizationFactor,
            List<ProfessionBpaContribution> professionContributions
    ) {
        public static BpaResult empty() {
            return new BpaResult(0, 0, 0.0, 0.0, 0.0, 0.0, 1.0,
                    0, 0.0, 0.0, 0.0, 1.0, List.of());
        }

        /** True when the source carries any evidence at all (positive or negative). */
        public boolean hasEvidence() {
            return mT > 0.0 || mF > 0.0 || relevantCount > 0 || negativeCount > 0;
        }

        public boolean professionWeighted() {
            return professionContributions != null && !professionContributions.isEmpty();
        }
    }

    /** Exact per-profession term of a curriculum-weighted BPA mixture. */
    public record ProfessionBpaContribution(
            String professionCode,
            String professionName,
            double weight,
            boolean hasEvidence,
            long relevantCount,
            long totalCount,
            double lambda,
            double kappa,
            double averageScore,
            long negativeCount,
            double negativeKappa,
            double averageNegativeScore,
            double massNormalizationFactor,
            double mT,
            double mTheta,
            double mF,
            double weightedMT,
            double weightedMTheta,
            double weightedMF
    ) {}

    private double lambdaExpL1()     { return settingsService.get().getLambdaExpL1(); }
    private double lambdaFcL1()      { return settingsService.get().getLambdaFcL1(); }
    private double lambdaVacL1()     { return settingsService.get().getLambdaVacL1(); }
    private double lambdaExpL2()     { return settingsService.get().getLambdaExpL2(); }
    private double lambdaFcL2()      { return settingsService.get().getLambdaFcL2(); }
    private double lambdaVacL2()     { return settingsService.get().getLambdaVacL2(); }
    private double lambdaExpDomain() { return settingsService.get().getLambdaExpDomain(); }
    private double lambdaFcDomain()  { return settingsService.get().getLambdaFcDomain(); }
    private double lambdaVacDomain() { return settingsService.get().getLambdaVacDomain(); }
    private long   totalExperts()    { return settingsService.effectiveTotalExperts(); }
    private long   totalSources()    { return settingsService.effectiveTotalSources(); }

    private boolean negativeEnabled() {
        Boolean flag = settingsService.get().getNegativeEvidenceEnabled();
        return flag == null || flag;
    }

    private static BpaResult computeBpa(long relevantCount, double avgScore, long total, double lambda) {
        return computeBpa(relevantCount, avgScore, 0L, 0.0, total, lambda);
    }

    /**
     * Builds the BPA from a positive and a negative evidence count.
     * κ = 1 − exp(−λ·n/N) is applied independently to both directions; if the
     * resulting masses exceed 1 they are scaled down so that m(Θ) ≥ 0.
     */
    static BpaResult computeBpa(long relevantCount, double avgScore,
                                long negativeCount, double avgNegativeScore,
                                long total, double lambda) {
        if (total == 0) return BpaResult.empty();

        double kappa = relevantCount > 0
                ? 1.0 - Math.exp(-lambda * (double) relevantCount / total)
                : 0.0;
        double negativeKappa = negativeCount > 0
                ? 1.0 - Math.exp(-lambda * (double) negativeCount / total)
                : 0.0;
        double mT = kappa * avgScore;
        double mF = negativeKappa * avgNegativeScore;

        double sum = mT + mF;
        double normalizationFactor = sum > 1.0 ? 1.0 / sum : 1.0;
        mT *= normalizationFactor;
        mF *= normalizationFactor;

        return new BpaResult(relevantCount, total, lambda, kappa, avgScore,
                mT, 1.0 - mT - mF, negativeCount, negativeKappa,
                avgNegativeScore, mF, normalizationFactor, List.of());
    }

    private BpaResult extractBpa(List<Object[]> rows, long total, double lambda) {
        return extractBpa(rows, List.of(), total, lambda);
    }

    /** Merges a POSITIVE aggregate row with its NEGATIVE mirror into a single BPA. */
    private BpaResult extractBpa(List<Object[]> positiveRows, List<Object[]> negativeRows,
                                 long total, double lambda) {
        long   cnt = 0L;
        double avg = 0.0;
        if (!positiveRows.isEmpty() && positiveRows.get(0)[0] != null) {
            Object[] row = positiveRows.get(0);
            cnt = ((Number) row[0]).longValue();
            avg = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
        }

        long   negCnt = 0L;
        double negAvg = 0.0;
        if (negativeEnabled() && !negativeRows.isEmpty() && negativeRows.get(0)[0] != null) {
            Object[] row = negativeRows.get(0);
            negCnt = ((Number) row[0]).longValue();
            negAvg = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
        }

        return computeBpa(cnt, avg, negCnt, negAvg, total, lambda);
    }

    @Transactional(readOnly = true)
    public BpaResult getExpBpaByDomain(String profCode, String domain) {
        return extractBpa(expertOpinionRepository.aggregateByDomain(domain, profCode),
                expertOpinionRepository.aggregateNegativeByDomain(domain, profCode),
                totalExperts(), lambdaExpDomain());
    }

    @Transactional(readOnly = true)
    public BpaResult getExpBpaByFamily(String profCode, String domain, String techFamily) {
        return extractBpa(expertOpinionRepository.aggregateByDomainAndFamily(domain, techFamily, profCode),
                expertOpinionRepository.aggregateNegativeByDomainAndFamily(domain, techFamily, profCode),
                totalExperts(), lambdaExpL1());
    }

    @Transactional(readOnly = true)
    public BpaResult getExpBpaByCanonical(String profCode, Long canonicalId) {
        return extractBpa(expertOpinionRepository.aggregateByCanonical(canonicalId, profCode),
                expertOpinionRepository.aggregateNegativeByCanonical(canonicalId, profCode),
                totalExperts(), lambdaExpL2());
    }

    @Transactional(readOnly = true)
    public BpaResult getFcBpaByDomain(String profCode, String domain) {
        return extractBpa(foresightRepository.aggregateByDomain(domain, profCode),
                foresightRepository.aggregateNegativeByDomain(domain, profCode),
                totalSources(), lambdaFcDomain());
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

    /**
     * Convex mixture of already-computed per-profession BPAs.
     *
     * The curriculum weights are normalised across every profession, including
     * professions without evidence for the current object. An empty profession
     * therefore contributes pure ignorance m(Θ)=1 instead of silently donating
     * its share to professions that happen to have data.
     */
    static BpaResult weightedAverage(List<ProfessionWeight> profs,
                                     java.util.function.Function<ProfessionWeight, BpaResult> fn) {
        if (profs == null || profs.isEmpty()) return BpaResult.empty();

        for (ProfessionWeight pw : profs) {
            if (!Double.isFinite(pw.weight()) || pw.weight() < 0.0) {
                throw new IllegalArgumentException("Profession weight must be finite and non-negative: "
                        + pw.professionCode());
            }
        }

        double rawWeightSum = profs.stream().mapToDouble(ProfessionWeight::weight).sum();
        boolean equalWeights = rawWeightSum <= 0.0;
        double equalWeight = 1.0 / profs.size();

        double sumMT = 0.0, sumMTheta = 0.0, sumMF = 0.0;
        double sumKappa = 0.0, sumNegativeKappa = 0.0;
        double sumScore = 0.0, sumNegativeScore = 0.0;
        double sumCnt = 0.0, sumNegCnt = 0.0, sumTotal = 0.0;
        double lambda = 0.0;
        List<ProfessionBpaContribution> contributions = new ArrayList<>();

        for (ProfessionWeight pw : profs) {
            double weight = equalWeights ? equalWeight : pw.weight() / rawWeightSum;
            BpaResult r = fn.apply(new ProfessionWeight(
                    pw.professionCode(), pw.professionName(), weight));

            sumMT            += weight * r.mT();
            sumMTheta        += weight * r.mTheta();
            sumMF            += weight * r.mF();
            sumKappa         += weight * r.kappa();
            sumNegativeKappa += weight * r.negativeKappa();
            sumScore         += weight * r.averageScore();
            sumNegativeScore += weight * r.averageNegativeScore();
            sumCnt           += weight * r.relevantCount();
            sumNegCnt        += weight * r.negativeCount();
            sumTotal         += weight * r.totalCount();
            if (lambda == 0.0 && r.lambda() > 0.0) lambda = r.lambda();

            contributions.add(new ProfessionBpaContribution(
                    pw.professionCode(), pw.professionName(), weight, r.hasEvidence(),
                    r.relevantCount(), r.totalCount(), r.lambda(), r.kappa(), r.averageScore(),
                    r.negativeCount(), r.negativeKappa(), r.averageNegativeScore(),
                    r.massNormalizationFactor(), r.mT(), r.mTheta(), r.mF(),
                    weight * r.mT(), weight * r.mTheta(), weight * r.mF()));
        }

        double massSum = sumMT + sumMTheta + sumMF;
        if (massSum > 0.0) {
            sumMT /= massSum;
            sumMTheta /= massSum;
            sumMF /= massSum;
        }

        return new BpaResult(
                Math.round(sumCnt), Math.round(sumTotal), lambda, sumKappa, sumScore,
                sumMT, sumMTheta, Math.round(sumNegCnt), sumNegativeKappa,
                sumNegativeScore, sumMF, 1.0, List.copyOf(contributions));
    }

    @Transactional(readOnly = true)
    public BpaResult getFcBpaByFamily(String profCode, String domain, String techFamily) {
        return extractBpa(foresightRepository.aggregateByDomainAndFamily(domain, techFamily, profCode),
                foresightRepository.aggregateNegativeByDomainAndFamily(domain, techFamily, profCode),
                totalSources(), lambdaFcL1());
    }

    @Transactional(readOnly = true)
    public BpaResult getFcBpaByCanonical(String profCode, Long canonicalId) {
        return extractBpa(foresightRepository.aggregateByCanonical(canonicalId, profCode),
                foresightRepository.aggregateNegativeByCanonical(canonicalId, profCode),
                totalSources(), lambdaFcL2());
    }

    @Transactional(readOnly = true)
    public BpaResult getVacBpaByFamilySkill(String profCode, String domain, String techFamily, Long canonicalId) {
        if (profCode == null || domain == null || techFamily == null || canonicalId == null) return BpaResult.empty();
        long total    = canonicalRepository.countVacanciesByTechFamilyAndDomainAndProfession(profCode, domain, techFamily);
        long relevant = canonicalRepository.countVacanciesByCanonicalAndProfession(profCode, canonicalId);
        if (total == 0) return BpaResult.empty();
        return computeBpa(relevant, 1.0, total, lambdaVacL2());
    }

    @Transactional(readOnly = true)
    public BpaResult getExpBpaByCanonicalAndDomain(String profCode, Long canonicalId, String domain) {
        return extractBpa(expertOpinionRepository.aggregateByCanonicalAndDomain(canonicalId, domain, profCode),
                expertOpinionRepository.aggregateNegativeByCanonicalAndDomain(canonicalId, domain, profCode),
                totalExperts(), lambdaExpL2());
    }

    @Transactional(readOnly = true)
    public BpaResult getFcBpaByCanonicalAndDomain(String profCode, Long canonicalId, String domain) {
        return extractBpa(foresightRepository.aggregateByCanonicalAndDomain(canonicalId, domain, profCode),
                foresightRepository.aggregateNegativeByCanonicalAndDomain(canonicalId, domain, profCode),
                totalSources(), lambdaFcL2());
    }

    @Transactional(readOnly = true)
    public BpaResult getWeightedVacBpaByFamilySkill(List<ProfessionWeight> profs, String domain, String techFamily, Long canonicalId) {
        return weightedAverage(profs, p -> getVacBpaByFamilySkill(p.professionCode(), domain, techFamily, canonicalId));
    }

    @Transactional(readOnly = true)
    public BpaResult getWeightedExpBpaByCanonicalAndDomain(List<ProfessionWeight> profs, Long canonicalId, String domain) {
        return weightedAverage(profs, p -> getExpBpaByCanonicalAndDomain(p.professionCode(), canonicalId, domain));
    }

    @Transactional(readOnly = true)
    public BpaResult getWeightedFcBpaByCanonicalAndDomain(List<ProfessionWeight> profs, Long canonicalId, String domain) {
        return weightedAverage(profs, p -> getFcBpaByCanonicalAndDomain(p.professionCode(), canonicalId, domain));
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

    @Transactional(readOnly = true)
    public BpaResult getVacBpaByDomain(String profCode, String domain) {
        if (profCode == null) return BpaResult.empty();
        long total    = canonicalRepository.countTotalVacanciesForProfession(profCode);
        long relevant = canonicalRepository.countVacanciesByProfessionAndDomain(profCode, domain);
        if (total == 0) return BpaResult.empty();
        return computeBpa(relevant, 1.0, total, lambdaVacDomain());
    }

    @Transactional(readOnly = true)
    public BpaResult getVacBpaByFamily(String profCode, String domain, String techFamily) {
        if (profCode == null || domain == null || techFamily == null) return BpaResult.empty();
        long total    = canonicalRepository.countTotalVacanciesForProfessionAndDomain(profCode, domain);
        long relevant = canonicalRepository.countVacanciesByTechFamilyAndDomainAndProfession(profCode, domain, techFamily);
        if (total == 0) return BpaResult.empty();
        return computeBpa(relevant, 1.0, total, lambdaVacL1());
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
        return computeBpa(stats.getVacancyCount(), avgScore, total, lambdaVacL2());
    }

    /**
     * Version variants for a canonical skill with global vacancy counts.
     * Primary: reads from skill_version table (populated after normalization).
     * Fallback: skill_canonical siblings that share the same version_group.
     */
    @Transactional(readOnly = true)
    public List<VersionInfo> getVersionsForSkill(Long canonicalId) {
        // Primary: skill_version table (normalized, populated by normalize_skills.py Phase 1)
        List<Object[]> versionRows = versionRepository.findVersionsWithCounts(canonicalId);
        if (!versionRows.isEmpty()) {
            return versionRows.stream()
                    .map(r -> new VersionInfo(
                            ((Number) r[0]).longValue(),
                            (String)  r[1],
                            (String)  r[2],
                            (String)  r[3],
                            r[4] != null && (Boolean) r[4],
                            r[5] != null ? ((Number) r[5]).longValue() : 0L
                    ))
                    .collect(Collectors.toList());
        }

        // Fallback: version_group siblings in skill_canonical (legacy, pre-normalization)
        List<Object[]> rows = canonicalRepository.findVersionSiblingsWithCounts(canonicalId);
        if (rows.isEmpty()) return List.of();

        SkillCanonical self = canonicalRepository.findById(canonicalId).orElse(null);
        String familyName = self != null
                ? (self.getVersionGroup() != null ? self.getVersionGroup() : self.getName())
                : "";
        String prefix = familyName + " ";

        return rows.stream()
                .filter(r -> ((Number) r[0]).longValue() != canonicalId)
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
