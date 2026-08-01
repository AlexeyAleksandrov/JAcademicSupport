package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.expert;

import io.github.alexeyaleksandrov.jacademicsupport.dtos.expert.ExpertOpinionRequestDto;
import io.github.alexeyaleksandrov.jacademicsupport.dtos.expert.ExpertOpinionResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertOpinionEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillCanonicalRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.ExpertOpinionService;
import io.github.alexeyaleksandrov.jacademicsupport.services.expert.ExpertService;
import io.github.alexeyaleksandrov.jacademicsupport.services.competency.CompetencyAchievementIndicatorService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
    private final SkillCanonicalRepository skillCanonicalRepository;

    @GetMapping
    public ResponseEntity<List<ExpertOpinionResponseDto>> getAllExpertOpinions() {
        List<ExpertOpinionEntity> expertOpinions = expertOpinionService.findAll();
        Map<Long, String> nameMap = buildNameMap(expertOpinions.stream()
                .map(ExpertOpinionEntity::getCanonicalId).filter(id -> id != null).distinct().toList());
        List<ExpertOpinionResponseDto> responseDtos = expertOpinions.stream()
                .map(e -> convertToDto(e, nameMap))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    private Map<Long, String> buildNameMap(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        return skillCanonicalRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(SkillCanonical::getId, SkillCanonical::getName));
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
        return convertToDto(entity, null);
    }

    private ExpertOpinionResponseDto convertToDto(ExpertOpinionEntity entity, Map<Long, String> nameMap) {
        ExpertOpinionResponseDto dto = new ExpertOpinionResponseDto();
        dto.setId(entity.getId());
        dto.setExpertId(entity.getExpert() != null ? entity.getExpert().getId() : null);
        dto.setCompetencyAchievementIndicatorId(entity.getCompetencyAchievementIndicator() != null ? entity.getCompetencyAchievementIndicator().getId() : null);
        dto.setWorkSkillId(entity.getWorkSkill() != null ? entity.getWorkSkill().getId() : null);
        dto.setSkillImportance(entity.getSkillImportance());
        dto.setCanonicalId(entity.getCanonicalId());
        if (nameMap != null && entity.getCanonicalId() != null) {
            dto.setCanonicalName(nameMap.get(entity.getCanonicalId()));
        } else if (entity.getCanonicalId() != null) {
            skillCanonicalRepository.findById(entity.getCanonicalId()).ifPresent(sc -> dto.setCanonicalName(sc.getName()));
        }
        dto.setDirection(entity.getDirection());
        dto.setProfessionCode(entity.getProfessionCode());
        dto.setDomain(entity.getDomain());
        dto.setTechFamily(entity.getTechFamily());
        return dto;
    }

    private ExpertOpinionEntity convertToEntity(ExpertOpinionRequestDto dto) {
        ExpertOpinionEntity entity = new ExpertOpinionEntity();
        entity.setSkillImportance(dto.getSkillImportance());
        entity.setCanonicalId(dto.getCanonicalId());
        entity.setDirection(dto.getDirection() != null ? dto.getDirection() : "POSITIVE");
        entity.setProfessionCode(dto.getProfessionCode());
        entity.setDomain(dto.getDomain());
        entity.setTechFamily(dto.getTechFamily());

        try {
            if (dto.getExpertId() != null) {
                entity.setExpert(expertService.findById(dto.getExpertId()));
            }
            if (dto.getCompetencyAchievementIndicatorId() != null) {
                entity.setCompetencyAchievementIndicator(competencyAchievementIndicatorService.findById(dto.getCompetencyAchievementIndicatorId()));
            }
            if (dto.getWorkSkillId() != null) {
                entity.setWorkSkill(workSkillService.findById(dto.getWorkSkillId()).orElse(null));
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid ID provided: " + e.getMessage());
        }

        return entity;
    }
}
