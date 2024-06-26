package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.keywords;

import io.github.alexeyaleksandrov.jacademicsupport.models.Keyword;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.KeywordRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillsGroupRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/keywords")
@AllArgsConstructor
public class KeywordsRestController {
    final KeywordRepository keywordRepository;
    final SkillsGroupRepository skillsGroupRepository;
    final WorkSkillRepository workSkillRepository;
    final OllamaService ollamaService;
    final WorkSkillsService workSkillsService;

    @PostMapping("/match/all")
    public ResponseEntity<List<Keyword>> matchKeywordsToWorkSkills() {
        return ResponseEntity.ok(workSkillsService.matchKeywordsToWorkSkills());
//        keywords.forEach(keyword -> {
//                    String prompt = "У тебя есть словосочетание " + keyword.getKeyword() + ", к какой группе навыков из списка его можно отнести? Список группы навыков: " + allSkillsPrompt + ". Ответ должен содержать только название группы";
//                    String answer = ollamaService.chat(prompt);     // получаем рекмендацию от языковой модели по группе технологий
//                    SkillsGroup skillsGroup = skillsGroups.stream()
//                            .filter(group -> answer.contains(group.getDescription()))
//                            .findFirst()
//                            .orElse(skillsGroupRepository.findByDescription("NO_GROUP"));   // если не найдено задаём дефолтную группу
//                    List<SkillsGroup> groups = new ArrayList<>();
//                    groups.add(skillsGroup);
//                    keyword.setSkillsGroups(groups);   // устанавливаем группу для ключевого слова
//                    keywordRepository.saveAndFlush(keyword);
//                    System.out.println("Для ключевого слова " + keyword.getKeyword() + " установлена группа " + skillsGroup.getDescription());
//                });
//
//        return ResponseEntity.ok(keywords);
    }
}
