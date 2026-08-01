package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level0;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService.ProfessionWeight;
import lombok.Data;

import java.util.List;

@Data
public class DstL0Response {
    private Long   curriculumId;
    private String curriculumName;
    private int    totalCurriculumHours;
    private String error;

    private List<ProfessionWeight>  professions;
    private List<DstL0DomainResult> domains;
}
