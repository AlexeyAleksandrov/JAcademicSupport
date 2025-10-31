package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.competency.CompetencyDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.competency.CreateCompetencyRequest;
import io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.competency.UpdateCompetencyRequest;
import io.github.alexeyaleksandrov.jacademicsupport.models.Competency;
import io.github.alexeyaleksandrov.jacademicsupport.models.Keyword;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CompetencyRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.KeywordRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.llm.LlmService;
import io.github.alexeyaleksandrov.jacademicsupport.services.llm.LlmServiceFactory;
import io.github.alexeyaleksandrov.jacademicsupport.dto.ErrorResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/competencies")
@AllArgsConstructor
public class CompetencyRestController {
    private final CompetencyRepository competencyRepository;
    private final LlmServiceFactory llmServiceFactory;
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
                    
                    // Update keywords if provided
                    if (request.getKeywordIds() != null) {
                        List<Keyword> keywords = request.getKeywordIds().stream()
                                .map(keywordId -> keywordRepository.findById(keywordId)
                                        .orElseThrow(() -> new RuntimeException("Keyword not found with id: " + keywordId)))
                                .collect(Collectors.toList());
                        competency.setKeywords(keywords);
                    }
                    
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
    public ResponseEntity<?> generateKeywordsForCompetency(
            @PathVariable Long id,
            @RequestParam(name = "model", defaultValue = "ollama") String modelProvider) {

        Optional<ResponseEntity<?>> result = competencyRepository.findById(id)
                .map(competency -> {
                    try {
                        // Get the appropriate LLM service based on the model parameter
                        LlmService llmService = llmServiceFactory.getService(modelProvider);
                        
                        String content = "Выдели основные ключевые слова из описания профессиональной компетенции: " + 
                                competency.getDescription() + 
                                " Исключи из ответа размытые и обобщённые словосочетания. " +
                                "Старайся извлечь, в первую очередь, словосочетания из двух слов, но если это невозможно, то из 1-4 слов, если это имеет смысл. " +
                                "Ответ должен быть только на русском языке. " +
                                "Все слова в ответе должны находиться в нормальной форме без склонений и спряжений. " +
                                "В ответе не пиши словосочетание \"ключевые слова\", напиши только сами слова через запятую.";
                        
                        String answer = llmService.chat(content);
                        List<Keyword> keywordList = Arrays.stream(answer.split(","))
                                .map(String::trim)
                                .filter(k -> !k.isEmpty())
                                .map(k -> {
                                    Keyword keyword = new Keyword();
                                    keyword.setKeyword(k);
                                    return keyword;
                                })
                                .toList();  // выделяем ключевые слова

                        List<Keyword> existingKeywords = keywordList.stream()
                                .filter(k -> keywordRepository.existsByKeyword(k.getKeyword()))
                                .map(k -> keywordRepository.findByKeyword(k.getKeyword()))
                                .toList();  // если ключевые слова уже есть в БД, добавляем их из БД

                        // ключевые слова, которые войдут для данной компетенции
                        List<Keyword> newKeywords = keywordList.stream()
                                .filter(k -> !keywordRepository.existsByKeyword(k.getKeyword()))
                                .map(keywordRepository::save)
                                .toList();

                        List<Keyword> allKeywords = new java.util.ArrayList<>(existingKeywords);
                        allKeywords.addAll(newKeywords);
                        competency.setKeywords(allKeywords);    // если ключевые слова уже есть в БД, добавляем их из БД
                        
                        Competency updated = competencyRepository.save(competency);
                        return ResponseEntity.ok((Object) CompetencyDto.fromEntity(updated));
                        
                    } catch (IllegalArgumentException e) {
                        // Unsupported LLM provider
                        ErrorResponse error = new ErrorResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            "Неподдерживаемый LLM провайдер",
                            "Провайдер '" + modelProvider + "' не поддерживается. Доступные: ollama, gigachat"
                        );
                        return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(error);
                                
                    } catch (RuntimeException e) {
                        // Check if it's an authentication error
                        String errorMessage = e.getMessage();
                        if (errorMessage != null && (
                                errorMessage.contains("401") || 
                                errorMessage.contains("Unauthorized") ||
                                errorMessage.contains("authentication") ||
                                errorMessage.contains("Authorization error"))) {
                            
                            ErrorResponse error = new ErrorResponse(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Ошибка авторизации LLM сервиса",
                                "Не удалось авторизоваться в " + modelProvider + ". Проверьте токен доступа: " + errorMessage
                            );
                            return ResponseEntity
                                    .status(HttpStatus.UNAUTHORIZED)
                                    .body(error);
                        }
                        
                        // Other runtime errors
                        ErrorResponse error = new ErrorResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Ошибка при обращении к LLM сервису",
                            "Провайдер: " + modelProvider + ". Детали: " + errorMessage
                        );
                        return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(error);
                    }
                });
        return result.orElse(ResponseEntity.notFound().build());
    }
}
