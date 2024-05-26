package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompetencyAchievementIndicatorRepository extends JpaRepository<CompetencyAchievementIndicator, Long> {
}