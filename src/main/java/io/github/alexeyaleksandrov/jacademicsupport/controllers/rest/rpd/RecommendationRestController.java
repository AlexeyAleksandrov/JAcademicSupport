package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.rpd.CreateRpdFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.rpd.EditRpdFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.crud.CreateRpdDTO;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation.*;
import io.github.alexeyaleksandrov.jacademicsupport.models.*;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.RecommendedSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.RpdRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.RpdService;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.recommendation.RecommendationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/api/rpd")
public class RecommendationRestController {
    private final RecommendationService recommendationService;
    private final RpdRepository rpdRepository;
    private final CompetencyAchievementIndicatorRepository indicatorRepository;
    private final WorkSkillRepository workSkillRepository;
    private final RecommendedSkillRepository recommendedSkillRepository;
    final RpdService rpdService;

    @PostMapping("/create")
    public ResponseEntity<Rpd> createRpd(@RequestBody CreateRpdDTO createRpdDTO) {
        Rpd rpd = rpdService.createRpd(createRpdDTO);
        return ResponseEntity.ok(rpd);
    }

    @GetMapping("/show")
    public ResponseEntity<List<Rpd>> showAllRpd() {
        return ResponseEntity.ok(rpdRepository.findAll());
    }

    @PostMapping("/edit/{id}")
    public ResponseEntity<EditRpdFormDto> processEditRpd(@PathVariable("id") Long id,  @RequestBody EditRpdFormDto editRpdFormDto) {
        Rpd rpd = rpdRepository.findById(id).orElseThrow();

        if(editRpdFormDto.getDisciplineName().isEmpty() || editRpdFormDto.getYear() < 1900 || editRpdFormDto.getYear() > 2100 || editRpdFormDto.getSelectedIndicators().isEmpty()) {
            return ResponseEntity.badRequest().body(editRpdFormDto);
        }

        List<CompetencyAchievementIndicator> indicators = indicatorRepository.findAll();

        rpd.setDisciplineName(editRpdFormDto.getDisciplineName());
        rpd.setYear(editRpdFormDto.getYear());
        rpd.setCompetencyAchievementIndicators(new ArrayList<>(indicators.stream()
                .filter(indicator -> editRpdFormDto.getSelectedIndicators().contains(indicator.getId()))
                .toList()));

        rpdRepository.saveAndFlush(rpd);
        return ResponseEntity.ok(editRpdFormDto);
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Long> processDeleteRpdPage( @PathVariable("id") Long id) {
        rpdRepository.deleteById(id);
        return ResponseEntity.ok(id);
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

    @GetMapping("/recommendations")
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
