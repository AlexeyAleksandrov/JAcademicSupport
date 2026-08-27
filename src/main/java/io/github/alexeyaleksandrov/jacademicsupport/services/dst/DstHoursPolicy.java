package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Shared, testable rules for coverage scaling and the normalisation budget T. */
final class DstHoursPolicy {

    private DstHoursPolicy() {}

    @Deprecated
    static double coverageScale(boolean explicit, int disciplineHours, int effectiveHours) {
        if (effectiveHours <= 0) return 0.0;
        double ratio = (double) Math.max(0, disciplineHours) / effectiveHours;
        return explicit ? Math.min(1.0, ratio) : ratio;
    }

    static double supply(double coveredHours, int budgetHours) {
        return budgetHours > 0 ? coveredHours / budgetHours : 0.0;
    }

    static int resolveBudget(DstCalcOptions options,
                             int independentHours,
                             Set<Long> touchedDisciplineIds,
                             Map<Long, Integer> disciplineHours) {
        Integer inherited = options.inheritedBudget();
        if (inherited != null) return inherited;
        return switch (options.hoursBase()) {
            case TOUCHED_DISCIPLINES -> touchedDisciplineIds.stream()
                    .mapToInt(id -> disciplineHours.getOrDefault(id, 0))
                    .sum();
            case SINGLE_DISCIPLINE -> {
                Long disciplineId = options.disciplineId();
                if (disciplineId == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Для базы SINGLE_DISCIPLINE нужно выбрать disciplineId");
                }
                Integer hours = disciplineHours.get(disciplineId);
                if (hours == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Выбранная дисциплина не входит в этот учебный план");
                }
                yield Math.max(0, hours);
            }
            case CURRICULUM -> independentHours;
        };
    }
}
