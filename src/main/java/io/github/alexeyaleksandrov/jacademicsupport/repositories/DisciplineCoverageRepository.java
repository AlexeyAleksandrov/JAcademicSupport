package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.DisciplineCoverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DisciplineCoverageRepository extends JpaRepository<DisciplineCoverage, Long> {

    List<DisciplineCoverage> findByDisciplineId(Long disciplineId);

    List<DisciplineCoverage> findByDisciplineIdIn(List<Long> disciplineIds);

    @Query("""
        SELECT COALESCE(SUM(dc.hours), 0)
        FROM DisciplineCoverage dc
        WHERE dc.disciplineId IN :disciplineIds
          AND dc.domain = :domain
        """)
    int sumHoursByDisciplineIdsAndDomain(@Param("disciplineIds") List<Long> disciplineIds,
                                         @Param("domain") String domain);

    @Query("""
        SELECT COALESCE(SUM(dc.hours), 0)
        FROM DisciplineCoverage dc
        WHERE dc.disciplineId IN :disciplineIds
          AND dc.techFamily = :techFamily
        """)
    int sumHoursByDisciplineIdsAndFamily(@Param("disciplineIds") List<Long> disciplineIds,
                                          @Param("techFamily") String techFamily);

    @Query("""
        SELECT COALESCE(SUM(dc.hours), 0)
        FROM DisciplineCoverage dc
        WHERE dc.disciplineId IN :disciplineIds
          AND dc.canonicalId = :canonicalId
        """)
    int sumHoursByDisciplineIdsAndCanonical(@Param("disciplineIds") List<Long> disciplineIds,
                                             @Param("canonicalId") Long canonicalId);

    @Query("""
        SELECT COALESCE(SUM(dc.hours), 0)
        FROM DisciplineCoverage dc
        WHERE dc.disciplineId IN :disciplineIds
        """)
    int sumTotalHoursByDisciplineIds(@Param("disciplineIds") List<Long> disciplineIds);

    @Query("""
        SELECT DISTINCT dc.disciplineId
        FROM DisciplineCoverage dc
        WHERE dc.disciplineId IN :disciplineIds
          AND dc.domain IS NOT NULL
          AND dc.techFamily IS NULL
          AND dc.canonicalId IS NULL
        """)
    Set<Long> findDisciplineIdsWithExplicitDomainBlocks(@Param("disciplineIds") List<Long> disciplineIds);

    @Query(nativeQuery = true, value = """
        SELECT COALESCE(SUM(dc.hours), 0)
        FROM discipline_coverage dc
        JOIN skill_canonical sc ON sc.id = dc.canonical_id
        WHERE dc.discipline_id IN :disciplineIds
          AND sc.domain = :domain
        """)
    int sumSkillInferredDomainHours(@Param("disciplineIds") List<Long> disciplineIds,
                                    @Param("domain") String domain);

    @Query(nativeQuery = true, value = """
        SELECT d.id, d.name, d.semester, COALESCE(SUM(dc.hours), 0) AS hours
        FROM discipline d
        JOIN discipline_coverage dc ON dc.discipline_id = d.id
        WHERE d.id IN :disciplineIds
          AND (dc.domain = :domain
               OR (dc.canonical_id IS NOT NULL AND dc.canonical_id IN (
                   SELECT sc.id FROM skill_canonical sc WHERE sc.domain = :domain
               ))
               OR (dc.domain IS NULL AND dc.canonical_id IS NULL AND dc.tech_family IN (
                   SELECT DISTINCT sc2.tech_family FROM skill_canonical sc2
                   WHERE sc2.domain = :domain AND sc2.tech_family IS NOT NULL
               )))
        GROUP BY d.id, d.name, d.semester
        HAVING SUM(dc.hours) > 0
        ORDER BY SUM(dc.hours) DESC
        """)
    List<Object[]> findDomainBreakdownByDisciplines(@Param("disciplineIds") List<Long> disciplineIds,
                                                     @Param("domain") String domain);
}
