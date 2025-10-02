package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.workskills;

import io.github.alexeyaleksandrov.jacademicsupport.dto.workskills.WorkSkillDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.workskills.WorkSkillResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillsGroupRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillsService;
import lombok.AllArgsConstructor;
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
    public ResponseEntity<List<WorkSkillResponseDto>> getAllWorkSkills() {
        return ResponseEntity.ok(workSkillsService.getAllWorkSkills());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkSkillResponseDto> getWorkSkillById(@PathVariable Long id) {
        return ResponseEntity.ok(workSkillsService.getWorkSkillById(id));
    }

    @PostMapping
    public ResponseEntity<WorkSkillResponseDto> createWorkSkill(@RequestBody WorkSkillDto workSkillDto) {
        return ResponseEntity.ok(workSkillsService.createWorkSkill(workSkillDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkSkillResponseDto> updateWorkSkill(
            @PathVariable Long id,
            @RequestBody WorkSkillDto workSkillDto) {
        return ResponseEntity.ok(workSkillsService.updateWorkSkill(id, workSkillDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkSkill(@PathVariable Long id) {
        workSkillsService.deleteWorkSkill(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/match-to-groups")
    public ResponseEntity<List<WorkSkillResponseDto>> matchSkillsToGroups() {
        List<WorkSkill> workSkills = workSkillsService.matchWorkSkillsToSkillsGroups();
        List<WorkSkillResponseDto> skills = workSkills.stream()
                .map(this::convertToWorkSkillResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(skills);
    }

    /**
     * Update the skills group of a work skill by its ID and the ID of the new skills group.
     * @param workSkillId the ID of the work skill to be updated
     * @param skillsGroupId the ID of the new skills group
     * @return the updated work skill
     */
    @PutMapping("/{workSkillId}/skills-group/{skillsGroupId}")
    public ResponseEntity<WorkSkillResponseDto> updateSkillsGroupForWorkSkill(
            @PathVariable Long workSkillId,
            @PathVariable Long skillsGroupId) {
        WorkSkill workSkill = workSkillRepository.findById(workSkillId).orElseThrow();
        SkillsGroup skillsGroup = skillsGroupRepository.findById(skillsGroupId).orElseThrow();
        workSkill.setSkillsGroupBySkillsGroupId(skillsGroup);
        workSkill = workSkillRepository.saveAndFlush(workSkill);
        return ResponseEntity.ok(convertToWorkSkillResponseDto(workSkill));
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
