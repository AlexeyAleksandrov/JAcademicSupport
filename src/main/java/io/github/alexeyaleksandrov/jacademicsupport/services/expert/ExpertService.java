package io.github.alexeyaleksandrov.jacademicsupport.services.expert;

import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertEntity;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.expert.ExpertRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ExpertService {
    private final ExpertRepository expertRepository;
    
    public ExpertService(ExpertRepository expertRepository) {
        this.expertRepository = expertRepository;
    }
    
    public ExpertEntity findById(Long id) {
        return expertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expert not found with id: " + id));
    }
}
