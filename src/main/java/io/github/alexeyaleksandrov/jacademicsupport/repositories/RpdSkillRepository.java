package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import io.github.alexeyaleksandrov.jacademicsupport.models.RpdSkill;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RpdSkillRepository extends JpaRepository<RpdSkill, Long> {
    
    // Найти все навыки для конкретной РПД
    List<RpdSkill> findByRpd(Rpd rpd);
    
    // Найти все навыки для конкретной РПД по ID
    List<RpdSkill> findByRpdId(Long rpdId);
    
    // Найти все РПД, которые содержат конкретный навык
    List<RpdSkill> findByWorkSkill(WorkSkill workSkill);
    
    // Найти все РПД, которые содержат конкретный навык по ID
    List<RpdSkill> findByWorkSkillId(Long workSkillId);
    
    // Проверить существование связи РПД-навык
    boolean existsByRpdAndWorkSkill(Rpd rpd, WorkSkill workSkill);
    boolean existsByRpdIdAndWorkSkillId(Long rpdId, Long workSkillId);
    
    // Найти конкретную связь РПД-навык
    RpdSkill findByRpdAndWorkSkill(Rpd rpd, WorkSkill workSkill);
    RpdSkill findByRpdIdAndWorkSkillId(Long rpdId, Long workSkillId);
    
    // Найти навыки с временем больше указанного
    List<RpdSkill> findByTimeGreaterThanEqual(Integer minTime);
    
    // Найти навыки для РПД с временем в диапазоне
    List<RpdSkill> findByRpdAndTimeBetween(Rpd rpd, Integer minTime, Integer maxTime);
    List<RpdSkill> findByRpdIdAndTimeBetween(Long rpdId, Integer minTime, Integer maxTime);
}
