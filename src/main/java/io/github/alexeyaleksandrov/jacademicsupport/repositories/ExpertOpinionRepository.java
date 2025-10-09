package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertOpinionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpertOpinionRepository extends JpaRepository<ExpertOpinionEntity, Long> {
    
    // Найти все мнения экспертов по конкретному навыку
    List<ExpertOpinionEntity> findByWorkSkillId(Long workSkillId);
    
    // Получить количество уникальных экспертов, которые высказали мнение по данному навыку
    @Query("SELECT COUNT(DISTINCT eo.expert.id) FROM ExpertOpinionEntity eo WHERE eo.workSkill.id = :workSkillId")
    Long countDistinctExpertsByWorkSkillId(@Param("workSkillId") Long workSkillId);
    
    // Получить общее количество уникальных экспертов в системе
    @Query("SELECT COUNT(DISTINCT eo.expert.id) FROM ExpertOpinionEntity eo")
    Long countDistinctExperts();
}
