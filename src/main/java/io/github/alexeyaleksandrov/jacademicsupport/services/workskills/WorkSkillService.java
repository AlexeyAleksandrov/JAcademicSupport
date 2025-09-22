package io.github.alexeyaleksandrov.jacademicsupport.services.workskills;

import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // Автоматически создает конструктор для внедрения зависимостей
@Transactional
public class WorkSkillService {

    private final WorkSkillRepository workSkillRepository;

    public List<WorkSkill> findAll() {
        return workSkillRepository.findAll();
    }

    public Optional<WorkSkill> findById(Long id) {
        return workSkillRepository.findById(id);
    }

    public WorkSkill save(WorkSkill workSkill) {
        // Здесь можно добавить дополнительную логику перед сохранением
        // Например, проверку на уникальность описания
        return workSkillRepository.save(workSkill);
    }

    public void deleteById(Long id) {
        workSkillRepository.deleteById(id);
    }

    public List<WorkSkill> findByDescriptionContaining(String searchTerm) {
        return workSkillRepository.findByDescriptionContainingIgnoreCase(searchTerm);
    }

    public List<WorkSkill> findHighDemandSkills(Double minDemand) {
        return workSkillRepository.findByMarketDemandGreaterThanEqual(minDemand);
    }

    // Дополнительный метод для проверки существования навыка по ID
    public boolean existsById(Long id) {
        return workSkillRepository.existsById(id);
    }

    public List<WorkSkill> findAllById(List<Long> skillIds) {
        return workSkillRepository.findAllById(skillIds);
    }
}