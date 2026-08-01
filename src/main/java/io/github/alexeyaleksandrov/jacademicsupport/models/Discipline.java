package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "discipline")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Discipline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "curriculum_id")
    private Long curriculumId;

    @Column(name = "name", nullable = false, length = 300)
    private String name;

    @Column(name = "total_hours")
    private Integer totalHours;

    @Column(name = "semester")
    private Integer semester;
}
