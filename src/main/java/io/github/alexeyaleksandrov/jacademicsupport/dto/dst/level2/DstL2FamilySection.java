package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level2;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstJsonFields;
import java.util.List;
import lombok.Data;

@Data
@DstJsonFields
public class DstL2FamilySection {
    private String domain;
    private String techFamily;
    private int    familyHours;
    private int nSkills;
    private List<DstL2SkillResult> skills;
}