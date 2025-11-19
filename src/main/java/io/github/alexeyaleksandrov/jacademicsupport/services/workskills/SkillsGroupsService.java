package io.github.alexeyaleksandrov.jacademicsupport.services.workskills;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillsGroup;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillsGroupRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.VacancyEntityRepository;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SkillsGroupsService {
    private SkillsGroupRepository skillsGroupRepository;
    private VacancyEntityRepository vacancyEntityRepository;
    private WorkSkillRepository workSkillRepository;

    public Optional<SkillsGroup> findById(Long id) {
        return skillsGroupRepository.findById(id);
    }

    public List<SkillsGroup> findAll() {
        return skillsGroupRepository.findAll();
    }

    public List<SkillsGroup> updateSkillsGroupsMarketDemand() {
        List<SkillsGroup> skillsGroups = skillsGroupRepository.findAll();
        List<VacancyEntity> vacancies = vacancyEntityRepository.findAll();

        skillsGroups.forEach(skillsGroup -> {
            List<WorkSkill> workSkills = workSkillRepository.findBySkillsGroupBySkillsGroupId(skillsGroup);     // получаем список навыков из этой группы
            long groupDemand = vacancies.stream()
                    .filter(vacancyEntity -> vacancyEntity.getSkills().stream()
                            .anyMatch(workSkills::contains))
                    .count();   // фильтруем вакансии, в которых есть нужные навыки
            skillsGroup.setMarketDemand((double)groupDemand/(double)vacancies.size());
            skillsGroupRepository.saveAndFlush(skillsGroup);
        });

        return skillsGroups;
    }
}
