package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rpd_skill", schema = "public", catalog = "AcademicSupport")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class RpdSkill {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rpd_id", referencedColumnName = "id", nullable = false)
    private Rpd rpd;

    @ManyToOne
    @JoinColumn(name = "work_skill_id", referencedColumnName = "id", nullable = false)
    private WorkSkill workSkill;

    @Basic
    @Column(name = "time", nullable = false)
    private Integer time; // время в академических часах
}
