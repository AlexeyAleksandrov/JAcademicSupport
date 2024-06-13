package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.models.Competency;
import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import io.github.alexeyaleksandrov.jacademicsupport.models.Keyword;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.KeywordRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.competency.IndicatorsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/competency/achievement/indicator")
public class IndicatorController {
    private final CompetencyRepository competencyRepository;
    private final CompetencyAchievementIndicatorRepository indicatorRepository;
    private final IndicatorsService indicatorsService;

    @GetMapping("/create")
    public ResponseEntity<CompetencyAchievementIndicator> createCompetencyAchievementIndicator(@RequestParam(name = "number") String number,
                                                                                               @RequestParam("description") String description,
                                                                                               @RequestParam("indicator_know") String indicatorKnow,
                                                                                               @RequestParam("indicator_able") String indicatorAble,
                                                                                               @RequestParam("indicator_possess") String indicatorPossess,
                                                                                               @RequestParam("competency_number") String competencyNumber) {
        CompetencyAchievementIndicator indicator = new CompetencyAchievementIndicator();
        indicator.setNumber(number);
        indicator.setDescription(description);
        indicator.setIndicatorKnow(indicatorKnow);
        indicator.setIndicatorAble(indicatorAble);
        indicator.setIndicatorPossess(indicatorPossess);
        indicator.setCompetencyByCompetencyId(competencyRepository.findByNumber(competencyNumber));
        indicator = indicatorRepository.saveAndFlush(indicator);
        return ResponseEntity.ok(indicator);
    }

    @GetMapping("/create/keywords")
    public ResponseEntity<CompetencyAchievementIndicator> createKeywordsForCompetencyAchievementIndicator(@RequestParam(name = "number") String number) {
        CompetencyAchievementIndicator indicator = indicatorRepository.findByNumber(number);
        indicator = indicatorsService.createKeywordsForCompetencyIndicator(indicator);
        return ResponseEntity.ok(indicator);
    }


}
