package io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation;

import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CoefficientWorkSkillComplianceWithDisciplineDTO {
    private WorkSkill workSkill;
    private double coefficient;
}
