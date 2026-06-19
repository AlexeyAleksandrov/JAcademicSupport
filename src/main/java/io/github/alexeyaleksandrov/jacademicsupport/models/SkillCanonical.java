package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "skill_canonical")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillCanonical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 200, unique = true)
    private String normalizedName;

    @Column(name = "tech_type", length = 50)
    private String techType;

    @Column(name = "version_group", length = 100)
    private String versionGroup;
}
