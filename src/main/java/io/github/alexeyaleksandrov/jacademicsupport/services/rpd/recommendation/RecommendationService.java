package io.github.alexeyaleksandrov.jacademicsupport.services.rpd.recommendation;

import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation.*;
import io.github.alexeyaleksandrov.jacademicsupport.models.*;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class RecommendationService {
    private RpdRepository rpdRepository;
    private final RecommendedSkillRepository recommendedSkillRepository;

    /**
     * Формирует рекомендации по технологиям (навыкам) для рабочей программы дисциплины (РПД).
     * 
     * Метод анализирует индикаторы достижения компетенций РПД и на их основе рассчитывает коэффициенты
     * соответствия различных технологий данной дисциплине. Алгоритм работы:
     * 
     * 1. Извлекает компетенции и их индикаторы из РПД
     * 2. Собирает все ключевые слова, связанные с компетенциями и индикаторами
     * 3. Получает навыки (WorkSkill) и группы навыков (SkillsGroup), связанные с ключевыми словами
     * 4. Сопоставляет индикаторы достижения компетенций с группами навыков
     * 5. Рассчитывает количество индикаторов, покрываемых каждой группой навыков
     * 6. Вычисляет весовой коэффициент покрытия компетенций для каждой группы навыков
     * 7. Рассчитывает коэффициент соответствия группы навыков дисциплине на основе покрытия компетенций и рыночного спроса
     * 8. Вычисляет коэффициент соответствия каждой технологии дисциплине с учетом коэффициента её группы и рыночного спроса
     * 9. Сохраняет рекомендованные навыки в РПД, отсортированные по убыванию коэффициента соответствия
     * 
     * Перед сохранением новых рекомендаций метод удаляет все существующие рекомендации для данной РПД.
     * 
     * @param rpd Рабочая программа дисциплины, для которой необходимо сформировать рекомендации
     * @return Обновленная РПД с сохраненными рекомендованными навыками
     */
    public Rpd getRecomendationsForRpd(Rpd rpd) {
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

        return rpd;
    }
}
