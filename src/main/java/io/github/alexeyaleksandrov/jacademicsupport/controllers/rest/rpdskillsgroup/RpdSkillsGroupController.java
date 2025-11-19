package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.rpdskillsgroup;

import io.github.alexeyaleksandrov.jacademicsupport.dto.rpdskillsgroup.RpdSkillsGroupDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpdskillsgroup.RpdSkillsGroupResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.RpdSkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpdskillsgroup.RpdSkillsGroupService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.SkillsGroupsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.RpdService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rpd-skills-groups")
@AllArgsConstructor
public class RpdSkillsGroupController {

    private final RpdSkillsGroupService rpdSkillsGroupService;
    private final SkillsGroupsService skillsGroupsService;
    private final RpdService rpdService;

    @GetMapping
    public ResponseEntity<List<RpdSkillsGroupResponseDto>> getAllRpdSkillsGroups() {
        List<RpdSkillsGroup> entities = rpdSkillsGroupService.findAll();
        List<RpdSkillsGroupResponseDto> dtos = entities.stream()
                .map(e -> new RpdSkillsGroupResponseDto(
                        e.getId(),
                        e.getRpd() != null ? e.getRpd().getId() : null,
                        e.getSkillsGroup() != null ? e.getSkillsGroup().getId() : null,
                        e.getTime()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RpdSkillsGroupResponseDto> getRpdSkillsGroupById(@PathVariable Long id) {
        Optional<RpdSkillsGroup> entity = rpdSkillsGroupService.findById(id);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        RpdSkillsGroup e = entity.get();
        return ResponseEntity.ok(new RpdSkillsGroupResponseDto(
                e.getId(),
                e.getRpd() != null ? e.getRpd().getId() : null,
                e.getSkillsGroup() != null ? e.getSkillsGroup().getId() : null,
                e.getTime()));
    }

    @GetMapping("/rpd/{rpdId}")
    public ResponseEntity<List<RpdSkillsGroupResponseDto>> getRpdSkillsGroupsByRpdId(@PathVariable Long rpdId) {
        List<RpdSkillsGroup> entities = rpdSkillsGroupService.findByRpdId(rpdId);
        List<RpdSkillsGroupResponseDto> dtos = entities.stream()
                .map(e -> new RpdSkillsGroupResponseDto(
                        e.getId(),
                        e.getRpd() != null ? e.getRpd().getId() : null,
                        e.getSkillsGroup() != null ? e.getSkillsGroup().getId() : null,
                        e.getTime()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/skills-group/{skillsGroupId}")
    public ResponseEntity<List<RpdSkillsGroupResponseDto>> getRpdSkillsGroupsBySkillsGroupId(@PathVariable Long skillsGroupId) {
        List<RpdSkillsGroup> entities = rpdSkillsGroupService.findBySkillsGroupId(skillsGroupId);
        List<RpdSkillsGroupResponseDto> dtos = entities.stream()
                .map(e -> new RpdSkillsGroupResponseDto(
                        e.getId(),
                        e.getRpd() != null ? e.getRpd().getId() : null,
                        e.getSkillsGroup() != null ? e.getSkillsGroup().getId() : null,
                        e.getTime()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/time/{minTime}")
    public ResponseEntity<List<RpdSkillsGroupResponseDto>> getRpdSkillsGroupsByMinTime(@PathVariable Integer minTime) {
        List<RpdSkillsGroup> entities = rpdSkillsGroupService.findByTimeGreaterThanEqual(minTime);
        List<RpdSkillsGroupResponseDto> dtos = entities.stream()
                .map(e -> new RpdSkillsGroupResponseDto(
                        e.getId(),
                        e.getRpd() != null ? e.getRpd().getId() : null,
                        e.getSkillsGroup() != null ? e.getSkillsGroup().getId() : null,
                        e.getTime()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<RpdSkillsGroupResponseDto> createRpdSkillsGroup(@RequestBody RpdSkillsGroupDto dto) {
        // Check for duplicate
        if (rpdSkillsGroupService.existsByRpdIdAndSkillsGroupId(dto.getRpdId(), dto.getSkillsGroupId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Validate SkillsGroup exists
        SkillsGroup skillsGroup = skillsGroupsService.findById(dto.getSkillsGroupId()).orElse(null);
        if (skillsGroup == null) {
            return ResponseEntity.badRequest().build();
        }

        // Validate Rpd exists
        Rpd rpd = rpdService.findById(dto.getRpdId()).orElse(null);
        if (rpd == null) {
            return ResponseEntity.badRequest().build();
        }

        // Validate time is positive
        if (dto.getTime() == null || dto.getTime() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        RpdSkillsGroup entity = new RpdSkillsGroup();
        entity.setRpd(rpd);
        entity.setSkillsGroup(skillsGroup);
        entity.setTime(dto.getTime());

        RpdSkillsGroup created = rpdSkillsGroupService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RpdSkillsGroupResponseDto(
                        created.getId(),
                        created.getRpd() != null ? created.getRpd().getId() : null,
                        created.getSkillsGroup().getId(),
                        created.getTime()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RpdSkillsGroupResponseDto> updateRpdSkillsGroup(@PathVariable Long id, @RequestBody RpdSkillsGroupDto dto) {
        Optional<RpdSkillsGroup> existingOpt = rpdSkillsGroupService.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        RpdSkillsGroup existing = existingOpt.get();

        // Check for duplicate (excluding current item)
        boolean duplicateExists = rpdSkillsGroupService.existsByRpdIdAndSkillsGroupId(dto.getRpdId(), dto.getSkillsGroupId());
        boolean isSameEntity = existing.getRpd() != null && existing.getRpd().getId() == dto.getRpdId() &&
                               existing.getSkillsGroup() != null && existing.getSkillsGroup().getId() == dto.getSkillsGroupId();

        if (duplicateExists && !isSameEntity) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Validate SkillsGroup exists
        SkillsGroup skillsGroup = skillsGroupsService.findById(dto.getSkillsGroupId()).orElse(null);
        if (skillsGroup == null) {
            return ResponseEntity.badRequest().build();
        }

        // Validate Rpd exists
        Rpd rpd = rpdService.findById(dto.getRpdId()).orElse(null);
        if (rpd == null) {
            return ResponseEntity.badRequest().build();
        }

        // Validate time is positive
        if (dto.getTime() == null || dto.getTime() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        existing.setRpd(rpd);
        existing.setSkillsGroup(skillsGroup);
        existing.setTime(dto.getTime());

        RpdSkillsGroup updated = rpdSkillsGroupService.save(existing);
        return ResponseEntity.ok(new RpdSkillsGroupResponseDto(
                updated.getId(),
                updated.getRpd() != null ? updated.getRpd().getId() : null,
                updated.getSkillsGroup().getId(),
                updated.getTime()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RpdSkillsGroupResponseDto> deleteRpdSkillsGroup(@PathVariable Long id) {
        RpdSkillsGroup entity = rpdSkillsGroupService.findById(id).orElse(null);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        rpdSkillsGroupService.deleteById(id);
        return ResponseEntity.ok(new RpdSkillsGroupResponseDto(
                entity.getId(),
                entity.getRpd() != null ? entity.getRpd().getId() : null,
                entity.getSkillsGroup() != null ? entity.getSkillsGroup().getId() : null,
                entity.getTime()));
    }
}
