package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.rpdskill;

import io.github.alexeyaleksandrov.jacademicsupport.dto.rpdskill.RpdSkillDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpdskill.RpdSkillResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.RpdSkill;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpdskill.RpdSkillService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.RpdService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rpd-skills")
@AllArgsConstructor
public class RpdSkillController {

    private final RpdSkillService rpdSkillService;
    private final WorkSkillService workSkillService;
    private final RpdService rpdService;

    @GetMapping
    public ResponseEntity<List<RpdSkillResponseDto>> getAllRpdSkills() {
        List<RpdSkill> entities = rpdSkillService.findAll();
        List<RpdSkillResponseDto> dtos = entities.stream()
                .map(e -> new RpdSkillResponseDto(
                        e.getId(),
                        e.getRpd() != null ? e.getRpd().getId() : null,
                        e.getWorkSkill() != null ? e.getWorkSkill().getId() : null,
                        e.getTime()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RpdSkillResponseDto> getRpdSkillById(@PathVariable Long id) {
        Optional<RpdSkill> entity = rpdSkillService.findById(id);
        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        RpdSkill e = entity.get();
        return ResponseEntity.ok(new RpdSkillResponseDto(
                e.getId(),
                e.getRpd() != null ? e.getRpd().getId() : null,
                e.getWorkSkill() != null ? e.getWorkSkill().getId() : null,
                e.getTime()));
    }

    @GetMapping("/rpd/{rpdId}")
    public ResponseEntity<List<RpdSkillResponseDto>> getRpdSkillsByRpdId(@PathVariable Long rpdId) {
        List<RpdSkill> entities = rpdSkillService.findByRpdId(rpdId);
        List<RpdSkillResponseDto> dtos = entities.stream()
                .map(e -> new RpdSkillResponseDto(
                        e.getId(),
                        e.getRpd() != null ? e.getRpd().getId() : null,
                        e.getWorkSkill() != null ? e.getWorkSkill().getId() : null,
                        e.getTime()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/work-skill/{workSkillId}")
    public ResponseEntity<List<RpdSkillResponseDto>> getRpdSkillsByWorkSkillId(@PathVariable Long workSkillId) {
        List<RpdSkill> entities = rpdSkillService.findByWorkSkillId(workSkillId);
        List<RpdSkillResponseDto> dtos = entities.stream()
                .map(e -> new RpdSkillResponseDto(
                        e.getId(),
                        e.getRpd() != null ? e.getRpd().getId() : null,
                        e.getWorkSkill() != null ? e.getWorkSkill().getId() : null,
                        e.getTime()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/time/{minTime}")
    public ResponseEntity<List<RpdSkillResponseDto>> getRpdSkillsByMinTime(@PathVariable Integer minTime) {
        List<RpdSkill> entities = rpdSkillService.findByTimeGreaterThanEqual(minTime);
        List<RpdSkillResponseDto> dtos = entities.stream()
                .map(e -> new RpdSkillResponseDto(
                        e.getId(),
                        e.getRpd() != null ? e.getRpd().getId() : null,
                        e.getWorkSkill() != null ? e.getWorkSkill().getId() : null,
                        e.getTime()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<RpdSkillResponseDto> createRpdSkill(@RequestBody RpdSkillDto dto) {
        // Check for duplicate
        if (rpdSkillService.existsByRpdIdAndWorkSkillId(dto.getRpdId(), dto.getWorkSkillId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Validate WorkSkill exists
        WorkSkill workSkill = workSkillService.findById(dto.getWorkSkillId()).orElse(null);
        if (workSkill == null) {
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

        RpdSkill entity = new RpdSkill();
        entity.setRpd(rpd);
        entity.setWorkSkill(workSkill);
        entity.setTime(dto.getTime());

        RpdSkill created = rpdSkillService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RpdSkillResponseDto(
                        created.getId(),
                        created.getRpd() != null ? created.getRpd().getId() : null,
                        created.getWorkSkill().getId(),
                        created.getTime()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RpdSkillResponseDto> updateRpdSkill(@PathVariable Long id, @RequestBody RpdSkillDto dto) {
        Optional<RpdSkill> existingOpt = rpdSkillService.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        RpdSkill existing = existingOpt.get();

        // Check for duplicate (excluding current item)
        boolean duplicateExists = rpdSkillService.existsByRpdIdAndWorkSkillId(dto.getRpdId(), dto.getWorkSkillId());
        boolean isSameEntity = existing.getRpd() != null && existing.getRpd().getId() == dto.getRpdId() &&
                               existing.getWorkSkill() != null && existing.getWorkSkill().getId() == dto.getWorkSkillId();

        if (duplicateExists && !isSameEntity) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Validate WorkSkill exists
        WorkSkill workSkill = workSkillService.findById(dto.getWorkSkillId()).orElse(null);
        if (workSkill == null) {
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
        existing.setWorkSkill(workSkill);
        existing.setTime(dto.getTime());

        RpdSkill updated = rpdSkillService.save(existing);
        return ResponseEntity.ok(new RpdSkillResponseDto(
                updated.getId(),
                updated.getRpd() != null ? updated.getRpd().getId() : null,
                updated.getWorkSkill().getId(),
                updated.getTime()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RpdSkillResponseDto> deleteRpdSkill(@PathVariable Long id) {
        RpdSkill entity = rpdSkillService.findById(id).orElse(null);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        rpdSkillService.deleteById(id);
        return ResponseEntity.ok(new RpdSkillResponseDto(
                entity.getId(),
                entity.getRpd() != null ? entity.getRpd().getId() : null,
                entity.getWorkSkill() != null ? entity.getWorkSkill().getId() : null,
                entity.getTime()));
    }
}
