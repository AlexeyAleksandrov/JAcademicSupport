package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.hh;

import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.dto.hh.Vacancy;
import io.github.alexeyaleksandrov.jacademicsupport.dto.hh.VacancyItem;
import io.github.alexeyaleksandrov.jacademicsupport.services.hh.HhService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@AllArgsConstructor
public class VacanciesRestController {
    private final HhService hhService;
    private final WorkSkillsService workSkillsService;

    @GetMapping("/vac/list")
    private ResponseEntity<List<VacancyItem>> vacanciesList() {
        List<VacancyItem> vacancyItemList = hhService.getAllVacancies("Java Junior Developer");
        return ResponseEntity.ok(vacancyItemList);
    }

    @GetMapping("/vac/{id}")
    private ResponseEntity<Vacancy> vacanciesList(@PathVariable(name = "id") Long id) {
        Vacancy vacancy = hhService.getVacancyById(id);
        return ResponseEntity.ok(vacancy);
    }

    @GetMapping("/vac/list/save")
    private ResponseEntity<List<VacancyEntity>> getAndSaveAllVacancies() {
        List<VacancyEntity> vacancyEntities = workSkillsService.getAndSaveAllVacancies("Java Junior Developer");
        return ResponseEntity.ok(vacancyEntities);
    }

    @GetMapping("/update/workSkillsMarketDemand")
    private ResponseEntity<List<WorkSkill>> updateWorkSkillsMarketDemand() {
        return ResponseEntity.ok(hhService.updateWorkSkillsMarketDemand());
    }

    @GetMapping("/vac/by-saved-searches")
    public ResponseEntity<List<VacancyEntity>> getVacanciesBySavedSearches() {
        try {
            List<VacancyEntity> vacancies = workSkillsService.getAllVacanciesBySavedSearches();
            return ResponseEntity.ok(vacancies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
