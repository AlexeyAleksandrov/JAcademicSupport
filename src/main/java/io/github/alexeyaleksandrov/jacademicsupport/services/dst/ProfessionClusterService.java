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
 * Computes profession_cluster.weight:
 * weight(profession P, cluster C) = fraction of P-vacancies that contain at least one skill from C.
 *
 * Requires vacancy_profession to be populated first (run VacancyProfessionService).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfessionClusterService {

    private final ProfessionRepository        professionRepository;
    private final ProfessionClusterRepository professionClusterRepository;
    private final VacancyProfessionRepository vacancyProfessionRepository;
    private final SkillsGroupRepository       skillsGroupRepository;
    private final WorkSkillRepository         workSkillRepository;

    @Transactional
    public WeightReport computeWeights() {
        List<Profession>  professions = professionRepository.findAll();
        List<SkillsGroup> clusters    = skillsGroupRepository.findAll();

        log.info("Computing profession-cluster weights: {} professions × {} clusters",
                professions.size(), clusters.size());

        int computed = 0;

        for (Profession profession : professions) {
            if ("other".equals(profession.getCode())) continue;

            List<VacancyProfession> vps = vacancyProfessionRepository.findByProfession(profession);
            if (vps.isEmpty()) continue;

            long totalVacancies = vps.size();

            for (SkillsGroup cluster : clusters) {
                List<WorkSkill> clusterSkills = workSkillRepository.findBySkillsGroupBySkillsGroupId(cluster);
                if (clusterSkills.isEmpty()) continue;

                Set<Long> clusterSkillIds = new HashSet<>();
                clusterSkills.forEach(s -> clusterSkillIds.add(s.getId()));

                long matchCount = vps.stream()
                        .filter(vp -> {
                            List<WorkSkill> vacSkills = vp.getVacancy().getSkills();
                            if (vacSkills == null) return false;
                            return vacSkills.stream().anyMatch(s -> clusterSkillIds.contains(s.getId()));
                        })
                        .count();

                if (matchCount == 0) continue;

                double w = (double) matchCount / totalVacancies;
                BigDecimal weight = BigDecimal.valueOf(w).setScale(4, RoundingMode.HALF_UP);

                saveProfessionCluster(profession, cluster, weight);
                computed++;
            }
        }

        log.info("Profession-cluster weights computed: {} rows upserted", computed);
        return new WeightReport(computed);
    }

    private void saveProfessionCluster(Profession profession, SkillsGroup cluster, BigDecimal weight) {
        ProfessionCluster.ProfessionClusterId id =
                new ProfessionCluster.ProfessionClusterId(profession.getId(), cluster.getId());

        professionClusterRepository.findById(id).ifPresentOrElse(
                existing -> {
                    existing.setWeight(weight);
                    professionClusterRepository.save(existing);
                },
                () -> {
                    ProfessionCluster pc = new ProfessionCluster();
                    pc.setProfession(profession);
                    pc.setCluster(cluster);
                    pc.setWeight(weight);
                    professionClusterRepository.save(pc);
                }
        );
    }

    public record WeightReport(int rowsUpserted) {}
}
