package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillDomainStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillDomainStatsRepository extends JpaRepository<SkillDomainStats, Long> {

    Optional<SkillDomainStats> findByCanonicalId(Long canonicalId);

    List<SkillDomainStats> findByDomainOrderByVacancyCountDesc(String domain);

    @org.springframework.data.jpa.repository.Query(nativeQuery = true,
        value = "SELECT COUNT(DISTINCT vacancy_id) FROM vacancy_domain WHERE primary_domain = :domain")
    long countVacanciesByDomain(@org.springframework.data.repository.query.Param("domain") String domain);

    @org.springframework.data.jpa.repository.Query(nativeQuery = true,
        value = """
            SELECT COUNT(DISTINCT vs.vacancy_entity_id)
            FROM vacancy_skills vs
            JOIN work_skill_canonical wsc ON wsc.work_skill_id = vs.skills_id
            JOIN skill_canonical sc ON sc.id = wsc.canonical_id
            WHERE sc.tech_family = :techFamily
            """)
    long countVacanciesByTechFamily(@org.springframework.data.repository.query.Param("techFamily") String techFamily);
}
