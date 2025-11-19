package io.github.alexeyaleksandrov.jacademicsupport.dto.foresight;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForesightSkillsGroupDto {
    private Long skillsGroupId;  // ID of referenced SkillsGroup
    private String sourceName;
    private String sourceUrl;
}
