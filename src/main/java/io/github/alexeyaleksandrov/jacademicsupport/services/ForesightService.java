package io.github.alexeyaleksandrov.jacademicsupport.services;

import io.github.alexeyaleksandrov.jacademicsupport.models.ForesightEntity;

import java.util.List;
import java.util.Optional;

public interface ForesightService {
    List<ForesightEntity> findAll();
    Optional<ForesightEntity> findById(Long id);
    ForesightEntity save(ForesightEntity foresightEntity);
    void deleteById(Long id);
    boolean existsByWorkSkillIdAndSourceUrl(Long workSkillId, String sourceUrl);
}
