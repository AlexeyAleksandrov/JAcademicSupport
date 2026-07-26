package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "work_skill_canonical")
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(WorkSkillCanonical.WorkSkillCanonicalId.class)
public class WorkSkillCanonical {

    @Id
    @Column(name = "work_skill_id", nullable = false)
    private Long workSkillId;

    @Id
    @Column(name = "canonical_id", nullable = false)
    private Long canonicalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canonical_id", insertable = false, updatable = false)
    private SkillCanonical canonical;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkSkillCanonicalId implements Serializable {
        private Long workSkillId;
        private Long canonicalId;
    }
}
