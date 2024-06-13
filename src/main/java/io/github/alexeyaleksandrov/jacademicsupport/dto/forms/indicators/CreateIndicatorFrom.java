package io.github.alexeyaleksandrov.jacademicsupport.dto.forms.indicators;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CreateIndicatorFrom {
    private String number;
    private String description;
    private String indicatorKnow;
    private String indicatorAble;
    private String indicatorPossess;
    private Long competencyId;
}
