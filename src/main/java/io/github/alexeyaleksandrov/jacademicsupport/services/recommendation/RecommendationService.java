package io.github.alexeyaleksandrov.jacademicsupport.services.recommendation;

import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class RecommendationService {
    private RpdRepository rpdRepository;
    private CompetencyRepository competencyRepository;
    private CompetencyAchievementIndicatorRepository competencyAchievementIndicatorRepository;
    private KeywordRepository keywordRepository;
    private VacancyEntityRepository vacancyEntityRepository;
    private WorkSkillRepository workSkillRepository;
    private SkillsGroupRepository skillsGroupRepository;

//    public Rpd getRecomendationsForRpd(Rpd inputRpd) {
//        List<WorkSkill> workSkills = workSkillRepository.findAll();     // получаем все имеющиеся навыки
//        List<SkillsGroup> skillsGroups = skillsGroupRepository.findAll();
//
//    }
}
