package io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurriculumDto {
    private String name;
    private String specialization;
    private String profile;
    private Integer academicYear;
}
