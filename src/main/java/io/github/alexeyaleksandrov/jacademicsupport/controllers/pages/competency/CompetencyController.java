package io.github.alexeyaleksandrov.jacademicsupport.controllers.pages.competency;

import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.competency.CreateCompetenceForm;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.competency.EditCompetenceForm;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation.RecommendedSkillDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation.RpdDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.Competency;
import io.github.alexeyaleksandrov.jacademicsupport.models.Keyword;
import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.KeywordRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.competency.CompetencyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/competency")
@AllArgsConstructor
public class CompetencyController {
    final CompetencyRepository competencyRepository;
    final KeywordRepository keywordRepository;
    final CompetencyService competencyService;

    @GetMapping("/create")
    public String showCreateCompetency(Model model, @RequestParam(name = "createError", required = false, defaultValue = "false") Boolean createError) {

        model.addAttribute("createCompetenceForm", new CreateCompetenceForm());
        model.addAttribute("create_error", createError);
        return "pages/competency/create";
    }

    @PostMapping("/create")
    public String processCreateCompetency(CreateCompetenceForm createCompetenceForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/competency/create?create_error=true";
        }

        if(createCompetenceForm.getNumber().isEmpty() || createCompetenceForm.getDescription().isEmpty() || competencyRepository.existsByNumber(createCompetenceForm.getNumber())) {
            return "redirect:/competency/create?create_error=true";
        }

        // создаём компетенцию
        Competency competency = new Competency();
        competency.setNumber(createCompetenceForm.getNumber());
        competency.setDescription(createCompetenceForm.getDescription());
        competency = competencyRepository.saveAndFlush(competency);
        return "redirect:/competency/show?appended_success=true";
    }

    @GetMapping("/show")
    public String showAllCompetency(Model model,
                             @RequestParam(name = "appended_success", required = false, defaultValue = "false") Boolean appendedSuccess,
                             @RequestParam(name = "deleted_success", required = false, defaultValue = "false") Boolean deletedSuccess) {
        List<Competency> competencies = competencyRepository.findAll();
        model.addAttribute("competencies", competencies);
        model.addAttribute("appended_success", appendedSuccess);
        model.addAttribute("deleted_success", deletedSuccess);
        return "pages/competency/show";
    }

    @GetMapping("/edit/{id}")
    public String showEditCompetency(Model model, @PathVariable("id") Long id,
                              @RequestParam(name = "edit_success", required = false, defaultValue = "false") Boolean editSuccess,
                              @RequestParam(name = "edit_error", required = false, defaultValue = "false") Boolean editError) {
        Competency competency = competencyRepository.findById(id).orElseThrow();
        model.addAttribute("competency", competency);

        List<Keyword> keywords = keywordRepository.findAll();
        model.addAttribute("keywords", keywords);

        // создаем DTO формы
        EditCompetenceForm editCompetenceForm = new EditCompetenceForm();
        editCompetenceForm.setNumber(competency.getNumber());
        editCompetenceForm.setDescription(competency.getDescription());
        editCompetenceForm.setSelectedKeywords(keywords.stream()
                .filter(c -> competency.getKeywords().contains(c))
                .map(Keyword::getId)
                .toList());
        model.addAttribute("editCompetenceForm", editCompetenceForm);

        model.addAttribute("edit_success", editSuccess);
        model.addAttribute("edit_error", editError);
        return "pages/competency/edit";
    }

    @PostMapping("/edit/{id}")
    public String processEditCompetency(@PathVariable("id") Long id, EditCompetenceForm editCompetenceForm, BindingResult bindingResult) {
        Competency competency = competencyRepository.findById(id).orElseThrow();

        if (bindingResult.hasErrors()) {
            return "redirect:/competency/edit/" + competency.getId() + "?edit_error=true";
        }

        if(editCompetenceForm.getNumber().isEmpty() || editCompetenceForm.getDescription().isEmpty()) {
            return "redirect:/competency/edit/" + competency.getId() + "?edit_error=true";
        }

        competency.setNumber(editCompetenceForm.getNumber());
        competency.setDescription(editCompetenceForm.getDescription());
        competency.setKeywords(new ArrayList<>(keywordRepository.findAll().stream()
                .filter(keyword -> editCompetenceForm.getSelectedKeywords().contains(keyword.getId()))
                .toList()));

        competency = competencyRepository.saveAndFlush(competency);

        return "redirect:/competency/edit/" + competency.getId() + "?edit_success=true";
    }

    @PostMapping("/delete/{id}")
    public String processDeleteCompetencyPage(@PathVariable("id") Long id) {
        competencyRepository.deleteById(id);
        return "redirect:/competency/show?deleted_success=true";
    }

    @GetMapping("/keywords/{id}")
    public String showCreateKeywordsPage(Model model, @PathVariable("id") Long id) {
        Competency competency = competencyService.createKeywordsForCompetency(id);
        model.addAttribute("competency", competency);
        return "pages/competency/keywords";
    }
}
