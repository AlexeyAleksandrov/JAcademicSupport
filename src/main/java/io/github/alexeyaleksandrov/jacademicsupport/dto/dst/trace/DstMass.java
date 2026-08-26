package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstJsonFields;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DstJsonFields
public class DstMass {
    private double mT;
    private double mU;
    private double mF;
}