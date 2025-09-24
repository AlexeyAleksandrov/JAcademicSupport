package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.CompetencyAchievementIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetencyAchievementIndicatorRepository extends JpaRepository<CompetencyAchievementIndicator, Long> {
    CompetencyAchievementIndicator findByNumber(String number);
    boolean existsByNumber(String number);

    List<CompetencyAchievementIndicator> findAllByCompetencyByCompetencyId_Number(String competencyNumber);
}