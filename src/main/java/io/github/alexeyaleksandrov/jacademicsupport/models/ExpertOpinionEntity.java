package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;

@Entity
@Table(name = "expert_opinion")
public class ExpertOpinionEntity {
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
    @JoinColumn(name = "work_skill_id")
    private WorkSkill workSkill;

    public ExpertOpinionEntity() {
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

    public WorkSkill getWorkSkill() {
        return workSkill;
    }

    public void setWorkSkill(WorkSkill workSkill) {
        this.workSkill = workSkill;
    }
}
