package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.workskills;

import io.github.alexeyaleksandrov.jacademicsupport.dto.workskills.WorkSkillDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.workskills.WorkSkillResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillsGroupRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillsService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/work-skills")
@AllArgsConstructor
public class WorkSkillsRestController {
    private final WorkSkillRepository workSkillRepository;
    private final WorkSkillsService workSkillsService;
    final SkillsGroupRepository skillsGroupRepository;

    @GetMapping
    public ResponseEntity<?> getAllWorkSkills() {
        try {
            return ResponseEntity.ok(workSkillsService.getAllWorkSkills());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getWorkSkillById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(workSkillsService.getWorkSkillById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createWorkSkill(@RequestBody WorkSkillDto workSkillDto) {
        try {
            WorkSkillResponseDto responseDto = workSkillsService.createWorkSkill(workSkillDto);
            return ResponseEntity.ok(responseDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (EntityExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorkSkill(
            @PathVariable Long id,
            @RequestBody WorkSkillDto workSkillDto) {
        try {
            WorkSkillResponseDto responseDto = workSkillsService.updateWorkSkill(id, workSkillDto);
            return ResponseEntity.ok(responseDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (EntityExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkSkill(@PathVariable Long id) {
        try {
            workSkillsService.deleteWorkSkill(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/match-to-groups")
    public ResponseEntity<?> matchSkillsToGroups() {
        try {
            List<WorkSkill> workSkills = workSkillsService.matchWorkSkillsToSkillsGroups();
            List<WorkSkillResponseDto> skills = workSkills.stream()
                    .map(this::convertToWorkSkillResponseDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(skills);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Update the skills group of a work skill by its ID and the ID of the new skills group.
     * @param workSkillId the ID of the work skill to be updated
     * @param skillsGroupId the ID of the new skills group
     * @return the updated work skill
     */
    @PutMapping("/{workSkillId}/skills-group/{skillsGroupId}")
    public ResponseEntity<?> updateSkillsGroupForWorkSkill(
            @PathVariable Long workSkillId,
            @PathVariable Long skillsGroupId) {
        try {
            WorkSkillResponseDto responseDto = workSkillsService.updateSkillsGroup(workSkillId, skillsGroupId);
            return ResponseEntity.ok(responseDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    private WorkSkillResponseDto convertToWorkSkillResponseDto(WorkSkill workSkill) {
        WorkSkillResponseDto dto = new WorkSkillResponseDto();
        dto.setId(workSkill.getId());
        dto.setDescription(workSkill.getDescription());
        if (workSkill.getSkillsGroupBySkillsGroupId() != null) {
            dto.setSkillsGroupId(workSkill.getSkillsGroupBySkillsGroupId().getId());
        }
        return dto;
    }
}
