package io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BpaResult {
    private String sourceName;
    private boolean enabled;

    private int totalCount;
    private int relevantCount;
    private double averageScore;
    private double agreementLevel;
    private double lambda;
    private double weight;

    private double kappa;
    @JsonProperty("mT") private double mT;
    @JsonProperty("mU") private double mU;
    @JsonProperty("mF") private double mF;

    @JsonProperty("mTDiscounted") private double mTDiscounted;
    @JsonProperty("mUDiscounted") private double mUDiscounted;
    @JsonProperty("mFDiscounted") private double mFDiscounted;

    public static BpaResult disabled(String name) {
        BpaResult r = new BpaResult();
        r.setSourceName(name);
        r.setEnabled(false);
        return r;
    }
}
