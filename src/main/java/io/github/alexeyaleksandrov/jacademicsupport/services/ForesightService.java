package io.github.alexeyaleksandrov.jacademicsupport.services;

import io.github.alexeyaleksandrov.jacademicsupport.models.ForesightEntity;

import java.util.List;

public interface ForesightService {
    List<ForesightEntity> findAll();
    ForesightEntity findById(Long id);
    ForesightEntity save(ForesightEntity foresightEntity);
    void deleteById(Long id);
}
