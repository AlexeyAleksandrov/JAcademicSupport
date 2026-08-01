package io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisciplineResponseDto {
    private Long id;
    private Long curriculumId;
    private String name;
    private Integer totalHours;
    private Integer semester;
    private List<DisciplineCoverageResponseDto> coverage;
}
