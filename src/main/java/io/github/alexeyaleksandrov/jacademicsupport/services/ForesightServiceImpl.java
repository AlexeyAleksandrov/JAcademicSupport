package io.github.alexeyaleksandrov.jacademicsupport.services;

import io.github.alexeyaleksandrov.jacademicsupport.models.ForesightEntity;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ForesightRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ForesightServiceImpl implements ForesightService {

    private final ForesightRepository foresightRepository;

    @Override
    public List<ForesightEntity> findAll() {
        return foresightRepository.findAll();
    }

    @Override
    public Optional<ForesightEntity> findById(Long id) {
        return foresightRepository.findById(id);
    }

    @Override
    public ForesightEntity save(ForesightEntity foresightEntity) {
        return foresightRepository.save(foresightEntity);
    }

    @Override
    public void deleteById(Long id) {
        foresightRepository.deleteById(id);
    }

    @Override
    public boolean existsByWorkSkillIdAndSourceUrl(Long workSkillId, String sourceUrl) {
        return foresightRepository.existsByWorkSkillIdAndSourceUrl(workSkillId, sourceUrl);
    }
}
