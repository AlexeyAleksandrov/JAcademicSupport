package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "discipline_coverage")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisciplineCoverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discipline_id", nullable = false)
    private Long disciplineId;

    @Column(name = "profession_code", length = 50)
    private String professionCode;

    @Column(name = "domain", length = 50)
    private String domain;

    @Column(name = "tech_family", length = 100)
    private String techFamily;

    @Column(name = "canonical_id")
    private Long canonicalId;

    @Column(name = "hours")
    private Integer hours = 0;
}
