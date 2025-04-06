package io.github.alexeyaleksandrov.jacademicsupport.controllers.pages.workskills;

import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.indicators.CreateIndicatorFrom;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.workskills.CreateWorkSkillFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.workskills.EditWorkSkillFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.*;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.*;
import io.github.alexeyaleksandrov.jacademicsupport.services.hh.HhService;
import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/work-skills")
public class WorkSkillsController {
    final WorkSkillRepository workSkillRepository;
    final SkillsGroupRepository skillsGroupRepository;
    final OllamaService ollamaService;
    final WorkSkillsService workSkillsService;
    final HhService hhService;

    @GetMapping("/create")
    public String showCreateWorkSkill(Model model, @RequestParam(name = "createError", required = false, defaultValue = "false") Boolean createError) {
        model.addAttribute("createWorkSkillFormDto", new CreateWorkSkillFormDto());
        model.addAttribute("create_error", createError);
        return "pages/work-skills/create";
    }

    @PostMapping("/create")
    public String processCreateWorkSkill(CreateWorkSkillFormDto createWorkSkillFormDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/work-skills/create?create_error=true";
        }

        if(createWorkSkillFormDto.getSearchText().isEmpty()) {
            return "redirect:/work-skills/create?create_error=true";
        }

        // выполняем поиск вакансий
        workSkillsService.getAndSaveAllVacancies(createWorkSkillFormDto.getSearchText());
        return "redirect:/work-skills/show?appended_success=true";
    }

    @GetMapping("/show")
    public String showAllWorkSkill(Model model,
                                   @RequestParam(name = "appended_success", required = false, defaultValue = "false") Boolean appendedSuccess,
                                   @RequestParam(name = "deleted_success", required = false, defaultValue = "false") Boolean deletedSuccess,
                                   @RequestParam(name = "matched_success", required = false, defaultValue = "false") Boolean matchedSuccess,
                                   @RequestParam(name = "update_market_demand_success", required = false, defaultValue = "false") Boolean updateMarketDemandSuccess) {
        List<WorkSkill> workSkills = workSkillRepository.findAll().stream()
//                .sorted(Comparator.comparing(WorkSkill::getDescription))
                .sorted(Comparator.comparing(WorkSkill::getMarketDemand).reversed())
                .toList();
        model.addAttribute("workSkills", workSkills);
        model.addAttribute("appended_success", appendedSuccess);
        model.addAttribute("deleted_success", deletedSuccess);
        model.addAttribute("matched_success", matchedSuccess);
        model.addAttribute("update_market_demand_success", updateMarketDemandSuccess);
        return "pages/work-skills/show";
    }

    @GetMapping("/edit/{id}")
    public String showEditWorkSkill(Model model, @PathVariable("id") Long id,
                                    @RequestParam(name = "edit_success", required = false, defaultValue = "false") Boolean editSuccess,
                                    @RequestParam(name = "edit_error", required = false, defaultValue = "false") Boolean editError) {
        List<SkillsGroup> skillsGroups = skillsGroupRepository.findAll().stream()
                .sorted(Comparator.comparing(SkillsGroup::getDescription))
                .toList();
        model.addAttribute("skillsGroups", skillsGroups);

        WorkSkill workSkill = workSkillRepository.findById(id).orElseThrow();
        model.addAttribute("workSkill", workSkill);

        // создаем DTO формы
        EditWorkSkillFormDto editWorkSkillFormDto = new EditWorkSkillFormDto();
        editWorkSkillFormDto.setSkillsGroupId(workSkill.getSkillsGroupBySkillsGroupId().getId());
        model.addAttribute("editWorkSkillFormDto", editWorkSkillFormDto);

        model.addAttribute("edit_success", editSuccess);
        model.addAttribute("edit_error", editError);
        return "pages/work-skills/edit";
    }

    @PostMapping("/edit/{id}")
    public String processEditWorkSkill(@PathVariable("id") Long id, EditWorkSkillFormDto editWorkSkillFormDto, BindingResult bindingResult) {
        WorkSkill workSkill = workSkillRepository.findById(id).orElseThrow();

        if (bindingResult.hasErrors()) {
            return "redirect:/work-skills/edit/" + workSkill.getId() + "?edit_error=true";
        }

        if(editWorkSkillFormDto.getSkillsGroupId() == null) {
            return "redirect:/work-skills/edit/" + workSkill.getId() + "?edit_error=true";
        }

        workSkill.setSkillsGroupBySkillsGroupId(skillsGroupRepository.findById(editWorkSkillFormDto.getSkillsGroupId()).orElseThrow());
        workSkillRepository.saveAndFlush(workSkill);

        return "redirect:/work-skills/edit/" + workSkill.getId() + "?edit_success=true";
    }

    @PostMapping("/delete/{id}")
    public String processDeleteWorkSkill(@PathVariable("id") Long id) {
        workSkillRepository.deleteById(id);
        return "redirect:/work-skills/show?deleted_success=true";
    }

    @GetMapping("/math-to-keywords")
    public String showMatchToSkillsGroups() {
        workSkillsService.matchKeywordsToWorkSkills();
        return "redirect:/work-skills/show?matched_success=true";
    }

    @GetMapping("/update-market-demand")
    public String showUpdateMarketDemand() {
        hhService.updateWorkSkillsMarketDemand();
        return "redirect:/work-skills/show?update_market_demand_success=true";
    }
}
