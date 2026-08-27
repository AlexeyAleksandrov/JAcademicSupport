package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstCombinationTrace;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstMass;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstTraceResponse;
import io.github.alexeyaleksandrov.jacademicsupport.models.DstSettings;
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

    private final List<BpaSourceProvider> sources;
    private final DstSettingsService       settingsService;

    private double tauK() { return settingsService.get().getTauK(); }

    /** Default BetP denominator on L0 (number of market clusters/domains). */
    public int defaultNClusters() {
        DstSettings s = settingsService.get();
        return s.getNClustersL0() != null ? s.getNClustersL0() : DstSettingsDefaults.N_CLUSTERS_L0;
    }

    /** True when L0 should use the actual number of domains instead of the fixed constant. */
    public boolean isNClustersAuto() {
        return Boolean.TRUE.equals(settingsService.get().getNClustersL0Auto());
    }

    public DstTraceResponse compute(DstContext ctx, double supply) {
        List<BpaResult> enabled = new ArrayList<>();
        List<BpaResult> all     = new ArrayList<>();

        for (BpaSourceProvider src : sources) {
            BpaResult r = src.isEnabled() ? src.compute(ctx) : BpaResult.disabled(src.getName());
            all.add(r);
            if (carriesEvidence(r)) enabled.add(r);
        }

        return finishCombination(all, enabled, supply);
    }

    /**
     * A source participates in combination when it carries any mass outside Θ —
     * either positive (m(T)) or negative (m(F)). Without the m(F) branch a purely
     * negative source would be silently dropped and K would stay zero.
     */
    private boolean carriesEvidence(BpaResult r) {
        return r.isEnabled() && (r.getMT() > 0 || r.getMF() > 0);
    }

    private double[] combineAdaptive(double m1T, double m1U, double m1F,
                                     double m2T, double m2U, double m2F) {
        double K = m1T * m2F + m1F * m2T;
        return K >= tauK()
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
        return computeWeighted(ctx, profs, supply, queryService, defaultNClusters());
    }

    public DstTraceResponse computeWeighted(DstContext ctx,
                                             List<ProfessionWeight> profs,
                                             double supply,
                                             DstQueryService queryService,
                                             int nClusters) {
        String domain = ctx.getDomain();
        DstQueryService.BpaResult vacRaw = queryService.getWeightedVacBpaByDomain(profs, domain);
        DstQueryService.BpaResult expRaw = queryService.getWeightedExpBpaByDomain(profs, domain);
        DstQueryService.BpaResult fcRaw  = queryService.getWeightedFcBpaByDomain(profs, domain);

        return combineSources(supply, nClusters, vacRaw, expRaw, fcRaw);
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

        return combineSources(supply, nFamilies, vacRaw, expRaw, fcRaw);
    }

    public DstTraceResponse computeWeightedSkill(DstContext ctx,
                                                  List<ProfessionWeight> profs,
                                                  double supply,
                                                  DstQueryService queryService,
                                                  int nSkills) {
        String domain      = ctx.getDomain();
        String techFamily  = ctx.getTechFamily();
        Long   canonicalId = ctx.getCanonicalId();
        DstQueryService.BpaResult vacRaw = queryService.getWeightedVacBpaByFamilySkill(profs, domain, techFamily, canonicalId);
        DstQueryService.BpaResult expRaw = queryService.getWeightedExpBpaByCanonicalAndDomain(profs, canonicalId, domain);
        DstQueryService.BpaResult fcRaw  = queryService.getWeightedFcBpaByCanonicalAndDomain(profs, canonicalId, domain);

        return combineSources(supply, nSkills, vacRaw, expRaw, fcRaw);
    }

    /** Shared source-assembly step for all three levels. */
    private DstTraceResponse combineSources(double supply, int n,
                                            DstQueryService.BpaResult vacRaw,
                                            DstQueryService.BpaResult expRaw,
                                            DstQueryService.BpaResult fcRaw) {
        List<BpaResult> all     = new ArrayList<>();
        List<BpaResult> enabled = new ArrayList<>();

        for (BpaSourceProvider src : sources) {
            DstQueryService.BpaResult raw = switch (src.getName()) {
                case "VAC" -> vacRaw;
                case "EXP" -> expRaw;
                case "FC"  -> fcRaw;
                default    -> DstQueryService.BpaResult.empty();
            };
            BpaResult r = (src.isEnabled() && raw.hasEvidence())
                    ? src.buildFromRaw(raw)
                    : BpaResult.disabled(src.getName());
            all.add(r);
            if (carriesEvidence(r)) enabled.add(r);
        }

        return finishCombination(all, enabled, supply, n);
    }

    private DstTraceResponse finishCombination(List<BpaResult> all, List<BpaResult> enabled, double supply) {
        return finishCombination(all, enabled, supply, defaultNClusters());
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
            boolean yager = K >= tauK();
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
        return resp;
    }
}
