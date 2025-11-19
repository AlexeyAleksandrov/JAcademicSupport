package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.expert;

import io.github.alexeyaleksandrov.jacademicsupport.dtos.expert.ExpertOpinionSkillsGroupRequestDto;
import io.github.alexeyaleksandrov.jacademicsupport.dtos.expert.ExpertOpinionSkillsGroupResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertOpinionSkillsGroupEntity;
import io.github.alexeyaleksandrov.jacademicsupport.services.expert.ExpertOpinionSkillsGroupService;
import io.github.alexeyaleksandrov.jacademicsupport.services.expert.ExpertService;
import io.github.alexeyaleksandrov.jacademicsupport.services.competency.CompetencyAchievementIndicatorService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.SkillsGroupsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/expert-opinions-skills-groups")
@AllArgsConstructor
public class ExpertOpinionSkillsGroupController {
    private final ExpertOpinionSkillsGroupService expertOpinionSkillsGroupService;
    private final ExpertService expertService;
    private final CompetencyAchievementIndicatorService competencyAchievementIndicatorService;
    private final SkillsGroupsService skillsGroupsService;

    @GetMapping
    public ResponseEntity<List<ExpertOpinionSkillsGroupResponseDto>> getAllExpertOpinionsSkillsGroups() {
        List<ExpertOpinionSkillsGroupEntity> expertOpinions = expertOpinionSkillsGroupService.findAll();
        List<ExpertOpinionSkillsGroupResponseDto> responseDtos = expertOpinions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExpertOpinionSkillsGroupById(@PathVariable Long id) {
        try {
            ExpertOpinionSkillsGroupEntity expertOpinion = expertOpinionSkillsGroupService.findById(id);
            if (expertOpinion == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(convertToDto(expertOpinion));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid ID provided: " + e.getMessage());
        }
    }

    @GetMapping("/expert/{expertId}")
    public ResponseEntity<List<ExpertOpinionSkillsGroupResponseDto>> getByExpertId(@PathVariable Long expertId) {
        List<ExpertOpinionSkillsGroupEntity> expertOpinions = expertOpinionSkillsGroupService.findByExpertId(expertId);
        List<ExpertOpinionSkillsGroupResponseDto> responseDtos = expertOpinions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/indicator/{indicatorId}")
    public ResponseEntity<List<ExpertOpinionSkillsGroupResponseDto>> getByIndicatorId(@PathVariable Long indicatorId) {
        List<ExpertOpinionSkillsGroupEntity> expertOpinions = 
                expertOpinionSkillsGroupService.findByCompetencyAchievementIndicatorId(indicatorId);
        List<ExpertOpinionSkillsGroupResponseDto> responseDtos = expertOpinions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/skills-group/{skillsGroupId}")
    public ResponseEntity<List<ExpertOpinionSkillsGroupResponseDto>> getBySkillsGroupId(@PathVariable Long skillsGroupId) {
        List<ExpertOpinionSkillsGroupEntity> expertOpinions = 
                expertOpinionSkillsGroupService.findBySkillsGroupId(skillsGroupId);
        List<ExpertOpinionSkillsGroupResponseDto> responseDtos = expertOpinions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/importance/{minImportance}")
    public ResponseEntity<List<ExpertOpinionSkillsGroupResponseDto>> getByMinImportance(
            @PathVariable double minImportance) {
        List<ExpertOpinionSkillsGroupEntity> expertOpinions = 
                expertOpinionSkillsGroupService.findByGroupImportanceGreaterThanEqual(minImportance);
        List<ExpertOpinionSkillsGroupResponseDto> responseDtos = expertOpinions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @PostMapping
    public ResponseEntity<?> createExpertOpinionSkillsGroup(@RequestBody ExpertOpinionSkillsGroupRequestDto requestDto) {
        try {
            // Check for duplicate
            if (expertOpinionSkillsGroupService.existsByExpertIdAndCompetencyAchievementIndicatorIdAndSkillsGroupId(
                    requestDto.getExpertId(), 
                    requestDto.getCompetencyAchievementIndicatorId(), 
                    requestDto.getSkillsGroupId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Expert opinion for this combination already exists");
            }

            ExpertOpinionSkillsGroupEntity entity = convertToEntity(requestDto);
            ExpertOpinionSkillsGroupEntity savedEntity = expertOpinionSkillsGroupService.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedEntity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpertOpinionSkillsGroup(
            @PathVariable Long id, 
            @RequestBody ExpertOpinionSkillsGroupRequestDto requestDto) {
        try {
            ExpertOpinionSkillsGroupEntity existingEntity = expertOpinionSkillsGroupService.findById(id);
            if (existingEntity == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check for duplicate (excluding current entity)
            ExpertOpinionSkillsGroupEntity duplicate = 
                    expertOpinionSkillsGroupService.findByExpertIdAndCompetencyAchievementIndicatorIdAndSkillsGroupId(
                            requestDto.getExpertId(), 
                            requestDto.getCompetencyAchievementIndicatorId(), 
                            requestDto.getSkillsGroupId());
            
            if (duplicate != null && !duplicate.getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Expert opinion for this combination already exists");
            }

            ExpertOpinionSkillsGroupEntity updatedEntity = convertToEntity(requestDto);
            updatedEntity.setId(id);
            ExpertOpinionSkillsGroupEntity savedEntity = expertOpinionSkillsGroupService.save(updatedEntity);
            return ResponseEntity.ok(convertToDto(savedEntity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpertOpinionSkillsGroup(@PathVariable Long id) {
        try {
            ExpertOpinionSkillsGroupEntity expertOpinion = expertOpinionSkillsGroupService.findById(id);
            if (expertOpinion == null) {
                return ResponseEntity.notFound().build();
            }
            expertOpinionSkillsGroupService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid ID provided: " + e.getMessage());
        }
    }

    private ExpertOpinionSkillsGroupResponseDto convertToDto(ExpertOpinionSkillsGroupEntity entity) {
        ExpertOpinionSkillsGroupResponseDto dto = new ExpertOpinionSkillsGroupResponseDto();
        dto.setId(entity.getId());
        dto.setExpertId(entity.getExpert() != null ? entity.getExpert().getId() : null);
        dto.setCompetencyAchievementIndicatorId(
                entity.getCompetencyAchievementIndicator() != null ? 
                        entity.getCompetencyAchievementIndicator().getId() : null);
        dto.setSkillsGroupId(entity.getSkillsGroup() != null ? entity.getSkillsGroup().getId() : null);
        dto.setGroupImportance(entity.getGroupImportance()); 
        return dto;
    }

    private ExpertOpinionSkillsGroupEntity convertToEntity(ExpertOpinionSkillsGroupRequestDto dto) {
        ExpertOpinionSkillsGroupEntity entity = new ExpertOpinionSkillsGroupEntity();
        entity.setGroupImportance(dto.getGroupImportance());
        
        // Fetch and set relationships
        try {
            entity.setExpert(expertService.findById(dto.getExpertId()));
            entity.setCompetencyAchievementIndicator(
                    competencyAchievementIndicatorService.findById(dto.getCompetencyAchievementIndicatorId()));
            entity.setSkillsGroup(skillsGroupsService.findById(dto.getSkillsGroupId()).orElseThrow());
        } catch (Exception e) {
            throw new RuntimeException("Invalid ID provided: " + e.getMessage());
        }
        
        return entity;
    }
}
