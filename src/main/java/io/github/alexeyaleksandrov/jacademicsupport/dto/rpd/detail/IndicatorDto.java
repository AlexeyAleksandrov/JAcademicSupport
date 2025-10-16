package io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.detail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndicatorDto {
    private Long id;
    private String number;
    private String description;
    private String indicatorKnow;
    private String indicatorAble;
    private String indicatorPossess;
}
