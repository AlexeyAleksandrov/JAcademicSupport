package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DstL2FamilySection {
    private String domain;
    private String techFamily;
    private int    familyHours;
    @JsonProperty("nSkills") private int nSkills;
    private List<DstL2SkillResult> skills;
}
