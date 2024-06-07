package io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link io.github.alexeyaleksandrov.jacademicsupport.models.Rpd}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RpdDto implements Serializable {
    String disciplineName;
    int year;
    List<RecommendedSkillDto> recommendedSkills;
}