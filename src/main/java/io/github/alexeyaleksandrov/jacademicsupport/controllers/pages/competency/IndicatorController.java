package io.github.alexeyaleksandrov.jacademicsupport.controllers.pages.competency;

import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.competency.EditCompetenceForm;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.indicators.CreateIndicatorFrom;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.indicators.EditIndicatorForm;
import io.github.alexeyaleksandrov.jacademicsupport.models.Competency;
import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import io.github.alexeyaleksandrov.jacademicsupport.models.Keyword;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.KeywordRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.competency.CompetencyService;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.competency.IndicatorsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/indicators")
@AllArgsConstructor
public class IndicatorController {
    final CompetencyAchievementIndicatorRepository indicatorRepository;
    final CompetencyRepository competencyRepository;
    final KeywordRepository keywordRepository;
    final CompetencyService competencyService;
    final IndicatorsService indicatorsService;

    @GetMapping("/create")
    public String showCreateIndicator(Model model, @RequestParam(name = "createError", required = false, defaultValue = "false") Boolean createError) {

        model.addAttribute("indicatorForm", new CreateIndicatorFrom());
        model.addAttribute("create_error", createError);
        return "pages/indicators/create";
    }

    @PostMapping("/create")
    public String processCreateIndicator(CreateIndicatorFrom createIndicatorFrom, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/indicators/create?create_error=true";
        }

        if(createIndicatorFrom.getNumber().isEmpty()
                || createIndicatorFrom.getDescription().isEmpty()
                || createIndicatorFrom.getIndicatorKnow().isEmpty()
                || createIndicatorFrom.getIndicatorAble().isEmpty()
                || createIndicatorFrom.getIndicatorPossess().isEmpty()
                || createIndicatorFrom.getCompetencyId() == null
                || indicatorRepository.existsByNumber(createIndicatorFrom.getNumber())) {
            return "redirect:/indicators/create?create_error=true";
        }

        // создаём индикатор
        CompetencyAchievementIndicator indicator = new CompetencyAchievementIndicator();
        indicator.setNumber(createIndicatorFrom.getNumber());
        indicator.setDescription(createIndicatorFrom.getDescription());
        indicator.setIndicatorKnow(createIndicatorFrom.getIndicatorKnow());
        indicator.setIndicatorAble(createIndicatorFrom.getIndicatorAble());
        indicator.setIndicatorPossess(createIndicatorFrom.getIndicatorPossess());
        indicator.setCompetencyByCompetencyId(competencyRepository.findById(createIndicatorFrom.getCompetencyId()).orElseThrow());
        indicator = indicatorRepository.saveAndFlush(indicator);
        return "redirect:/indicators/show?appended_success=true";
    }

    @GetMapping("/show")
    public String showAllIndicator(Model model,
                             @RequestParam(name = "appended_success", required = false, defaultValue = "false") Boolean appendedSuccess,
                             @RequestParam(name = "deleted_success", required = false, defaultValue = "false") Boolean deletedSuccess) {
        List<CompetencyAchievementIndicator> indicators = indicatorRepository.findAll();
        model.addAttribute("indicators", indicators);
        model.addAttribute("appended_success", appendedSuccess);
        model.addAttribute("deleted_success", deletedSuccess);
        return "pages/indicators/show";
    }

    @GetMapping("/edit/{id}")
    public String showEditIndicator(Model model, @PathVariable("id") Long id,
                              @RequestParam(name = "edit_success", required = false, defaultValue = "false") Boolean editSuccess,
                              @RequestParam(name = "edit_error", required = false, defaultValue = "false") Boolean editError) {
        List<Competency> competencies = competencyRepository.findAll();
        model.addAttribute("competencies", competencies);

        CompetencyAchievementIndicator indicator = indicatorRepository.findById(id).orElseThrow();
        model.addAttribute("indicator", indicator);

        List<Keyword> keywords = keywordRepository.findAll();
        model.addAttribute("keywords", keywords);

        // создаем DTO формы
        EditIndicatorForm editIndicatorForm = new EditIndicatorForm();
        editIndicatorForm.setNumber(indicator.getNumber());
        editIndicatorForm.setDescription(indicator.getDescription());
        editIndicatorForm.setIndicatorKnow(indicator.getIndicatorKnow());
        editIndicatorForm.setIndicatorAble(indicator.getIndicatorAble());
        editIndicatorForm.setIndicatorPossess(indicator.getIndicatorPossess());
        editIndicatorForm.setCompetencyId(indicator.getCompetencyByCompetencyId().getId());
        editIndicatorForm.setSelectedKeywords(keywords.stream()
                .filter(c -> indicator.getKeywords().contains(c))
                .map(Keyword::getId)
                .toList());
        model.addAttribute("editIndicatorForm", editIndicatorForm);

        model.addAttribute("edit_success", editSuccess);
        model.addAttribute("edit_error", editError);
        return "pages/indicators/edit";
    }

    @PostMapping("/edit/{id}")
    public String processEditIndicator(@PathVariable("id") Long id, EditIndicatorForm editIndicatorForm, BindingResult bindingResult) {
        CompetencyAchievementIndicator indicator = indicatorRepository.findById(id).orElseThrow();

        if (bindingResult.hasErrors()) {
            return "redirect:/indicators/edit" + indicator.getId() + "?edit_error=true";
        }

        if(editIndicatorForm.getNumber().isEmpty()
                || editIndicatorForm.getDescription().isEmpty()
                || editIndicatorForm.getIndicatorKnow().isEmpty()
                || editIndicatorForm.getIndicatorAble().isEmpty()
                || editIndicatorForm.getIndicatorPossess().isEmpty()
                || editIndicatorForm.getCompetencyId() == null
                || indicatorRepository.existsByNumber(editIndicatorForm.getNumber())) {
            return "redirect:/indicators/edit" + indicator.getId() + "?edit_error=true";
        }

        indicator.setNumber(editIndicatorForm.getNumber());
        indicator.setDescription(editIndicatorForm.getDescription());
        indicator.setIndicatorKnow(editIndicatorForm.getIndicatorKnow());
        indicator.setIndicatorAble(editIndicatorForm.getIndicatorAble());
        indicator.setIndicatorPossess(editIndicatorForm.getIndicatorPossess());
        indicator.setCompetencyByCompetencyId(competencyRepository.findById(editIndicatorForm.getCompetencyId()).orElseThrow());
        indicator.setKeywords(new ArrayList<>(keywordRepository.findAll().stream()
                .filter(keyword -> editIndicatorForm.getSelectedKeywords().contains(keyword.getId()))
                .toList()));

        indicator = indicatorRepository.saveAndFlush(indicator);

        return "redirect:/indicators/edit/" + indicator.getId() + "?edit_success=true";
    }

    @PostMapping("/delete/{id}")
    public String processDeleteIndicator(@PathVariable("id") Long id) {
        indicatorRepository.deleteById(id);
        return "redirect:/indicators/show?deleted_success=true";
    }

    @GetMapping("/keywords/{id}")
    public String showCreateIndicatorKeywordsPage(Model model, @PathVariable("id") Long id) {
        CompetencyAchievementIndicator indicator = indicatorRepository.findById(id).orElseThrow();
        indicator = indicatorsService.createKeywordsForCompetencyIndicator(indicator);
        model.addAttribute("indicator", indicator);
        return "pages/indicators/keywords";
    }
}
