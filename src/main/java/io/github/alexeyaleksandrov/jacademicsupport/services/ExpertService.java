package io.github.alexeyaleksandrov.jacademicsupport.services;

import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertEntity;

import java.util.List;

public interface ExpertService {
    List<ExpertEntity> findAll();
    ExpertEntity save(ExpertEntity expertEntity);
    ExpertEntity findById(Long id);
    void delete(ExpertEntity expertEntity);
    void deleteById(Long id);
}
