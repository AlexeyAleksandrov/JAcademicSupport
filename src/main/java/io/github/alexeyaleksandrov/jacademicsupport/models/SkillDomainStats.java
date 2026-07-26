package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "skill_domain_stats")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillDomainStats {

    @Id
    @Column(name = "canonical_id", nullable = false)
    private Long canonicalId;

    @Column(name = "domain", nullable = false, length = 20)
    private String domain;

    @Column(name = "vacancy_count", nullable = false)
    private int vacancyCount;

    @Column(name = "domain_vacancy_count", nullable = false)
    private int domainVacancyCount;

    @Column(name = "pct_in_domain", nullable = false, precision = 7, scale = 6)
    private BigDecimal pctInDomain;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "top_cooccurrences", columnDefinition = "jsonb")
    private List<Map<String, Object>> topCooccurrences;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;
}
