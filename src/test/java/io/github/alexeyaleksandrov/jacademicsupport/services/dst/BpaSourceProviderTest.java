package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.BpaResult;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.BpaSourceProvider;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.DstContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BpaSourceProviderTest {

    @Test
    void rawNegativeMassSurvivesConstructionAndShaferDiscounting() {
        BpaSourceProvider provider = new BpaSourceProvider() {
            @Override public String getName() { return "FC"; }
            @Override public boolean isEnabled() { return true; }
            @Override public double getLambda() { return 2.0; }
            @Override public double getWeight() { return 0.6; }
            @Override public BpaResult compute(DstContext context) { return BpaResult.disabled("FC"); }
        };

        DstQueryService.BpaResult raw = new DstQueryService.BpaResult(
                2, 8, 0.5, 0.30 / 0.70, 0.7, 0.30, 0.50,
                1, 0.25, 0.8, 0.20, 1.0, List.of());
        BpaResult result = provider.buildFromRaw(raw);

        assertTrue(result.isEnabled());
        assertEquals(0.30 / 0.70, result.getKappa(), 1e-9);
        assertEquals(1, result.getNegativeCount());
        assertEquals(0.20, result.getMF(), 1e-9);
        assertEquals(0.12, result.getMFDiscounted(), 1e-9);
        assertEquals(1.0,
                result.getMTDiscounted() + result.getMUDiscounted() + result.getMFDiscounted(), 1e-9);
    }
}
