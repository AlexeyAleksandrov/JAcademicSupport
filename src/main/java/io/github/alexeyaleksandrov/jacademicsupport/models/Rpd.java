package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

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
    @ManyToMany(fetch = FetchType.EAGER)
    private List<CompetencyAchievementIndicator> competencyAchievementIndicators = new ArrayList<>();
    @ManyToMany(fetch = FetchType.EAGER)
    private Map<CompetencyAchievementIndicator, Keyword> keywordsForIndicatorInContextRpdMap = new HashMap<>();

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "rpd_id")
    private List<RecommendedSkill> recommendedSkills = new ArrayList<>();
}
