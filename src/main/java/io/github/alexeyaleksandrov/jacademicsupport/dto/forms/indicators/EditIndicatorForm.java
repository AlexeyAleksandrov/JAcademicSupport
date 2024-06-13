package io.github.alexeyaleksandrov.jacademicsupport.dto.forms.indicators;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class EditIndicatorForm {
    private String number;
    private String description;
    private String indicatorKnow;
    private String indicatorAble;
    private String indicatorPossess;
    private Long competencyId;
    private List<Long> selectedKeywords;
}
