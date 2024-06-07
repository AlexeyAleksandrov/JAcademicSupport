package io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation;

import lombok.*;

import java.io.Serializable;

/**
 * DTO for {@link io.github.alexeyaleksandrov.jacademicsupport.models.RecommendedSkill}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RecommendedSkillDto implements Serializable {
    WorkSkillDto workSkill;
    Double coefficient;
}