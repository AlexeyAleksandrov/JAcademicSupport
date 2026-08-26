package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level0;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstJsonFields;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstCombinationTrace;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.BpaResult;
import java.util.List;
import lombok.Data;

@Data
@DstJsonFields
public class DstL0DomainResult {
    private String domain;

    private double mT;
    private double mU;
    private double mF;
    private double K;

    private double  betp;
    private double  supply;
    private int     supplyHours;
    private double  delta;
    private boolean usedYager;
    private String  recommendation;

    private List<BpaResult>           sources;
    private List<DstCombinationTrace> combinations;
}