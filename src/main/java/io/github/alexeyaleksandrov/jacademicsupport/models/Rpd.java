package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Rpd {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private long id;
    @Basic
    @Column(name = "discipline_name", nullable = false, length = -1)
    private String disciplineName;
    @Basic
    @Column(name = "year", nullable = false)
    private int year;
    @OneToMany(fetch = FetchType.EAGER)
    private List<CompetencyAchievementIndicator> competencyAchievementIndicators;
    @OneToMany(fetch = FetchType.EAGER)
    private Map<CompetencyAchievementIndicator, Keyword> keywordsForIndicatorInContextRpdMap;
    @OneToMany(fetch = FetchType.EAGER)
    private List<WorkSkill> recommendedWorkSkills;
}
