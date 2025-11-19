package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertOpinionSkillsGroupEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpertOpinionSkillsGroupRepository extends JpaRepository<ExpertOpinionSkillsGroupEntity, Long> {
    
    // Найти все оценки конкретного эксперта
    List<ExpertOpinionSkillsGroupEntity> findByExpert(ExpertEntity expert);
    List<ExpertOpinionSkillsGroupEntity> findByExpertId(Long expertId);
    
    // Найти все оценки для конкретного индикатора компетенции
    List<ExpertOpinionSkillsGroupEntity> findByCompetencyAchievementIndicator(CompetencyAchievementIndicator indicator);
    List<ExpertOpinionSkillsGroupEntity> findByCompetencyAchievementIndicatorId(Long indicatorId);
    
    // Найти все оценки для конкретной группы технологий
    List<ExpertOpinionSkillsGroupEntity> findBySkillsGroup(SkillsGroup skillsGroup);
    List<ExpertOpinionSkillsGroupEntity> findBySkillsGroupId(Long skillsGroupId);
    
    // Найти оценку конкретного эксперта для конкретной группы и индикатора
    ExpertOpinionSkillsGroupEntity findByExpertAndCompetencyAchievementIndicatorAndSkillsGroup(
            ExpertEntity expert, 
            CompetencyAchievementIndicator indicator, 
            SkillsGroup skillsGroup
    );
    
    ExpertOpinionSkillsGroupEntity findByExpertIdAndCompetencyAchievementIndicatorIdAndSkillsGroupId(
            Long expertId, 
            Long indicatorId, 
            Long skillsGroupId
    );
    
    // Проверить существование оценки
    boolean existsByExpertIdAndCompetencyAchievementIndicatorIdAndSkillsGroupId(
            Long expertId, 
            Long indicatorId, 
            Long skillsGroupId
    );
    
    // Найти оценки с важностью выше заданного порога
    List<ExpertOpinionSkillsGroupEntity> findByGroupImportanceGreaterThanEqual(double minImportance);
    
    // Найти оценки эксперта для конкретного индикатора
    List<ExpertOpinionSkillsGroupEntity> findByExpertIdAndCompetencyAchievementIndicatorId(
            Long expertId, 
            Long indicatorId
    );
}
