package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vacancy_domain")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacancyDomain {

    @Id
    @Column(name = "vacancy_id", nullable = false)
    private Long vacancyId;

    @Column(name = "primary_domain", length = 20)
    private String primaryDomain;

    @Column(name = "domain_score", precision = 5, scale = 4)
    private BigDecimal domainScore;

    @Column(name = "computed_at")
    private LocalDateTime computedAt;
}
