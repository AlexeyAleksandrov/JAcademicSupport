package io.github.alexeyaleksandrov.jacademicsupport.services.hh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexeyaleksandrov.jacademicsupport.models.hh.Vacancy;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class HhWebClient {
    public List<Vacancy> getAllVacancies(String searchText) {
        searchText = searchText.replace(" ", "%20");
        String url = "https://api.hh.ru/vacancies?text=" + searchText +"&area=1";
//        String url = "https://api.hh.ru/vacancies?text=juniur%20java%20deleveloper&area=1";
        System.out.println(url);

        WebClient.Builder webClientBuilder = WebClient.builder();

        WebClient webClient = WebClient.builder()
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64);")
                .build();
        String responseText = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println(responseText);
        System.out.println(responseText.length());

        List<Vacancy> vacancies = new ArrayList<>();

//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode jsonNode = null;
//        try {
//            jsonNode = objectMapper.readTree(responseText);
//            JsonNode items = jsonNode.get("items");
//            if(items.isArray()) {
//                for (JsonNode itemNode : items) {
//                    Vacancy vacancy = new Vacancy();
//                    vacancy.setId(itemNode.get("id").asInt());
//                    vacancy.setName(itemNode.get("name").asText());
//                    vacancies.add(vacancy);
//                }
//            }
//
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }

        return vacancies;

//        List<Vacancy> objectList = webClientBuilder.build()
//                .get()
//                .uri(url)
//                .retrieve()
//                .bodyToFlux(Vacancy.class)
//                .collectList()
//                .block();
//
//        return objectList;

//        return response;

//        List<Vacancy> objectList = WebClient.builder().build()
//                .get()
//                .uri(url)
//                .retrieve()
//                .bodyToFlux(Vacancy.class)
//                .collectList()
//                .block();
//
//        return objectList;
    }
}
