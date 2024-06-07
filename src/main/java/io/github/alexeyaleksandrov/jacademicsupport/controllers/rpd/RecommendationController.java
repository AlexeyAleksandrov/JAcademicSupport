package io.github.alexeyaleksandrov.jacademicsupport.controllers.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.crud.CreateRpdDTO;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation.*;
import io.github.alexeyaleksandrov.jacademicsupport.models.*;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.RecommendedSkillRepository;
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
    private final RecommendedSkillRepository recommendedSkillRepository;

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

//    @PostMapping("/rpd/recommendations")
//    public ResponseEntity<Rpd> setRecommendations(@RequestBody RpdRecommendationSkillsDTO skillsDTO) {
//        Rpd rpd = rpdRepository.findById(skillsDTO.getRpdId()).orElseThrow();
//
//        if(!rpd.getRecommendedWorkSkills().isEmpty())
//        {
//            rpd.setRecommendedWorkSkills(new ArrayList<>());
//            rpd = rpdRepository.saveAndFlush(rpd);
//        }
//
//        List<WorkSkill> skills = skillsDTO.getSkills().stream()
//                .map(skill -> workSkillRepository.findById(skill).orElseThrow())
//                .collect(Collectors.toList());
//
//        rpd.setRecommendedWorkSkills(skills);
//        rpd = rpdRepository.saveAndFlush(rpd);
//        return ResponseEntity.ok(rpd);
//    }

    @GetMapping("/rpd/recommendations")
    public ResponseEntity<RpdDto> getRecommendations(@RequestParam(name = "id") Long id) {
        Rpd rpd = rpdRepository.findById(id).orElseThrow();
//        List<WorkSkill> recommendedSkills = new ArrayList<>();  // навыки, которые будут рекомендованы
        List<CompetencyAchievementIndicator> competencyAchievementIndicators = rpd.getCompetencyAchievementIndicators();    // индикаторы в РПД
        Set<Competency> competencies = new HashSet<>(competencyAchievementIndicators.stream()
                .map(CompetencyAchievementIndicator::getCompetencyByCompetencyId)
                .toList());     // получаем список компетенций для РПД
        Set<Keyword> keywords = new HashSet<>();    // сет всех ключевых слов, доступных в данном РПД

        // добавляем ключевые слова из компетенций
        competencies.forEach(competency -> keywords.addAll(competency.getKeywords()));
        // добавляем ключевые слова из индикаторов
        competencyAchievementIndicators.forEach(indicator -> keywords.addAll(indicator.getKeywords()));

        // получаем список технологий, доступных для выборки
        Set<WorkSkill> workSkills = new HashSet<>();
        keywords.forEach(keyword -> workSkills.addAll(keyword.getWorkSkills()));    // добавляем все технологии, приавязанные к данным ключевым словам

        // получаем список доступных для выборки групп технологий
        Set<SkillsGroup> skillsGroups = new HashSet<>();
        workSkills.forEach(workSkill -> skillsGroups.add(workSkill.getSkillsGroupBySkillsGroupId()));

        // сопоставляем компетенции с группами навыков
        List<RpdSkillsGroupForCompetencyIndicatorDTO> indicatorsAndSkillsGroups = new ArrayList<>();
        competencyAchievementIndicators.forEach(indicator -> {
            Set<SkillsGroup> indicatorSkillsGroups = new HashSet<>(); // список подходящих групп технологий
            List<Keyword> indicatorKeywords = indicator.getKeywords();   // ключевые слова идикатора
            indicatorKeywords.forEach(keyword -> {
                List<WorkSkill> indicatorWorkSkills = keyword.getWorkSkills();   // навыки данного ключевого слова
                indicatorWorkSkills.forEach(workSkill -> indicatorSkillsGroups.add(workSkill.getSkillsGroupBySkillsGroupId()));   // добавляем группу технологий навыка
            });
            indicatorsAndSkillsGroups.add(new RpdSkillsGroupForCompetencyIndicatorDTO(indicator, indicatorSkillsGroups.stream().toList()));    // добавляем сопоставление индикатора с группой навыков
        });

        // считаем кол-во закрываемых компетенций группами технологий
        List<SkillsGroupClosedCompetenciesCountDTO> skillsGroupClosedCompetenciesCountDTOS = new ArrayList<>();
        skillsGroups.forEach(skillsGroup ->
                skillsGroupClosedCompetenciesCountDTOS.add(
                        new SkillsGroupClosedCompetenciesCountDTO(skillsGroup,
                                (int) indicatorsAndSkillsGroups.stream()
                                        .filter(rpdSkillsGroupForCompetencyIndicatorDTO -> rpdSkillsGroupForCompetencyIndicatorDTO.getSkillsGroups().contains(skillsGroup))
                                        .count())));     // добавляем в мап группы, для которых считаем, сколько компетенций содержат данную группу

        // считаем весовой коэффициент покрытия компетенций
        List<WeightingCoefficientOfCompetencyCoverageDTO> weightingCoefficientOfCompetencyCoverageDTOS =
                skillsGroupClosedCompetenciesCountDTOS.stream()
                        .map(skillsGroupClosedCompetenciesCountDTO ->
                                new WeightingCoefficientOfCompetencyCoverageDTO(
                                    skillsGroupClosedCompetenciesCountDTO.getSkillsGroup(),
                                    (double)skillsGroupClosedCompetenciesCountDTO.getCount() / (double) competencyAchievementIndicators.size()))
                .sorted(Comparator.comparingDouble(WeightingCoefficientOfCompetencyCoverageDTO::getCoefficient).reversed())
                .toList();

        // считаем коэффициент соответствия группы технологий дисциплине
        List<CoefficientComplianceSkillsGroupWithDisciplineDTO> complianceSkillsGroupWithDisciplineDTOS =
                weightingCoefficientOfCompetencyCoverageDTOS.stream()
                        .map(coefficient ->
                                new CoefficientComplianceSkillsGroupWithDisciplineDTO(
                                        coefficient.getSkillsGroup(),
                                        (coefficient.getCoefficient() + coefficient.getSkillsGroup().getMarketDemand())/2.0))
                        .sorted(Comparator.comparingDouble(CoefficientComplianceSkillsGroupWithDisciplineDTO::getCoefficient).reversed())
                        .toList();

        // считаем коэффициент соответствия технологии дисциплине
        List<CoefficientWorkSkillComplianceWithDisciplineDTO> workSkillComplianceWithDisciplineDTOS = workSkills.stream()
                .map(workSkill -> {
                    CoefficientWorkSkillComplianceWithDisciplineDTO compliance = new CoefficientWorkSkillComplianceWithDisciplineDTO();
                    compliance.setWorkSkill(workSkill);
                    CoefficientComplianceSkillsGroupWithDisciplineDTO groupCoefficient = complianceSkillsGroupWithDisciplineDTOS.stream()
                            .filter(coefficient -> coefficient.getSkillsGroup().equals(workSkill.getSkillsGroupBySkillsGroupId()))
                            .findFirst().orElseThrow();     // получаем коэффициент соответствия группы технологий дисциплине
                    compliance.setCoefficient((groupCoefficient.getCoefficient() + workSkill.getMarketDemand())/2.0);
                    return compliance;
                })
                .sorted(Comparator.comparingDouble(CoefficientWorkSkillComplianceWithDisciplineDTO::getCoefficient).reversed())
                .toList();

        // добавляем результат в РПД
//        Rpd finalRpd = rpd;
        if (!rpd.getRecommendedSkills().isEmpty()) {
            recommendedSkillRepository.deleteAll(rpd.getRecommendedSkills());
//            rpd.setRecommendedSkills(new ArrayList<>());      // очищаем, если не пустой
        }
        List<RecommendedSkill> recommendedSkilslList = workSkillComplianceWithDisciplineDTOS.stream()
                .map(compliance -> {
                    RecommendedSkill recommendedSkill = new RecommendedSkill();
//                    recommendedSkill.setRpd(finalRpd);
                    recommendedSkill.setWorkSkill(compliance.getWorkSkill());
                    recommendedSkill.setCoefficient(compliance.getCoefficient());
                    recommendedSkill = recommendedSkillRepository.save(recommendedSkill);
                    return recommendedSkill;
                })
                .toList();

        List<RecommendedSkill> recommendedSkills = new ArrayList<>(recommendedSkilslList);
//        recommendedSkilslList = recommendedSkillRepository.saveAllAndFlush(recommendedSkilslList);
//        rpd.setKeywordsForIndicatorInContextRpdMap(new HashMap<>());
        rpd.setRecommendedSkills(recommendedSkills);
        rpd = rpdRepository.save(rpd);
//        rpd = rpdRepository.findById(id).orElseThrow();

        RpdDto rpdDto = new RpdDto();
        rpdDto.setDisciplineName(rpd.getDisciplineName());
        rpdDto.setYear(rpd.getYear());
        rpdDto.setRecommendedSkills(recommendedSkills.stream()
                .map(recommendedSkill -> {
                    RecommendedSkillDto recommendedSkillDto = new RecommendedSkillDto();
                    recommendedSkillDto.setCoefficient(recommendedSkill.getCoefficient());
                    recommendedSkillDto.setWorkSkill(new WorkSkillDto(recommendedSkill.getWorkSkill().getDescription()));
                    return recommendedSkillDto;
                })
                .toList());

        return ResponseEntity.ok(rpdDto);
    }
}
