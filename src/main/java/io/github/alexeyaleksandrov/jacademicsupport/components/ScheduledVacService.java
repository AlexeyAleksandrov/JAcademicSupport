package io.github.alexeyaleksandrov.jacademicsupport.components;

import io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.hh.VacanciesRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ScheduledVacService {
    private final VacanciesRestController vacanciesRestController;

    // Используем RestTemplate для вызова своего же API
    private final RestTemplate restTemplate;

    public ScheduledVacService(VacanciesRestController vacanciesRestController, RestTemplateBuilder restTemplateBuilder) {
        this.vacanciesRestController = vacanciesRestController;
        this.restTemplate = restTemplateBuilder.build();
    }

    // Аннотация @Scheduled определяет расписание.
    // Cron-выражение "0 0 12 * * ?" означает "каждый день в 12:00".
    // Выражение "0 0 12 * * ?" означает "каждый день в 12:00 по UTC".
    // Для московского времени (UTC+3) используйте "0 0 15 * * ?"
    @Scheduled(cron = "0 25 13 * * ?", zone = "Europe/Moscow")
    public void triggerSavedSearches() {
        try {
            var result = vacanciesRestController.getVacanciesBySavedSearches();
            System.out.println("Scheduled task executed. Result: " + result.getBody());
        } catch (Exception e) {
            System.err.println("Error during scheduled task: " + e.getMessage());
        }
    }
}