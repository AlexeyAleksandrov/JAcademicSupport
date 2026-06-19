package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "profession_cluster")
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ProfessionCluster.ProfessionClusterId.class)
public class ProfessionCluster {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profession_id", nullable = false)
    private Profession profession;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    private SkillsGroup cluster;

    @Column(name = "weight", nullable = false, precision = 5, scale = 4)
    private BigDecimal weight = BigDecimal.ZERO;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfessionClusterId implements Serializable {
        private Long profession;
        private Long cluster;
    }
}
