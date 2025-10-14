package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.keywords;

import io.github.alexeyaleksandrov.jacademicsupport.dto.keywords.CreateKeywordRequest;
import io.github.alexeyaleksandrov.jacademicsupport.dto.keywords.KeywordDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.keywords.UpdateKeywordRequest;
import io.github.alexeyaleksandrov.jacademicsupport.models.Keyword;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.KeywordRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillsGroupRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillsService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/keywords")
@AllArgsConstructor
public class KeywordsRestController {
    final KeywordRepository keywordRepository;
    final SkillsGroupRepository skillsGroupRepository;
    final WorkSkillRepository workSkillRepository;
    final OllamaService ollamaService;
    final WorkSkillsService workSkillsService;

    @GetMapping
    public ResponseEntity<List<KeywordDto>> getAllKeywords() {
        List<Keyword> keywords = keywordRepository.findAll();
        List<KeywordDto> response = keywords.stream()
                .map(KeywordDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KeywordDto> getKeywordById(@PathVariable Long id) {
        return keywordRepository.findById(id)
                .map(KeywordDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<KeywordDto> createKeyword(@Valid @RequestBody CreateKeywordRequest request) {
        // Check if keyword already exists
        if (keywordRepository.existsByKeyword(request.getKeyword())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Keyword keyword = new Keyword();
        keyword.setKeyword(request.getKeyword());

        // Set work skills if provided
        if (request.getWorkSkillIds() != null && !request.getWorkSkillIds().isEmpty()) {
            List<WorkSkill> workSkills = request.getWorkSkillIds().stream()
                    .map(wsId -> workSkillRepository.findById(wsId)
                            .orElseThrow(() -> new RuntimeException("WorkSkill not found with id: " + wsId)))
                    .collect(Collectors.toList());
            keyword.setWorkSkills(workSkills);
        }

        Keyword savedKeyword = keywordRepository.save(keyword);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(KeywordDto.fromEntity(savedKeyword));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KeywordDto> updateKeyword(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKeywordRequest request) {

        return keywordRepository.findById(id)
                .map(keyword -> {
                    keyword.setKeyword(request.getKeyword());

                    // Update work skills if provided
                    if (request.getWorkSkillIds() != null) {
                        List<WorkSkill> workSkills = request.getWorkSkillIds().stream()
                                .map(wsId -> workSkillRepository.findById(wsId)
                                        .orElseThrow(() -> new RuntimeException("WorkSkill not found with id: " + wsId)))
                                .collect(Collectors.toList());
                        keyword.setWorkSkills(workSkills);
                    }

                    Keyword updatedKeyword = keywordRepository.save(keyword);
                    return ResponseEntity.ok(KeywordDto.fromEntity(updatedKeyword));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKeyword(@PathVariable Long id) {
        return keywordRepository.findById(id)
                .map(keyword -> {
                    keywordRepository.delete(keyword);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/match/all")
    public ResponseEntity<List<Keyword>> matchKeywordsToWorkSkills() {
        return ResponseEntity.ok(workSkillsService.matchKeywordsToWorkSkills());
    }
}
