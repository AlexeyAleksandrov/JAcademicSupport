package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillDependency;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillCanonicalRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillDependencyRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.VacancyEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Builds a directed skill dependency graph from vacancy co-occurrence statistics.
 *
 * Direction:  child → parent  means "having child implies parent is needed".
 * Example:    FastAPI → Python (95% of FastAPI vacancies also require Python).
 *
 * Weight = P(parent | child) = co_occurrence(A,B) / total_vacancies_with_B
 *
 * Only pairs with weight >= threshold are persisted (default 0.30).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SkillDependencyService {

    private final VacancyEntityRepository  vacancyRepository;
    private final SkillCanonicalRepository canonicalRepository;
    private final SkillDependencyRepository dependencyRepository;

    private static final double DEFAULT_THRESHOLD = 0.30;
    private static final int    MIN_CO_OCCURRENCE = 5;

    @Transactional
    public DependencyReport buildGraph(double threshold) {
        List<VacancyEntity>   vacancies  = vacancyRepository.findAll();
        List<SkillCanonical>  canonicals = canonicalRepository.findAll();

        log.info("Building co-occurrence graph: {} vacancies, {} canonical skills, threshold={}",
                vacancies.size(), canonicals.size(), threshold);

        // Map canonical_id → SkillCanonical for fast lookup
        Map<Long, SkillCanonical> canonicalMap = new HashMap<>();
        canonicals.forEach(c -> canonicalMap.put(c.getId(), c));

        // co[A][B] = number of vacancies containing both A and B
        Map<Long, Map<Long, Integer>> coMatrix = new HashMap<>();
        // totalWithSkill[A] = vacancies containing A
        Map<Long, Integer> totalWith = new HashMap<>();

        for (VacancyEntity vacancy : vacancies) {
            if (vacancy.getSkills() == null) continue;

            Set<Long> canonicalIds = new HashSet<>();
            for (WorkSkill skill : vacancy.getSkills()) {
                if (skill.getCanonicalId() != null) {
                    canonicalIds.add(skill.getCanonicalId());
                }
            }

            for (Long id : canonicalIds) {
                totalWith.merge(id, 1, Integer::sum);
                for (Long other : canonicalIds) {
                    if (!id.equals(other)) {
                        coMatrix.computeIfAbsent(id, k -> new HashMap<>())
                                .merge(other, 1, Integer::sum);
                    }
                }
            }
        }

        int saved = 0, skipped = 0;

        for (Map.Entry<Long, Map<Long, Integer>> parentEntry : coMatrix.entrySet()) {
            Long parentId = parentEntry.getKey();
            SkillCanonical parent = canonicalMap.get(parentId);
            if (parent == null) continue;

            for (Map.Entry<Long, Integer> childEntry : parentEntry.getValue().entrySet()) {
                Long childId = childEntry.getKey();
                int  coCount = childEntry.getValue();

                if (coCount < MIN_CO_OCCURRENCE) { skipped++; continue; }

                SkillCanonical child = canonicalMap.get(childId);
                if (child == null) continue;

                int totalWithChild = totalWith.getOrDefault(childId, 1);
                double w = (double) coCount / totalWithChild;

                if (w < threshold) { skipped++; continue; }

                BigDecimal weight = BigDecimal.valueOf(w).setScale(5, RoundingMode.HALF_UP);

                Optional<SkillDependency> existing = dependencyRepository.findByParentAndChild(parent, child);
                if (existing.isPresent()) {
                    SkillDependency dep = existing.get();
                    dep.setCoOccurrenceCnt(coCount);
                    dep.setWeight(weight);
                    dependencyRepository.save(dep);
                } else {
                    SkillDependency dep = new SkillDependency();
                    dep.setParent(parent);
                    dep.setChild(child);
                    dep.setCoOccurrenceCnt(coCount);
                    dep.setWeight(weight);
                    dependencyRepository.save(dep);
                    saved++;
                }
            }
        }

        log.info("Dependency graph built: {} edges saved, {} skipped (below threshold or min co-occ)",
                saved, skipped);
        return new DependencyReport(saved, skipped, threshold);
    }

    @Transactional
    public DependencyReport buildGraph() {
        return buildGraph(DEFAULT_THRESHOLD);
    }

    /**
     * Returns all canonical skills that are implied by the given child skill
     * (i.e. all parents reachable via dependency edges with weight >= minWeight).
     */
    public Set<SkillCanonical> resolveImpliedSkills(SkillCanonical child, double minWeight) {
        Set<SkillCanonical> implied = new HashSet<>();
        Queue<SkillCanonical> queue = new LinkedList<>();
        queue.add(child);
        Set<Long> visited = new HashSet<>();

        BigDecimal minW = BigDecimal.valueOf(minWeight);

        while (!queue.isEmpty()) {
            SkillCanonical current = queue.poll();
            if (!visited.add(current.getId())) continue;

            List<SkillDependency> deps = dependencyRepository.findByChildWithMinWeight(current, minW);
            for (SkillDependency dep : deps) {
                SkillCanonical parent = dep.getParent();
                implied.add(parent);
                queue.add(parent);
            }
        }
        return implied;
    }

    public record DependencyReport(int edgesSaved, int edgesSkipped, double threshold) {}
}
