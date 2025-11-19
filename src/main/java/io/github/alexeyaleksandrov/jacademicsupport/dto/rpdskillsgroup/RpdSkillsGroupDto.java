package io.github.alexeyaleksandrov.jacademicsupport.dto.rpdskillsgroup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpdSkillsGroupDto {
    private Long rpdId;
    private Long skillsGroupId;
    private Integer time; // время в академических часах
}
