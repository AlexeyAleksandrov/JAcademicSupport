package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "curriculum_profession",
       uniqueConstraints = @UniqueConstraint(columnNames = {"curriculum_id", "profession_code"}))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurriculumProfession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "curriculum_id", nullable = false)
    private Long curriculumId;

    @Column(name = "profession_code", nullable = false, length = 100)
    private String professionCode;

    @Column(name = "profession_name", length = 200)
    private String professionName;

    @Column(name = "weight", nullable = false)
    private Double weight = 1.0;
}
