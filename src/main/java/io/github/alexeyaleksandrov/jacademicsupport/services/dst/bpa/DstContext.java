package io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DstContext {
    private String professionCode;
    private String domain;
    private String techFamily;
    private Long canonicalId;
}
