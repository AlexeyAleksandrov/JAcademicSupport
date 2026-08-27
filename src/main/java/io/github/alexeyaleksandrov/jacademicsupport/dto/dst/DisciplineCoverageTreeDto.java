package io.github.alexeyaleksandrov.jacademicsupport.dto.dst;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DisciplineCoverageTreeDto {
    private Long disciplineId;
    private String disciplineName;
    private int totalHours;
    private int allocatedHours;
    private int unallocatedHours;
    private int excessHours;
    private List<Node> domains = new ArrayList<>();
    private List<Violation> violations = new ArrayList<>();

    @Data
    public static class Node {
        private String key;
        private String label;
        private String level;
        private Long canonicalId;
        private int explicitHours;
        private int implicitHours;
        private int totalHours;
        private int childrenSum;
        private boolean implicit;
        private String derivedFrom;
        private Violation violation;
        private List<Node> children = new ArrayList<>();
    }

    @Data
    public static class Violation {
        private Long disciplineId;
        private String disciplineName;
        private String level;
        private String parent;
        private int parentHours;
        private int childrenSum;
        private int excess;
        private String message;
    }
}
