package io.github.alexeyaleksandrov.jacademicsupport.components;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ScheduledVacService {

    // Используем RestTemplate для вызова своего же API
    private final RestTemplate restTemplate;

    public ScheduledVacService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    // Аннотация @Scheduled определяет расписание.
    // Cron-выражение "0 0 12 * * ?" означает "каждый день в 12:00".
    // Выражение "0 0 12 * * ?" означает "каждый день в 12:00 по UTC".
    // Для московского времени (UTC+3) используйте "0 0 15 * * ?"
    @Scheduled(cron = "0 0 12 * * ?", zone = "Europe/Moscow")
    public void triggerSavedSearches() {
        try {
            String url = "http://localhost:8080/vac/by-saved-searches";
            String result = restTemplate.getForObject(url, String.class);
            System.out.println("Scheduled task executed. Result: " + result);
        } catch (Exception e) {
            System.err.println("Error during scheduled task: " + e.getMessage());
        }
    }
}