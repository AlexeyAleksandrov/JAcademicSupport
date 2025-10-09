package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.ForesightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ForesightRepository extends JpaRepository<ForesightEntity, Long> {
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
