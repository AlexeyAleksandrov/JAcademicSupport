package io.github.alexeyaleksandrov.jacademicsupport.services;

import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertEntity;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ExpertRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExpertServiceImpl implements ExpertService {
    private final ExpertRepository expertRepository;

    public ExpertServiceImpl(ExpertRepository expertRepository) {
        this.expertRepository = expertRepository;
    }

    @Override
    public List<ExpertEntity> findAll() {
        return expertRepository.findAll();
    }

    @Override
    public ExpertEntity save(ExpertEntity expertEntity) {
        return expertRepository.save(expertEntity);
    }

    @Override
    public ExpertEntity findById(Long id) {
        Optional<ExpertEntity> expert = expertRepository.findById(id);
        return expert.orElse(null);
    }

    @Override
    public void delete(ExpertEntity expertEntity) {
        expertRepository.delete(expertEntity);
    }

    @Override
    public void deleteById(Long id) {
        expertRepository.deleteById(id);
    }
}
