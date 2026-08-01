package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.foresight;

import io.github.alexeyaleksandrov.jacademicsupport.dto.foresight.ForesightDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.foresight.ForesightResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.ForesightEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillCanonicalRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.ForesightService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/foresights")
@AllArgsConstructor
public class ForesightController {

    private final ForesightService foresightService;
    private final WorkSkillService workSkillService;
    private final SkillCanonicalRepository skillCanonicalRepository;

    private ForesightResponseDto toDto(ForesightEntity e) {
        return toDto(e, null);
    }

    private ForesightResponseDto toDto(ForesightEntity e, Map<Long, String> nameMap) {
        ForesightResponseDto dto = new ForesightResponseDto(
                e.getId(),
                e.getWorkSkill() != null ? e.getWorkSkill().getId() : null,
                e.getSourceName(),
                e.getSourceUrl());
        dto.setCanonicalId(e.getCanonicalId());
        if (nameMap != null && e.getCanonicalId() != null) {
            dto.setCanonicalName(nameMap.get(e.getCanonicalId()));
        } else if (e.getCanonicalId() != null) {
            skillCanonicalRepository.findById(e.getCanonicalId()).ifPresent(sc -> dto.setCanonicalName(sc.getName()));
        }
        dto.setConfidence(e.getConfidence());
        dto.setDirection(e.getDirection());
        dto.setProfessionCode(e.getProfessionCode());
        dto.setDomain(e.getDomain());
        dto.setTechFamily(e.getTechFamily());
        dto.setForecastDate(e.getForecastDate());
        return dto;
    }

    @GetMapping
    public ResponseEntity<List<ForesightResponseDto>> getAllForesights() {
        List<ForesightEntity> entities = foresightService.findAll();
        Map<Long, String> nameMap = buildNameMap(entities.stream()
                .map(ForesightEntity::getCanonicalId).filter(id -> id != null).distinct().toList());
        List<ForesightResponseDto> dtos = entities.stream().map(e -> toDto(e, nameMap)).toList();
        return ResponseEntity.ok(dtos);
    }

    private Map<Long, String> buildNameMap(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        return skillCanonicalRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(SkillCanonical::getId, SkillCanonical::getName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForesightResponseDto> getForesightById(@PathVariable Long id) {
        Optional<ForesightEntity> entity = foresightService.findById(id);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDto(entity.get()));
    }

    private void applyDtoToEntity(ForesightDto dto, ForesightEntity entity) {
        entity.setSourceName(dto.getSourceName());
        entity.setSourceUrl(dto.getSourceUrl());
        entity.setCanonicalId(dto.getCanonicalId());
        entity.setDirection(dto.getDirection() != null ? dto.getDirection() : "POSITIVE");
        entity.setProfessionCode(dto.getProfessionCode());
        entity.setDomain(dto.getDomain());
        entity.setTechFamily(dto.getTechFamily());
        entity.setForecastDate(dto.getForecastDate());
        if (dto.getConfidence() != null) {
            entity.setConfidence(dto.getConfidence());
        }
        if (dto.getWorkSkillId() != null) {
            WorkSkill ws = workSkillService.findById(dto.getWorkSkillId()).orElse(null);
            entity.setWorkSkill(ws);
        }
    }

    @PostMapping
    public ResponseEntity<ForesightResponseDto> createForesight(@RequestBody ForesightDto dto) {
        if (dto.getWorkSkillId() != null &&
                foresightService.existsByWorkSkillIdAndSourceUrl(dto.getWorkSkillId(), dto.getSourceUrl())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        ForesightEntity entity = new ForesightEntity();
        applyDtoToEntity(dto, entity);

        ForesightEntity created = foresightService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ForesightResponseDto> updateForesight(@PathVariable Long id, @RequestBody ForesightDto dto) {
        Optional<ForesightEntity> existingOpt = foresightService.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ForesightEntity existing = existingOpt.get();

        boolean duplicateExists = dto.getWorkSkillId() != null &&
                foresightService.existsByWorkSkillIdAndSourceUrl(dto.getWorkSkillId(), dto.getSourceUrl());
        boolean isSameEntity = existing.getWorkSkill() != null &&
                Long.valueOf(existing.getWorkSkill().getId()).equals(dto.getWorkSkillId()) &&
                dto.getSourceUrl().equals(existing.getSourceUrl());
        if (duplicateExists && !isSameEntity) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        applyDtoToEntity(dto, existing);
        ForesightEntity updated = foresightService.save(existing);
        return ResponseEntity.ok(toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ForesightResponseDto> deleteForesight(@PathVariable Long id) {
        ForesightEntity entity = foresightService.findById(id).orElse(null);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        ForesightResponseDto snapshot = toDto(entity);
        foresightService.deleteById(id);
        return ResponseEntity.ok(snapshot);
    }
}
