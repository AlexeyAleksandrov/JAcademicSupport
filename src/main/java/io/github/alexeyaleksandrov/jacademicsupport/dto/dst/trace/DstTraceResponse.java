package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstJsonFields;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.BpaResult;
import java.util.List;
import lombok.Data;

@Data
@DstJsonFields
public class DstTraceResponse {
    private List<BpaResult>           sources;
    private List<DstCombinationTrace> combinations;

    private double  mT;
    private double  mU;
    private double  mF;
    private double  K;
    private double  betp;
    private double  delta;
    private double  supply;
    private int     nClusters;
    private boolean usedYager;
    private String  recommendation;
    private String  error;
}