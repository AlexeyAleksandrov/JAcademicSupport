package io.github.alexeyaleksandrov.jacademicsupport.controllers.pages.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.rpd.CreateRpdFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.rpd.EditRpdFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.crud.CreateRpdDTO;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation.RecommendedSkillDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation.RpdDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.RpdRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.RpdService;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.recommendation.RecommendationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
// TODO: отображение рекомендованных навыков, запуск процесса рекомендации
@Controller
@RequestMapping("/rpd")
@AllArgsConstructor
public class RpdController {
    final CompetencyAchievementIndicatorRepository indicatorRepository;
    final RpdRepository rpdRepository;
    final RpdService rpdService;
    final RecommendationService recommendationService;

    @GetMapping("/create")
    public String showCreateRpd(Model model, @RequestParam(name = "createError", required = false, defaultValue = "false") Boolean createError) {
        List<CompetencyAchievementIndicator> indicators = indicatorRepository.findAll();
        model.addAttribute("indicatorList", indicators);
        model.addAttribute("rpdFormDto", new CreateRpdFormDto());
        model.addAttribute("create_error", createError);
        return "pages/rpd/create";
    }

    @PostMapping("/create")
    public String processCreateRpd(CreateRpdFormDto createRpdFormDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/rpd/create?create_error=true";
        }

        if(createRpdFormDto.getDisciplineName().isEmpty() || createRpdFormDto.getYear() < 1900 || createRpdFormDto.getYear() > 2100 || createRpdFormDto.getSelectedIndicators().isEmpty()) {
            return "redirect:/rpd/create?create_error=true";
        }

        // DTO создания РПД
        CreateRpdDTO createRpdDTO = new CreateRpdDTO();
        createRpdDTO.setDiscipline_name(createRpdFormDto.getDisciplineName());
        createRpdDTO.setYear(createRpdFormDto.getYear());
        createRpdDTO.setCompetencyAchievementIndicators(createRpdFormDto.getSelectedIndicators().stream()
                .map(id -> indicatorRepository.findById(id).orElseThrow().getNumber())
                .toList());

        Rpd rpd = rpdService.createRpd(createRpdDTO);     // создаем РПД
        return "redirect:/rpd/show?appended_success=true";
    }

    @GetMapping("/show")
    public String showAllRpd(Model model,
                             @RequestParam(name = "appended_success", required = false, defaultValue = "false") Boolean appendedSuccess,
                             @RequestParam(name = "deleted_success", required = false, defaultValue = "false") Boolean deletedSuccess) {
        List<Rpd> rpds = rpdRepository.findAll();
        model.addAttribute("rpds", rpds);
        model.addAttribute("appended_success", appendedSuccess);
        model.addAttribute("deleted_success", deletedSuccess);
        return "pages/rpd/show";
    }

    @GetMapping("/edit/{id}")
    public String showEditRpd(Model model, @PathVariable("id") Long id,
                              @RequestParam(name = "edit_success", required = false, defaultValue = "false") Boolean editSuccess,
                              @RequestParam(name = "edit_error", required = false, defaultValue = "false") Boolean editError) {
        Rpd rpd = rpdRepository.findById(id).orElseThrow();
        model.addAttribute("rpd", rpd);

        List<CompetencyAchievementIndicator> indicators = indicatorRepository.findAll();
        model.addAttribute("indicatorList", indicators);

        EditRpdFormDto editRpdFormDto = new EditRpdFormDto();
        editRpdFormDto.setDisciplineName(rpd.getDisciplineName());
        editRpdFormDto.setYear(rpd.getYear());
        editRpdFormDto.setSelectedIndicators(indicators.stream()
                .filter(indicator -> rpd.getCompetencyAchievementIndicators().contains(indicator))
                .map(CompetencyAchievementIndicator::getId)
                .toList());
        model.addAttribute("editRpdFormDto", editRpdFormDto);
        model.addAttribute("edit_success", editSuccess);
        model.addAttribute("edit_error", editError);
        return "pages/rpd/edit";
    }

    @PostMapping("/edit/{id}")
    public String processEditRpd(@PathVariable("id") Long id, EditRpdFormDto editRpdFormDto, BindingResult bindingResult) {
        Rpd rpd = rpdRepository.findById(id).orElseThrow();

        if (bindingResult.hasErrors()) {
            return "redirect:/rpd/edit" + rpd.getId() + "?edit_error=true";
        }

        if(editRpdFormDto.getDisciplineName().isEmpty() || editRpdFormDto.getYear() < 1900 || editRpdFormDto.getYear() > 2100 || editRpdFormDto.getSelectedIndicators().isEmpty()) {
            return "redirect:/rpd/edit" + rpd.getId() + "?edit_error=true";
        }

        List<CompetencyAchievementIndicator> indicators = indicatorRepository.findAll();

        rpd.setDisciplineName(editRpdFormDto.getDisciplineName());
        rpd.setYear(editRpdFormDto.getYear());
        rpd.setCompetencyAchievementIndicators(new ArrayList<>(indicators.stream()
                .filter(indicator -> editRpdFormDto.getSelectedIndicators().contains(indicator.getId()))
                .toList()));

        rpdRepository.saveAndFlush(rpd);
        return "redirect:/rpd/edit/" + rpd.getId() + "?edit_success=true";
    }

    @PostMapping("/delete/{id}")
    public String processDeleteRpdPage( @PathVariable("id") Long id) {
        rpdRepository.deleteById(id);
        return "redirect:/rpd/show?deleted_success=true";
    }

    @GetMapping("/recommendations/{id}")
    public String showRecommendationsPage(Model model, @PathVariable("id") Long id) {
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

        model.addAttribute("rpd_dto", rpdDto);
        return "pages/rpd/recommendation";
    }
}
