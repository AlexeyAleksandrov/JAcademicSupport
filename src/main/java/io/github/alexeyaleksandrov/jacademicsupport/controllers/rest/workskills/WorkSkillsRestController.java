package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.workskills;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillsGroupRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.VacancyEntityRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.SkillsGroupsService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/workskillsgroups")
@AllArgsConstructor
public class WorkSkillsRestController {
    final SkillsGroupRepository skillsGroupRepository;
    final WorkSkillRepository workSkillRepository;
    final OllamaService ollamaService;
    final VacancyEntityRepository vacancyEntityRepository;
    final WorkSkillsService workSkillsService;
    final SkillsGroupsService skillsGroupsService;

    @GetMapping("/create")
    public ResponseEntity<SkillsGroup> createSkillsGroup(@RequestParam(name = "name") String name) {
        SkillsGroup skillsGroup = new SkillsGroup();
        skillsGroup.setDescription(name);   // задаем название группы
        skillsGroup.setMarketDemand(-1.0);
        skillsGroup = skillsGroupRepository.saveAndFlush(skillsGroup);
        return ResponseEntity.ok(skillsGroup);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SkillsGroup>> getAllSkillsGroups(){
        return ResponseEntity.ok(skillsGroupRepository.findAll());
    }

    @GetMapping("/match/all")
    public ResponseEntity<List<WorkSkill>> matchSkillsToGroups() {
        List<WorkSkill> skills = workSkillsService.matchWorkSkillsToSkillsGroups();
        return ResponseEntity.ok(skills);
    }

    @PostMapping("/update/workskill/{workSkillId}/skillsgroup/{skillsGroupId}")
    public ResponseEntity<WorkSkill> updateSkillsGroupForWorkSkill(@PathVariable Long workSkillId,
                                                                   @PathVariable Long skillsGroupId) {
        WorkSkill workSkill = workSkillRepository.findById(workSkillId).orElseThrow();
        SkillsGroup skillsGroup = skillsGroupRepository.findById(skillsGroupId).orElseThrow();
        workSkill.setSkillsGroupBySkillsGroupId(skillsGroup);
        workSkill = workSkillRepository.saveAndFlush(workSkill);
        return ResponseEntity.ok(workSkill);
    }

    @GetMapping("/workskills/all")
    public ResponseEntity<List<WorkSkill>> getAllWorkSkills() {
        return ResponseEntity.ok(workSkillRepository.findAll());
    }

    @PostMapping("/updateSkillsGroupsMarketDemand")
    public ResponseEntity<List<SkillsGroup>> updateSkillsGroupsMarketDemand() {
//        List<SkillsGroup> skillsGroups = skillsGroupRepository.findAll();
//        List<VacancyEntity> vacancies = vacancyEntityRepository.findAll();
//
//        skillsGroups.forEach(skillsGroup -> {
//            List<WorkSkill> workSkills = workSkillRepository.findBySkillsGroupBySkillsGroupId(skillsGroup);     // получаем список навыков из этой группы
//                long groupDemand = vacancies.stream()
//                        .filter(vacancyEntity -> vacancyEntity.getSkills().stream()
//                                .anyMatch(workSkills::contains))
//                        .count();   // фильтруем вакансии, в которых есть нужные навыки
//                skillsGroup.setMarketDemand((double)groupDemand/(double)vacancies.size());
//                skillsGroupRepository.saveAndFlush(skillsGroup);
//            });

        return ResponseEntity.ok(skillsGroupsService.updateSkillsGroupsMarketDemand());
    }
}
