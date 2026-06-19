package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyClusterScore;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VacancyClusterScoreRepository extends JpaRepository<VacancyClusterScore, VacancyClusterScore.VacancyClusterScoreId> {

    List<VacancyClusterScore> findByVacancy(VacancyEntity vacancy);

    List<VacancyClusterScore> findByCluster(SkillsGroup cluster);

    @Query("SELECT vcs FROM VacancyClusterScore vcs WHERE vcs.cluster = :cluster AND vcs.score >= :minScore ORDER BY vcs.score DESC")
    List<VacancyClusterScore> findByClusterWithMinScore(@Param("cluster") SkillsGroup cluster,
                                                        @Param("minScore") BigDecimal minScore);

    @Query("""
        SELECT vcs FROM VacancyClusterScore vcs
        JOIN VacancyProfession vp ON vp.vacancy = vcs.vacancy
        WHERE vp.profession.code = :profCode
          AND vcs.cluster.id = :clusterId
          AND vcs.score >= :minScore
        ORDER BY vcs.score DESC
        """)
    List<VacancyClusterScore> findByProfessionAndCluster(@Param("profCode") String profCode,
                                                          @Param("clusterId") Long clusterId,
                                                          @Param("minScore") BigDecimal minScore);

    @Query("""
        SELECT AVG(vcs.score) FROM VacancyClusterScore vcs
        JOIN VacancyProfession vp ON vp.vacancy = vcs.vacancy
        WHERE vp.profession.code = :profCode
          AND vcs.cluster.id = :clusterId
        """)
    Double avgScoreByProfessionAndCluster(@Param("profCode") String profCode,
                                          @Param("clusterId") Long clusterId);
}
