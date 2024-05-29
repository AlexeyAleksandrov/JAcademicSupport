package io.github.alexeyaleksandrov.jacademicsupport.services.hh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexeyaleksandrov.jacademicsupport.models.hh.Vacancy;
import io.github.alexeyaleksandrov.jacademicsupport.models.hh.VacancyItem;
import io.github.alexeyaleksandrov.jacademicsupport.services.webclient.WebClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HhService {
    private final WebClient webClient;

    public HhService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<VacancyItem> getAllVacancies(String searchText) {
        searchText = searchText.replace(" ", "%20");    // добавляем пробелы
        String urlText = "https://api.hh.ru/vacancies?text=" + searchText + "&area=1";
        String vacanciesJson = webClient.get(urlText);
        return parseVacancies(vacanciesJson);
    }

    public Vacancy getVacancyById(long vacancyId) {
        String urlText = "https://api.hh.ru/vacancies/" + vacancyId;
        String vacancyJson = webClient.get(urlText);
        return parseVacancy(vacancyJson);
    }

    private List<VacancyItem> parseVacancies(String vacanciesJson) {
        List<VacancyItem> vacancies = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(vacanciesJson);
            JsonNode items = jsonNode.get("items");
            if(items.isArray()) {
                for (JsonNode itemNode : items) {
                    VacancyItem vacancyItem = new VacancyItem();
                    vacancyItem.setId(itemNode.get("id").asInt());
                    vacancyItem.setName(itemNode.get("name").asText());
                    vacancyItem.setPublishedAt(itemNode.get("published_at").asText());
                    vacancies.add(vacancyItem);
                }
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return vacancies;
    }

    private Vacancy parseVacancy(String vacancyJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode vacancieNode = null;
        try {
            vacancieNode = objectMapper.readTree(vacancyJson);
            Vacancy vacancy = new Vacancy();
            vacancy.setId(vacancieNode.get("id").asInt());
            vacancy.setName(vacancieNode.get("name").asText());
            vacancy.setPublishedAt(vacancieNode.get("published_at").asText());
            vacancy.setDescription(vacancieNode.get("description").asText());
            JsonNode keySkills = vacancieNode.get("key_skills");
            if(keySkills.isArray()) {
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
}
