package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DstMass {
    @JsonProperty("mT") private double mT;
    @JsonProperty("mU") private double mU;
    @JsonProperty("mF") private double mF;
}
