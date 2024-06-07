package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "competency_achievement_indicator", schema = "public", catalog = "AcademicSupport")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CompetencyAchievementIndicator {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private long id;
    @Basic
    @Column(name = "description", nullable = false, length = 255)
    private String description;
    @Basic
    @Column(name = "indicator_know", nullable = false, length = 255)
    private String indicatorKnow;
    @Basic
    @Column(name = "indicator_able", nullable = false, length = 255)
    private String indicatorAble;
    @Basic
    @Column(name = "indicator_possess", nullable = false, length = 255)
    private String indicatorPossess;
    @Basic
    @Column(name = "number", nullable = false, length = 10)
    private String number;
    @ManyToOne
    @JoinColumn(name = "competency_id", referencedColumnName = "id", nullable = false)
    private Competency competencyByCompetencyId;
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Keyword> keywords;
}
