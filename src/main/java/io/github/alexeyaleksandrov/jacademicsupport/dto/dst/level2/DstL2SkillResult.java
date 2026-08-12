package io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level2;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstCombinationTrace;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.BpaResult;
import lombok.Data;

import java.util.List;

@Data
public class DstL2SkillResult {
    private Long   canonicalId;
    private String skillName;

    @JsonProperty("mT") private double mT;
    @JsonProperty("mU") private double mU;
    @JsonProperty("mF") private double mF;
    @JsonProperty("K")  private double K;

    private double  betp;
    private double  supply;
    private int     supplyHours;
    private double  delta;
    private boolean usedYager;
    private String  recommendation;

    private List<BpaResult>           sources;
    private List<DstCombinationTrace> combinations;
}
