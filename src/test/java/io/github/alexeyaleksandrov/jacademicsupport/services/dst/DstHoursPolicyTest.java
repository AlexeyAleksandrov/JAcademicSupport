package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DstHoursPolicyTest {

    @Test
    void explicitCoverageKeepsSmallerHoursAndCapsOnlyExcess() {
        assertEquals(1.0, DstHoursPolicy.coverageScale(true, 180, 40), 1e-9);
        assertEquals(0.5, DstHoursPolicy.coverageScale(true, 180, 360), 1e-9);
    }

    @Test
    void derivedCoverageStillExpandsToTheDisciplineVolume() {
        assertEquals(4.5, DstHoursPolicy.coverageScale(false, 180, 40), 1e-9);
    }

    @Test
    void supplyIsNotClampedWhenCoverageExceedsInheritedBudget() {
        assertEquals(2.0, DstHoursPolicy.supply(240, 120), 1e-9);
        assertEquals(0.0, DstHoursPolicy.supply(20, 0), 1e-9);
    }

    @Test
    void budgetModesUseIndependentTouchedAndInheritedHours() {
        Map<Long, Integer> hours = Map.of(1L, 180, 2L, 72, 3L, 144);
        Set<Long> touched = Set.of(1L, 2L);

        assertEquals(500, DstHoursPolicy.resolveBudget(options(
                DstCalcOptions.HoursBase.CURRICULUM,
                DstCalcOptions.BudgetMode.INDEPENDENT, null, null), 500, touched, hours));
        assertEquals(252, DstHoursPolicy.resolveBudget(options(
                DstCalcOptions.HoursBase.TOUCHED_DISCIPLINES,
                DstCalcOptions.BudgetMode.INDEPENDENT, null, null), 500, touched, hours));
        assertEquals(72, DstHoursPolicy.resolveBudget(options(
                DstCalcOptions.HoursBase.SINGLE_DISCIPLINE,
                DstCalcOptions.BudgetMode.INDEPENDENT, null, 2L), 500, touched, hours));
        assertEquals(374, DstHoursPolicy.resolveBudget(options(
                DstCalcOptions.HoursBase.CURRICULUM,
                DstCalcOptions.BudgetMode.INHERIT_TARGET, 374, null), 500, touched, hours));
        assertEquals(0, DstHoursPolicy.resolveBudget(options(
                DstCalcOptions.HoursBase.CURRICULUM,
                DstCalcOptions.BudgetMode.INHERIT_CURRENT, 0, null), 500, touched, hours));
        assertThrows(ResponseStatusException.class, () -> DstHoursPolicy.resolveBudget(options(
                DstCalcOptions.HoursBase.SINGLE_DISCIPLINE,
                DstCalcOptions.BudgetMode.INDEPENDENT, null, null), 500, touched, hours));
    }

    private DstCalcOptions options(DstCalcOptions.HoursBase hoursBase,
                                   DstCalcOptions.BudgetMode budgetMode,
                                   Integer budgetHours,
                                   Long disciplineId) {
        return new DstCalcOptions(
                DstCalcOptions.CoverageMode.DERIVED,
                DstCalcOptions.CoverageMode.DERIVED,
                DstCalcOptions.CoverageMode.DERIVED,
                hoursBase, budgetMode, budgetHours, disciplineId);
    }
}
