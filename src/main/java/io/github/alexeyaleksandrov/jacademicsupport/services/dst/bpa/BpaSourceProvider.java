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
        b.setNegativeCount((int) raw.negativeCount());
        b.setNegativeKappa(raw.negativeKappa());
        b.setAverageNegativeScore(raw.averageNegativeScore());
        b.setMassNormalizationFactor(raw.massNormalizationFactor());
        b.setProfessionWeighted(raw.professionWeighted());
        b.setProfessionContributions(raw.professionContributions());

        double mT = raw.mT();
        double mF = raw.mF();
        double mU = raw.mTheta();
        b.setKappa(raw.kappa());
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
