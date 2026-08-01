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
    @JoinColumn(name = "work_skill_id", nullable = true)
    private WorkSkill workSkill;

    @Column(name = "skill_importance", nullable = false)
    private double skillImportance;

    @Column(name = "canonical_id")
    private Long canonicalId;

    @Column(name = "direction", nullable = false, length = 10)
    private String direction = "POSITIVE";

    @Column(name = "profession_code")
    private String professionCode;

    @Column(name = "domain")
    private String domain;

    @Column(name = "tech_family")
    private String techFamily;

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

    public double getSkillImportance() {
        return skillImportance;
    }

    public void setSkillImportance(double skillImportance) {
        this.skillImportance = skillImportance;
    }

    public Long getCanonicalId() {
        return canonicalId;
    }

    public void setCanonicalId(Long canonicalId) {
        this.canonicalId = canonicalId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getProfessionCode() {
        return professionCode;
    }

    public void setProfessionCode(String professionCode) {
        this.professionCode = professionCode;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTechFamily() {
        return techFamily;
    }

    public void setTechFamily(String techFamily) {
        this.techFamily = techFamily;
    }
}
