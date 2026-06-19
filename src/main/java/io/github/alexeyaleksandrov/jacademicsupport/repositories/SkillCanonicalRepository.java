package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillCanonicalRepository extends JpaRepository<SkillCanonical, Long> {

    Optional<SkillCanonical> findByNormalizedName(String normalizedName);

    boolean existsByNormalizedName(String normalizedName);
}
