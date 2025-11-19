package io.github.alexeyaleksandrov.jacademicsupport.dtos.expert;

import lombok.Data;

@Data
public class ExpertOpinionSkillsGroupResponseDto {
    private Long id;
    private Long expertId;
    private Long competencyAchievementIndicatorId;
    private Long skillsGroupId;
    private double groupImportance; // Importance of the skills group, between 0 and 1
}
