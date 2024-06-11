package io.github.alexeyaleksandrov.jacademicsupport.dto.forms.rpd;

import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RpdFormDto {
    private String disciplineName;
    private int year;
    private List<Long> selectedIndicators = new ArrayList<>();
}
