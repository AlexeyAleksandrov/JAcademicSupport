package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level2;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService.ProfessionWeight;
import lombok.Data;

import java.util.List;

@Data
public class DstL2DisciplineResponse {
    private Long   disciplineId;
    private String disciplineName;
    private int    totalHours;
    private String error;

    private List<ProfessionWeight>  professions;
    private List<DstL2FamilySection> sections;
}
