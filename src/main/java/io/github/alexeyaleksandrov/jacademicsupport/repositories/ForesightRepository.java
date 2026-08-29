package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.ForesightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ForesightRepository extends JpaRepository<ForesightEntity, Long> {

    /** See ExpertOpinionRepository.PROFESSION_SCOPE for the inference policy. */
    String PROFESSION_SCOPE = """
          AND (
                :professionCode IS NULL
                OR f.profession_code = :professionCode
                OR (
                    COALESCE(f.profession_code, '') = ''
                    AND (
                        (f.canonical_id IS NOT NULL AND EXISTS (
                            SELECT 1
                            FROM work_skill_canonical rel_wsc
                            JOIN vacancy_skills rel_vs     ON rel_vs.skills_id = rel_wsc.work_skill_id
                            JOIN vacancy_profession rel_vp ON rel_vp.vacancy_id = rel_vs.vacancy_entity_id
                            JOIN profession rel_p          ON rel_p.id = rel_vp.profession_id
                            WHERE rel_wsc.canonical_id = f.canonical_id AND rel_p.code = :professionCode
                        ))
                        OR (f.canonical_id IS NULL AND f.work_skill_id IS NOT NULL AND EXISTS (
                            SELECT 1
                            FROM vacancy_skills rel_vs
                            JOIN vacancy_profession rel_vp ON rel_vp.vacancy_id = rel_vs.vacancy_entity_id
                            JOIN profession rel_p          ON rel_p.id = rel_vp.profession_id
                            WHERE rel_vs.skills_id = f.work_skill_id AND rel_p.code = :professionCode
                        ))
                        OR (f.canonical_id IS NULL AND f.work_skill_id IS NULL
                            AND f.tech_family IS NOT NULL AND EXISTS (
                            SELECT 1
                            FROM skill_canonical rel_sc
                            JOIN work_skill_canonical rel_wsc ON rel_wsc.canonical_id = rel_sc.id
                            JOIN vacancy_skills rel_vs         ON rel_vs.skills_id = rel_wsc.work_skill_id
                            JOIN vacancy_profession rel_vp     ON rel_vp.vacancy_id = rel_vs.vacancy_entity_id
                            JOIN profession rel_p              ON rel_p.id = rel_vp.profession_id
                            WHERE rel_sc.domain = f.domain AND rel_sc.tech_family = f.tech_family
                              AND rel_p.code = :professionCode
                        ))
                        OR (f.canonical_id IS NULL AND f.work_skill_id IS NULL
                            AND f.tech_family IS NULL AND EXISTS (
                            SELECT 1
                            FROM skill_canonical rel_sc
                            JOIN work_skill_canonical rel_wsc ON rel_wsc.canonical_id = rel_sc.id
                            JOIN vacancy_skills rel_vs         ON rel_vs.skills_id = rel_wsc.work_skill_id
                            JOIN vacancy_profession rel_vp     ON rel_vp.vacancy_id = rel_vs.vacancy_entity_id
                            JOIN profession rel_p              ON rel_p.id = rel_vp.profession_id
                            WHERE rel_sc.domain = f.domain AND rel_p.code = :professionCode
                        ))
                    )
                )
          )
        """;

    // ─── DST aggregation queries (L0/L1/L2) ───────────────────────────────────

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS relevantCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.domain = :domain
          AND f.direction = 'POSITIVE'
    """ + PROFESSION_SCOPE)
    List<Object[]> aggregateByDomain(@Param("domain") String domain,
                                    @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS relevantCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.domain = :domain
          AND f.tech_family = :techFamily
          AND f.direction = 'POSITIVE'
    """ + PROFESSION_SCOPE)
    List<Object[]> aggregateByDomainAndFamily(@Param("domain") String domain,
                                             @Param("techFamily") String techFamily,
                                             @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS relevantCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.canonical_id = :canonicalId
          AND f.direction = 'POSITIVE'
    """ + PROFESSION_SCOPE)
    List<Object[]> aggregateByCanonical(@Param("canonicalId") Long canonicalId,
                                       @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS relevantCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.canonical_id = :canonicalId
          AND f.domain = :domain
          AND f.direction = 'POSITIVE'
    """ + PROFESSION_SCOPE)
    List<Object[]> aggregateByCanonicalAndDomain(@Param("canonicalId")    Long canonicalId,
                                                 @Param("domain")         String domain,
                                                 @Param("professionCode") String professionCode);

    // ─── Negative evidence aggregation (m(F) support) ─────────────────────────
    // Mirrors of the POSITIVE queries above; feed the m(F) mass so that the
    // conflict coefficient K can become non-zero and Yager's rule reachable.

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS negativeCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.domain = :domain
          AND f.direction = 'NEGATIVE'
    """ + PROFESSION_SCOPE)
    List<Object[]> aggregateNegativeByDomain(@Param("domain") String domain,
                                             @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS negativeCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.domain = :domain
          AND f.tech_family = :techFamily
          AND f.direction = 'NEGATIVE'
    """ + PROFESSION_SCOPE)
    List<Object[]> aggregateNegativeByDomainAndFamily(@Param("domain") String domain,
                                                      @Param("techFamily") String techFamily,
                                                      @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS negativeCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.canonical_id = :canonicalId
          AND f.direction = 'NEGATIVE'
    """ + PROFESSION_SCOPE)
    List<Object[]> aggregateNegativeByCanonical(@Param("canonicalId") Long canonicalId,
                                                @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS negativeCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.canonical_id = :canonicalId
          AND f.domain = :domain
          AND f.direction = 'NEGATIVE'
    """ + PROFESSION_SCOPE)
    List<Object[]> aggregateNegativeByCanonicalAndDomain(@Param("canonicalId")    Long canonicalId,
                                                         @Param("domain")         String domain,
                                                         @Param("professionCode") String professionCode);

    // ─── Legacy queries (backward compat) ────────────────────────────────────

    boolean existsByWorkSkillIdAndSourceUrl(Long workSkillId, String sourceUrl);
    
    // Найти все прогнозы по конкретному навыку
    List<ForesightEntity> findByWorkSkillId(Long workSkillId);
    
    // Получить количество уникальных источников (sourceUrl), которые рекомендуют данный навык
    @Query("SELECT COUNT(DISTINCT f.sourceUrl) FROM ForesightEntity f WHERE f.workSkill.id = :workSkillId")
    Long countDistinctSourceUrlsByWorkSkillId(@Param("workSkillId") Long workSkillId);
    
    // Получить общее количество уникальных источников в системе
    @Query("SELECT COUNT(DISTINCT f.sourceUrl) FROM ForesightEntity f")
    Long countDistinctSourceUrls();
}
