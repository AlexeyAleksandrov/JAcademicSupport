package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DstCombinationTrace {
    private String  label;
    @JsonProperty("K") private double  K;
    private String  rule;
    private DstMass output;
}
