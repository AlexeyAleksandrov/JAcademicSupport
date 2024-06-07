package io.github.alexeyaleksandrov.jacademicsupport.controllers.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation.RpdRecommendationSkillsDTO;
import io.github.alexeyaleksandrov.jacademicsupport.models.*;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation.CreateRpdDTO;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.RpdRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.recommendation.RecommendationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;
    private final RpdRepository rpdRepository;
    private final CompetencyAchievementIndicatorRepository indicatorRepository;
    private final WorkSkillRepository workSkillRepository;

    @PostMapping("/rpd/create")
    public ResponseEntity<Rpd> createRpd(@RequestBody CreateRpdDTO createRpdDTO) {
        Rpd rpd = new Rpd();
        rpd.setDisciplineName(createRpdDTO.getDiscipline_name());
        rpd.setYear(createRpdDTO.getYear());
        rpd.setCompetencyAchievementIndicators(
                createRpdDTO.getCompetencyAchievementIndicators().stream()
                        .map(indicatorRepository::findByNumber)
                        .toList()
        );
        rpd = rpdRepository.saveAndFlush(rpd);
        return ResponseEntity.ok(rpd);
    }

    @PostMapping("/rpd/recommendations")
    public ResponseEntity<Rpd> setRecommendations(@RequestBody RpdRecommendationSkillsDTO skillsDTO) {
        Rpd rpd = rpdRepository.findById(skillsDTO.getRpdId()).orElseThrow();

        if(!rpd.getRecommendedWorkSkills().isEmpty())
        {
            rpd.setRecommendedWorkSkills(new ArrayList<>());
            rpd = rpdRepository.saveAndFlush(rpd);
        }

        List<WorkSkill> skills = skillsDTO.getSkills().stream()
                .map(skill -> workSkillRepository.findById(skill).orElseThrow())
                .collect(Collectors.toList());

        rpd.setRecommendedWorkSkills(skills);
        rpd = rpdRepository.saveAndFlush(rpd);
        return ResponseEntity.ok(rpd);
    }

    @GetMapping("/rpd/recommendations")
    public ResponseEntity<Map<CompetencyAchievementIndicator, List<SkillsGroup>>> getRecommendations(@RequestParam(name = "id") Long id) {
        Rpd rpd = rpdRepository.findById(id).orElseThrow();
        List<WorkSkill> recommendedSkills = new ArrayList<>();  // навыки, которые будут рекомендованы
        List<CompetencyAchievementIndicator> competencyAchievementIndicators = rpd.getCompetencyAchievementIndicators();    // индикаторы в РПД
        Set<Competency> competencies = new HashSet<>(competencyAchievementIndicators.stream()
                .map(CompetencyAchievementIndicator::getCompetencyByCompetencyId)
                .toList());     // получаем список компетенций для РПД
//        Set<Keyword> keywords = new HashSet<>();    // сет всех ключевых слов, доступных в данном РПД

//        // добавляем ключевые слова из компетенций
//        competencies.forEach(competency -> keywords.addAll(competency.getKeywords()));
//        // добавляем ключевые слова из индикаторов
//        competencyAchievementIndicators.forEach(indicator -> keywords.addAll(indicator.getKeywords()));
//
//        // получаем список технологий, доступных для выборки
//        Set<WorkSkill> workSkills = new HashSet<>();
//        keywords.forEach(keyword -> workSkills.addAll(keyword.getWorkSkills()));    // добавляем все технологии, приавязанные к данным ключевым словам
//
//        // получаем список доступных для выборки групп технологий
//        Set<SkillsGroup> skillsGroups = new HashSet<>();
//        workSkills.forEach(workSkill -> skillsGroups.add(workSkill.getSkillsGroupBySkillsGroupId()));

        // сопоставляем компетенции с группами навыков
        Map<CompetencyAchievementIndicator, List<SkillsGroup>> indicatorsAndSkillsGroupsMap = new HashMap<>();
        competencyAchievementIndicators.forEach(indicator -> {
            Set<SkillsGroup> skillsGroups = new HashSet<>(); // список подходящих групп технологий
            List<Keyword> keywords = indicator.getKeywords();   // ключевые слова идикатора
            keywords.forEach(keyword -> {
                List<WorkSkill> workSkills = keyword.getWorkSkills();   // навыки данного ключевого слова
                workSkills.forEach(workSkill -> skillsGroups.add(workSkill.getSkillsGroupBySkillsGroupId()));   // добавляем группу технологий навыка
            });
            indicatorsAndSkillsGroupsMap.put(indicator, skillsGroups.stream().toList());    // добавляем сопоставление индикатора с группой навыков
        });

        return ResponseEntity.ok(indicatorsAndSkillsGroupsMap);
    }
}
