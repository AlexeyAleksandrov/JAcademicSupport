package io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CreateRpdDTO {
    String discipline_name;
    Integer year;
    List<String> competencyAchievementIndicators;
}
