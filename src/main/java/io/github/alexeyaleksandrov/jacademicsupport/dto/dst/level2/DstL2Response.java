package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level2;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstJsonFields;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstLevelMeta;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService.ProfessionWeight;
import java.util.List;
import lombok.Data;

@Data
@DstJsonFields
public class DstL2Response {
    private Long   curriculumId;
    private String curriculumName;
    private String domain;
    private String techFamily;
    private int    totalFamilyHours;
    private int nSkills;
    private String error;

    private List<ProfessionWeight> professions;
    private List<DstL2SkillResult> skills;

    private DstLevelMeta meta;
}