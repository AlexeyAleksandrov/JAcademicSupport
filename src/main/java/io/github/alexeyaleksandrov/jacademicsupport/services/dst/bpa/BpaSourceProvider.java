package io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService;

/**
 * Pluggable BPA source for DST aggregation.
 * Implementations: VAC, EXP, FC, STD (stub, disabled).
 * To add/remove a source: create/delete a Spring @Component bean.
 */
public interface BpaSourceProvider {

    String getName();

    boolean isEnabled();

    double getLambda();

    double getWeight();

    BpaResult compute(DstContext ctx);

    default BpaResult buildFromRaw(DstQueryService.BpaResult raw) {
        BpaResult b = new BpaResult();
        b.setSourceName(getName());
        b.setEnabled(true);
        b.setRelevantCount((int) raw.relevantCount());
        b.setTotalCount((int) raw.totalCount());
        b.setAverageScore(raw.averageScore());
        b.setAgreementLevel(1.0);
        b.setLambda(raw.lambda() > 0 ? raw.lambda() : getLambda());
        b.setWeight(getWeight());

        double mT = raw.mT();
        double mF = 0.0;
        double mU = 1.0 - mT;
        double kappa = (raw.averageScore() > 0) ? mT / raw.averageScore() : mT;
        b.setKappa(kappa);
        b.setMT(mT); b.setMU(mU); b.setMF(mF);

        double w   = getWeight();
        double mTD = mT * w;
        double mFD = mF * w;
        double mUD = mU * w + (1.0 - w);
        double sum = mTD + mUD + mFD;
        if (sum > 0) { mTD /= sum; mUD /= sum; mFD /= sum; }
        b.setMTDiscounted(mTD); b.setMUDiscounted(mUD); b.setMFDiscounted(mFD);
        return b;
    }
}
