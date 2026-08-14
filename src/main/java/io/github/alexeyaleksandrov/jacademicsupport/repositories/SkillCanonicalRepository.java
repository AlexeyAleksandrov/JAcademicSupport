package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillCanonicalRepository extends JpaRepository<SkillCanonical, Long> {

    Optional<SkillCanonical> findByNormalizedName(String normalizedName);

    boolean existsByNormalizedName(String normalizedName);

    @Query("SELECT sc FROM SkillCanonical sc WHERE LOWER(sc.name) LIKE LOWER(CONCAT('%',:q,'%')) ORDER BY sc.name")
    List<SkillCanonical> searchByName(@Param("q") String q, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT sc FROM SkillCanonical sc WHERE " +
           "(:q = '' OR LOWER(sc.name) LIKE LOWER(CONCAT('%',:q,'%'))) " +
           "AND (:domain IS NULL OR sc.domain = :domain) " +
           "AND (:family IS NULL OR sc.techFamily = :family) " +
           "ORDER BY sc.name")
    List<SkillCanonical> searchByNameFiltered(
            @Param("q") String q,
            @Param("domain") String domain,
            @Param("family") String family,
            org.springframework.data.domain.Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT DISTINCT domain FROM skill_canonical WHERE domain IS NOT NULL ORDER BY domain")
    List<String> findDistinctDomains();

    @Query(nativeQuery = true, value = """
        SELECT DISTINCT tech_family FROM skill_canonical
        WHERE tech_family IS NOT NULL
          AND (:domain IS NULL OR domain = :domain)
        ORDER BY tech_family
        """)
    List<String> findDistinctFamilies(@Param("domain") String domain);

    @Query(nativeQuery = true, value = """
        SELECT sc.id,
               sc.name,
               sc.version_group,
               COUNT(DISTINCT vs.vacancy_entity_id) AS vacancyCount
        FROM skill_canonical sc
        LEFT JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
        LEFT JOIN work_skill ws             ON ws.id = wsc.work_skill_id
        LEFT JOIN vacancy_skills vs         ON vs.skills_id = ws.id
        WHERE sc.version_group = (
            SELECT COALESCE(s2.version_group, s2.name)
            FROM skill_canonical s2
            WHERE s2.id = :id
        )
        GROUP BY sc.id, sc.name, sc.version_group
        ORDER BY COUNT(DISTINCT vs.vacancy_entity_id) DESC, sc.name
        """)
    List<Object[]> findVersionSiblingsWithCounts(@Param("id") Long id);

    @Query(nativeQuery = true, value = """
        SELECT sc.domain,
               COUNT(DISTINCT vp.vacancy_id)                                   AS vacancyCount,
               COUNT(DISTINCT vp.vacancy_id)::double precision
                   / NULLIF((SELECT COUNT(DISTINCT vp2.vacancy_id)
                              FROM vacancy_profession vp2
                              JOIN profession p2 ON p2.id = vp2.profession_id
                              WHERE p2.code = :profCode), 0)                   AS weight
        FROM skill_canonical sc
        JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
        JOIN work_skill ws             ON ws.id = wsc.work_skill_id
        JOIN vacancy_skills vs         ON vs.skills_id = ws.id
        JOIN vacancy_profession vp     ON vp.vacancy_id = vs.vacancy_entity_id
        JOIN profession p              ON p.id = vp.profession_id AND p.code = :profCode
        WHERE sc.domain IS NOT NULL
        GROUP BY sc.domain
        ORDER BY COUNT(DISTINCT vp.vacancy_id) DESC
        """)
    List<Object[]> findDomainDistributionForProfession(@Param("profCode") String profCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT vp.vacancy_id)
        FROM skill_canonical sc
        JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
        JOIN work_skill ws             ON ws.id = wsc.work_skill_id
        JOIN vacancy_skills vs         ON vs.skills_id = ws.id
        JOIN vacancy_profession vp     ON vp.vacancy_id = vs.vacancy_entity_id
        JOIN profession p              ON p.id = vp.profession_id AND p.code = :profCode
        WHERE sc.domain = :domain
        """)
    long countVacanciesByProfessionAndDomain(@Param("profCode") String profCode,
                                             @Param("domain") String domain);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT vp.vacancy_id)
        FROM vacancy_profession vp
        JOIN profession p ON p.id = vp.profession_id AND p.code = :profCode
        """)
    long countTotalVacanciesForProfession(@Param("profCode") String profCode);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT vp.vacancy_id)
        FROM skill_canonical sc
        JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
        JOIN work_skill ws             ON ws.id = wsc.work_skill_id
        JOIN vacancy_skills vs         ON vs.skills_id = ws.id
        JOIN vacancy_profession vp     ON vp.vacancy_id = vs.vacancy_entity_id
        JOIN profession p              ON p.id = vp.profession_id AND p.code = :profCode
        WHERE sc.domain = :domain AND sc.tech_family = :techFamily
        """)
    long countVacanciesByTechFamilyAndDomainAndProfession(@Param("profCode")   String profCode,
                                                          @Param("domain")     String domain,
                                                          @Param("techFamily") String techFamily);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT vp.vacancy_id)
        FROM skill_canonical sc
        JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
        JOIN work_skill ws             ON ws.id = wsc.work_skill_id
        JOIN vacancy_skills vs         ON vs.skills_id = ws.id
        JOIN vacancy_profession vp     ON vp.vacancy_id = vs.vacancy_entity_id
        JOIN profession p              ON p.id = vp.profession_id AND p.code = :profCode
        WHERE sc.domain = :domain
        """)
    long countTotalVacanciesForProfessionAndDomain(@Param("profCode") String profCode,
                                                   @Param("domain")   String domain);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(DISTINCT vp.vacancy_id)
        FROM work_skill_canonical wsc
        JOIN work_skill ws         ON ws.id = wsc.work_skill_id
        JOIN vacancy_skills vs     ON vs.skills_id = ws.id
        JOIN vacancy_profession vp ON vp.vacancy_id = vs.vacancy_entity_id
        JOIN profession p          ON p.id = vp.profession_id AND p.code = :profCode
        WHERE wsc.canonical_id = :canonicalId
        """)
    long countVacanciesByCanonicalAndProfession(@Param("profCode")   String profCode,
                                               @Param("canonicalId") Long canonicalId);

    @Query(nativeQuery = true, value = """
        SELECT sc.id, sc.name, sc.domain,
               COUNT(DISTINCT vp.vacancy_id)                                   AS absoluteCount,
               COUNT(DISTINCT vp.vacancy_id)::double precision
                   / NULLIF((SELECT COUNT(DISTINCT vp2.vacancy_id)
                              FROM vacancy_profession vp2
                              JOIN profession p2 ON p2.id = vp2.profession_id
                              WHERE p2.code = :profCode), 0)                   AS relativeFrequency,
               sc.tech_type, sc.version_group
        FROM skill_canonical sc
        JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
        JOIN work_skill ws             ON ws.id = wsc.work_skill_id
        JOIN vacancy_skills vs         ON vs.skills_id = ws.id
        JOIN vacancy_profession vp     ON vp.vacancy_id = vs.vacancy_entity_id
        JOIN profession p              ON p.id = vp.profession_id AND p.code = :profCode
        WHERE sc.domain = :domain
        GROUP BY sc.id, sc.name, sc.domain, sc.tech_type, sc.version_group
        ORDER BY COUNT(DISTINCT vp.vacancy_id) DESC
        """)
    List<Object[]> findSkillsByDomainAndProfession(@Param("profCode") String profCode,
                                                    @Param("domain") String domain);

    @Query(nativeQuery = true, value = """
        SELECT sc.tech_family,
               COUNT(DISTINCT vp.vacancy_id)                                   AS vacancyCount,
               COUNT(DISTINCT vp.vacancy_id)::double precision
                   / NULLIF((SELECT COUNT(DISTINCT vp2.vacancy_id)
                              FROM vacancy_profession vp2
                              JOIN profession p2 ON p2.id = vp2.profession_id
                              WHERE p2.code = :profCode), 0)                   AS weight
        FROM skill_canonical sc
        JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
        JOIN work_skill ws             ON ws.id = wsc.work_skill_id
        JOIN vacancy_skills vs         ON vs.skills_id = ws.id
        JOIN vacancy_profession vp     ON vp.vacancy_id = vs.vacancy_entity_id
        JOIN profession p              ON p.id = vp.profession_id AND p.code = :profCode
        WHERE sc.domain = :domain AND sc.tech_family IS NOT NULL
        GROUP BY sc.tech_family
        ORDER BY COUNT(DISTINCT vp.vacancy_id) DESC
        """)
    List<Object[]> findFamilyDistributionByDomainAndProfession(@Param("profCode") String profCode,
                                                               @Param("domain")   String domain);

    @Query(nativeQuery = true, value = """
        SELECT sc.id, sc.name, sc.domain,
               COUNT(DISTINCT vp.vacancy_id)                                   AS absoluteCount,
               COUNT(DISTINCT vp.vacancy_id)::double precision
                   / NULLIF((SELECT COUNT(DISTINCT vp2.vacancy_id)
                              FROM vacancy_profession vp2
                              JOIN profession p2 ON p2.id = vp2.profession_id
                              WHERE p2.code = :profCode), 0)                   AS relativeFrequency,
               sc.tech_type, sc.version_group
        FROM skill_canonical sc
        JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
        JOIN work_skill ws             ON ws.id = wsc.work_skill_id
        JOIN vacancy_skills vs         ON vs.skills_id = ws.id
        JOIN vacancy_profession vp     ON vp.vacancy_id = vs.vacancy_entity_id
        JOIN profession p              ON p.id = vp.profession_id AND p.code = :profCode
        WHERE sc.domain = :domain AND sc.tech_family = :techFamily
        GROUP BY sc.id, sc.name, sc.domain, sc.tech_type, sc.version_group
        ORDER BY COUNT(DISTINCT vp.vacancy_id) DESC
        """)
    List<Object[]> findSkillsByDomainAndFamilyAndProfession(@Param("profCode")   String profCode,
                                                            @Param("domain")     String domain,
                                                            @Param("techFamily") String techFamily);

    @Query(nativeQuery = true, value = """
        SELECT sc.id            AS canonicalId,
               sc.name          AS description,
               sc.domain,
               COUNT(DISTINCT vp.vacancy_id)                          AS absoluteCount,
               COUNT(DISTINCT vp.vacancy_id)::double precision
                   / NULLIF((SELECT COUNT(DISTINCT vp2.vacancy_id)
                              FROM vacancy_profession vp2
                              JOIN profession p2 ON p2.id = vp2.profession_id
                              WHERE p2.code = :profCode), 0)           AS relativeFrequency,
               sc.tech_type, sc.version_group
        FROM skill_canonical sc
        JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
        JOIN work_skill ws             ON ws.id = wsc.work_skill_id
                                      AND ws.skills_group_id = :clusterId
        JOIN vacancy_skills vs         ON vs.skills_id = ws.id
        JOIN vacancy_profession vp     ON vp.vacancy_id = vs.vacancy_entity_id
        JOIN profession p              ON p.id = vp.profession_id
                                      AND p.code = :profCode
        GROUP BY sc.id, sc.name, sc.domain, sc.tech_type, sc.version_group
        ORDER BY COUNT(DISTINCT vp.vacancy_id) DESC
        """)
    List<Object[]> findStrictClusterSkills(@Param("profCode") String profCode,
                                           @Param("clusterId") Long clusterId);

    @Query(nativeQuery = true, value = """
        SELECT tech_family, domain
        FROM (
            SELECT tech_family, domain,
                   ROW_NUMBER() OVER (PARTITION BY tech_family ORDER BY COUNT(*) DESC) AS rn
            FROM skill_canonical
            WHERE tech_family IN (:families) AND domain IS NOT NULL
            GROUP BY tech_family, domain
        ) sub
        WHERE rn = 1
        """)
    List<Object[]> findDomainsByTechFamilies(@Param("families") java.util.Collection<String> families);
}
