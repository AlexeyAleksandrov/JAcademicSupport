package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.rpd.EditRpdFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.crud.CreateRpdDTO;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation.*;
import io.github.alexeyaleksandrov.jacademicsupport.models.*;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.RpdRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.RpdService;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.recommendation.RecommendationService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/rpd")
public class RpdRestController {
    private final RecommendationService recommendationService;
    private final RpdRepository rpdRepository;
    private final CompetencyAchievementIndicatorRepository indicatorRepository;
    final RpdService rpdService;

    @PostMapping
    public ResponseEntity<Rpd> createRpd(@RequestBody CreateRpdDTO createRpdDTO) {
        Rpd rpd = rpdService.createRpd(createRpdDTO);
        return ResponseEntity.ok(rpd);
    }

    @GetMapping
    public ResponseEntity<List<Rpd>> getAllRpd() {
        return ResponseEntity.ok(rpdRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rpd> getRpdById(@PathVariable Long id) {
        Rpd rpd = rpdRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(rpd);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EditRpdFormDto> updateRpd(@PathVariable("id") Long id, @RequestBody EditRpdFormDto editRpdFormDto) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRpd(@PathVariable("id") Long id) {
        rpdRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<RpdDto> getRecommendations(@PathVariable("id") Long id) {
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
