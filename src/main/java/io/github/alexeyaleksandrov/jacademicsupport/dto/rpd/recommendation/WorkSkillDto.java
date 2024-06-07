package io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation;

import lombok.*;

import java.io.Serializable;

/**
 * DTO for {@link io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class WorkSkillDto implements Serializable {
    String description;
}