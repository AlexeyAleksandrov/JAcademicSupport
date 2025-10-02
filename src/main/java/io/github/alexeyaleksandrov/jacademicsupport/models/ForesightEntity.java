package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;

@Entity
@Table(name = "foresight")
public class ForesightEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "work_skill_id", nullable = false)
    private WorkSkill workSkill;

    @Column(name = "source_name")
    private String sourceName;

    @Column(name = "source_url")
    private String sourceUrl;

    public ForesightEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkSkill getWorkSkill() {
        return workSkill;
    }

    public void setWorkSkill(WorkSkill workSkill) {
        this.workSkill = workSkill;
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
