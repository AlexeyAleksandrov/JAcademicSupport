package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level1;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DstL1DomainSection {
    private String domain;
    private int    domainHours;
    @JsonProperty("nFamilies") private int nFamilies;

    private List<DstL1FamilyResult> families;
}
