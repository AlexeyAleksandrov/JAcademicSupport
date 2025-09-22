package io.github.alexeyaleksandrov.jacademicsupport.services;

import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertOpinionEntity;

import java.util.List;

public interface ExpertOpinionService {
    List<ExpertOpinionEntity> findAll();
    ExpertOpinionEntity save(ExpertOpinionEntity expertOpinionEntity);
    ExpertOpinionEntity findById(Long id);
    void delete(ExpertOpinionEntity expertOpinionEntity);
    void deleteById(Long id);
}
