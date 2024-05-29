package io.github.alexeyaleksandrov.jacademicsupport.services.recommendation;

import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
//        List<VacancyEntity> vacancyEntities = vacancyEntityRepository.findAll();    // получаем все доступные вакансии
//
//    }
}
