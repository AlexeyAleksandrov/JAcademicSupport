package io.github.alexeyaleksandrov.jacademicsupport.services;

import io.github.alexeyaleksandrov.jacademicsupport.models.ForesightSkillsGroupEntity;

import java.util.List;
import java.util.Optional;

public interface ForesightSkillsGroupService {
    List<ForesightSkillsGroupEntity> findAll();
    Optional<ForesightSkillsGroupEntity> findById(Long id);
    ForesightSkillsGroupEntity save(ForesightSkillsGroupEntity entity);
    void deleteById(Long id);
    boolean existsBySkillsGroupIdAndSourceUrl(Long skillsGroupId, String sourceUrl);
    List<ForesightSkillsGroupEntity> findBySkillsGroupId(Long skillsGroupId);
    List<ForesightSkillsGroupEntity> findBySourceName(String sourceName);
}
