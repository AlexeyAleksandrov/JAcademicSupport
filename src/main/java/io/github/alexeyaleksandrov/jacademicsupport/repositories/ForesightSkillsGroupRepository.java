package io.github.alexeyaleksandrov.jacademicsupport.repositories;

import io.github.alexeyaleksandrov.jacademicsupport.models.ForesightSkillsGroupEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForesightSkillsGroupRepository extends JpaRepository<ForesightSkillsGroupEntity, Long> {
    
    // Найти все предсказания для конкретной группы технологий
    List<ForesightSkillsGroupEntity> findBySkillsGroup(SkillsGroup skillsGroup);
    List<ForesightSkillsGroupEntity> findBySkillsGroupId(Long skillsGroupId);
    
    // Найти предсказания по источнику
    List<ForesightSkillsGroupEntity> findBySourceName(String sourceName);
    
    // Проверить существование предсказания по группе технологий и URL источника
    boolean existsBySkillsGroupIdAndSourceUrl(Long skillsGroupId, String sourceUrl);
    
    // Найти конкретное предсказание по группе технологий и URL источника
    ForesightSkillsGroupEntity findBySkillsGroupIdAndSourceUrl(Long skillsGroupId, String sourceUrl);
    
    // Поиск предсказаний по части URL
    List<ForesightSkillsGroupEntity> findBySourceUrlContaining(String urlPart);
}
