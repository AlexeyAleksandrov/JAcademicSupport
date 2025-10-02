package io.github.alexeyaleksandrov.jacademicsupport.services;

import io.github.alexeyaleksandrov.jacademicsupport.models.ForesightEntity;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ForesightRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ForesightServiceImpl implements ForesightService {

    private final ForesightRepository foresightRepository;

    public ForesightServiceImpl(ForesightRepository foresightRepository) {
        this.foresightRepository = foresightRepository;
    }

    @Override
    public List<ForesightEntity> findAll() {
        return foresightRepository.findAll();
    }

    @Override
    public ForesightEntity findById(Long id) {
        Optional<ForesightEntity> foresight = foresightRepository.findById(id);
        return foresight.orElse(null);
    }

    @Override
    public ForesightEntity save(ForesightEntity foresightEntity) {
        return foresightRepository.save(foresightEntity);
    }

    @Override
    public void deleteById(Long id) {
        foresightRepository.deleteById(id);
    }
}
