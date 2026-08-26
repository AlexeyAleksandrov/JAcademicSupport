package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstJsonFields;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DstJsonFields
public class DstCombinationTrace {
    private String  label;
    private double  K;
    private String  rule;
    private DstMass output;
}