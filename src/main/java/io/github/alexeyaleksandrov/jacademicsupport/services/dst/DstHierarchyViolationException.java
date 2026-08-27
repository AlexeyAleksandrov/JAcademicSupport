package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DisciplineCoverageTreeDto;

import java.util.List;

public class DstHierarchyViolationException extends RuntimeException {
    private final List<DisciplineCoverageTreeDto.Violation> violations;

    public DstHierarchyViolationException(List<DisciplineCoverageTreeDto.Violation> violations) {
        super(violations.isEmpty() ? "Нарушена иерархия часов" : violations.get(0).getMessage());
        this.violations = List.copyOf(violations);
    }

    public List<DisciplineCoverageTreeDto.Violation> getViolations() {
        return violations;
    }
}
