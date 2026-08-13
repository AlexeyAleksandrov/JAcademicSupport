package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillVersionRepository extends JpaRepository<SkillVersion, Long> {

    List<SkillVersion> findByCanonical(SkillCanonical canonical);

    boolean existsByCanonicalAndRawString(SkillCanonical canonical, String rawString);

    @Query("SELECT sv FROM SkillVersion sv WHERE sv.canonical.id = :canonicalId ORDER BY sv.id")
    List<SkillVersion> findByCanonicalId(@Param("canonicalId") Long canonicalId);

    /**
     * Returns versions for a canonical skill with vacancy counts.
     * Joins through work_skill_canonical (all work_skills for this canonical),
     * then filters by version number present in the description using a word-boundary regex.
     * This handles typos and embedded versions (e.g. "PostrgeSQL 16", "Postgre 14").
     */
    @Query(nativeQuery = true, value = """
        SELECT sv.id,
               sv.raw_string,
               sv.version_min,
               sv.version_max,
               sv.is_plus,
               COUNT(DISTINCT vs.vacancy_entity_id) AS vacancy_count
        FROM skill_version sv
        LEFT JOIN work_skill_canonical wsc ON wsc.canonical_id = sv.canonical_id
        LEFT JOIN work_skill ws
               ON ws.id = wsc.work_skill_id
              AND ws.description ~ ('(^|[^0-9])' || sv.version_min || '([^0-9]|$)')
        LEFT JOIN vacancy_skills vs ON vs.skills_id = ws.id
        WHERE sv.canonical_id = :canonicalId
        GROUP BY sv.id, sv.raw_string, sv.version_min, sv.version_max, sv.is_plus
        ORDER BY vacancy_count DESC, sv.version_min DESC NULLS LAST
        """)
    List<Object[]> findVersionsWithCounts(@Param("canonicalId") Long canonicalId);
}
