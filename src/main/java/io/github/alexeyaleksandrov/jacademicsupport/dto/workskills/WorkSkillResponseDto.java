package io.github.alexeyaleksandrov.jacademicsupport.dto.workskills;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkSkillResponseDto {
    private Long id;
    private String description;
    private Long skillsGroupId; // ID of the associated skills group
}
