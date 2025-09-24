package io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.indicator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorDto {
    private Long id;
    private String number;
    private String description;
    private String indicatorKnow;
    private String indicatorAble;
    private String indicatorPossess;
    private Set<String> keywords;
    private String competencyNumber;
}
