package io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisciplineCoverageResponseDto {
    private Long id;
    private Long disciplineId;
    private String professionCode;
    private String professionName;
    private String domain;
    private String techFamily;
    private Long canonicalId;
    private String canonicalName;
    private Integer hours;
}
