package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "recommended_skills")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RecommendedSkill {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "work_skill_id", nullable = false)
    private WorkSkill workSkill;

    @Column(name = "coefficient", nullable = false)
    private Double coefficient;
}