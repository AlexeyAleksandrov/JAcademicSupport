package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;

@Entity
@Table(name = "expert_opinion_skills_group")
public class ExpertOpinionSkillsGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "expert_id")
    private ExpertEntity expert;

    @ManyToOne
    @JoinColumn(name = "competency_achievement_indicator_id")
    private CompetencyAchievementIndicator competencyAchievementIndicator;

    @ManyToOne
    @JoinColumn(name = "skills_group_id")
    private SkillsGroup skillsGroup;

    @Column(name = "group_importance", nullable = false, columnDefinition = "double precision default 0.0")
    private double groupImportance; // Importance of the skills group, between 0 and 1

    public ExpertOpinionSkillsGroupEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExpertEntity getExpert() {
        return expert;
    }

    public void setExpert(ExpertEntity expert) {
        this.expert = expert;
    }

    public CompetencyAchievementIndicator getCompetencyAchievementIndicator() {
        return competencyAchievementIndicator;
    }

    public void setCompetencyAchievementIndicator(CompetencyAchievementIndicator competencyAchievementIndicator) {
        this.competencyAchievementIndicator = competencyAchievementIndicator;
    }

    public SkillsGroup getSkillsGroup() {
        return skillsGroup;
    }

    public void setSkillsGroup(SkillsGroup skillsGroup) {
        this.skillsGroup = skillsGroup;
    }

    public double getGroupImportance() {
        return groupImportance;
    }

    public void setGroupImportance(double groupImportance) {
        this.groupImportance = groupImportance;
    }
}
