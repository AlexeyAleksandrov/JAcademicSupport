package io.github.alexeyaleksandrov.jacademicsupport.controllers.pages.competency;

import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.competency.CreateKeywordForm;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.competency.EditKeywordForm;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.workskills.CreateSkillsGroupFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.workskills.EditSkillsGroupFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.Keyword;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.KeywordRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillsGroupRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/keywords")
@AllArgsConstructor
public class KeywordsController {
    final KeywordRepository keywordRepository;
    final SkillsGroupRepository skillsGroupRepository;
    final WorkSkillRepository workSkillRepository;

    @GetMapping("/create")
    public String showCreateKeyword(Model model, @RequestParam(name = "createError", required = false, defaultValue = "false") Boolean createError) {
        model.addAttribute("createKeywordForm", new CreateKeywordForm());
        model.addAttribute("create_error", createError);
        return "pages/keywords/create";
    }

    @PostMapping("/create")
    public String processKeyword(CreateKeywordForm createKeywordForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/keywords/create?create_error=true";
        }

        if(createKeywordForm.getDescription().isEmpty()) {
            return "redirect:/keywords/create?create_error=true";
        }

        Keyword keyword = new Keyword();
        keyword.setKeyword(createKeywordForm.getDescription());
        keywordRepository.saveAndFlush(keyword);
        return "redirect:/keywords/show?appended_success=true";
    }

    @GetMapping("/show")
    public String showAllKeywords(Model model,
                                  @RequestParam(name = "appended_success", required = false, defaultValue = "false") Boolean appendedSuccess,
                                  @RequestParam(name = "deleted_success", required = false, defaultValue = "false") Boolean deletedSuccess) {
        List<Keyword> keywords = keywordRepository.findAll().stream()
                .sorted(Comparator.comparing(Keyword::getKeyword))
                .toList();
        model.addAttribute("keywords", keywords);
        model.addAttribute("appended_success", appendedSuccess);
        model.addAttribute("deleted_success", deletedSuccess);
        return "pages/keywords/show";
    }

    @GetMapping("/edit/{id}")
    public String showEditKeyword(Model model, @PathVariable("id") Long id,
                                    @RequestParam(name = "edit_success", required = false, defaultValue = "false") Boolean editSuccess,
                                    @RequestParam(name = "edit_error", required = false, defaultValue = "false") Boolean editError) {
        Keyword keyword = keywordRepository.findById(id).orElseThrow();
        model.addAttribute("keyword", keyword);

        List<WorkSkill> workSkills = workSkillRepository.findAll().stream()
                .sorted(Comparator.comparing(WorkSkill::getDescription))
                .toList();
        model.addAttribute("workSkills", workSkills);

        // создаем DTO формы
        EditKeywordForm editKeywordForm = new EditKeywordForm();
        editKeywordForm.setSelectedWorkSkills(keyword.getWorkSkills().stream()
                .map(WorkSkill::getId)
                .toList());
        model.addAttribute("editKeywordForm", editKeywordForm);

        model.addAttribute("edit_success", editSuccess);
        model.addAttribute("edit_error", editError);
        return "pages/keywords/edit";
    }

    @PostMapping("/edit/{id}")
    public String processEditKeyword(@PathVariable("id") Long id, EditKeywordForm editKeywordForm, BindingResult bindingResult) {
        Keyword keyword = keywordRepository.findById(id).orElseThrow();

        if (bindingResult.hasErrors()) {
            return "redirect:/keywords/edit/" + keyword.getId() + "?edit_error=true";
        }

        if(editKeywordForm.getSelectedWorkSkills() == null || editKeywordForm.getSelectedWorkSkills().isEmpty()) {
            return "redirect:/keywords/edit/" + keyword.getId() + "?edit_error=true";
        }

        keyword.setWorkSkills(new ArrayList<>(editKeywordForm.getSelectedWorkSkills().stream()
                .map(s -> workSkillRepository.findById(s).orElseThrow())
                .toList()));
        keywordRepository.saveAndFlush(keyword);

        return "redirect:/keywords/edit/" + keyword.getId() + "?edit_success=true";
    }

    @PostMapping("/delete/{id}")
    public String processDeleteKeyword(@PathVariable("id") Long id) {
        keywordRepository.deleteById(id);
        return "redirect:/keywords/show?deleted_success=true";
    }
}
