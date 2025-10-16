package io.github.alexeyaleksandrov.jacademicsupport.services.competency;

import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.indicator.CreateIndicatorRequest;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.indicator.IndicatorDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.indicator.UpdateIndicatorRequest;
import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import io.github.alexeyaleksandrov.jacademicsupport.models.Competency;
import io.github.alexeyaleksandrov.jacademicsupport.models.Keyword;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.competency.IndicatorsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompetencyAchievementIndicatorService {
    private final CompetencyAchievementIndicatorRepository repository;
    private final CompetencyRepository competencyRepository;
    private final IndicatorsService indicatorsService;
    
    public CompetencyAchievementIndicator findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Competency achievement indicator not found with id: " + id));
    }
    
    public Optional<CompetencyAchievementIndicator> findByIdOptional(Long id) {
        return repository.findById(id);
    }
    
    public CompetencyAchievementIndicator findByNumber(String number) {
        CompetencyAchievementIndicator indicator = repository.findByNumber(number);
        if (indicator == null) {
            throw new EntityNotFoundException("Indicator not found with number: " + number);
        }
        return indicator;
    }
    
    public Optional<CompetencyAchievementIndicator> findByNumberOptional(String number) {
        return Optional.ofNullable(repository.findByNumber(number));
    }
    
    public List<CompetencyAchievementIndicator> findAllByCompetencyNumber(String competencyNumber) {
        return repository.findAllByCompetencyByCompetencyId_Number(competencyNumber);
    }
    
    public List<CompetencyAchievementIndicator> findAll() {
        return repository.findAll();
    }
    
    public CompetencyAchievementIndicator createIndicator(String competencyNumber, CreateIndicatorRequest createRequest) {
        // Check if competency exists
        Competency competency = competencyRepository.findByNumber(competencyNumber);
        if (competency == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Competency not found");
        }
                
        // Check if indicator with this number already exists
        if (repository.existsByNumber(createRequest.getNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Indicator with this number already exists");
        }
        
        CompetencyAchievementIndicator indicator = new CompetencyAchievementIndicator();
        indicator.setNumber(createRequest.getNumber());
        indicator.setDescription(createRequest.getDescription());
        indicator.setIndicatorKnow(createRequest.getIndicatorKnow());
        indicator.setIndicatorAble(createRequest.getIndicatorAble());
        indicator.setIndicatorPossess(createRequest.getIndicatorPossess());
        indicator.setCompetencyByCompetencyId(competency);
        
        return repository.saveAndFlush(indicator);
    }
    
    public CompetencyAchievementIndicator createIndicatorWithoutCompetency(CreateIndicatorRequest createRequest) {
        // Check if indicator with this number already exists
        if (repository.existsByNumber(createRequest.getNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Indicator with this number already exists");
        }
        
        CompetencyAchievementIndicator indicator = new CompetencyAchievementIndicator();
        indicator.setNumber(createRequest.getNumber());
        indicator.setDescription(createRequest.getDescription());
        indicator.setIndicatorKnow(createRequest.getIndicatorKnow());
        indicator.setIndicatorAble(createRequest.getIndicatorAble());
        indicator.setIndicatorPossess(createRequest.getIndicatorPossess());
        
        // If competency number is provided, try to find and set it
        if (createRequest.getCompetencyNumber() != null) {
            Competency competency = competencyRepository.findByNumber(createRequest.getCompetencyNumber());
            if (competency != null) {
                indicator.setCompetencyByCompetencyId(competency);
            }
        }
        
        return repository.saveAndFlush(indicator);
    }
    
    public CompetencyAchievementIndicator createKeywordsForIndicator(String number) {
        CompetencyAchievementIndicator indicator = repository.findByNumber(number);
        if (indicator == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Indicator not found");
        }
        return indicatorsService.createKeywordsForCompetencyIndicator(indicator);
    }
    
    public CompetencyAchievementIndicator updateIndicatorById(Long id, UpdateIndicatorRequest updateRequest) {
        CompetencyAchievementIndicator indicator = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Indicator not found"));
        
        indicator.setDescription(updateRequest.getDescription());
        indicator.setIndicatorKnow(updateRequest.getIndicatorKnow());
        indicator.setIndicatorAble(updateRequest.getIndicatorAble());
        indicator.setIndicatorPossess(updateRequest.getIndicatorPossess());
        
        return repository.save(indicator);
    }
    
    public CompetencyAchievementIndicator updateIndicatorByNumber(String number, UpdateIndicatorRequest updateRequest) {
        CompetencyAchievementIndicator indicator = repository.findByNumber(number);
        if (indicator == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Indicator not found");
        }
        
        indicator.setDescription(updateRequest.getDescription());
        indicator.setIndicatorKnow(updateRequest.getIndicatorKnow());
        indicator.setIndicatorAble(updateRequest.getIndicatorAble());
        indicator.setIndicatorPossess(updateRequest.getIndicatorPossess());
        
        return repository.save(indicator);
    }
    
    public void deleteIndicatorById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Indicator not found");
        }
        repository.deleteById(id);
    }
    
    public void deleteIndicatorByNumber(String number) {
        CompetencyAchievementIndicator indicator = repository.findByNumber(number);
        if (indicator == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Indicator not found");
        }
        repository.delete(indicator);
    }
    
    public IndicatorDto convertToDto(CompetencyAchievementIndicator indicator) {
        IndicatorDto dto = new IndicatorDto();
        dto.setId(indicator.getId());
        dto.setNumber(indicator.getNumber());
        dto.setDescription(indicator.getDescription());
        dto.setIndicatorKnow(indicator.getIndicatorKnow());
        dto.setIndicatorAble(indicator.getIndicatorAble());
        dto.setIndicatorPossess(indicator.getIndicatorPossess());
        if (indicator.getKeywords() != null) {
            dto.setKeywords(indicator.getKeywords().stream()
                    .map(Keyword::getKeyword)
                    .collect(Collectors.toSet()));
        }
        if (indicator.getCompetencyByCompetencyId() != null) {
            dto.setCompetencyNumber(indicator.getCompetencyByCompetencyId().getNumber());
        }
        return dto;
    }
    
    public boolean competencyExists(String competencyNumber) {
        return competencyRepository.findByNumber(competencyNumber) != null;
    }
    
    public boolean indicatorExists(String number) {
        return repository.existsByNumber(number);
    }
    
    public boolean indicatorExistsById(Long id) {
        return repository.existsById(id);
    }
}
