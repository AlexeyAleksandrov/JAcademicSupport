package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;

@Entity
@Table(name = "foresight_skills_group")
public class ForesightSkillsGroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "skills_group_id", nullable = false)
    private SkillsGroup skillsGroup;

    @Column(name = "source_name")
    private String sourceName;

    @Column(name = "source_url")
    private String sourceUrl;

    public ForesightSkillsGroupEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SkillsGroup getSkillsGroup() {
        return skillsGroup;
    }

    public void setSkillsGroup(SkillsGroup skillsGroup) {
        this.skillsGroup = skillsGroup;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
}
