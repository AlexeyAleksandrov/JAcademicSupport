package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level1;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService.ProfessionWeight;
import lombok.Data;

import java.util.List;

@Data
public class DstL1Response {
    private Long   curriculumId;
    private String curriculumName;
    private String domain;
    private int    totalDomainHours;
    @JsonProperty("nFamilies") private int nFamilies;
    private String error;

    private List<ProfessionWeight>  professions;
    private List<DstL1FamilyResult> families;
}
