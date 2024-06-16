package io.github.alexeyaleksandrov.jacademicsupport.dto.forms.workskills;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CreateSkillsGroupFormDto {
    private String name;
}
