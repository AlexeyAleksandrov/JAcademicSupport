package io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstJsonFields;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DstJsonFields
public class BpaResult {
    private String sourceName;
    private boolean enabled;

    private int totalCount;
    private int relevantCount;
    private double averageScore;
    private double agreementLevel;
    private double lambda;
    private double weight;

    /** Number of negative sources/experts behind m(F); 0 means no negative evidence. */
    private int    negativeCount;
    private double averageNegativeScore;

    private double kappa;
    private double mT;
    private double mU;
    private double mF;

    private double mTDiscounted;
    private double mUDiscounted;
    private double mFDiscounted;

    public static BpaResult disabled(String name) {
        BpaResult r = new BpaResult();
        r.setSourceName(name);
        r.setEnabled(false);
        return r;
    }
}