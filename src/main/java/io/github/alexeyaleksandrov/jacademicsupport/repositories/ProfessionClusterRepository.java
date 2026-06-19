package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.Profession;
import io.github.alexeyaleksandrov.jacademicsupport.models.ProfessionCluster;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfessionClusterRepository extends JpaRepository<ProfessionCluster, ProfessionCluster.ProfessionClusterId> {

    List<ProfessionCluster> findByProfession(Profession profession);

    List<ProfessionCluster> findByCluster(SkillsGroup cluster);

    @Query("SELECT pc FROM ProfessionCluster pc WHERE pc.profession.code = :profCode ORDER BY pc.weight DESC")
    List<ProfessionCluster> findByProfessionCodeOrderByWeightDesc(@Param("profCode") String profCode);
}
