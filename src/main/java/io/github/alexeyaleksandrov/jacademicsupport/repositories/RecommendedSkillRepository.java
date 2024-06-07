package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.RecommendedSkill;
import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendedSkillRepository extends JpaRepository<RecommendedSkill, Long> {
//    void deleteAllByRpd(Rpd rpd);
}