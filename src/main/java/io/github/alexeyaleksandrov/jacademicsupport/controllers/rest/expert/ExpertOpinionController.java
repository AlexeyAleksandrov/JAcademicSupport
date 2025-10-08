package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.expert;

import io.github.alexeyaleksandrov.jacademicsupport.dtos.expert.ExpertOpinionRequestDto;
import io.github.alexeyaleksandrov.jacademicsupport.dtos.expert.ExpertOpinionResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertOpinionEntity;
import io.github.alexeyaleksandrov.jacademicsupport.services.ExpertOpinionService;
import io.github.alexeyaleksandrov.jacademicsupport.services.expert.ExpertService;
import io.github.alexeyaleksandrov.jacademicsupport.services.competency.CompetencyAchievementIndicatorService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expert-opinions")
@AllArgsConstructor
public class ExpertOpinionController {
    private final ExpertOpinionService expertOpinionService;
    private final ExpertService expertService;
    private final CompetencyAchievementIndicatorService competencyAchievementIndicatorService;
    private final WorkSkillService workSkillService;

    @GetMapping
    public ResponseEntity<List<ExpertOpinionResponseDto>> getAllExpertOpinions() {
        List<ExpertOpinionEntity> expertOpinions = expertOpinionService.findAll();
        List<ExpertOpinionResponseDto> responseDtos = expertOpinions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExpertOpinionById(@PathVariable Long id) {
        try {
            ExpertOpinionEntity expertOpinion = expertOpinionService.findById(id);
            if (expertOpinion == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(convertToDto(expertOpinion));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid ID provided: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createExpertOpinion(@RequestBody ExpertOpinionRequestDto requestDto) {
        try {
            ExpertOpinionEntity entity = convertToEntity(requestDto);
            ExpertOpinionEntity savedEntity = expertOpinionService.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedEntity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpertOpinion(@PathVariable Long id, @RequestBody ExpertOpinionRequestDto requestDto) {
        try {
            ExpertOpinionEntity existingEntity = expertOpinionService.findById(id);
            if (existingEntity == null) {
                return ResponseEntity.notFound().build();
            }
            
            ExpertOpinionEntity updatedEntity = convertToEntity(requestDto);
            updatedEntity.setId(id);
            ExpertOpinionEntity savedEntity = expertOpinionService.save(updatedEntity);
            return ResponseEntity.ok(convertToDto(savedEntity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpertOpinion(@PathVariable Long id) {
        try {
            ExpertOpinionEntity expertOpinion = expertOpinionService.findById(id);
            if (expertOpinion == null) {
                return ResponseEntity.notFound().build();
            }
            expertOpinionService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid ID provided: " + e.getMessage());
        }
    }

    private ExpertOpinionResponseDto convertToDto(ExpertOpinionEntity entity) {
        ExpertOpinionResponseDto dto = new ExpertOpinionResponseDto();
        dto.setId(entity.getId());
        dto.setExpertId(entity.getExpert() != null ? entity.getExpert().getId() : null);
        dto.setCompetencyAchievementIndicatorId(entity.getCompetencyAchievementIndicator() != null ? entity.getCompetencyAchievementIndicator().getId() : null);
        dto.setWorkSkillId(entity.getWorkSkill() != null ? entity.getWorkSkill().getId() : null);
        dto.setSkillImportance(entity.getSkillImportance()); 
        return dto;
    }

    private ExpertOpinionEntity convertToEntity(ExpertOpinionRequestDto dto) {
        ExpertOpinionEntity entity = new ExpertOpinionEntity();
        entity.setSkillImportance(dto.getSkillImportance());
        
        // Fetch and set relationships
        try {
            entity.setExpert(expertService.findById(dto.getExpertId()));
            entity.setCompetencyAchievementIndicator(competencyAchievementIndicatorService.findById(dto.getCompetencyAchievementIndicatorId()));
            entity.setWorkSkill(workSkillService.findById(dto.getWorkSkillId()).orElseThrow());
        } catch (Exception e) {
            throw new RuntimeException("Invalid ID provided: " + e.getMessage());
        }
        
        return entity;
    }
}
