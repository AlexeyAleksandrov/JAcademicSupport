package io.github.alexeyaleksandrov.jacademicsupport.dtos.expert;

import lombok.Data;

@Data
public class ExpertOpinionResponseDto {
    private Long id;
    private Long expertId;
    private Long competencyAchievementIndicatorId;
    private Long workSkillId;
    private double skillImportance;
    private Long canonicalId;
    private String canonicalName;
    private String direction;
    private String professionCode;
    private String domain;
    private String techFamily;
}
