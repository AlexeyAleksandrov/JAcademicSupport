package io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation;

import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RpdSkillsGroupForCompetencyIndicatorDTO {
    private CompetencyAchievementIndicator competencyAchievementIndicator;
    private List<SkillsGroup> skillsGroups;
}
