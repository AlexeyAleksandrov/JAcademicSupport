package io.github.alexeyaleksandrov.jacademicsupport.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "skill_version")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canonical_id", nullable = false)
    private SkillCanonical canonical;

    @Column(name = "raw_string", length = 255)
    private String rawString;

    @Column(name = "version_min", length = 20)
    private String versionMin;

    @Column(name = "version_max", length = 20)
    private String versionMax;

    @Column(name = "is_plus", nullable = false)
    private boolean isPlus = false;
}
