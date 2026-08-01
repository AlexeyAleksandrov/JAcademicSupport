package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "curriculum")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Curriculum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "specialization", length = 20)
    private String specialization;

    @Column(name = "profile", length = 200)
    private String profile;

    @Column(name = "academic_year")
    private Integer academicYear;
}
