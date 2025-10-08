package io.github.alexeyaleksandrov.jacademicsupport.services.competency;

import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CompetencyAchievementIndicatorService {
    private final CompetencyAchievementIndicatorRepository repository;
    
    public CompetencyAchievementIndicatorService(CompetencyAchievementIndicatorRepository repository) {
        this.repository = repository;
    }
    
    public CompetencyAchievementIndicator findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Competency achievement indicator not found with id: " + id));
    }
}
