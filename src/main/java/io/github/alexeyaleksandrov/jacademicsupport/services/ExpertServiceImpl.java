package io.github.alexeyaleksandrov.jacademicsupport.services;

import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertEntity;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ExpertsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExpertServiceImpl implements ExpertService {
    private final ExpertsRepository expertsRepository;

    public ExpertServiceImpl(ExpertsRepository expertsRepository) {
        this.expertsRepository = expertsRepository;
    }

    @Override
    public List<ExpertEntity> findAll() {
        return expertsRepository.findAll();
    }

    @Override
    public ExpertEntity save(ExpertEntity expertEntity) {
        return expertsRepository.save(expertEntity);
    }

    @Override
    public ExpertEntity findById(Long id) {
        Optional<ExpertEntity> expert = expertsRepository.findById(id);
        return expert.orElse(null);
    }

    @Override
    public void delete(ExpertEntity expertEntity) {
        expertsRepository.delete(expertEntity);
    }

    @Override
    public void deleteById(Long id) {
        expertsRepository.deleteById(id);
    }
}
