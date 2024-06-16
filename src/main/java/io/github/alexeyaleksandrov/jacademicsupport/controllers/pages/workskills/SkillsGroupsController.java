package io.github.alexeyaleksandrov.jacademicsupport.controllers.pages.workskills;

import io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.workskills.WorkSkillsRestController;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.workskills.CreateSkillsGroupFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.workskills.CreateWorkSkillFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.workskills.EditSkillsGroupFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.workskills.EditWorkSkillFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillsGroupRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.hh.HhService;
import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.SkillsGroupsService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/skills-groups")
@AllArgsConstructor
public class SkillsGroupsController {
    final WorkSkillRepository workSkillRepository;
    final SkillsGroupRepository skillsGroupRepository;
    final OllamaService ollamaService;
    final WorkSkillsService workSkillsService;
    final HhService hhService;
    private SkillsGroupsService skillsGroupsService;

    @GetMapping("/create")
    public String showCreateSkillsGroup(Model model, @RequestParam(name = "createError", required = false, defaultValue = "false") Boolean createError) {
        model.addAttribute("createSkillsGroupFormDto", new CreateSkillsGroupFormDto());
        model.addAttribute("create_error", createError);
        return "pages/skills-groups/create";
    }

    @PostMapping("/create")
    public String processSkillsGroup(CreateSkillsGroupFormDto createSkillsGroupFormDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/skills-groups/create?create_error=true";
        }

        if(createSkillsGroupFormDto.getName().isEmpty()) {
            return "redirect:/skills-groups/create?create_error=true";
        }

        SkillsGroup skillsGroup = new SkillsGroup();
        skillsGroup.setDescription(createSkillsGroupFormDto.getName());
        skillsGroupRepository.saveAndFlush(skillsGroup);
        return "redirect:/skills-groups/show?appended_success=true";
    }

    @GetMapping("/show")
    public String showAllSkillsGroup(Model model,
                                   @RequestParam(name = "appended_success", required = false, defaultValue = "false") Boolean appendedSuccess,
                                   @RequestParam(name = "deleted_success", required = false, defaultValue = "false") Boolean deletedSuccess,
                                     @RequestParam(name = "matched_success", required = false, defaultValue = "false") Boolean matchedSuccess,
                                   @RequestParam(name = "update_market_demand_success", required = false, defaultValue = "false") Boolean updateMarketDemandSuccess) {
        List<SkillsGroup> skillsGroups = skillsGroupRepository.findAll().stream()
                .sorted(Comparator.comparing(SkillsGroup::getDescription))
                .toList();
        model.addAttribute("skillsGroups", skillsGroups);
        model.addAttribute("appended_success", appendedSuccess);
        model.addAttribute("matched_success", matchedSuccess);
        model.addAttribute("deleted_success", deletedSuccess);
        model.addAttribute("update_market_demand_success", updateMarketDemandSuccess);
        return "pages/skills-groups/show";
    }

    @GetMapping("/edit/{id}")
    public String showEditWorkSkill(Model model, @PathVariable("id") Long id,
                                    @RequestParam(name = "edit_success", required = false, defaultValue = "false") Boolean editSuccess,
                                    @RequestParam(name = "edit_error", required = false, defaultValue = "false") Boolean editError) {
        List<SkillsGroup> skillsGroups = skillsGroupRepository.findAll().stream()
                .sorted(Comparator.comparing(SkillsGroup::getDescription))
                .toList();
        model.addAttribute("skillsGroups", skillsGroups);

        SkillsGroup skillsGroup = skillsGroupRepository.findById(id).orElseThrow();
        model.addAttribute("skillsGroup", skillsGroup);

        // создаем DTO формы
        EditSkillsGroupFormDto editSkillsGroupFormDto = new EditSkillsGroupFormDto();
        editSkillsGroupFormDto.setName(skillsGroup.getDescription());
        model.addAttribute("editSkillsGroupFormDto", editSkillsGroupFormDto);

        model.addAttribute("edit_success", editSuccess);
        model.addAttribute("edit_error", editError);
        return "pages/skills-groups/edit";
    }

    @PostMapping("/edit/{id}")
    public String processEditWorkSkill(@PathVariable("id") Long id, EditSkillsGroupFormDto editSkillsGroupFormDto, BindingResult bindingResult) {
        SkillsGroup skillsGroup = skillsGroupRepository.findById(id).orElseThrow();

        if (bindingResult.hasErrors()) {
            return "redirect:/skills-groups/edit/" + skillsGroup.getId() + "?edit_error=true";
        }

        if(editSkillsGroupFormDto.getName() == null || editSkillsGroupFormDto.getName().isEmpty()
                || (skillsGroupRepository.existsByDescription(editSkillsGroupFormDto.getName())
                    && skillsGroupRepository.findByDescription(editSkillsGroupFormDto.getName()).getId() != id)) {
            return "redirect:/skills-groups/edit/" + skillsGroup.getId() + "?edit_error=true";
        }

        skillsGroup.setDescription(editSkillsGroupFormDto.getName());
        skillsGroupRepository.saveAndFlush(skillsGroup);

        return "redirect:/skills-groups/edit/" + skillsGroup.getId() + "?edit_success=true";
    }

    @PostMapping("/delete/{id}")
    public String processDeleteWorkSkill(@PathVariable("id") Long id) {
        skillsGroupRepository.deleteById(id);
        return "redirect:/skills-groups/show?deleted_success=true";
    }

    @GetMapping("/math-to-work-skills")
    public String showMatchToSkillsGroups() {
        workSkillsService.matchWorkSkillsToSkillsGroups();
        return "redirect:/skills-groups/show?matched_success=true";
    }

    @GetMapping("/update-market-demand")
    public String showUpdateMarketDemand() {
        skillsGroupsService.updateSkillsGroupsMarketDemand();
        return "redirect:/skills-groups/show?update_market_demand_success=true";
    }
}
