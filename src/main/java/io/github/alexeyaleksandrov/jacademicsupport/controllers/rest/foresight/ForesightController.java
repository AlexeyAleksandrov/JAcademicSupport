package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.foresight;

import io.github.alexeyaleksandrov.jacademicsupport.dto.foresight.ForesightDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.foresight.ForesightResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.ForesightEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.services.ForesightService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/foresights")
@AllArgsConstructor
public class ForesightController {

    private final ForesightService foresightService;
    private final WorkSkillService workSkillService;

    @GetMapping
    public ResponseEntity<List<ForesightResponseDto>> getAllForesights() {
        List<ForesightEntity> entities = foresightService.findAll();
        List<ForesightResponseDto> dtos = entities.stream()
                .map(e -> new ForesightResponseDto(
                        e.getId(),
                        e.getWorkSkill() != null ? e.getWorkSkill().getId() : null,
                        e.getSourceName(),
                        e.getSourceUrl()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForesightResponseDto> getForesightById(@PathVariable Long id) {
        Optional<ForesightEntity> entity = foresightService.findById(id);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ForesightEntity e = entity.get();
        return ResponseEntity.ok(new ForesightResponseDto(
                e.getId(),
                e.getWorkSkill() != null ? e.getWorkSkill().getId() : null,
                e.getSourceName(),
                e.getSourceUrl()));
    }

    @PostMapping
    public ResponseEntity<ForesightResponseDto> createForesight(@RequestBody ForesightDto dto) {
        // Check for duplicate
        if (foresightService.existsByWorkSkillIdAndSourceUrl(dto.getWorkSkillId(), dto.getSourceUrl())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        WorkSkill workSkill = workSkillService.findById(dto.getWorkSkillId()).orElse(null);
        if (workSkill == null) {
            return ResponseEntity.badRequest().build();
        }

        ForesightEntity entity = new ForesightEntity();
        entity.setWorkSkill(workSkill);
        entity.setSourceName(dto.getSourceName());
        entity.setSourceUrl(dto.getSourceUrl());

        ForesightEntity created = foresightService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ForesightResponseDto(
                        created.getId(),
                        created.getWorkSkill().getId(),
                        created.getSourceName(),
                        created.getSourceUrl()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ForesightResponseDto> updateForesight(@PathVariable Long id, @RequestBody ForesightDto dto) {
        Optional<ForesightEntity> existingOpt = foresightService.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ForesightEntity existing = existingOpt.get();

        // Check for duplicate (excluding current item) with null safety
        boolean duplicateExists = foresightService.existsByWorkSkillIdAndSourceUrl(dto.getWorkSkillId(), dto.getSourceUrl());
        boolean isSameEntity = existing.getWorkSkill() != null &&
                                existing.getWorkSkill().getId() == dto.getWorkSkillId() &&
                                existing.getSourceUrl().equals(dto.getSourceUrl());
        
        if (duplicateExists && !isSameEntity) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Update workSkill with null safety
        if (existing.getWorkSkill() == null || 
            existing.getWorkSkill().getId() != dto.getWorkSkillId()) {
            
            WorkSkill workSkill = workSkillService.findById(dto.getWorkSkillId()).orElse(null);
            if (workSkill == null) {
                return ResponseEntity.badRequest().build();
            }
            existing.setWorkSkill(workSkill);
        }

        existing.setSourceName(dto.getSourceName());
        existing.setSourceUrl(dto.getSourceUrl());

        ForesightEntity updated = foresightService.save(existing);
        return ResponseEntity.ok(new ForesightResponseDto(
                updated.getId(),
                updated.getWorkSkill().getId(),
                updated.getSourceName(),
                updated.getSourceUrl()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ForesightResponseDto> deleteForesight(@PathVariable Long id) {
        ForesightEntity entity = foresightService.findById(id).orElse(null);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        foresightService.deleteById(id);
        return ResponseEntity.ok(new ForesightResponseDto(
                entity.getId(),
                entity.getWorkSkill().getId(),
                entity.getSourceName(),
                entity.getSourceUrl()));
    }
}
