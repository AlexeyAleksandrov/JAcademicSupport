package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.models.DstSettings;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.DstSettingsRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ExpertOpinionRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.ForesightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

/**
 * Single access point for all editable DST constants.
 *
 * The settings row is cached in memory because it is read many times per
 * L0/L1/L2 request; the cache is invalidated on every write.
 */
@Service
@RequiredArgsConstructor
public class DstSettingsService {

    private final DstSettingsRepository   repository;
    private final ExpertOpinionRepository expertOpinionRepository;
    private final ForesightRepository     foresightRepository;

    private volatile DstSettings cached;

    @Transactional
    public DstSettings get() {
        DstSettings local = cached;
        if (local != null) return local;
        synchronized (this) {
            if (cached == null) {
                DstSettings loaded = repository.findById(DstSettings.SINGLETON_ID)
                        .orElseGet(() -> repository.save(new DstSettings()));
                if (initializeMissingLevelLambdas(loaded)) loaded = repository.save(loaded);
                cached = loaded;
            }
            return cached;
        }
    }

    @Transactional
    public DstSettings update(DstSettings incoming) {
        DstSettings current = repository.findById(DstSettings.SINGLETON_ID)
                .orElseGet(() -> new DstSettings());
        initializeMissingLevelLambdas(current);
        copyEditableFields(incoming, current);
        validate(current);
        current.setId(DstSettings.SINGLETON_ID);
        DstSettings saved = repository.save(current);
        cached = saved;
        return saved;
    }

    @Transactional
    public DstSettings resetToDefaults() {
        DstSettings fresh = new DstSettings();
        fresh.setId(DstSettings.SINGLETON_ID);
        DstSettings saved = repository.save(fresh);
        cached = saved;
        return saved;
    }

    /** Fresh entity holding factory defaults; never persisted. */
    public DstSettings defaults() {
        return new DstSettings();
    }

    // ── Derived denominators ─────────────────────────────────────────────────

    /** Number of experts used as the κ denominator for EXP; 0 in settings = count from DB. */
    @Transactional(readOnly = true)
    public long effectiveTotalExperts() {
        Integer configured = get().getTotalExperts();
        if (configured != null && configured > 0) return configured;
        Long counted = expertOpinionRepository.countDistinctExperts();
        return counted != null && counted > 0 ? counted : 1L;
    }

    /** Number of foresight sources used as the κ denominator for FC; 0 in settings = count from DB. */
    @Transactional(readOnly = true)
    public long effectiveTotalSources() {
        Integer configured = get().getTotalSources();
        if (configured != null && configured > 0) return configured;
        Long counted = foresightRepository.countDistinctSourceUrls();
        return counted != null && counted > 0 ? counted : 1L;
    }

    /** Actual DB counts, shown next to the auto-capable fields in the UI. */
    @Transactional(readOnly = true)
    public long actualExpertCount() {
        Long counted = expertOpinionRepository.countDistinctExperts();
        return counted != null ? counted : 0L;
    }

