package io.github.alexeyaleksandrov.jacademicsupport.services.rpd.competency;

import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import io.github.alexeyaleksandrov.jacademicsupport.models.Keyword;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyAchievementIndicatorRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.KeywordRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class IndicatorsService {
    final OllamaService ollamaService;
    final KeywordRepository keywordRepository;
    final CompetencyAchievementIndicatorRepository indicatorRepository;

    public CompetencyAchievementIndicator createKeywordsForCompetencyIndicator(CompetencyAchievementIndicator indicator) {
        StringBuilder indicatorTextBuilder = new StringBuilder();
        indicatorTextBuilder.append(indicator.getDescription());    // описание
        indicatorTextBuilder.append(". ");
        indicatorTextBuilder.append(indicator.getIndicatorKnow());
        indicatorTextBuilder.append(". ");
        indicatorTextBuilder.append(indicator.getIndicatorAble());
        indicatorTextBuilder.append(". ");
        indicatorTextBuilder.append(indicator.getIndicatorPossess());
        indicatorTextBuilder.append(".");
        String indicatorText =indicatorTextBuilder.toString();
        String content = "Выдели основные ключевые слова из описания профессиональной компетенции: " + indicatorText + "Исключи из ответа размытые и обобщённые словосочетания. Ответ должен быть только на русском языке. Все слова в ответе должны находиться в нормальной форме без склонений и спряжений.  В ответе не пиши словоочетание \"ключевые слова\", напиши только сами слова\"";
        String answer = ollamaService.chat(content);
//        System.out.println("answer:" + answer);
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

        if(!indicator.getKeywords().isEmpty())
        {
            indicator.setKeywords(new ArrayList<>());
            indicatorRepository.saveAndFlush(indicator);    // сбрасываем ключевые слова
        }

        indicator.setKeywords(competencyKeywords);     // добавляем ключевые слова для индикатора компетенции
        indicator = indicatorRepository.saveAndFlush(indicator);     // сохраняем обновлённый индикатор компетенции
        return indicator;
    }
}
