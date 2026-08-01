package io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurriculumResponseDto {
    private Long id;
    private String name;
    private String specialization;
    private String profile;
    private Integer academicYear;
    private List<DisciplineResponseDto> disciplines;
}
