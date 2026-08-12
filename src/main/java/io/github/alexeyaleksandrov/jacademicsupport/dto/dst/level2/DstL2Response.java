package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level2;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService.ProfessionWeight;
import lombok.Data;

import java.util.List;

@Data
public class DstL2Response {
    private Long   curriculumId;
    private String curriculumName;
    private String domain;
    private String techFamily;
    private int    totalFamilyHours;
    @JsonProperty("nSkills") private int nSkills;
    private String error;

    private List<ProfessionWeight> professions;
    private List<DstL2SkillResult> skills;
}
