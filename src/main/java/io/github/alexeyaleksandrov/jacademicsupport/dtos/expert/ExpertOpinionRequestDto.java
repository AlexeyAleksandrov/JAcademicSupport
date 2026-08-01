package io.github.alexeyaleksandrov.jacademicsupport.dtos.expert;

import lombok.Data;

@Data
public class ExpertOpinionRequestDto {
    private Long expertId;
    private Long competencyAchievementIndicatorId;
    private Long workSkillId;
    private double skillImportance;
    private Long canonicalId;
    private String direction = "POSITIVE";
    private String professionCode;
    private String domain;
    private String techFamily;
}
