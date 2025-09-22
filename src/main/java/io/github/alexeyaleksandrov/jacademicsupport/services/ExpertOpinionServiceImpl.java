package io.github.alexeyaleksandrov.jacademicsupport.services;

import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertOpinionEntity;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ExpertOpinionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExpertOpinionServiceImpl implements ExpertOpinionService {
    private final ExpertOpinionRepository expertOpinionRepository;

    public ExpertOpinionServiceImpl(ExpertOpinionRepository expertOpinionRepository) {
        this.expertOpinionRepository = expertOpinionRepository;
    }

    @Override
    public List<ExpertOpinionEntity> findAll() {
        return expertOpinionRepository.findAll();
    }

    @Override
    public ExpertOpinionEntity save(ExpertOpinionEntity expertOpinionEntity) {
        return expertOpinionRepository.save(expertOpinionEntity);
    }

    @Override
    public ExpertOpinionEntity findById(Long id) {
        Optional<ExpertOpinionEntity> expertOpinion = expertOpinionRepository.findById(id);
        return expertOpinion.orElse(null);
    }

    @Override
    public void delete(ExpertOpinionEntity expertOpinionEntity) {
        expertOpinionRepository.delete(expertOpinionEntity);
    }

    @Override
    public void deleteById(Long id) {
        expertOpinionRepository.deleteById(id);
    }
}
