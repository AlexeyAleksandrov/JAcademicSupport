package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.workskills;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillsGroupRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-skills")
@AllArgsConstructor
public class WorkSkillsRestController {
    private final WorkSkillRepository workSkillRepository;
    private final WorkSkillsService workSkillsService;
    final SkillsGroupRepository skillsGroupRepository;

    @GetMapping
    public ResponseEntity<List<WorkSkill>> getAllWorkSkills() {
        return ResponseEntity.ok(workSkillRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkSkill> getWorkSkillById(@PathVariable Long id) {
        return workSkillRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WorkSkill> createWorkSkill(@RequestBody WorkSkill workSkill) {
        WorkSkill savedSkill = workSkillRepository.save(workSkill);
        return ResponseEntity.ok(savedSkill);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkSkill> updateWorkSkill(
            @PathVariable Long id,
            @RequestBody WorkSkill workSkillDetails) {
        return workSkillRepository.findById(id)
                .map(existingSkill -> {
                    workSkillDetails.setId(id);
                    return ResponseEntity.ok(workSkillRepository.save(workSkillDetails));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkSkill(@PathVariable Long id) {
        return workSkillRepository.findById(id)
                .map(workSkill -> {
                    workSkillRepository.delete(workSkill);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/match-to-groups")
    public ResponseEntity<List<WorkSkill>> matchSkillsToGroups() {
        List<WorkSkill> skills = workSkillsService.matchWorkSkillsToSkillsGroups();
        return ResponseEntity.ok(skills);
    }

    /**
     * Update the skills group of a work skill by its ID and the ID of the new skills group.
     * @param workSkillId the ID of the work skill to be updated
     * @param skillsGroupId the ID of the new skills group
     * @return the updated work skill
     */
    @PutMapping("/{workSkillId}/skills-group/{skillsGroupId}")
    public ResponseEntity<WorkSkill> updateSkillsGroupForWorkSkill(
            @PathVariable Long workSkillId,
            @PathVariable Long skillsGroupId) {
        WorkSkill workSkill = workSkillRepository.findById(workSkillId).orElseThrow();
        SkillsGroup skillsGroup = skillsGroupRepository.findById(skillsGroupId).orElseThrow();
        workSkill.setSkillsGroupBySkillsGroupId(skillsGroup);
        workSkill = workSkillRepository.saveAndFlush(workSkill);
        return ResponseEntity.ok(workSkill);
    }
}
