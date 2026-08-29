package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "vacancy")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacancyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "hh_id", nullable = false)
    private Long hhId;

    @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
    private String name;

    @Column(name = "published_at", length = Integer.MAX_VALUE)
    private String publishedAt;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "vacancy_skills",
            joinColumns = @JoinColumn(name = "vacancy_entity_id"),
            inverseJoinColumns = @JoinColumn(name = "skills_id"),
            indexes = {
                    @Index(name = "idx_vacancy_skills_vacancy", columnList = "vacancy_entity_id"),
                    @Index(name = "idx_vacancy_skills_skill", columnList = "skills_id")
            })
    private List<WorkSkill> skills;
}
