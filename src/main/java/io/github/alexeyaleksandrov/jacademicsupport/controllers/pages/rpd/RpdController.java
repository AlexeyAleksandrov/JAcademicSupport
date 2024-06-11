package io.github.alexeyaleksandrov.jacademicsupport.controllers.pages.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.dto.forms.rpd.RpdFormDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.crud.CreateRpdDTO;
import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.rpd.RpdService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/rpd")
@AllArgsConstructor
public class RpdController {
    final CompetencyAchievementIndicatorRepository indicatorRepository;
    final RpdService rpdService;

    @GetMapping("/create")
    public String showCreateRpdForm(Model model, @RequestParam(name = "error", required = false, defaultValue = "false") Boolean error) {
        // Предполагаем, что у нас есть список конкурентных индикаторов достижения компетенций
        List<CompetencyAchievementIndicator> indicators = indicatorRepository.findAll();
        model.addAttribute("indicatorList", indicators);
        model.addAttribute("rpdFormDto", new RpdFormDto());
        model.addAttribute("error", error);
        return "rpd/create";
    }

    @PostMapping("/create")
    public String processCreateRpdForm(RpdFormDto rpdFormDto, BindingResult bindingResult) {
        System.out.println(rpdFormDto);
        if (bindingResult.hasErrors()) {
            System.out.println("Ошибки!");
            return "redirect:/rpd/create?error=true";
        }

        if(rpdFormDto.getDisciplineName().isEmpty() || rpdFormDto.getYear() < 1900 || rpdFormDto.getYear() > 2100 || rpdFormDto.getSelectedIndicators().isEmpty()) {
            System.out.println("Ошибка данных!");
            return "redirect:/rpd/create?error=true";
        }

        // DTO создания РПД
        CreateRpdDTO createRpdDTO = new CreateRpdDTO();
        createRpdDTO.setDiscipline_name(rpdFormDto.getDisciplineName());
        createRpdDTO.setYear(rpdFormDto.getYear());
        createRpdDTO.setCompetencyAchievementIndicators(rpdFormDto.getSelectedIndicators().stream()
                .map(id -> indicatorRepository.findById(id).orElseThrow().getNumber())
                .toList());

        Rpd rpd = rpdService.createRpd(createRpdDTO);     // создаем РПД
        return "redirect:/rpd/create";
    }
}
