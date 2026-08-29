package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertOpinionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpertOpinionRepository extends JpaRepository<ExpertOpinionEntity, Long> {

    /**
     * Explicit profession_code wins. For legacy/null codes, infer applicability
     * from the most specific evidence object through vacancies classified into
     * professions. EXISTS prevents one opinion from being multiplied by the
     * number of matching vacancies.
     */
    String PROFESSION_SCOPE = """
          AND (
                :professionCode IS NULL
                OR eo.profession_code = :professionCode
                OR (
                    COALESCE(eo.profession_code, '') = ''
                    AND (
                        (eo.canonical_id IS NOT NULL AND EXISTS (
                            SELECT 1
                            FROM work_skill_canonical rel_wsc
                            JOIN vacancy_skills rel_vs     ON rel_vs.skills_id = rel_wsc.work_skill_id
                            JOIN vacancy_profession rel_vp ON rel_vp.vacancy_id = rel_vs.vacancy_entity_id
                            JOIN profession rel_p          ON rel_p.id = rel_vp.profession_id
                            WHERE rel_wsc.canonical_id = eo.canonical_id AND rel_p.code = :professionCode
                        ))
                        OR (eo.canonical_id IS NULL AND eo.work_skill_id IS NOT NULL AND EXISTS (
                            SELECT 1
                            FROM vacancy_skills rel_vs
                            JOIN vacancy_profession rel_vp ON rel_vp.vacancy_id = rel_vs.vacancy_entity_id
                            JOIN profession rel_p          ON rel_p.id = rel_vp.profession_id
                            WHERE rel_vs.skills_id = eo.work_skill_id AND rel_p.code = :professionCode
                        ))
                        OR (eo.canonical_id IS NULL AND eo.work_skill_id IS NULL
                            AND eo.tech_family IS NOT NULL AND EXISTS (
                            SELECT 1
                            FROM skill_canonical rel_sc
                            JOIN work_skill_canonical rel_wsc ON rel_wsc.canonical_id = rel_sc.id
                            JOIN vacancy_skills rel_vs         ON rel_vs.skills_id = rel_wsc.work_skill_id
                            JOIN vacancy_profession rel_vp     ON rel_vp.vacancy_id = rel_vs.vacancy_entity_id
                            JOIN profession rel_p              ON rel_p.id = rel_vp.profession_id
                            WHERE rel_sc.domain = eo.domain AND rel_sc.tech_family = eo.tech_family
                              AND rel_p.code = :professionCode
                        ))
                        OR (eo.canonical_id IS NULL AND eo.work_skill_id IS NULL
                            AND eo.tech_family IS NULL AND EXISTS (
                            SELECT 1
                            FROM skill_canonical rel_sc
                            JOIN work_skill_canonical rel_wsc ON rel_wsc.canonical_id = rel_sc.id
                            JOIN vacancy_skills rel_vs         ON rel_vs.skills_id = rel_wsc.work_skill_id
                            JOIN vacancy_profession rel_vp     ON rel_vp.vacancy_id = rel_vs.vacancy_entity_id
                            JOIN profession rel_p              ON rel_p.id = rel_vp.profession_id
                            WHERE rel_sc.domain = eo.domain AND rel_p.code = :professionCode
                        ))
                    )
                )
          )
        """;

    // ─── DST aggregation queries (L0/L1/L2) ───────────────────────────────────

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT eo.expert_id) AS relevantCount,
               AVG(eo.skill_importance)     AS avgImportance
        FROM expert_opinion eo
        JOIN expert e ON e.id = eo.expert_id
        WHERE eo.domain = :domain
          AND eo.direction = 'POSITIVE'
    """ + PROFESSION_SCOPE)
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
    """ + PROFESSION_SCOPE)
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
    """ + PROFESSION_SCOPE)
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
    """ + PROFESSION_SCOPE)
    List<Object[]> aggregateByCanonicalAndDomain(@Param("canonicalId")    Long canonicalId,
                                                 @Param("domain")         String domain,
                                                 @Param("professionCode") String professionCode);
    
    // ─── Negative evidence aggregation (m(F) support) ─────────────────────────
    // Mirrors of the POSITIVE queries above; feed the m(F) mass so that the
    // conflict coefficient K can become non-zero and Yager's rule reachable.

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT eo.expert_id) AS negativeCount,
               AVG(eo.skill_importance)     AS avgImportance
        FROM expert_opinion eo
        JOIN expert e ON e.id = eo.expert_id
        WHERE eo.domain = :domain
          AND eo.direction = 'NEGATIVE'
    """ + PROFESSION_SCOPE)
    List<Object[]> aggregateNegativeByDomain(@Param("domain") String domain,
                                             @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT eo.expert_id) AS negativeCount,
               AVG(eo.skill_importance)     AS avgImportance
        FROM expert_opinion eo
        JOIN expert e ON e.id = eo.expert_id
        WHERE eo.domain = :domain
          AND eo.tech_family = :techFamily
          AND eo.direction = 'NEGATIVE'
    """ + PROFESSION_SCOPE)
    List<Object[]> aggregateNegativeByDomainAndFamily(@Param("domain") String domain,
                                                      @Param("techFamily") String techFamily,
                                                      @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT eo.expert_id) AS negativeCount,
               AVG(eo.skill_importance)     AS avgImportance
        FROM expert_opinion eo
        JOIN expert e ON e.id = eo.expert_id
        WHERE eo.canonical_id = :canonicalId
          AND eo.direction = 'NEGATIVE'
    """ + PROFESSION_SCOPE)
    List<Object[]> aggregateNegativeByCanonical(@Param("canonicalId") Long canonicalId,
                                                @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT eo.expert_id) AS negativeCount,
               AVG(eo.skill_importance)     AS avgImportance
        FROM expert_opinion eo
        JOIN expert e ON e.id = eo.expert_id
        WHERE eo.canonical_id = :canonicalId
          AND eo.domain = :domain
          AND eo.direction = 'NEGATIVE'
    """ + PROFESSION_SCOPE)
    List<Object[]> aggregateNegativeByCanonicalAndDomain(@Param("canonicalId")    Long canonicalId,
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
