package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstCombinationTrace;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstMass;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstTraceResponse;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstQueryService.ProfessionWeight;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.BpaResult;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.BpaSourceProvider;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.DstContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DstCombinationService {

    private static final double TAU_DELTA   = 0.15;
    private static final double TAU_K       = 0.4;
    private static final double TAU_THETA   = 0.15;
    private static final int    N_CLUSTERS  = 25;

    private final List<BpaSourceProvider> sources;

    public DstTraceResponse compute(DstContext ctx, double supply) {
        List<BpaResult> enabled = new ArrayList<>();
        List<BpaResult> all     = new ArrayList<>();

        for (BpaSourceProvider src : sources) {
            BpaResult r = src.isEnabled() ? src.compute(ctx) : BpaResult.disabled(src.getName());
            all.add(r);
            if (r.isEnabled() && r.getMT() > 0) enabled.add(r);
        }

        return finishCombination(all, enabled, supply);
    }

    private double[] combineAdaptive(double m1T, double m1U, double m1F,
                                     double m2T, double m2U, double m2F) {
        double K = m1T * m2F + m1F * m2T;
        return K >= TAU_K
                ? combineYager(m1T, m1U, m1F, m2T, m2U, m2F)
                : combineDST(m1T, m1U, m1F, m2T, m2U, m2F);
    }

    private double[] combineDST(double m1T, double m1U, double m1F,
                                 double m2T, double m2U, double m2F) {
        double K    = m1T * m2F + m1F * m2T;
        double norm = Math.max(1.0 - K, 0.0001);
        double mT = (m1T * m2T + m1T * m2U + m1U * m2T) / norm;
        double mU = (m1U * m2U)                           / norm;
        double mF = (m1F * m2F + m1F * m2U + m1U * m2F) / norm;
        double sum = mT + mU + mF;
        if (sum > 0) { mT /= sum; mU /= sum; mF /= sum; }
        return new double[]{mT, mU, mF, K};
    }

    private double[] combineYager(double m1T, double m1U, double m1F,
                                   double m2T, double m2U, double m2F) {
        double K  = m1T * m2F + m1F * m2T;
        double mT = m1T * m2T + m1T * m2U + m1U * m2T;
        double mU = m1U * m2U + K;
        double mF = m1F * m2F + m1F * m2U + m1U * m2F;
        double sum = mT + mU + mF;
        if (sum > 0) { mT /= sum; mU /= sum; mF /= sum; }
        return new double[]{mT, mU, mF, K};
    }

    public DstTraceResponse computeWeighted(DstContext ctx,
                                             List<ProfessionWeight> profs,
                                             double supply,
                                             DstQueryService queryService) {
        String domain = ctx.getDomain();
        DstQueryService.BpaResult vacRaw = queryService.getWeightedVacBpaByDomain(profs, domain);
        DstQueryService.BpaResult expRaw = queryService.getWeightedExpBpaByDomain(profs, domain);
        DstQueryService.BpaResult fcRaw  = queryService.getWeightedFcBpaByDomain(profs, domain);

        List<BpaResult> all = new ArrayList<>();
        List<BpaResult> enabled = new ArrayList<>();

        for (BpaSourceProvider src : sources) {
            DstQueryService.BpaResult raw = switch (src.getName()) {
                case "VAC" -> vacRaw;
                case "EXP" -> expRaw;
                case "FC"  -> fcRaw;
                default    -> DstQueryService.BpaResult.empty();
            };
            BpaResult r;
            if (raw.relevantCount() > 0) {
                r = src.buildFromRaw(raw);
            } else {
                r = BpaResult.disabled(src.getName());
            }
            all.add(r);
            if (r.isEnabled() && r.getMT() > 0) enabled.add(r);
        }

        return finishCombination(all, enabled, supply, N_CLUSTERS);
    }

    public DstTraceResponse computeWeightedFamily(DstContext ctx,
                                                   List<ProfessionWeight> profs,
                                                   double supply,
                                                   DstQueryService queryService,
                                                   int nFamilies) {
        String domain     = ctx.getDomain();
        String techFamily = ctx.getTechFamily();
        DstQueryService.BpaResult vacRaw = queryService.getWeightedVacBpaByFamily(profs, domain, techFamily);
        DstQueryService.BpaResult expRaw = queryService.getWeightedExpBpaByFamily(profs, domain, techFamily);
        DstQueryService.BpaResult fcRaw  = queryService.getWeightedFcBpaByFamily(profs, domain, techFamily);

        List<BpaResult> all = new ArrayList<>();
        List<BpaResult> enabled = new ArrayList<>();

        for (BpaSourceProvider src : sources) {
            DstQueryService.BpaResult raw = switch (src.getName()) {
                case "VAC" -> vacRaw;
                case "EXP" -> expRaw;
                case "FC"  -> fcRaw;
                default    -> DstQueryService.BpaResult.empty();
            };
            BpaResult r = raw.relevantCount() > 0 ? src.buildFromRaw(raw) : BpaResult.disabled(src.getName());
            all.add(r);
            if (r.isEnabled() && r.getMT() > 0) enabled.add(r);
        }

        return finishCombination(all, enabled, supply, nFamilies);
    }

    private DstTraceResponse finishCombination(List<BpaResult> all, List<BpaResult> enabled, double supply) {
        return finishCombination(all, enabled, supply, N_CLUSTERS);
    }

    private DstTraceResponse finishCombination(List<BpaResult> all, List<BpaResult> enabled, double supply, int n) {
        if (enabled.isEmpty()) {
            DstTraceResponse resp = new DstTraceResponse();
            resp.setSources(all);
            resp.setError("Нет данных ни в одном из активных источников");
            return resp;
        }

        List<DstCombinationTrace> combos = new ArrayList<>();
        double[] current = {enabled.get(0).getMTDiscounted(),
                            enabled.get(0).getMUDiscounted(),
                            enabled.get(0).getMFDiscounted()};
        double maxK = 0.0;
        boolean usedYager = false;

        for (int i = 1; i < enabled.size(); i++) {
            BpaResult next = enabled.get(i);
            double[] result = combineAdaptive(current[0], current[1], current[2],
                                              next.getMTDiscounted(), next.getMUDiscounted(), next.getMFDiscounted());
            double K = result[3];
            boolean yager = K >= TAU_K;
            if (K > maxK) maxK = K;
            if (yager) usedYager = true;

            String label = enabled.get(i - 1).getSourceName() + " ⊕ " + next.getSourceName();
            combos.add(new DstCombinationTrace(label, K, yager ? "Yager" : "Dempster",
                    new DstMass(result[0], result[1], result[2])));
            current = new double[]{result[0], result[1], result[2]};
        }

        double mT = current[0], mU = current[1], mF = current[2];
        double betp = mT + mU / Math.max(1, n);
        double delta = betp - supply;

        DstTraceResponse resp = new DstTraceResponse();
        resp.setSources(all);
        resp.setCombinations(combos);
        resp.setMT(mT); resp.setMU(mU); resp.setMF(mF);
        resp.setK(maxK);
        resp.setBetp(betp); resp.setDelta(delta); resp.setSupply(supply);
        resp.setNClusters(n); resp.setUsedYager(usedYager);
        resp.setRecommendation(decide(mT, mU, mF, maxK, delta));
        return resp;
    }

    private String decide(double mT, double mU, double mF, double K, double delta) {
        if (mF > 0.8 && mT < 0.1) return "reduce";
        if (delta > TAU_DELTA && K <= TAU_K) {
            boolean clearSignal = mU <= TAU_THETA || delta > 0.35;
            if (clearSignal)
                return delta > 0.5 ? "strong" : "moderate";
        }
        if (delta < -TAU_DELTA) return "reduce";
        if (K > TAU_K || mU > TAU_THETA) return "expertise";
        return "preserve";
    }
}
