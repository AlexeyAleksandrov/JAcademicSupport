package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstTraceResponse;
import io.github.alexeyaleksandrov.jacademicsupport.models.DstSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Translates DST combination numbers (mT, mU, mF, K, delta) into the final
 * allocation recommendation and the expertise flag.
 *
 * <p>The allocation action ({@code recommendation}) is independent from the
 * confidence flag ({@code expertiseRequired}). The UI shows the action normally,
 * but replaces it with an "Экспертиза" warning when the flag is set.
 */
@Service
@RequiredArgsConstructor
public class DstDecisionResolver {

    private final DstSettingsService settingsService;

    /**
     * Fills {@code recommendation} and {@code expertiseRequired} on the trace.
     *
     * @param trace       non-null response from the combination step
     * @param supplyHours actual hours already allocated in the curriculum
     * @param totalBetP   sum of BetP across all objects on this level; used to
     *                    compute the normalized delta {@code nBetP − supply}
     */
    public void resolve(DstTraceResponse trace, int supplyHours, double totalBetP) {
        if (trace == null) {
            return;
        }
        DstSettings s = settingsService.get();
        double tauDelta = s.getTauDelta();
        double tauK = s.getTauK();
        double obsoleteMf = s.getObsoleteMf();
        double obsoleteMt = s.getObsoleteMt();

        double mT = trace.getMT();
        double mF = trace.getMF();
        double K = trace.getK();
        double betp = trace.getBetp();
        double supply = trace.getSupply();

        double nBetP = totalBetP > 0 ? betp / totalBetP : 0.0;
        double deltaNorm = nBetP - supply;

        String recommendation;
        if (mF > obsoleteMf && mT < obsoleteMt && supplyHours > 0) {
            recommendation = "delete";
        } else if (supplyHours == 0 && deltaNorm > tauDelta) {
            recommendation = "introduce";
        } else if (supplyHours > 0 && deltaNorm > tauDelta) {
            recommendation = "boost";
        } else if (deltaNorm < -tauDelta) {
            recommendation = "reduce";
        } else {
            recommendation = "preserve";
        }

        boolean expertiseRequired = K >= tauK;

        trace.setRecommendation(recommendation);
        trace.setExpertiseRequired(expertiseRequired);
    }
}
