package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.workskills;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillsGroupRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.VacancyEntityRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.SkillsGroupsService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/skills-groups")
@AllArgsConstructor
public class SkillsGroupRestController {
    final SkillsGroupRepository skillsGroupRepository;
    final WorkSkillRepository workSkillRepository;
    final OllamaService ollamaService;
    final VacancyEntityRepository vacancyEntityRepository;
    final WorkSkillsService workSkillsService;
    final SkillsGroupsService skillsGroupsService;

    @PostMapping
    public ResponseEntity<SkillsGroup> createSkillsGroup(@RequestParam(name = "name") String name) {
        SkillsGroup skillsGroup = new SkillsGroup();
        skillsGroup.setDescription(name);   // задаем название группы
        skillsGroup.setMarketDemand(-1.0);
        skillsGroup = skillsGroupRepository.saveAndFlush(skillsGroup);
        return ResponseEntity.ok(skillsGroup);
    }

    @GetMapping
    public ResponseEntity<List<SkillsGroup>> getAllSkillsGroups() {
        return ResponseEntity.ok(skillsGroupRepository.findAll());
    }
    
    @GetMapping("/{id}/work-skills")
    public ResponseEntity<List<WorkSkill>> getWorkSkillsBySkillsGroupId(@PathVariable Long id) {
        Optional<SkillsGroup> skillsGroupOptional = skillsGroupRepository.findById(id);
        if (skillsGroupOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        SkillsGroup skillsGroup = skillsGroupOptional.get();
        List<WorkSkill> workSkills = workSkillRepository.findBySkillsGroupBySkillsGroupId(skillsGroup);
        return ResponseEntity.ok(workSkills);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillsGroup> getSkillsGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(skillsGroupRepository.findById(id).orElseThrow());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SkillsGroup> updateSkillsGroup(@PathVariable Long id, @RequestBody SkillsGroup skillsGroup) {
        skillsGroup.setId(id);
        return ResponseEntity.ok(skillsGroupRepository.saveAndFlush(skillsGroup));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkillsGroup(@PathVariable Long id) {
        skillsGroupRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/match-to-work-skills")
    public ResponseEntity<List<WorkSkill>> matchSkillsToGroups(
            @RequestParam(value = "llmProvider", defaultValue = "gigachat") String llmProvider) {
        List<WorkSkill> skills = workSkillsService.matchWorkSkillsToSkillsGroups(llmProvider);
        return ResponseEntity.ok(skills);
    }

    @PutMapping("/update-market-demand")
    public ResponseEntity<List<SkillsGroup>> updateSkillsGroupsMarketDemand() {
        List<SkillsGroup> updatedGroups = skillsGroupsService.updateSkillsGroupsMarketDemand();
        return ResponseEntity.ok(updatedGroups);
    }

//    @PutMapping("/work-skills/{workSkillId}/skills-group/{skillsGroupId}")
//    public ResponseEntity<WorkSkill> updateSkillsGroupForWorkSkill(@PathVariable Long workSkillId,
//                                                                  @PathVariable Long skillsGroupId) {
//        WorkSkill workSkill = workSkillRepository.findById(workSkillId).orElseThrow();
//        SkillsGroup skillsGroup = skillsGroupRepository.findById(skillsGroupId).orElseThrow();
//        workSkill.setSkillsGroupBySkillsGroupId(skillsGroup);
//        workSkill = workSkillRepository.saveAndFlush(workSkill);
//        return ResponseEntity.ok(workSkill);
//    }

//    @GetMapping("/work-skills")
//    public ResponseEntity<List<WorkSkill>> getAllWorkSkills() {
//        return ResponseEntity.ok(workSkillRepository.findAll());
//    }
}
