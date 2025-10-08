package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.expert;

import io.github.alexeyaleksandrov.jacademicsupport.dtos.expert.ExpertOpinionRequestDto;
import io.github.alexeyaleksandrov.jacademicsupport.dtos.expert.ExpertOpinionResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertOpinionEntity;
import io.github.alexeyaleksandrov.jacademicsupport.services.ExpertOpinionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expert-opinions")
public class ExpertOpinionController {
    private final ExpertOpinionService expertOpinionService;

    public ExpertOpinionController(ExpertOpinionService expertOpinionService) {
        this.expertOpinionService = expertOpinionService;
    }

    @GetMapping
    public ResponseEntity<List<ExpertOpinionResponseDto>> getAllExpertOpinions() {
        List<ExpertOpinionEntity> expertOpinions = expertOpinionService.findAll();
        List<ExpertOpinionResponseDto> responseDtos = expertOpinions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpertOpinionResponseDto> getExpertOpinionById(@PathVariable Long id) {
        ExpertOpinionEntity expertOpinion = expertOpinionService.findById(id);
        if (expertOpinion == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convertToDto(expertOpinion));
    }

    @PostMapping
    public ResponseEntity<ExpertOpinionResponseDto> createExpertOpinion(@RequestBody ExpertOpinionRequestDto requestDto) {
        ExpertOpinionEntity entity = convertToEntity(requestDto);
        ExpertOpinionEntity savedEntity = expertOpinionService.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedEntity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpertOpinionResponseDto> updateExpertOpinion(@PathVariable Long id, @RequestBody ExpertOpinionRequestDto requestDto) {
        ExpertOpinionEntity existingEntity = expertOpinionService.findById(id);
        if (existingEntity == null) {
            return ResponseEntity.notFound().build();
        }
        
        ExpertOpinionEntity updatedEntity = convertToEntity(requestDto);
        updatedEntity.setId(id);
        ExpertOpinionEntity savedEntity = expertOpinionService.save(updatedEntity);
        return ResponseEntity.ok(convertToDto(savedEntity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpertOpinion(@PathVariable Long id) {
        ExpertOpinionEntity expertOpinion = expertOpinionService.findById(id);
        if (expertOpinion == null) {
            return ResponseEntity.notFound().build();
        }
        expertOpinionService.deleteById(id);
        return ResponseEntity.noContent().build();
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
        return entity;
    }
}
