package io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisciplineDto {
    private Long curriculumId;
    private String name;
    private Integer totalHours;
    private Integer semester;
}
