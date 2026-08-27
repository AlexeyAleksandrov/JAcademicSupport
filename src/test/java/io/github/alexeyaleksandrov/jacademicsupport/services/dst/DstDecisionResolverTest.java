package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstTraceResponse;
import io.github.alexeyaleksandrov.jacademicsupport.models.DstSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies that allocation actions and the expertise flag are computed
 * independently on the backend from the DST combination numbers.
 */
class DstDecisionResolverTest {

    private static DstSettingsService settingsWithDefaults() {
        DstSettingsService service = mock(DstSettingsService.class);
        when(service.get()).thenReturn(new DstSettings());
        return service;
    }

    private static DstTraceResponse trace(double mT, double mU, double mF, double K, double deltaNorm) {
        DstTraceResponse r = new DstTraceResponse();
        r.setMT(mT);
        r.setMU(mU);
        r.setMF(mF);
        r.setK(K);
        double supply = 0.20;
        r.setSupply(supply);
        r.setBetp(deltaNorm + supply);
        r.setDelta(deltaNorm);
        return r;
    }

    @Test
    void deleteWhenObsoleteMassDominatesAndHoursExist() {
        DstDecisionResolver resolver = new DstDecisionResolver(settingsWithDefaults());
        DstTraceResponse r = trace(0.05, 0.05, 0.90, 0.0, -0.05);

        resolver.resolve(r, 20, 1.0);

        assertEquals("delete", r.getRecommendation());
        assertFalse(r.isExpertiseRequired());
    }

    @Test
    void deleteNotSuggestedWhenAlreadyAbsent() {
        DstDecisionResolver resolver = new DstDecisionResolver(settingsWithDefaults());
        DstTraceResponse r = trace(0.05, 0.05, 0.90, 0.0, 0.0);

        resolver.resolve(r, 0, 1.0);

        assertNotEquals("delete", r.getRecommendation());
    }

    @Test
    void introduceWhenAbsentAndPositiveDelta() {
        DstDecisionResolver resolver = new DstDecisionResolver(settingsWithDefaults());
        DstTraceResponse r = trace(0.85, 0.05, 0.0, 0.0, 0.20);

        resolver.resolve(r, 0, 1.0);

        assertEquals("introduce", r.getRecommendation());
        assertFalse(r.isExpertiseRequired());
    }

    @Test
    void introduceBelowThresholdIsPreserve() {
        DstDecisionResolver resolver = new DstDecisionResolver(settingsWithDefaults());
        DstTraceResponse r = trace(0.90, 0.10, 0.0, 0.0, 0.02);

        resolver.resolve(r, 0, 1.0);

        assertEquals("preserve", r.getRecommendation());
        assertFalse(r.isExpertiseRequired());
    }

    @Test
    void boostWhenHoursExistAndPositiveDelta() {
        DstDecisionResolver resolver = new DstDecisionResolver(settingsWithDefaults());
        DstTraceResponse r = trace(0.85, 0.05, 0.0, 0.0, 0.25);

        resolver.resolve(r, 10, 1.0);

        assertEquals("boost", r.getRecommendation());
        assertFalse(r.isExpertiseRequired());
    }

    @Test
    void reduceWhenNegativeDelta() {
        DstDecisionResolver resolver = new DstDecisionResolver(settingsWithDefaults());
        DstTraceResponse r = trace(0.20, 0.05, 0.0, 0.0, -0.20);

        resolver.resolve(r, 20, 1.0);

        assertEquals("reduce", r.getRecommendation());
        assertFalse(r.isExpertiseRequired());
    }

    @Test
    void preserveInsideThreshold() {
        DstDecisionResolver resolver = new DstDecisionResolver(settingsWithDefaults());
        DstTraceResponse r = trace(0.85, 0.05, 0.0, 0.0, 0.02);

        resolver.resolve(r, 20, 1.0);

        assertEquals("preserve", r.getRecommendation());
        assertFalse(r.isExpertiseRequired());
    }

    @Test
    void expertiseWhenKequalsTauK() {
        // K = 0.40 exactly must trigger expertise (boundary aligned with Yager switch)
        DstSettings tuned = new DstSettings();
        tuned.setTauK(0.40);
        DstSettingsService settings = mock(DstSettingsService.class);
        when(settings.get()).thenReturn(tuned);
        DstDecisionResolver resolver = new DstDecisionResolver(settings);
        DstTraceResponse r = trace(0.60, 0.10, 0.30, 0.40, 0.25);

        resolver.resolve(r, 10, 1.0);

        assertTrue(r.isExpertiseRequired(), "K == tauK должно требовать экспертизы");
        assertEquals("boost", r.getRecommendation());
    }

    @Test
    void uncertaintyAloneDoesNotRequireExpertise() {
        DstDecisionResolver resolver = new DstDecisionResolver(settingsWithDefaults());
        DstTraceResponse r = trace(0.20, 0.60, 0.0, 0.0, 0.25);

        resolver.resolve(r, 10, 1.0);

        assertFalse(r.isExpertiseRequired(), "теперь mU не влияет на флаг экспертизы");
        assertEquals("boost", r.getRecommendation());
    }

    @Test
    void noExpertiseWhenConflictBelowTauK() {
        DstSettings tuned = new DstSettings();
        tuned.setTauK(0.40);
        DstSettingsService settings = mock(DstSettingsService.class);
        when(settings.get()).thenReturn(tuned);
        DstDecisionResolver resolver = new DstDecisionResolver(settings);
        DstTraceResponse r = trace(0.50, 0.14, 0.0, 0.39, 0.05);

        resolver.resolve(r, 20, 1.0);

        assertFalse(r.isExpertiseRequired());
    }

    @Test
    void normalizedDecisionUsesTauAllocRatherThanLegacyAbsoluteThreshold() {
        DstSettings tuned = new DstSettings();
        tuned.setTauDelta(0.90);
        tuned.setTauAlloc(0.03);
        DstSettingsService settings = mock(DstSettingsService.class);
        when(settings.get()).thenReturn(tuned);
        DstDecisionResolver resolver = new DstDecisionResolver(settings);
        DstTraceResponse r = trace(0.60, 0.20, 0.20, 0.0, 0.05);

        resolver.resolve(r, 20, 1.0);

        assertEquals("boost", r.getRecommendation(),
                "решение по Δ_norm должно использовать tauAlloc=0.03, а не tauDelta=0.90");
    }

    @Test
    void missingEvidenceRequiresExpertiseInsteadOfReducingHours() {
        DstDecisionResolver resolver = new DstDecisionResolver(settingsWithDefaults());
        DstTraceResponse r = trace(0.0, 0.0, 0.0, 0.0, -0.20);
        r.setError("Нет данных ни в одном из активных источников");

        resolver.resolve(r, 20, 1.0);

        assertEquals("preserve", r.getRecommendation());
        assertTrue(r.isExpertiseRequired(),
                "отсутствие данных нельзя интерпретировать как негативное свидетельство");
    }
}
