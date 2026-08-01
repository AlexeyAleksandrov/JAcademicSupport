package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "foresight")
public class ForesightEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "work_skill_id", nullable = true)
    private WorkSkill workSkill;

    @Column(name = "source_name")
    private String sourceName;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "canonical_id")
    private Long canonicalId;

    @Column(name = "confidence", nullable = false, precision = 4, scale = 3)
    private BigDecimal confidence = new BigDecimal("0.500");

    @Column(name = "direction", nullable = false, length = 10)
    private String direction = "POSITIVE";

    @Column(name = "profession_code")
    private String professionCode;

    @Column(name = "domain")
    private String domain;

    @Column(name = "tech_family")
    private String techFamily;

    @Column(name = "forecast_date")
    private LocalDate forecastDate;

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

    public Long getCanonicalId() {
        return canonicalId;
    }

    public void setCanonicalId(Long canonicalId) {
        this.canonicalId = canonicalId;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
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

    public LocalDate getForecastDate() {
        return forecastDate;
    }

    public void setForecastDate(LocalDate forecastDate) {
        this.forecastDate = forecastDate;
    }
}
