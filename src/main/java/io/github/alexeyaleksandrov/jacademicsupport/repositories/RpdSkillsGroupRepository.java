package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.Rpd;
import io.github.alexeyaleksandrov.jacademicsupport.models.RpdSkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RpdSkillsGroupRepository extends JpaRepository<RpdSkillsGroup, Long> {
    
    // Найти все группы технологий для конкретной РПД
    List<RpdSkillsGroup> findByRpd(Rpd rpd);
    
    // Найти все группы технологий для конкретной РПД по ID
    List<RpdSkillsGroup> findByRpdId(Long rpdId);
    
    // Найти все РПД, которые содержат конкретную группу технологий
    List<RpdSkillsGroup> findBySkillsGroup(SkillsGroup skillsGroup);
    
    // Найти все РПД, которые содержат конкретную группу технологий по ID
    List<RpdSkillsGroup> findBySkillsGroupId(Long skillsGroupId);
    
    // Проверить существование связи РПД-группа технологий
    boolean existsByRpdAndSkillsGroup(Rpd rpd, SkillsGroup skillsGroup);
    boolean existsByRpdIdAndSkillsGroupId(Long rpdId, Long skillsGroupId);
    
    // Найти конкретную связь РПД-группа технологий
    RpdSkillsGroup findByRpdAndSkillsGroup(Rpd rpd, SkillsGroup skillsGroup);
    RpdSkillsGroup findByRpdIdAndSkillsGroupId(Long rpdId, Long skillsGroupId);
    
    // Найти группы технологий с временем больше указанного
    List<RpdSkillsGroup> findByTimeGreaterThanEqual(Integer minTime);
    
    // Найти группы технологий для РПД с временем в диапазоне
    List<RpdSkillsGroup> findByRpdAndTimeBetween(Rpd rpd, Integer minTime, Integer maxTime);
    List<RpdSkillsGroup> findByRpdIdAndTimeBetween(Long rpdId, Integer minTime, Integer maxTime);
    
    // Получить общее количество часов для конкретной группы технологий
    @Query("SELECT COALESCE(SUM(rsg.time), 0) FROM RpdSkillsGroup rsg WHERE rsg.skillsGroup.id = :skillsGroupId")
    Long getTotalTimeBySkillsGroupId(@Param("skillsGroupId") Long skillsGroupId);
    
    // Получить общее количество часов для всех RpdSkillsGroup
    @Query("SELECT COALESCE(SUM(rsg.time), 0) FROM RpdSkillsGroup rsg")
    Long getTotalTime();
}
