package io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.detail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompetencyWithIndicatorsDto {
    private Long id;
    private String number;
    private String description;
    private List<IndicatorDto> indicators;
}
