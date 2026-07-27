package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillVersionRepository extends JpaRepository<SkillVersion, Long> {

    List<SkillVersion> findByCanonical(SkillCanonical canonical);

    boolean existsByCanonicalAndRawString(SkillCanonical canonical, String rawString);

    @Query("SELECT sv FROM SkillVersion sv WHERE sv.canonical.id = :canonicalId ORDER BY sv.id")
    List<SkillVersion> findByCanonicalId(@Param("canonicalId") Long canonicalId);
}
