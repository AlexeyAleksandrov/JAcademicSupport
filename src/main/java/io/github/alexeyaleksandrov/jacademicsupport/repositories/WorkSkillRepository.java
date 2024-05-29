package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkSkillRepository extends JpaRepository<WorkSkill, Long> {
    boolean existsWorkSkillByDescription(String skillName);
    WorkSkill findByDescription(String skillName);
}