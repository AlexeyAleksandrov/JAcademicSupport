package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillDomainStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillDomainStatsRepository extends JpaRepository<SkillDomainStats, Long> {

    Optional<SkillDomainStats> findByCanonicalId(Long canonicalId);

    List<SkillDomainStats> findByDomainOrderByVacancyCountDesc(String domain);
}