    @Transactional(readOnly = true)
    public long actualSourceCount() {
        Long counted = foresightRepository.countDistinctSourceUrls();
        return counted != null ? counted : 0L;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void copyEditableFields(DstSettings from, DstSettings to) {
        if (from == null) return;
        if (from.getWVac() != null) to.setWVac(from.getWVac());
        if (from.getWExp() != null) to.setWExp(from.getWExp());
        if (from.getWFc()  != null) to.setWFc(from.getWFc());
        if (from.getWStd() != null) to.setWStd(from.getWStd());

        if (from.getVacEnabled() != null) to.setVacEnabled(from.getVacEnabled());
        if (from.getExpEnabled() != null) to.setExpEnabled(from.getExpEnabled());
        if (from.getFcEnabled()  != null) to.setFcEnabled(from.getFcEnabled());
        if (from.getStdEnabled() != null) to.setStdEnabled(from.getStdEnabled());

        if (from.getLambdaVacDomain() != null) to.setLambdaVacDomain(from.getLambdaVacDomain());
        if (from.getLambdaExpDomain() != null) to.setLambdaExpDomain(from.getLambdaExpDomain());
        if (from.getLambdaFcDomain()  != null) to.setLambdaFcDomain(from.getLambdaFcDomain());

        if (from.getLambdaVacL1() != null) to.setLambdaVacL1(from.getLambdaVacL1());
        if (from.getLambdaExpL1() != null) to.setLambdaExpL1(from.getLambdaExpL1());
        if (from.getLambdaFcL1()  != null) to.setLambdaFcL1(from.getLambdaFcL1());
        if (from.getLambdaStdL1() != null) to.setLambdaStdL1(from.getLambdaStdL1());

        if (from.getLambdaVacL2() != null) to.setLambdaVacL2(from.getLambdaVacL2());
        if (from.getLambdaExpL2() != null) to.setLambdaExpL2(from.getLambdaExpL2());
        if (from.getLambdaFcL2()  != null) to.setLambdaFcL2(from.getLambdaFcL2());
        if (from.getLambdaStdL2() != null) to.setLambdaStdL2(from.getLambdaStdL2());

        if (from.getTotalExperts() != null) to.setTotalExperts(from.getTotalExperts());
        if (from.getTotalSources() != null) to.setTotalSources(from.getTotalSources());

        if (from.getNClustersL0()     != null) to.setNClustersL0(from.getNClustersL0());
        if (from.getNClustersL0Auto() != null) to.setNClustersL0Auto(from.getNClustersL0Auto());

        if (from.getTauDelta()          != null) to.setTauDelta(from.getTauDelta());
        if (from.getTauK()              != null) to.setTauK(from.getTauK());
        if (from.getTauTheta()          != null) to.setTauTheta(from.getTauTheta());
        if (from.getStrongSignalDelta() != null) to.setStrongSignalDelta(from.getStrongSignalDelta());
        if (from.getStrongBoostDelta()  != null) to.setStrongBoostDelta(from.getStrongBoostDelta());
        if (from.getObsoleteMf()        != null) to.setObsoleteMf(from.getObsoleteMf());
        if (from.getObsoleteMt()        != null) to.setObsoleteMt(from.getObsoleteMt());

        if (from.getTauAlloc() != null) to.setTauAlloc(from.getTauAlloc());

        if (from.getNegativeEvidenceEnabled() != null)
            to.setNegativeEvidenceEnabled(from.getNegativeEvidenceEnabled());

        if (from.getWLocTitle()          != null) to.setWLocTitle(from.getWLocTitle());
        if (from.getWLocSkills()         != null) to.setWLocSkills(from.getWLocSkills());
        if (from.getWLocDesc()           != null) to.setWLocDesc(from.getWLocDesc());
        if (from.getDepEdgeThreshold()   != null) to.setDepEdgeThreshold(from.getDepEdgeThreshold());
        if (from.getDepMinCoOccurrence() != null) to.setDepMinCoOccurrence(from.getDepMinCoOccurrence());
        if (from.getRhoDep()             != null) to.setRhoDep(from.getRhoDep());
        if (from.getClusterMinScore()    != null) to.setClusterMinScore(from.getClusterMinScore());

        if (from.getTreeMode()   != null) to.setTreeMode(from.getTreeMode());
        if (from.getDomainMode() != null) to.setDomainMode(from.getDomainMode());
        if (from.getFamilyMode() != null) to.setFamilyMode(from.getFamilyMode());
        if (from.getSkillMode()  != null) to.setSkillMode(from.getSkillMode());
        if (from.getHoursBase()  != null) to.setHoursBase(from.getHoursBase());
        if (from.getBudgetMode() != null) to.setBudgetMode(from.getBudgetMode());
    }

    private void validate(DstSettings s) {
        range("wVac", s.getWVac(), 0, 1);
        range("wExp", s.getWExp(), 0, 1);
        range("wFc", s.getWFc(), 0, 1);
        range("wStd", s.getWStd(), 0, 1);

        nonNegative("lambdaVacDomain", s.getLambdaVacDomain());
        nonNegative("lambdaExpDomain", s.getLambdaExpDomain());
        nonNegative("lambdaFcDomain", s.getLambdaFcDomain());
        nonNegative("lambdaVacL1", s.getLambdaVacL1());
        nonNegative("lambdaExpL1", s.getLambdaExpL1());
        nonNegative("lambdaFcL1", s.getLambdaFcL1());
        nonNegative("lambdaStdL1", s.getLambdaStdL1());
        nonNegative("lambdaVacL2", s.getLambdaVacL2());
        nonNegative("lambdaExpL2", s.getLambdaExpL2());
        nonNegative("lambdaFcL2", s.getLambdaFcL2());
        nonNegative("lambdaStdL2", s.getLambdaStdL2());

        nonNegative("totalExperts", s.getTotalExperts());
        nonNegative("totalSources", s.getTotalSources());
        positive("nClustersL0", s.getNClustersL0());

        range("tauDelta", s.getTauDelta(), 0, 1);
        range("tauK", s.getTauK(), 0, 1);
        range("tauTheta", s.getTauTheta(), 0, 1);
        range("strongSignalDelta", s.getStrongSignalDelta(), 0, 1);
        range("strongBoostDelta", s.getStrongBoostDelta(), 0, 1);
        range("obsoleteMf", s.getObsoleteMf(), 0, 1);
        range("obsoleteMt", s.getObsoleteMt(), 0, 1);
        range("tauAlloc", s.getTauAlloc(), 0, 1);

        nonNegative("wLocTitle", s.getWLocTitle());
        nonNegative("wLocSkills", s.getWLocSkills());
        nonNegative("wLocDesc", s.getWLocDesc());
        range("depEdgeThreshold", s.getDepEdgeThreshold(), 0, 1);
        positive("depMinCoOccurrence", s.getDepMinCoOccurrence());
        range("rhoDep", s.getRhoDep(), 0, 1);
        nonNegative("clusterMinScore", s.getClusterMinScore());

        enumValue("treeMode", s.getTreeMode(), Set.of("FULL_TREE", "EXPLICIT_ONLY"));
        enumValue("domainMode", s.getDomainMode(), Set.of("DERIVED", "EXPLICIT"));
        enumValue("familyMode", s.getFamilyMode(), Set.of("DERIVED", "EXPLICIT"));
        enumValue("skillMode", s.getSkillMode(), Set.of("DERIVED", "EXPLICIT"));
        enumValue("hoursBase", s.getHoursBase(), Set.of("CURRICULUM", "TOUCHED_DISCIPLINES", "SINGLE_DISCIPLINE"));
        enumValue("budgetMode", s.getBudgetMode(), Set.of("INDEPENDENT", "INHERIT_CURRENT", "INHERIT_TARGET"));

        if (Boolean.TRUE.equals(s.getStdEnabled())) {
            badRequest("stdEnabled", "источник STD пока не реализован и не может быть включён");
        }
    }

    private void range(String field, Double value, double min, double max) {
        if (value == null || !Double.isFinite(value) || value < min || value > max) {
            badRequest(field, "ожидается число от " + min + " до " + max);
        }
    }

    private void nonNegative(String field, Double value) {
        if (value == null || !Double.isFinite(value) || value < 0) {
            badRequest(field, "ожидается неотрицательное число");
        }
    }

    private void nonNegative(String field, Integer value) {
        if (value == null || value < 0) badRequest(field, "ожидается целое число не меньше 0");
    }

    private void positive(String field, Integer value) {
        if (value == null || value < 1) badRequest(field, "ожидается целое число не меньше 1");
    }

    private void enumValue(String field, String value, Set<String> allowed) {
        if (value == null || !allowed.contains(value.trim().toUpperCase())) {
            badRequest(field, "допустимые значения: " + String.join(", ", allowed));
        }
    }

    private void badRequest(String field, String message) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + ": " + message);
    }

    /**
     * Existing installations only have the former shared L1/L2 columns. Copy
     * those values into the new L2 columns once, preserving the user's current
     * tuning instead of silently replacing it with factory defaults.
     */
    private boolean initializeMissingLevelLambdas(DstSettings s) {
        boolean changed = false;
        if (s.getLambdaVacL2() == null) {
            s.setLambdaVacL2(s.getLambdaVacL1() != null
                    ? s.getLambdaVacL1() : DstSettingsDefaults.LAMBDA_VAC_L2);
            changed = true;
        }
        if (s.getLambdaExpL2() == null) {
            s.setLambdaExpL2(s.getLambdaExpL1() != null
                    ? s.getLambdaExpL1() : DstSettingsDefaults.LAMBDA_EXP_L2);
            changed = true;
        }
        if (s.getLambdaFcL2() == null) {
            s.setLambdaFcL2(s.getLambdaFcL1() != null
                    ? s.getLambdaFcL1() : DstSettingsDefaults.LAMBDA_FC_L2);
            changed = true;
        }
        if (s.getLambdaStdL2() == null) {
            s.setLambdaStdL2(s.getLambdaStdL1() != null
                    ? s.getLambdaStdL1() : DstSettingsDefaults.LAMBDA_STD_L2);
            changed = true;
        }
        return changed;
    }
}
