package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeightedBpaAggregationTest {

    private static DstQueryService.BpaResult positive(
            long relevant, long total, double lambda, double kappa, double score) {
        double mT = kappa * score;
        return new DstQueryService.BpaResult(
                relevant, total, lambda, kappa, score, mT, 1.0 - mT,
                0, 0.0, 0.0, 0.0, 1.0, List.of());
    }

    @Test
    void kappaUsesTheDisplayedExponentialFormula() {
        assertEquals(0.166247081924819, DstQueryService.computeBpa(2, 1.0, 0, 0.0, 11, 1.0).kappa(), 1e-12);
        assertEquals(0.0832830479745136, DstQueryService.computeBpa(4, 1.0, 0, 0.0, 23, 0.5).kappa(), 1e-12);
        assertEquals(0.669475525601659, DstQueryService.computeBpa(1158, 1.0, 0, 0.0, 2092, 2.0).kappa(), 1e-12);
    }

    @Test
    void professionWithoutEvidenceKeepsItsWeightAsIgnorance() {
        List<DstQueryService.ProfessionWeight> professions = List.of(
                new DstQueryService.ProfessionWeight("A", "Profession A", 75.0),
                new DstQueryService.ProfessionWeight("B", "Profession B", 25.0));

        DstQueryService.BpaResult result = DstQueryService.weightedAverage(
                professions,
                p -> p.professionCode().equals("A")
                        ? positive(4, 10, 1.0, 0.5, 0.8)
                        : DstQueryService.BpaResult.empty());

        assertEquals(0.30, result.mT(), 1e-9);
        assertEquals(0.70, result.mTheta(), 1e-9);
        assertEquals(0.0, result.mF(), 1e-9);
        assertEquals(0.375, result.kappa(), 1e-9);
        assertEquals(2, result.professionContributions().size());
        assertFalse(result.professionContributions().get(1).hasEvidence());
        assertEquals(0.25, result.professionContributions().get(1).weightedMTheta(), 1e-9);
    }

    @Test
    void curriculumPercentagesAreNormalisedExactlyOnceAcrossAllProfessions() {
        double[] rawWeights = {5, 15, 25, 25, 25, 5};
        List<DstQueryService.ProfessionWeight> professions = java.util.stream.IntStream
                .range(0, rawWeights.length)
                .mapToObj(i -> new DstQueryService.ProfessionWeight("P" + i, "P" + i, rawWeights[i]))
                .toList();

        DstQueryService.BpaResult result = DstQueryService.weightedAverage(
                professions, p -> positive(2, 11, 1.0,
                        1.0 - Math.exp(-2.0 / 11.0), 0.82));

        assertEquals(1.0, result.professionContributions().stream()
                .mapToDouble(DstQueryService.ProfessionBpaContribution::weight).sum(), 1e-12);
        assertEquals(1.0, result.mT() + result.mTheta() + result.mF(), 1e-12);
        assertEquals(1.0 - Math.exp(-2.0 / 11.0), result.kappa(), 1e-12);
        assertTrue(result.professionWeighted());
    }

    @Test
    void invalidNegativeWeightIsRejected() {
        List<DstQueryService.ProfessionWeight> professions = List.of(
                new DstQueryService.ProfessionWeight("A", "A", 1.0),
                new DstQueryService.ProfessionWeight("B", "B", -0.1));

        assertThrows(IllegalArgumentException.class,
                () -> DstQueryService.weightedAverage(professions, p -> DstQueryService.BpaResult.empty()));
    }
}
