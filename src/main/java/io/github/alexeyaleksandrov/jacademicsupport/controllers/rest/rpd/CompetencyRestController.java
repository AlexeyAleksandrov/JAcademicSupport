package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.models.Competency;
import io.github.alexeyaleksandrov.jacademicsupport.models.Keyword;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.KeywordRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/competencies")
@AllArgsConstructor
public class CompetencyRestController {
    private final CompetencyRepository competencyRepository;
    private final OllamaService ollamaService;
    private final KeywordRepository keywordRepository;

    @GetMapping
    public ResponseEntity<List<CompetencyDto>> getAllCompetencies() {
        List<Competency> competencies = competencyRepository.findAll();
        List<CompetencyDto> response = competencies.stream()
                .map(CompetencyDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetencyDto> getCompetencyById(@PathVariable Long id) {
        return competencyRepository.findById(id)
                .map(CompetencyDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CompetencyDto> createCompetency(@Valid @RequestBody CreateCompetencyRequest request) {
        Competency competency = new Competency();
        competency.setNumber(request.getNumber());
        competency.setDescription(request.getDescription());
        
        Competency savedCompetency = competencyRepository.save(competency);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CompetencyDto.fromEntity(savedCompetency));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetencyDto> updateCompetency(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCompetencyRequest request) {
        
        return competencyRepository.findById(id)
                .map(competency -> {
                    competency.setNumber(request.getNumber());
                    competency.setDescription(request.getDescription());
                    Competency updatedCompetency = competencyRepository.save(competency);
                    return ResponseEntity.ok(CompetencyDto.fromEntity(updatedCompetency));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompetency(@PathVariable Long id) {
        return competencyRepository.findById(id)
                .map(competency -> {
                    competencyRepository.delete(competency);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/keywords/generate")
    public ResponseEntity<CompetencyDto> generateKeywordsForCompetency(@PathVariable Long id) {
        return competencyRepository.findById(id)
                .map(competency -> {
                    String content = "Выдели основные ключевые слова из описания профессиональной компетенции: " + 
                            competency.getDescription() + 
                            " Исключи из ответа размытые и обобщённые словосочетания. Ответ должен быть только на русском языке. " +
                            "Все слова в ответе должны находиться в нормальной форме без склонений и спряжений. " +
                            "В ответе не пиши словоочетание \"ключевые слова\", напиши только сами слова через запятую.";
                    
                    String answer = ollamaService.chat(content);
                    List<Keyword> keywordList = Arrays.stream(answer.split(","))
                            .map(String::trim)
                            .filter(k -> !k.isEmpty())
                            .map(k -> {
                                Keyword keyword = new Keyword();
                                keyword.setKeyword(k);
                                return keyword;
                            })
                            .collect(Collectors.toList());  // выделяем ключевые слова

                    List<Keyword> existingKeywords = keywordList.stream()
                            .filter(k -> keywordRepository.existsByKeyword(k.getKeyword()))
                            .map(k -> keywordRepository.findByKeyword(k.getKeyword()))
                            .collect(Collectors.toList());  // если ключевые слова уже есть в БД, добавляем их из БД

                    // ключевые слова, которые войдут для данной компетенции
                    List<Keyword> newKeywords = keywordList.stream()
                            .filter(k -> !keywordRepository.existsByKeyword(k.getKeyword()))
                            .map(keywordRepository::save)
                            .collect(Collectors.toList());

                    List<Keyword> allKeywords = new java.util.ArrayList<>(existingKeywords);
                    allKeywords.addAll(newKeywords);
                    competency.setKeywords(allKeywords);    // если ключевые слова уже есть в БД, добавляем их из БД
                    
                    Competency updated = competencyRepository.save(competency);
                    return ResponseEntity.ok(CompetencyDto.fromEntity(updated));
                })
                .orElse(ResponseEntity.notFound().build()); // добавляем и сохраняем новые ключевые слова
    }

    @Data
    private static class CompetencyDto {
        private Long id;
        private String number;
        private String description;
        private List<String> keywords;

        public static CompetencyDto fromEntity(Competency competency) {
            CompetencyDto dto = new CompetencyDto();
            dto.setId(competency.getId());
            dto.setNumber(competency.getNumber());
            dto.setDescription(competency.getDescription());
            if (competency.getKeywords() != null) {
                dto.setKeywords(competency.getKeywords().stream()
                        .map(Keyword::getKeyword)
                        .collect(Collectors.toList()));
            }
            return dto;
        }
    }

    @Data
    private static class CreateCompetencyRequest {
        @NotBlank(message = "Number is required")
        private String number;
        
        @NotBlank(message = "Description is required")
        private String description;
    }

    @Data
    private static class UpdateCompetencyRequest {
        @NotBlank(message = "Number is required")
        private String number;
        
        @NotBlank(message = "Description is required")
        private String description;
    }
}
