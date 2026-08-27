package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstTraceResponse;
import io.github.alexeyaleksandrov.jacademicsupport.models.DstSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Translates DST combination numbers (mT, mU, mF, K) and the normalized
 * allocation gap into the final recommendation and the expertise flag.
 *
 * <p>The allocation action ({@code recommendation}) is independent from the
 * confidence flag ({@code expertiseRequired}). The flag is raised for a high
 * source conflict or when no active source carries evidence. The UI shows the
 * action normally, but replaces it with an "Экспертиза" warning when the flag
 * is set.
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
        double tauDeltaNorm = s.getTauAlloc() != null
                ? s.getTauAlloc() : DstSettingsDefaults.TAU_ALLOC;
        double tauK = s.getTauK();
        double obsoleteMf = s.getObsoleteMf();
        double obsoleteMt = s.getObsoleteMt();

        // Absence of evidence must not be interpreted as negative evidence.
        // Without this guard an object present in the curriculum receives
        // BetP=0 and can be incorrectly classified as "reduce".
        if (trace.getError() != null) {
            trace.setRecommendation("preserve");
            trace.setExpertiseRequired(true);
            return;
        }

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
        } else if (supplyHours == 0 && deltaNorm > tauDeltaNorm) {
            recommendation = "introduce";
        } else if (supplyHours > 0 && deltaNorm > tauDeltaNorm) {
            recommendation = "boost";
        } else if (deltaNorm < -tauDeltaNorm) {
            recommendation = "reduce";
        } else {
            recommendation = "preserve";
        }

        boolean expertiseRequired = K >= tauK;

        trace.setRecommendation(recommendation);
        trace.setExpertiseRequired(expertiseRequired);
    }
}
