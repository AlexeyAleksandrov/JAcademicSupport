package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "vacancy_cluster_score")
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(VacancyClusterScore.VacancyClusterScoreId.class)
public class VacancyClusterScore {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private VacancyEntity vacancy;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    private SkillsGroup cluster;

    @Column(name = "score", nullable = false, precision = 6, scale = 5)
    private BigDecimal score = BigDecimal.ZERO;

    @Column(name = "from_title", nullable = false)
    private boolean fromTitle = false;

    @Column(name = "from_skills", nullable = false)
    private boolean fromSkills = false;

    @Column(name = "from_desc", nullable = false)
    private boolean fromDesc = false;

    @Column(name = "via_dependency", nullable = false)
    private boolean viaDependency = false;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VacancyClusterScoreId implements Serializable {
        private Long vacancy;
        private Long cluster;
    }
}
