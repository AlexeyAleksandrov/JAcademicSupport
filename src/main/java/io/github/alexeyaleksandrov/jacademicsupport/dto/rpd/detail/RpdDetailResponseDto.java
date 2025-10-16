package io.github.alexeyaleksandrov.jacademicsupport.dto.rpd.detail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpdDetailResponseDto {
    private Long id;
    private String disciplineName;
    private Integer year;
    private List<CompetencyWithIndicatorsDto> competencies;
}
