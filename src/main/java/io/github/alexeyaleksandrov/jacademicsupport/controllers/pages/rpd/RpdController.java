package io.github.alexeyaleksandrov.jacademicsupport.controllers.pages.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.rpd.CreateRpdFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.rpd.EditRpdFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.crud.CreateRpdDTO;
import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.RpdRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.RpdService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/rpd")
@AllArgsConstructor
public class RpdController {
    final CompetencyAchievementIndicatorRepository indicatorRepository;
    final RpdRepository rpdRepository;
    final RpdService rpdService;

    @GetMapping("/create")
    public String showCreateRpdForm(Model model, @RequestParam(name = "error", required = false, defaultValue = "false") Boolean error) {
        List<CompetencyAchievementIndicator> indicators = indicatorRepository.findAll();
        model.addAttribute("indicatorList", indicators);
        model.addAttribute("rpdFormDto", new CreateRpdFormDto());
        model.addAttribute("error", error);
        return "rpd/create";
    }

    @PostMapping("/create")
    public String processCreateRpdForm(CreateRpdFormDto createRpdFormDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/rpd/create?error=true";
        }

        if(createRpdFormDto.getDisciplineName().isEmpty() || createRpdFormDto.getYear() < 1900 || createRpdFormDto.getYear() > 2100 || createRpdFormDto.getSelectedIndicators().isEmpty()) {
            return "redirect:/rpd/create?error=true";
        }

        // DTO создания РПД
        CreateRpdDTO createRpdDTO = new CreateRpdDTO();
        createRpdDTO.setDiscipline_name(createRpdFormDto.getDisciplineName());
        createRpdDTO.setYear(createRpdFormDto.getYear());
        createRpdDTO.setCompetencyAchievementIndicators(createRpdFormDto.getSelectedIndicators().stream()
                .map(id -> indicatorRepository.findById(id).orElseThrow().getNumber())
                .toList());

        Rpd rpd = rpdService.createRpd(createRpdDTO);     // создаем РПД
        return "redirect:/rpd/create";
    }

    @GetMapping("/show")
    public String showAllRpd(Model model) {
        List<Rpd> rpds = rpdRepository.findAll();
        model.addAttribute("rpds", rpds);
        return "rpd/show";
    }

    @GetMapping("/edit/{id}")
    public String showEditRpd(Model model, @PathVariable("id") Long id) {
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

        indicators.forEach(indicator -> System.out.println(indicator.getNumber() + ": " + rpd.getCompetencyAchievementIndicators().contains(indicator)));

        return "rpd/edit";
    }

    @PostMapping("/edit/{id}")
    public String processEditRpd(@PathVariable("id") Long id, EditRpdFormDto editRpdFormDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "redirect:/rpd/edit?error=true";
        }

        if(editRpdFormDto.getDisciplineName().isEmpty() || editRpdFormDto.getYear() < 1900 || editRpdFormDto.getYear() > 2100 || editRpdFormDto.getSelectedIndicators().isEmpty()) {
            return "redirect:/rpd/create?error=true";
        }

        Rpd rpd = rpdRepository.findById(id).orElseThrow();
        List<CompetencyAchievementIndicator> indicators = indicatorRepository.findAll();

        rpd.setDisciplineName(editRpdFormDto.getDisciplineName());
        rpd.setYear(editRpdFormDto.getYear());
        rpd.setCompetencyAchievementIndicators(new ArrayList<>(indicators.stream()
                .filter(indicator -> editRpdFormDto.getSelectedIndicators().contains(indicator.getId()))
                .toList()));

        rpdRepository.saveAndFlush(rpd);
        return "redirect:/rpd/show";
    }
}
