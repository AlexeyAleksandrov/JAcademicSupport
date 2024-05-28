package io.github.alexeyaleksandrov.jacademicsupport.controllers.hh;

import io.github.alexeyaleksandrov.jacademicsupport.models.hh.Vacancy;
import io.github.alexeyaleksandrov.jacademicsupport.models.hh.VacancyItem;
import io.github.alexeyaleksandrov.jacademicsupport.services.hh.HhService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class VacanciesController {
    private HhService hhService;

    public VacanciesController(HhService hhService) {
        this.hhService = hhService;
    }

    @GetMapping("/vaclist")
    private ResponseEntity<List<VacancyItem>> vacanciesList() {
        List<VacancyItem> vacancyItemList = hhService.getAllVacancies("Java Junior Developer");
        return ResponseEntity.ok(vacancyItemList);
    }

    @GetMapping("/vac/{id}")
    private ResponseEntity<Vacancy> vacanciesList(@PathVariable(name = "id") Long id) {
        Vacancy vacancy = hhService.getVacancyById(id);
        return ResponseEntity.ok(vacancy);
    }
}
