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
                        e.getWorkSkill().getId(),
                        e.getSourceName(),
                        e.getSourceUrl()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForesightResponseDto> getForesightById(@PathVariable Long id) {
        ForesightEntity entity = foresightService.findById(id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new ForesightResponseDto(
                entity.getId(),
                entity.getWorkSkill().getId(),
                entity.getSourceName(),
                entity.getSourceUrl()));
    }

    @PostMapping
    public ResponseEntity<ForesightResponseDto> createForesight(@RequestBody ForesightDto dto) {
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
        ForesightEntity existing = foresightService.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        if (existing.getWorkSkill().getId() != dto.getWorkSkillId()) {
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
        ForesightEntity entity = foresightService.findById(id);
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
