package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.foresight;

import io.github.alexeyaleksandrov.jacademicsupport.dto.foresight.ForesightSkillsGroupDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.foresight.ForesightSkillsGroupResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.ForesightSkillsGroupEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.services.ForesightSkillsGroupService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.SkillsGroupsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/foresights-skills-groups")
@AllArgsConstructor
public class ForesightSkillsGroupController {

    private final ForesightSkillsGroupService foresightSkillsGroupService;
    private final SkillsGroupsService skillsGroupsService;

    @GetMapping
    public ResponseEntity<List<ForesightSkillsGroupResponseDto>> getAllForesightsSkillsGroups() {
        List<ForesightSkillsGroupEntity> entities = foresightSkillsGroupService.findAll();
        List<ForesightSkillsGroupResponseDto> dtos = entities.stream()
                .map(e -> new ForesightSkillsGroupResponseDto(
                        e.getId(),
                        e.getSkillsGroup() != null ? e.getSkillsGroup().getId() : null,
                        e.getSourceName(),
                        e.getSourceUrl()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForesightSkillsGroupResponseDto> getForesightSkillsGroupById(@PathVariable Long id) {
        Optional<ForesightSkillsGroupEntity> entity = foresightSkillsGroupService.findById(id);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ForesightSkillsGroupEntity e = entity.get();
        return ResponseEntity.ok(new ForesightSkillsGroupResponseDto(
                e.getId(),
                e.getSkillsGroup() != null ? e.getSkillsGroup().getId() : null,
                e.getSourceName(),
                e.getSourceUrl()));
    }

    @GetMapping("/skills-group/{skillsGroupId}")
    public ResponseEntity<List<ForesightSkillsGroupResponseDto>> getForesightsBySkillsGroupId(
            @PathVariable Long skillsGroupId) {
        List<ForesightSkillsGroupEntity> entities = foresightSkillsGroupService.findBySkillsGroupId(skillsGroupId);
        List<ForesightSkillsGroupResponseDto> dtos = entities.stream()
                .map(e -> new ForesightSkillsGroupResponseDto(
                        e.getId(),
                        e.getSkillsGroup() != null ? e.getSkillsGroup().getId() : null,
                        e.getSourceName(),
                        e.getSourceUrl()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/source/{sourceName}")
    public ResponseEntity<List<ForesightSkillsGroupResponseDto>> getForesightsBySourceName(
            @PathVariable String sourceName) {
        List<ForesightSkillsGroupEntity> entities = foresightSkillsGroupService.findBySourceName(sourceName);
        List<ForesightSkillsGroupResponseDto> dtos = entities.stream()
                .map(e -> new ForesightSkillsGroupResponseDto(
                        e.getId(),
                        e.getSkillsGroup() != null ? e.getSkillsGroup().getId() : null,
                        e.getSourceName(),
                        e.getSourceUrl()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<ForesightSkillsGroupResponseDto> createForesightSkillsGroup(
            @RequestBody ForesightSkillsGroupDto dto) {
        // Check for duplicate
        if (foresightSkillsGroupService.existsBySkillsGroupIdAndSourceUrl(
                dto.getSkillsGroupId(), dto.getSourceUrl())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        SkillsGroup skillsGroup = skillsGroupsService.findById(dto.getSkillsGroupId()).orElse(null);
        if (skillsGroup == null) {
            return ResponseEntity.badRequest().build();
        }

        ForesightSkillsGroupEntity entity = new ForesightSkillsGroupEntity();
        entity.setSkillsGroup(skillsGroup);
        entity.setSourceName(dto.getSourceName());
        entity.setSourceUrl(dto.getSourceUrl());

        ForesightSkillsGroupEntity created = foresightSkillsGroupService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ForesightSkillsGroupResponseDto(
                        created.getId(),
                        created.getSkillsGroup().getId(),
                        created.getSourceName(),
                        created.getSourceUrl()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ForesightSkillsGroupResponseDto> updateForesightSkillsGroup(
            @PathVariable Long id, @RequestBody ForesightSkillsGroupDto dto) {
        Optional<ForesightSkillsGroupEntity> existingOpt = foresightSkillsGroupService.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ForesightSkillsGroupEntity existing = existingOpt.get();

        // Check for duplicate (excluding current item) with null safety
        boolean duplicateExists = foresightSkillsGroupService.existsBySkillsGroupIdAndSourceUrl(
                dto.getSkillsGroupId(), dto.getSourceUrl());
        boolean isSameEntity = existing.getSkillsGroup() != null &&
                                existing.getSkillsGroup().getId() == dto.getSkillsGroupId() &&
                                existing.getSourceUrl().equals(dto.getSourceUrl());
        
        if (duplicateExists && !isSameEntity) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Update skillsGroup with null safety
        if (existing.getSkillsGroup() == null || 
            existing.getSkillsGroup().getId() != dto.getSkillsGroupId()) {
            
            SkillsGroup skillsGroup = skillsGroupsService.findById(dto.getSkillsGroupId()).orElse(null);
            if (skillsGroup == null) {
                return ResponseEntity.badRequest().build();
            }
            existing.setSkillsGroup(skillsGroup);
        }

        existing.setSourceName(dto.getSourceName());
        existing.setSourceUrl(dto.getSourceUrl());

        ForesightSkillsGroupEntity updated = foresightSkillsGroupService.save(existing);
        return ResponseEntity.ok(new ForesightSkillsGroupResponseDto(
                updated.getId(),
                updated.getSkillsGroup().getId(),
                updated.getSourceName(),
                updated.getSourceUrl()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ForesightSkillsGroupResponseDto> deleteForesightSkillsGroup(@PathVariable Long id) {
        ForesightSkillsGroupEntity entity = foresightSkillsGroupService.findById(id).orElse(null);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        foresightSkillsGroupService.deleteById(id);
        return ResponseEntity.ok(new ForesightSkillsGroupResponseDto(
                entity.getId(),
                entity.getSkillsGroup().getId(),
                entity.getSourceName(),
                entity.getSourceUrl()));
    }
}
