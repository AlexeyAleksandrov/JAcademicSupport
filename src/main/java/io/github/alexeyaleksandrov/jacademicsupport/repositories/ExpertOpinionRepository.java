package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertOpinionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpertOpinionRepository extends JpaRepository<ExpertOpinionEntity, Long> {

    // ─── DST aggregation queries (L0/L1/L2) ───────────────────────────────────

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT eo.expert_id) AS relevantCount,
               AVG(eo.skill_importance)     AS avgImportance
        FROM expert_opinion eo
        JOIN expert e ON e.id = eo.expert_id
        WHERE eo.domain = :domain
          AND eo.direction = 'POSITIVE'
          AND (:professionCode IS NULL OR eo.profession_code = :professionCode OR COALESCE(eo.profession_code,'') = '')
    """)
    List<Object[]> aggregateByDomain(@Param("domain") String domain,
                                    @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT eo.expert_id) AS relevantCount,
               AVG(eo.skill_importance)     AS avgImportance
        FROM expert_opinion eo
        JOIN expert e ON e.id = eo.expert_id
        WHERE eo.domain = :domain
          AND eo.tech_family = :techFamily
          AND eo.direction = 'POSITIVE'
          AND (:professionCode IS NULL OR eo.profession_code = :professionCode OR COALESCE(eo.profession_code,'') = '')
    """)
    List<Object[]> aggregateByDomainAndFamily(@Param("domain") String domain,
                                             @Param("techFamily") String techFamily,
                                             @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT eo.expert_id) AS relevantCount,
               AVG(eo.skill_importance)     AS avgImportance
        FROM expert_opinion eo
        JOIN expert e ON e.id = eo.expert_id
        WHERE eo.canonical_id = :canonicalId
          AND eo.direction = 'POSITIVE'
          AND (:professionCode IS NULL OR eo.profession_code = :professionCode OR COALESCE(eo.profession_code,'') = '')
    """)
    List<Object[]> aggregateByCanonical(@Param("canonicalId") Long canonicalId,
                                       @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT eo.expert_id) AS relevantCount,
               AVG(eo.skill_importance)     AS avgImportance
        FROM expert_opinion eo
        JOIN expert e ON e.id = eo.expert_id
        WHERE eo.canonical_id = :canonicalId
          AND eo.domain = :domain
          AND eo.direction = 'POSITIVE'
          AND (:professionCode IS NULL OR eo.profession_code = :professionCode OR COALESCE(eo.profession_code,'') = '')
    """)
    List<Object[]> aggregateByCanonicalAndDomain(@Param("canonicalId")    Long canonicalId,
                                                 @Param("domain")         String domain,
                                                 @Param("professionCode") String professionCode);
    
    // ─── Legacy queries (backward compat) ────────────────────────────────────

    // Найти все мнения экспертов по конкретному навыку
    List<ExpertOpinionEntity> findByWorkSkillId(Long workSkillId);
    
    // Получить количество уникальных экспертов, которые высказали мнение по данному навыку
    @Query("SELECT COUNT(DISTINCT eo.expert.id) FROM ExpertOpinionEntity eo WHERE eo.workSkill.id = :workSkillId")
    Long countDistinctExpertsByWorkSkillId(@Param("workSkillId") Long workSkillId);
    
    // Получить общее количество уникальных экспертов в системе
    @Query("SELECT COUNT(DISTINCT eo.expert.id) FROM ExpertOpinionEntity eo")
    Long countDistinctExperts();
}
