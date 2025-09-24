package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.indicator.IndicatorDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.indicator.UpdateIndicatorRequest;
import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import io.github.alexeyaleksandrov.jacademicsupport.models.Keyword;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.competency.IndicatorsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.indicator.CreateIndicatorRequest;

@RestController
@AllArgsConstructor
@RequestMapping("api/competencies/{competencyNumber}/indicators")
public class IndicatorRestController {
    private final CompetencyRepository competencyRepository;
    private final CompetencyAchievementIndicatorRepository indicatorRepository;
    private final IndicatorsService indicatorsService;

    @PostMapping
    public ResponseEntity<IndicatorDto> createCompetencyAchievementIndicator(
            @PathVariable("competencyNumber") String competencyNumber,
            @Valid @RequestBody CreateIndicatorRequest createRequest) {
        
        // Check if competency exists
        var competency = competencyRepository.findByNumber(competencyNumber);
        if (competency == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Competency not found");
        }
                
        // Check if indicator with this number already exists
        if (indicatorRepository.existsByNumber(createRequest.getNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Indicator with this number already exists");
        }
        
        CompetencyAchievementIndicator indicator = new CompetencyAchievementIndicator();
        indicator.setNumber(createRequest.getNumber());
        indicator.setDescription(createRequest.getDescription());
        indicator.setIndicatorKnow(createRequest.getIndicatorKnow());
        indicator.setIndicatorAble(createRequest.getIndicatorAble());
        indicator.setIndicatorPossess(createRequest.getIndicatorPossess());
        indicator.setCompetencyByCompetencyId(competency);
        
        CompetencyAchievementIndicator savedIndicator = indicatorRepository.saveAndFlush(indicator);
        
        return ResponseEntity
                .created(URI.create("/api/competencies/" + competencyNumber + "/indicators/" + savedIndicator.getId()))
                .body(convertToDto(savedIndicator));
    }

    @PostMapping("/{number}/keywords")
    public ResponseEntity<IndicatorDto> createKeywordsForCompetencyAchievementIndicator(@PathVariable("number") String number) {
        CompetencyAchievementIndicator indicator = indicatorRepository.findByNumber(number);
        indicator = indicatorsService.createKeywordsForCompetencyIndicator(indicator);
        IndicatorDto dto = convertToDto(indicator);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<IndicatorDto>> getAllIndicators(@PathVariable("competencyNumber") String competencyNumber) {
        List<CompetencyAchievementIndicator> indicators = indicatorRepository.findAllByCompetencyByCompetencyId_Number(competencyNumber);
        List<IndicatorDto> indicatorDtos = indicators.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(indicatorDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IndicatorDto> getIndicatorById(@PathVariable Long id) {
        return indicatorRepository.findById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<IndicatorDto> getIndicatorByNumber(@PathVariable("number") String number) {
        return Optional.ofNullable(indicatorRepository.findByNumber(number))
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<IndicatorDto> updateIndicatorById(@PathVariable Long id, @Valid @RequestBody UpdateIndicatorRequest updateRequest) {
        return indicatorRepository.findById(id)
                .map(indicator -> {
                    indicator.setDescription(updateRequest.getDescription());
                    indicator.setIndicatorKnow(updateRequest.getIndicatorKnow());
                    indicator.setIndicatorAble(updateRequest.getIndicatorAble());
                    indicator.setIndicatorPossess(updateRequest.getIndicatorPossess());
                    CompetencyAchievementIndicator saved = indicatorRepository.save(indicator);
                    return ResponseEntity.ok(convertToDto(saved));
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Indicator not found"));
    }

    @PutMapping("/number/{number}")
    public ResponseEntity<IndicatorDto> updateIndicatorByNumber(@PathVariable("number") String number, @Valid @RequestBody UpdateIndicatorRequest updateRequest) {
        CompetencyAchievementIndicator indicator = indicatorRepository.findByNumber(number);
        if (indicator == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Indicator not found");
        }
        indicator.setDescription(updateRequest.getDescription());
        indicator.setIndicatorKnow(updateRequest.getIndicatorKnow());
        indicator.setIndicatorAble(updateRequest.getIndicatorAble());
        indicator.setIndicatorPossess(updateRequest.getIndicatorPossess());
        CompetencyAchievementIndicator saved = indicatorRepository.save(indicator);
        return ResponseEntity.ok(convertToDto(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndicatorById(@PathVariable Long id) {
        if (!indicatorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Indicator not found");
        }
        indicatorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/number/{number}")
    public ResponseEntity<Void> deleteIndicatorByNumber(@PathVariable("number") String number) {
        CompetencyAchievementIndicator indicator = indicatorRepository.findByNumber(number);
        if (indicator == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Indicator not found");
        }
        indicatorRepository.delete(indicator);
        return ResponseEntity.noContent().build();
    }

    private IndicatorDto convertToDto(CompetencyAchievementIndicator indicator) {
        IndicatorDto dto = new IndicatorDto();
        dto.setId(indicator.getId());
        dto.setNumber(indicator.getNumber());
        dto.setDescription(indicator.getDescription());
        dto.setIndicatorKnow(indicator.getIndicatorKnow());
        dto.setIndicatorAble(indicator.getIndicatorAble());
        dto.setIndicatorPossess(indicator.getIndicatorPossess());
        if (indicator.getKeywords() != null) {
            dto.setKeywords(indicator.getKeywords().stream()
                    .map(Keyword::getKeyword)  // Assuming there's a getWord() method in Keyword class
                    .collect(Collectors.toSet()));      // Collect to a Set to match the target type
        }
        if (indicator.getCompetencyByCompetencyId() != null) {
            dto.setCompetencyNumber(indicator.getCompetencyByCompetencyId().getNumber());
        }
        return dto;
    }
}
