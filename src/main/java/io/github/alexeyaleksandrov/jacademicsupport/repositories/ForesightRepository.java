package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.ForesightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ForesightRepository extends JpaRepository<ForesightEntity, Long> {

    // ─── DST aggregation queries (L0/L1/L2) ───────────────────────────────────

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS relevantCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.domain = :domain
          AND f.direction = 'POSITIVE'
          AND (:professionCode IS NULL OR f.profession_code = :professionCode OR COALESCE(f.profession_code,'') = '')
    """)
    List<Object[]> aggregateByDomain(@Param("domain") String domain,
                                    @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS relevantCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.domain = :domain
          AND f.tech_family = :techFamily
          AND f.direction = 'POSITIVE'
          AND (:professionCode IS NULL OR f.profession_code = :professionCode OR COALESCE(f.profession_code,'') = '')
    """)
    List<Object[]> aggregateByDomainAndFamily(@Param("domain") String domain,
                                             @Param("techFamily") String techFamily,
                                             @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS relevantCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.canonical_id = :canonicalId
          AND f.direction = 'POSITIVE'
          AND (:professionCode IS NULL OR f.profession_code = :professionCode OR COALESCE(f.profession_code,'') = '')
    """)
    List<Object[]> aggregateByCanonical(@Param("canonicalId") Long canonicalId,
                                       @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS relevantCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.canonical_id = :canonicalId
          AND f.domain = :domain
          AND f.direction = 'POSITIVE'
          AND (:professionCode IS NULL OR f.profession_code = :professionCode OR COALESCE(f.profession_code,'') = '')
    """)
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
          AND (:professionCode IS NULL OR f.profession_code = :professionCode OR COALESCE(f.profession_code,'') = '')
    """)
    List<Object[]> aggregateNegativeByDomain(@Param("domain") String domain,
                                             @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS negativeCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.domain = :domain
          AND f.tech_family = :techFamily
          AND f.direction = 'NEGATIVE'
          AND (:professionCode IS NULL OR f.profession_code = :professionCode OR COALESCE(f.profession_code,'') = '')
    """)
    List<Object[]> aggregateNegativeByDomainAndFamily(@Param("domain") String domain,
                                                      @Param("techFamily") String techFamily,
                                                      @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS negativeCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.canonical_id = :canonicalId
          AND f.direction = 'NEGATIVE'
          AND (:professionCode IS NULL OR f.profession_code = :professionCode OR COALESCE(f.profession_code,'') = '')
    """)
    List<Object[]> aggregateNegativeByCanonical(@Param("canonicalId") Long canonicalId,
                                                @Param("professionCode") String professionCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT f.source_url) AS negativeCount,
               AVG(f.confidence)            AS avgConfidence
        FROM foresight f
        WHERE f.canonical_id = :canonicalId
          AND f.domain = :domain
          AND f.direction = 'NEGATIVE'
          AND (:professionCode IS NULL OR f.profession_code = :professionCode OR COALESCE(f.profession_code,'') = '')
    """)
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
