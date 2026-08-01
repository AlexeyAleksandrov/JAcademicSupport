package io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisciplineCoverageDto {
    private String professionCode;
    private String domain;
    private String techFamily;
    private Long canonicalId;
    private Integer hours;
}
