package io.github.alexeyaleksandrov.jacademicsupport.dtos.expert;

import lombok.Data;

@Data
public class ExpertOpinionResponseDto {
    private Long id;
    private Long expertId;
    private Long competencyAchievementIndicatorId;
    private Long workSkillId;
    private double skillImportance; // New field: importance of the skill, between 0 and 1
}
