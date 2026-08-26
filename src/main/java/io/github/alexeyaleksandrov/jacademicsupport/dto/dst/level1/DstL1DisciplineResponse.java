package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level1;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstJsonFields;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstLevelMeta;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService.ProfessionWeight;
import java.util.List;
import lombok.Data;

@Data
@DstJsonFields
public class DstL1DisciplineResponse {
    private Long   disciplineId;
    private String disciplineName;
    private int    totalHours;
    private String error;

    private List<ProfessionWeight>  professions;
    private List<DstL1DomainSection> domains;

    private DstLevelMeta meta;
}