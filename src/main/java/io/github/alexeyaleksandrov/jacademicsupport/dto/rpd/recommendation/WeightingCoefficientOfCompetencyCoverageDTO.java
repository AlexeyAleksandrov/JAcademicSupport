package io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class WeightingCoefficientOfCompetencyCoverageDTO {
    private SkillsGroup skillsGroup;
    private double coefficient;
}
