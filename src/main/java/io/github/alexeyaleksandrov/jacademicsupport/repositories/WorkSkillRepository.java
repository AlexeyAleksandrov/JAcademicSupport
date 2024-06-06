package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkSkillRepository extends JpaRepository<WorkSkill, Long> {
    boolean existsWorkSkillByDescription(String skillName);
    WorkSkill findByDescription(String skillName);
    List<WorkSkill> findBySkillsGroupBySkillsGroupId(SkillsGroup skillsGroup);
}