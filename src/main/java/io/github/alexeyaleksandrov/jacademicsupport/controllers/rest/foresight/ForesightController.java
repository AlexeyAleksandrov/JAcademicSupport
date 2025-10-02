package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.foresight;

import io.github.alexeyaleksandrov.jacademicsupport.dto.foresight.ForesightDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.foresight.ForesightResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.ForesightEntity;
import io.github.alexeyaleksandrov.jacademicsupport.services.ForesightService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foresights")
public class ForesightController {

    private final ForesightService foresightService;

    public ForesightController(ForesightService foresightService) {
        this.foresightService = foresightService;
    }

    @GetMapping
    public ResponseEntity<List<ForesightResponseDto>> getAllForesights() {
        List<ForesightEntity> entities = foresightService.findAll();
        List<ForesightResponseDto> dtos = entities.stream()
                .map(e -> new ForesightResponseDto(e.getId(),
                        e.getForesightName(),
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
        return ResponseEntity.ok(new ForesightResponseDto(entity.getId(), entity.getForesightName(), entity.getSourceName(), entity.getSourceUrl()));
    }

    @PostMapping
    public ResponseEntity<ForesightResponseDto> createForesight(@RequestBody ForesightDto dto) {
        ForesightEntity entity = new ForesightEntity();
        entity.setForesightName(dto.getForesightName());
        entity.setSourceName(dto.getSourceName());
        entity.setSourceUrl(dto.getSourceUrl());
        
        ForesightEntity created = foresightService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ForesightResponseDto(created.getId(), created.getForesightName(), created.getSourceName(), created.getSourceUrl()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ForesightResponseDto> updateForesight(@PathVariable Long id, @RequestBody ForesightDto dto) {
        ForesightEntity existing = foresightService.findById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        
        existing.setForesightName(dto.getForesightName());
        existing.setSourceName(dto.getSourceName());
        existing.setSourceUrl(dto.getSourceUrl());
        
        ForesightEntity updated = foresightService.save(existing);
        return ResponseEntity.ok(new ForesightResponseDto(updated.getId(), updated.getForesightName(), updated.getSourceName(), updated.getSourceUrl()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteForesight(@PathVariable Long id) {
        ForesightEntity entity = foresightService.findById(id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        foresightService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
