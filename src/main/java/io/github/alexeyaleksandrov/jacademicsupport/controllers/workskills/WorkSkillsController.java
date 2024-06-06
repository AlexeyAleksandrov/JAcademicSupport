package io.github.alexeyaleksandrov.jacademicsupport.controllers.workskills;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillsGroupRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.VacancyEntityRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/workskillsgroups")
@AllArgsConstructor
public class WorkSkillsController {
    final SkillsGroupRepository skillsGroupRepository;
    final WorkSkillRepository workSkillRepository;
    final OllamaService ollamaService;
    final VacancyEntityRepository vacancyEntityRepository;

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
        List<WorkSkill> skills = workSkillRepository.findAll();
        List<SkillsGroup> skillsGroups = skillsGroupRepository.findAll();

        String allSkillsPrompt = skillsGroups.stream()
                .map(SkillsGroup::getDescription)
                .toList()
                .stream()
                .collect(Collectors.joining(", ", "[", "]"));   // собираем данные в массив вида ["элемент1", "элемент2", "элемент3"]

        // выполняем cопоставление
        skills = skills.stream()
                .peek(workSkill -> {
                    String prompt = "У тебя есть профессиональный навык " + workSkill.getDescription() + ", к какой группе навыков из списка его можно отнести? Список группы навыков: " + allSkillsPrompt + ". Ответ должен содержать только название группы";
                    String answer = ollamaService.chat(prompt);     // получаем рекмендацию от языковой модели по группе технологий
                    SkillsGroup skillsGroup = skillsGroups.stream()
                            .filter(group -> answer.contains(group.getDescription()))
                            .findFirst()
                            .orElse(skillsGroupRepository.findByDescription("NO_GROUP"));   // если не найдено задаём дефолтную группу
                    workSkill.setSkillsGroupBySkillsGroupId(skillsGroup);   // устанавливаем группу для навыка
                    System.out.println("Для навыка " + workSkill.getDescription() + " установлена группа " + skillsGroup.getDescription());
                })
                .toList();

        workSkillRepository.saveAllAndFlush(skills);
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
        List<SkillsGroup> skillsGroups = skillsGroupRepository.findAll();
        List<VacancyEntity> vacancies = vacancyEntityRepository.findAll();

        skillsGroups.forEach(skillsGroup -> {
            List<WorkSkill> workSkills = workSkillRepository.findBySkillsGroupBySkillsGroupId(skillsGroup);     // получаем список навыков из этой группы
                long groupDemand = vacancies.stream()
                        .filter(vacancyEntity -> vacancyEntity.getSkills().stream()
                                .anyMatch(workSkills::contains))
                        .count();   // фильтруем вакансии, в которых есть нужные навыки
                skillsGroup.setMarketDemand((double)groupDemand/(double)vacancies.size());
                skillsGroupRepository.saveAndFlush(skillsGroup);
            });

        return ResponseEntity.ok(skillsGroups);
    }
}
