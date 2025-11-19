package io.github.alexeyaleksandrov.jacademicsupport.services.expert;

import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertOpinionSkillsGroupEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ExpertOpinionSkillsGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpertOpinionSkillsGroupService {

    private final ExpertOpinionSkillsGroupRepository repository;

    public List<ExpertOpinionSkillsGroupEntity> findAll() {
        return repository.findAll();
    }

    public ExpertOpinionSkillsGroupEntity findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public ExpertOpinionSkillsGroupEntity save(ExpertOpinionSkillsGroupEntity entity) {
        return repository.save(entity);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<ExpertOpinionSkillsGroupEntity> findByExpertId(Long expertId) {
        return repository.findByExpertId(expertId);
    }

    public List<ExpertOpinionSkillsGroupEntity> findByCompetencyAchievementIndicatorId(Long indicatorId) {
        return repository.findByCompetencyAchievementIndicatorId(indicatorId);
    }

    public List<ExpertOpinionSkillsGroupEntity> findBySkillsGroupId(Long skillsGroupId) {
        return repository.findBySkillsGroupId(skillsGroupId);
    }

    public List<ExpertOpinionSkillsGroupEntity> findByExpertIdAndCompetencyAchievementIndicatorId(
            Long expertId, Long indicatorId) {
        return repository.findByExpertIdAndCompetencyAchievementIndicatorId(expertId, indicatorId);
    }

    public ExpertOpinionSkillsGroupEntity findByExpertIdAndCompetencyAchievementIndicatorIdAndSkillsGroupId(
            Long expertId, Long indicatorId, Long skillsGroupId) {
        return repository.findByExpertIdAndCompetencyAchievementIndicatorIdAndSkillsGroupId(
                expertId, indicatorId, skillsGroupId);
    }

    public boolean existsByExpertIdAndCompetencyAchievementIndicatorIdAndSkillsGroupId(
            Long expertId, Long indicatorId, Long skillsGroupId) {
        return repository.existsByExpertIdAndCompetencyAchievementIndicatorIdAndSkillsGroupId(
                expertId, indicatorId, skillsGroupId);
    }

    public List<ExpertOpinionSkillsGroupEntity> findByGroupImportanceGreaterThanEqual(double minImportance) {
        return repository.findByGroupImportanceGreaterThanEqual(minImportance);
    }

    public List<ExpertOpinionSkillsGroupEntity> findByExpert(ExpertEntity expert) {
        return repository.findByExpert(expert);
    }

    public List<ExpertOpinionSkillsGroupEntity> findByCompetencyAchievementIndicator(
            CompetencyAchievementIndicator indicator) {
        return repository.findByCompetencyAchievementIndicator(indicator);
    }

    public List<ExpertOpinionSkillsGroupEntity> findBySkillsGroup(SkillsGroup skillsGroup) {
        return repository.findBySkillsGroup(skillsGroup);
    }

    public ExpertOpinionSkillsGroupEntity findByExpertAndCompetencyAchievementIndicatorAndSkillsGroup(
            ExpertEntity expert, CompetencyAchievementIndicator indicator, SkillsGroup skillsGroup) {
        return repository.findByExpertAndCompetencyAchievementIndicatorAndSkillsGroup(
                expert, indicator, skillsGroup);
    }

    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
