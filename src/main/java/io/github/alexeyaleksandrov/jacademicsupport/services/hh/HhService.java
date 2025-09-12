package io.github.alexeyaleksandrov.jacademicsupport.services.hh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexeyaleksandrov.jacademicsupport.dto.hh.VacancyPage;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.dto.hh.Vacancy;
import io.github.alexeyaleksandrov.jacademicsupport.dto.hh.VacancyItem;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SavedSearchRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.VacancyEntityRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.webclient.WebClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class HhService {
    private final WebClient webClient;
    private final WorkSkillRepository workSkillRepository;
    private final VacancyEntityRepository vacancyEntityRepository;

    public List<VacancyItem> getAllVacancies(String searchText) {
        searchText = searchText.replace(" ", "%20");
        List<VacancyItem> allVacancies = new ArrayList<>();
        int page = 0;
        int pages;

        do {
            try {
                String urlText = "https://api.hh.ru/vacancies?text=" + searchText + "&area=1&page=" + page;
                String vacanciesJson = webClient.get(urlText);
                page++;
                VacancyPage vacancyPage = parseVacancies(vacanciesJson);
                pages = vacancyPage.getPages();
                allVacancies.addAll(vacancyPage.getVacancies());
                System.out.println("Page " + page + "/" + pages);
                // добавление задержки
                Thread.sleep(2000); // например, 2 секунды
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
        } while (page < pages && page < 10);   // добавляем ограничение, что мы ищем не более, чем 10 страниц

        return allVacancies;
    }


    public Vacancy getVacancyById(long vacancyId) {
        try {
            String urlText = "https://api.hh.ru/vacancies/" + vacancyId;
            String vacancyJson = webClient.get(urlText);
            Thread.sleep(2000); // например, 2 секунды
            return parseVacancy(vacancyJson);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted", e);
        }
    }


    private VacancyPage parseVacancies(String vacanciesJson) {
        List<VacancyItem> vacancies = new ArrayList<>();
        int pages = 0;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(vacanciesJson);
            JsonNode items = jsonNode.get("items");
            pages = jsonNode.get("pages").asInt();

            if (items.isArray()) {
                for (JsonNode itemNode : items) {
                    VacancyItem vacancyItem = new VacancyItem();
                    vacancyItem.setId(itemNode.get("id").asLong());
                    vacancyItem.setName(itemNode.get("name").asText());
                    vacancyItem.setPublishedAt(itemNode.get("published_at").asText());
                    vacancies.add(vacancyItem);
                }
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new VacancyPage(vacancies, pages);
    }

    private Vacancy parseVacancy(String vacancyJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode vacancieNode = objectMapper.readTree(vacancyJson);
            Vacancy vacancy = new Vacancy();
            vacancy.setId(vacancieNode.get("id").asLong());
            vacancy.setName(vacancieNode.get("name").asText());
            vacancy.setPublishedAt(vacancieNode.get("published_at").asText());
            vacancy.setDescription(vacancieNode.get("description").asText());
            JsonNode keySkills = vacancieNode.get("key_skills");

            if (keySkills.isArray()) {
                List<String> skillsList = new ArrayList<>();
                for (JsonNode keySkillsNode : keySkills) {
                    skillsList.add(keySkillsNode.get("name").asText());
                }
                vacancy.setSkills(skillsList);
            }
            return vacancy;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<WorkSkill> updateWorkSkillsMarketDemand() {
        List<WorkSkill> workSkills = workSkillRepository.findAll();     // получаем все имеющиеся навыки
        List<VacancyEntity> vacancyEntities = vacancyEntityRepository.findAll();    // получаем все доступные вакансии

        Map<WorkSkill, Integer> workSkillMarketDemand = new HashMap<>();    // кол-во вакансий с данным навыком
        workSkills.forEach(workSkill -> workSkillMarketDemand.put(workSkill, 0));   // заполняем базовыми значениями
        workSkills.forEach(workSkill -> {
            int demand = (int)vacancyEntities.stream()
                    .filter(vacancyEntity -> vacancyEntity.getSkills().contains(workSkill))
                    .count();
            demand += workSkillMarketDemand.get(workSkill);     // добавляем текущую востребованность
            workSkillMarketDemand.put(workSkill, demand);
        });     // считаем, в скольких вакансиях встречается требованияе данного навыка

        workSkills.forEach(workSkill -> {
            double demand = (double) workSkillMarketDemand.get(workSkill) / (double) vacancyEntities.size();
            workSkill.setMarketDemand(demand);
        });     // считаем популярность на рынке

        workSkills = workSkillRepository.saveAllAndFlush(workSkills);    // сохраняем популярность навыка
        return workSkills;
    }
}
