package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "vacancy_profession")
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(VacancyProfession.VacancyProfessionId.class)
public class VacancyProfession {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private VacancyEntity vacancy;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profession_id", nullable = false)
    private Profession profession;

    @Column(name = "confidence", nullable = false, precision = 4, scale = 3)
    private BigDecimal confidence;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VacancyProfessionId implements Serializable {
        private Long vacancy;
        private Long profession;
    }
}
