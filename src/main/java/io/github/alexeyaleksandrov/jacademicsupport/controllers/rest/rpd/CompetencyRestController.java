package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.models.Competency;
import io.github.alexeyaleksandrov.jacademicsupport.models.Keyword;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.KeywordRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/api")
public class CompetencyRestController {
    private final CompetencyRepository competencyRepository;
    private final OllamaService ollamaService;
    private final KeywordRepository keywordRepository;

    @GetMapping("/competency/create")
    public ResponseEntity<Competency> createCompetency(@RequestParam(name = "number") String number, @RequestParam("description") String description) {
        Competency competency = new Competency();
        competency.setNumber(number);
        competency.setDescription(description);
        competency = competencyRepository.saveAndFlush(competency);
        return ResponseEntity.ok(competency);
    }

    @GetMapping("/competency/create/keywords")
    public ResponseEntity<Competency> createKeywordsForCompetency(@RequestParam(name = "number") String number) {
        Competency competency = competencyRepository.findByNumber(number);
        String content = "Выдели основные ключевые слова из описания профессиональной компетенции: " + competency.getDescription() + "Исключи из ответа размытые и обобщённые словосочетания. Ответ должен быть только на русском языке. Все слова в ответе должны находиться в нормальной форме без склонений и спряжений.  В ответе не пиши словоочетание \"ключевые слова\", напиши только сами слова\"";
        String answer = ollamaService.chat(content);
        System.out.println("answer:" + answer);
        List<String> keywords = Arrays.stream(answer.split(",")).toList();
        List<Keyword> keywordList = keywords.stream()
                .map(String::trim)
                .map(k -> {
                    Keyword keyword = new Keyword();
                    keyword.setKeyword(k);
                    return keyword;
                })
                .toList();      // выделяем ключевые слова

        List<Keyword> competencyKeywords = new ArrayList<>();   // ключевые слова, которые войдут для данной компетенции
        competencyKeywords.addAll(
                keywordList.stream()
                        .filter(keyword -> keywordRepository.existsByKeyword(keyword.getKeyword()))
                        .map(keyword -> keywordRepository.findByKeyword(keyword.getKeyword()))
                        .toList()
        );  // если ключевые слова уже есть в БД, добавляем их из БД
        competencyKeywords.addAll(
                keywordList.stream()
                        .filter(keyword -> !keywordRepository.existsByKeyword(keyword.getKeyword()))
                        .map(keywordRepository::saveAndFlush)
                        .toList()
        );  // добавляем и сохраняем новые ключевые слова

        competency.setKeywords(competencyKeywords);     // добавляем ключевые слова для компетенции
        competency = competencyRepository.saveAndFlush(competency);     // сохраняем обновлённую компетенцию

        return ResponseEntity.ok(competency);
    }
}
