package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "work_skill", schema = "public", catalog = "AcademicSupport")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class WorkSkill {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private long id;
    @Basic
    @Column(name = "description", nullable = true, length = 255)
    private String description;
    @Basic
    @Column(name = "market_demand", nullable = true, precision = 0)
    private Double marketDemand;
    @ManyToOne
    @JoinColumn(name = "skills_group_id", referencedColumnName = "id")
    private SkillsGroup skillsGroupBySkillsGroupId;
}
