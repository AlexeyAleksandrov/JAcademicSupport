package io.github.alexeyaleksandrov.jacademicsupport.dto.forms.workskills;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EditWorkSkillFormDto {
    private Long skillsGroupId;
}
