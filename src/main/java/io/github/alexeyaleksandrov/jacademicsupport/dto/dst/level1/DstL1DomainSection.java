package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level1;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstJsonFields;
import java.util.List;
import lombok.Data;

@Data
@DstJsonFields
public class DstL1DomainSection {
    private String domain;
    private int    domainHours;
    private int nFamilies;

    private List<DstL1FamilyResult> families;
}