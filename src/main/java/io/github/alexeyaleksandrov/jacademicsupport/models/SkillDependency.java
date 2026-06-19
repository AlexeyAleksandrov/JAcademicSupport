package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "skill_dependency")
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(SkillDependency.SkillDependencyId.class)
public class SkillDependency {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private SkillCanonical parent;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private SkillCanonical child;

    @Column(name = "co_occurrence_cnt", nullable = false)
    private int coOccurrenceCnt = 0;

    @Column(name = "weight", precision = 6, scale = 5)
    private BigDecimal weight;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillDependencyId implements Serializable {
        private Long parent;
        private Long child;
    }
}
