package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level1;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstJsonFields;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstLevelMeta;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService.ProfessionWeight;
import java.util.List;
import lombok.Data;

@Data
@DstJsonFields
public class DstL1Response {
    private Long   curriculumId;
    private String curriculumName;
    private String domain;
    private int    totalDomainHours;
    private int nFamilies;
    private String error;

    private List<ProfessionWeight>  professions;
    private List<DstL1FamilyResult> families;

    private DstLevelMeta meta;
}