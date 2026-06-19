package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillDependencyRepository extends JpaRepository<SkillDependency, SkillDependency.SkillDependencyId> {

    List<SkillDependency> findByChild(SkillCanonical child);

    List<SkillDependency> findByParent(SkillCanonical parent);

    Optional<SkillDependency> findByParentAndChild(SkillCanonical parent, SkillCanonical child);

    @Query("SELECT d FROM SkillDependency d WHERE d.child = :child AND d.weight >= :minWeight")
    List<SkillDependency> findByChildWithMinWeight(@Param("child") SkillCanonical child,
                                                   @Param("minWeight") java.math.BigDecimal minWeight);
}
