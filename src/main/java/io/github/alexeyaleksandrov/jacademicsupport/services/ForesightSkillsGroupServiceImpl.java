package io.github.alexeyaleksandrov.jacademicsupport.services;

import io.github.alexeyaleksandrov.jacademicsupport.models.ForesightSkillsGroupEntity;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ForesightSkillsGroupRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ForesightSkillsGroupServiceImpl implements ForesightSkillsGroupService {

    private final ForesightSkillsGroupRepository repository;

    @Override
    public List<ForesightSkillsGroupEntity> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<ForesightSkillsGroupEntity> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public ForesightSkillsGroupEntity save(ForesightSkillsGroupEntity entity) {
        return repository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsBySkillsGroupIdAndSourceUrl(Long skillsGroupId, String sourceUrl) {
        return repository.existsBySkillsGroupIdAndSourceUrl(skillsGroupId, sourceUrl);
    }

    @Override
    public List<ForesightSkillsGroupEntity> findBySkillsGroupId(Long skillsGroupId) {
        return repository.findBySkillsGroupId(skillsGroupId);
    }

    @Override
    public List<ForesightSkillsGroupEntity> findBySourceName(String sourceName) {
        return repository.findBySourceName(sourceName);
    }
}
