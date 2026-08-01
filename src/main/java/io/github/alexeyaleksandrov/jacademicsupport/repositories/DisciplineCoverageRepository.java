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
}
