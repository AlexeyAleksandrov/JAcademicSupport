package io.github.alexeyaleksandrov.jacademicsupport.controllers.hh;

import io.github.alexeyaleksandrov.jacademicsupport.models.hh.Vacancy;
import io.github.alexeyaleksandrov.jacademicsupport.services.hh.HhWebClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class VacanciesController {
    private HhWebClient hhWebClient;

    public VacanciesController(HhWebClient hhWebClient) {
        this.hhWebClient = hhWebClient;
    }

    @GetMapping("/vaclist")
    private ResponseEntity<List<Vacancy>> vacanciesList() {
        List<Vacancy> vacancyList = hhWebClient.getAllVacancies("Java Junior Developer");
        return ResponseEntity.ok(vacancyList);
    }
}
