package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.BpaResult;
import lombok.Data;

import java.util.List;

@Data
public class DstTraceResponse {
    private List<BpaResult>           sources;
    private List<DstCombinationTrace> combinations;

    @JsonProperty("mT") private double  mT;
    @JsonProperty("mU") private double  mU;
    @JsonProperty("mF") private double  mF;
    @JsonProperty("K")  private double  K;
    private double  betp;
    private double  delta;
    private double  supply;
    private int     nClusters;
    private boolean usedYager;
    private String  recommendation;
    private String  error;
}
