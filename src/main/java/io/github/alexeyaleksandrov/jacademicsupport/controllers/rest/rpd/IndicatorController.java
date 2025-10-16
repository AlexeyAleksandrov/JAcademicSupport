package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.indicator.CreateIndicatorRequest;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.indicator.IndicatorDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.indicator.UpdateIndicatorRequest;
import io.github.alexeyaleksandrov.jacademicsupport.services.competency.CompetencyAchievementIndicatorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("api/indicators")
public class IndicatorController {
    private final CompetencyAchievementIndicatorService service;

    @PostMapping
    public ResponseEntity<IndicatorDto> createIndicator(@Valid @RequestBody CreateIndicatorRequest createRequest) {
        var savedIndicator = service.createIndicatorWithoutCompetency(createRequest);
        
        return ResponseEntity
                .created(URI.create("/api/indicators/" + savedIndicator.getId()))
                .body(service.convertToDto(savedIndicator));
    }

    @PostMapping("/{number}/keywords")
    public ResponseEntity<IndicatorDto> createKeywordsForIndicator(@PathVariable("number") String number) {
        var indicator = service.createKeywordsForIndicator(number);
        return ResponseEntity.ok(service.convertToDto(indicator));
    }

    @GetMapping
    public ResponseEntity<List<IndicatorDto>> getAllIndicators() {
        List<IndicatorDto> indicatorDtos = service.findAll().stream()
                .map(service::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(indicatorDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IndicatorDto> getIndicatorById(@PathVariable Long id) {
        return service.findByIdOptional(id)
                .map(service::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<IndicatorDto> getIndicatorByNumber(@PathVariable("number") String number) {
        return service.findByNumberOptional(number)
                .map(service::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<IndicatorDto> updateIndicatorById(@PathVariable Long id, @Valid @RequestBody UpdateIndicatorRequest updateRequest) {
        var updated = service.updateIndicatorById(id, updateRequest);
        return ResponseEntity.ok(service.convertToDto(updated));
    }

    @PutMapping("/number/{number}")
    public ResponseEntity<IndicatorDto> updateIndicatorByNumber(@PathVariable("number") String number, @Valid @RequestBody UpdateIndicatorRequest updateRequest) {
        var updated = service.updateIndicatorByNumber(number, updateRequest);
        return ResponseEntity.ok(service.convertToDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndicatorById(@PathVariable Long id) {
        service.deleteIndicatorById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/number/{number}")
    public ResponseEntity<Void> deleteIndicatorByNumber(@PathVariable("number") String number) {
        service.deleteIndicatorByNumber(number);
        return ResponseEntity.noContent().build();
    }
}
