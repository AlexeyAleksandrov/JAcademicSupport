package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstCombinationTrace;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstTraceResponse;
import io.github.alexeyaleksandrov.jacademicsupport.models.DstSettings;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.BpaResult;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.BpaSourceProvider;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.DstContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies that the conflict coefficient K and Yager's rule are actually reachable
 * once sources are allowed to carry a non-zero m(F).
 */
class DstCombinationServiceTest {

    /** Minimal provider returning a fixed, already-discounted BPA. */
    private static BpaSourceProvider provider(String name, double mT, double mU, double mF) {
        return new BpaSourceProvider() {
            @Override public String getName()   { return name; }
            @Override public boolean isEnabled() { return true; }
            @Override public double getLambda() { return 1.0; }
            @Override public double getWeight() { return 1.0; }

            @Override public BpaResult compute(DstContext ctx) {
                BpaResult r = new BpaResult();
                r.setSourceName(name);
                r.setEnabled(true);
                r.setRelevantCount(1);
                r.setTotalCount(1);
                r.setMT(mT); r.setMU(mU); r.setMF(mF);
                r.setMTDiscounted(mT); r.setMUDiscounted(mU); r.setMFDiscounted(mF);
                return r;
            }
        };
    }

    private static DstSettingsService settingsWithDefaults() {
        DstSettingsService service = mock(DstSettingsService.class);
        when(service.get()).thenReturn(new DstSettings());
        return service;
    }

    @Test
    void purelyPositiveSourcesProduceZeroConflictAndUseDempster() {
        DstCombinationService service = new DstCombinationService(
                List.of(provider("VAC", 0.7, 0.3, 0.0),
                        provider("EXP", 0.6, 0.4, 0.0)),
                settingsWithDefaults());

        DstTraceResponse resp = service.compute(new DstContext("backend", "DevOps", null, null), 0.1);

        assertNull(resp.getError());
        assertEquals(0.0, resp.getK(), 1e-9, "без m(F) конфликт K обязан быть нулевым");
        assertFalse(resp.isUsedYager());
        assertEquals(1, resp.getCombinations().size());
        assertEquals("Dempster", resp.getCombinations().get(0).getRule());
    }

    @Test
    void conflictingSourcesProduceNonZeroConflict() {
        // VAC strongly supports T, EXP strongly supports F → K = 0.7*0.5 + 0.0*0.4 = 0.35
        DstCombinationService service = new DstCombinationService(
                List.of(provider("VAC", 0.7, 0.3, 0.0),
                        provider("EXP", 0.4, 0.1, 0.5)),
                settingsWithDefaults());

        DstTraceResponse resp = service.compute(new DstContext("backend", "DevOps", null, null), 0.1);

        assertNull(resp.getError());
        assertEquals(0.35, resp.getK(), 1e-9);
        // 0.35 < τ_K = 0.40 → still Dempster, but the conflict is now visible
        assertFalse(resp.isUsedYager());
        assertEquals("Dempster", resp.getCombinations().get(0).getRule());
        assertTrue(resp.getMF() > 0, "негативная масса обязана дожить до итоговой комбинации");
    }

    @Test
    void highConflictSwitchesToYager() {
        // K = 0.9*0.9 + 0.0*0.05 = 0.81 ≥ τ_K = 0.40
        DstCombinationService service = new DstCombinationService(
                List.of(provider("VAC", 0.9, 0.1, 0.0),
                        provider("EXP", 0.05, 0.05, 0.9)),
                settingsWithDefaults());

        DstTraceResponse resp = service.compute(new DstContext("backend", "DevOps", null, null), 0.1);

        assertNull(resp.getError());
        assertTrue(resp.getK() >= 0.40, "ожидался конфликт выше порога, получено K=" + resp.getK());
        assertTrue(resp.isUsedYager(), "при K ≥ τ_K расчёт обязан переключиться на правило Ягера");
        DstCombinationTrace combo = resp.getCombinations().get(0);
        assertEquals("Yager", combo.getRule());
        // Yager reassigns the conflict to ignorance instead of renormalising it away
        assertTrue(combo.getOutput().getMU() > 0.1);
    }

    @Test
    void negativeOnlySourceIsNotDropped() {
        DstCombinationService service = new DstCombinationService(
                List.of(provider("VAC", 0.6, 0.4, 0.0),
                        provider("FC", 0.0, 0.2, 0.8)),
                settingsWithDefaults());

        DstTraceResponse resp = service.compute(new DstContext("backend", "DevOps", null, null), 0.1);

        assertNull(resp.getError(), "источник только с m(F) не должен отбрасываться");
        assertEquals(1, resp.getCombinations().size());
        assertEquals(0.6 * 0.8, resp.getK(), 1e-9);
    }

    @Test
    void obsoleteIsReachableWhenNegativeMassDominates() {
        // Both sources push F: final mF > 0.8 and mT < 0.1 → "obsolete" / «Удалить»
        DstCombinationService service = new DstCombinationService(
                List.of(provider("EXP", 0.0, 0.1, 0.9),
                        provider("FC", 0.0, 0.15, 0.85)),
                settingsWithDefaults());

        DstTraceResponse resp = service.compute(new DstContext("backend", "DevOps", null, null), 0.5);

        assertNull(resp.getError());
        assertTrue(resp.getMF() > 0.8, "ожидалась доминирующая негативная масса, получено mF=" + resp.getMF());
        assertTrue(resp.getMT() < 0.1);
        assertEquals("obsolete", resp.getRecommendation());
    }

    @Test
    void loweringTauKMakesYagerTriggerEarlier() {
        DstSettings tuned = new DstSettings();
        tuned.setTauK(0.10);
        DstSettingsService settings = mock(DstSettingsService.class);
        when(settings.get()).thenReturn(tuned);

        DstCombinationService service = new DstCombinationService(
                List.of(provider("VAC", 0.7, 0.3, 0.0),
                        provider("EXP", 0.4, 0.1, 0.5)),
                settings);

        DstTraceResponse resp = service.compute(new DstContext("backend", "DevOps", null, null), 0.1);

        assertEquals(0.35, resp.getK(), 1e-9);
        assertTrue(resp.isUsedYager(), "при τ_K = 0.10 конфликт 0.35 обязан включить Ягера");
    }
}
