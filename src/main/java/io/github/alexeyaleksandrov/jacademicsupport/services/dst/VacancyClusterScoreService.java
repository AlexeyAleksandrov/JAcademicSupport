package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.models.*;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Computes vacancy_cluster_score for every (vacancy, cluster) pair.
 *
 * Score accumulation weights:
 *   - Skill found in vacancy name (title):        1.0
 *   - Skill found in vacancy skills list:         0.8
 *   - Skill found in vacancy description text:    0.5
 *   - Skill reached via dependency graph:         weight × base_weight
 *
 * Raw accumulated score is then normalised to [0, 1] using max-normalisation per vacancy.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VacancyClusterScoreService {

    private final VacancyEntityRepository       vacancyRepository;
    private final SkillsGroupRepository         skillsGroupRepository;
    private final WorkSkillRepository           workSkillRepository;
    private final VacancyClusterScoreRepository scoreRepository;
    private final SkillCanonicalRepository      canonicalRepository;
    private final SkillDependencyRepository     dependencyRepository;

    private static final double WEIGHT_TITLE      = 1.0;
    private static final double WEIGHT_SKILLS     = 0.8;
    private static final double WEIGHT_DESC       = 0.5;
    private static final double MIN_DEP_WEIGHT    = 0.30;

    @Transactional
    public ScoreReport computeScores() {
        List<VacancyEntity> vacancies = vacancyRepository.findAll();
        List<SkillsGroup>   clusters  = skillsGroupRepository.findAll();

        log.info("Computing vacancy-cluster scores: {} vacancies × {} clusters", vacancies.size(), clusters.size());

        // Build cluster → canonical skill IDs map
        Map<Long, Set<Long>> clusterCanonicalIds = buildClusterCanonicalMap(clusters);

        // Build canonical → all implied canonical IDs (via dependency) map
        // (pre-computed for performance)
        Map<Long, Set<Long>> impliedMap = buildImpliedMap();

        // Pre-load ALL canonicals into memory to avoid N+1 queries inside loops
        Map<Long, SkillCanonical> canonicalCache = new HashMap<>();
        canonicalRepository.findAll().forEach(c -> canonicalCache.put(c.getId(), c));
        log.info("Loaded {} canonical skills into cache", canonicalCache.size());

        int saved = 0;
        int total = vacancies.size(), idx = 0;

        for (VacancyEntity vacancy : vacancies) {
            idx++;
            Map<Long, ScoreAccumulator> accumulators = new HashMap<>();

            String titleLower = vacancy.getName() != null
                    ? vacancy.getName().toLowerCase() : "";
            String descLower  = vacancy.getDescription() != null
                    ? vacancy.getDescription().toLowerCase() : "";
            List<WorkSkill> skills = vacancy.getSkills() != null
                    ? vacancy.getSkills() : Collections.emptyList();

            // Collect canonical IDs from skill list
            Set<Long> directCanonicals = new HashSet<>();
            for (WorkSkill skill : skills) {
                if (skill.getCanonicalId() != null) {
                    directCanonicals.add(skill.getCanonicalId());
                }
            }

            // Expand via dependencies
            Set<Long> allCanonicals = new HashSet<>(directCanonicals);
            for (Long cid : directCanonicals) {
                Set<Long> implied = impliedMap.getOrDefault(cid, Collections.emptySet());
                allCanonicals.addAll(implied);
            }

            for (Map.Entry<Long, Set<Long>> clusterEntry : clusterCanonicalIds.entrySet()) {
                Long     clusterId        = clusterEntry.getKey();
                Set<Long> clusterCanonics = clusterEntry.getValue();

                double raw        = 0.0;
                boolean fromTitle = false, fromSkills = false, fromDesc = false, viaDep = false;

                for (Long canonId : clusterCanonics) {
                    // Find canonical name for text-matching
                    SkillCanonical canonical = canonicalCache.get(canonId);
                    if (canonical == null || "unknown".equals(canonical.getTechType())) continue;

                    String skillNameLower = canonical.getNormalizedName();

                    boolean inSkillsList = directCanonicals.contains(canonId);
                    boolean inImplied    = !inSkillsList && allCanonicals.contains(canonId);
                    boolean inTitle      = !titleLower.isEmpty() && titleLower.contains(skillNameLower);
                    boolean inDesc       = !descLower.isEmpty()  && descLower.contains(skillNameLower);

                    if (inTitle) {
                        raw += WEIGHT_TITLE;
                        fromTitle = true;
                    }
                    if (inSkillsList) {
                        raw += WEIGHT_SKILLS;
                        fromSkills = true;
                    }
                    if (inDesc) {
                        raw += WEIGHT_DESC;
                        fromDesc = true;
                    }
                    if (inImplied) {
                        raw += WEIGHT_SKILLS * MIN_DEP_WEIGHT;
                        viaDep = true;
                    }
                }

                if (raw > 0) {
                    accumulators.put(clusterId, new ScoreAccumulator(raw, fromTitle, fromSkills, fromDesc, viaDep));
                }
            }

            if (accumulators.isEmpty()) {
                if (idx % 100 == 0)
                    log.info("Cluster scores progress: {}/{} | rows={}", idx, total, saved);
                continue;
            }

            // Normalise scores within this vacancy
            double maxRaw = accumulators.values().stream()
                    .mapToDouble(a -> a.raw).max().orElse(1.0);

            for (Map.Entry<Long, ScoreAccumulator> entry : accumulators.entrySet()) {
                Long             clusterId = entry.getKey();
                ScoreAccumulator acc       = entry.getValue();

                double normalised = acc.raw / maxRaw;
                BigDecimal score  = BigDecimal.valueOf(normalised).setScale(5, RoundingMode.HALF_UP);

                SkillsGroup cluster = skillsGroupRepository.findById(clusterId).orElse(null);
                if (cluster == null) continue;

                upsertScore(vacancy, cluster, score, acc);
                saved++;
            }

            if (idx % 100 == 0)
                log.info("Cluster scores progress: {}/{} | rows={}", idx, total, saved);
        }

        log.info("Cluster scores DONE: {}/{} | rows upserted={}", total, total, saved);
        return new ScoreReport(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private Map<Long, Set<Long>> buildClusterCanonicalMap(List<SkillsGroup> clusters) {
        Map<Long, Set<Long>> map = new HashMap<>();
        for (SkillsGroup cluster : clusters) {
            List<WorkSkill> skills = workSkillRepository.findBySkillsGroupBySkillsGroupId(cluster);
            Set<Long> ids = new HashSet<>();
            for (WorkSkill s : skills) {
                if (s.getCanonicalId() != null) ids.add(s.getCanonicalId());
            }
            if (!ids.isEmpty()) map.put(cluster.getId(), ids);
        }
        return map;
    }

    private Map<Long, Set<Long>> buildImpliedMap() {
        Map<Long, Set<Long>> map = new HashMap<>();
        List<SkillDependency> allDeps = dependencyRepository.findAll();
        for (SkillDependency dep : allDeps) {
            if (dep.getWeight() == null) continue;
            if (dep.getWeight().doubleValue() < MIN_DEP_WEIGHT) continue;
            Long childId  = dep.getChild().getId();
            Long parentId = dep.getParent().getId();
            map.computeIfAbsent(childId, k -> new HashSet<>()).add(parentId);
        }
        return map;
    }

    private void upsertScore(VacancyEntity vacancy, SkillsGroup cluster,
                             BigDecimal score, ScoreAccumulator acc) {
        VacancyClusterScore.VacancyClusterScoreId id =
                new VacancyClusterScore.VacancyClusterScoreId(vacancy.getId(), cluster.getId());

        scoreRepository.findById(id).ifPresentOrElse(
                existing -> {
                    existing.setScore(score);
                    existing.setFromTitle(acc.fromTitle);
                    existing.setFromSkills(acc.fromSkills);
                    existing.setFromDesc(acc.fromDesc);
                    existing.setViaDependency(acc.viaDep);
                    scoreRepository.save(existing);
                },
                () -> {
                    VacancyClusterScore vcs = new VacancyClusterScore();
                    vcs.setVacancy(vacancy);
                    vcs.setCluster(cluster);
                    vcs.setScore(score);
                    vcs.setFromTitle(acc.fromTitle);
                    vcs.setFromSkills(acc.fromSkills);
                    vcs.setFromDesc(acc.fromDesc);
                    vcs.setViaDependency(acc.viaDep);
                    scoreRepository.save(vcs);
                }
        );
    }

    private record ScoreAccumulator(double raw, boolean fromTitle, boolean fromSkills,
                                     boolean fromDesc, boolean viaDep) {}

    public record ScoreReport(int rowsUpserted) {}
}
