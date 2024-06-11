package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.rpd;

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

        rpd = recommendationService.getRecomendationsForRpd(rpd);

        RpdDto rpdDto = new RpdDto();
        rpdDto.setDisciplineName(rpd.getDisciplineName());
        rpdDto.setYear(rpd.getYear());
        rpdDto.setRecommendedSkills(rpd.getRecommendedSkills().stream()
                .map(recommendedSkill -> {
                    RecommendedSkillDto recommendedSkillDto = new RecommendedSkillDto();

                    recommendedSkillDto.setCoefficient(Math.round(recommendedSkill.getCoefficient() * 1000.0) / 1000.0);
                    recommendedSkillDto.setDescription(recommendedSkill.getWorkSkill().getDescription());
                    return recommendedSkillDto;
                })
                .toList());

        return ResponseEntity.ok(rpdDto);
    }
}
