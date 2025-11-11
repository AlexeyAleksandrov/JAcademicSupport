package io.github.alexeyaleksandrov.jacademicsupport.services.workskills;

import io.github.alexeyaleksandrov.jacademicsupport.configuration.ItKeywordsConfig;
import io.github.alexeyaleksandrov.jacademicsupport.dto.hh.Vacancy;
import io.github.alexeyaleksandrov.jacademicsupport.dto.hh.VacancyItem;
import io.github.alexeyaleksandrov.jacademicsupport.dto.workskills.WorkSkillDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.workskills.WorkSkillResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.*;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.*;
import io.github.alexeyaleksandrov.jacademicsupport.services.hh.HhService;
import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;
import io.github.alexeyaleksandrov.jacademicsupport.services.gigachat.GigaChatService;
import io.github.alexeyaleksandrov.jacademicsupport.services.vacancies.VacancyService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WorkSkillsService {
    final WorkSkillRepository workSkillRepository;
    final SkillsGroupRepository skillsGroupRepository;
    private final VacancyEntityRepository vacancyEntityRepository;
    final OllamaService ollamaService;
    final GigaChatService gigaChatService;
    private final HhService hhService;
    private KeywordRepository keywordRepository;
    private final SavedSearchRepository searchRepository;
    private final VacancyService vacancyService;
    private final ItKeywordsConfig itKeywordsConfig;

    // Convert WorkSkill entity to WorkSkillResponseDto
    public WorkSkillResponseDto convertToResponseDto(WorkSkill workSkill) {
        return new WorkSkillResponseDto(
                workSkill.getId(),
                workSkill.getDescription(),
                workSkill.getMarketDemand(),
                workSkill.getSkillsGroupBySkillsGroupId() != null ? workSkill.getSkillsGroupBySkillsGroupId().getId() : null
        );
    }

    // Convert WorkSkillDto to WorkSkill entity
    public WorkSkill convertToEntity(WorkSkillDto workSkillDto) {
        WorkSkill workSkill = new WorkSkill();
        workSkill.setDescription(workSkillDto.getDescription());
        
        if (workSkillDto.getSkillsGroupId() != null) {
            SkillsGroup skillsGroup = skillsGroupRepository.findById(workSkillDto.getSkillsGroupId())
                    .orElseThrow(() -> new EntityNotFoundException("SkillsGroup not found"));
            workSkill.setSkillsGroupBySkillsGroupId(skillsGroup);
        }
        
        return workSkill;
    }

    // Create a new WorkSkill from DTO
    public WorkSkillResponseDto createWorkSkill(WorkSkillDto workSkillDto) throws EntityNotFoundException, EntityExistsException {
        if (workSkillRepository.existsByDescription(workSkillDto.getDescription())) {
            throw new EntityExistsException("WorkSkill with this description already exists");
        }
        WorkSkill workSkill = convertToEntity(workSkillDto);
        WorkSkill savedSkill = workSkillRepository.save(workSkill);
        return convertToResponseDto(savedSkill);
    }

    // Update an existing WorkSkill from DTO
    public WorkSkillResponseDto updateWorkSkill(Long id, WorkSkillDto workSkillDto) throws EntityNotFoundException, EntityExistsException {
        WorkSkill existingSkill = workSkillRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("WorkSkill not found"));
        
        // Check if another skill has the same description
        if (!existingSkill.getDescription().equals(workSkillDto.getDescription()) && 
            workSkillRepository.existsByDescription(workSkillDto.getDescription())) {
            throw new EntityExistsException("WorkSkill with this description already exists");
        }
        
        existingSkill.setDescription(workSkillDto.getDescription());
        
        if (workSkillDto.getSkillsGroupId() != null) {
            SkillsGroup skillsGroup = skillsGroupRepository.findById(workSkillDto.getSkillsGroupId())
                    .orElseThrow(() -> new EntityNotFoundException("SkillsGroup not found"));
            existingSkill.setSkillsGroupBySkillsGroupId(skillsGroup);
        }
        
        WorkSkill updatedSkill = workSkillRepository.save(existingSkill);
        return convertToResponseDto(updatedSkill);
    }

    // Update skills group for an existing WorkSkill
    public WorkSkillResponseDto updateSkillsGroup(Long workSkillId, Long skillsGroupId) throws EntityNotFoundException {
        WorkSkill workSkill = workSkillRepository.findById(workSkillId)
                .orElseThrow(() -> new EntityNotFoundException("WorkSkill not found"));
        SkillsGroup skillsGroup = skillsGroupRepository.findById(skillsGroupId)
                .orElseThrow(() -> new EntityNotFoundException("SkillsGroup not found"));
        workSkill.setSkillsGroupBySkillsGroupId(skillsGroup);
        workSkill = workSkillRepository.saveAndFlush(workSkill);
        return convertToResponseDto(workSkill);
    }

    // Get all WorkSkills as DTOs
    public List<WorkSkillResponseDto> getAllWorkSkills() {
        return workSkillRepository.findAll().stream()
                .sorted(Comparator.comparing(WorkSkill::getDescription))
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // Get a single WorkSkill by ID as DTO
    public WorkSkillResponseDto getWorkSkillById(Long id) {
        WorkSkill workSkill = workSkillRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("WorkSkill not found"));
        return convertToResponseDto(workSkill);
    }

    // Delete a WorkSkill by ID
    public void deleteWorkSkill(Long id) {
        workSkillRepository.deleteById(id);
    }

    public List<WorkSkill> matchWorkSkillsToSkillsGroups(String llmProvider) {
        List<WorkSkill> skills = workSkillRepository.findAll();
        List<SkillsGroup> skillsGroups = skillsGroupRepository.findAll();

        String allSkillsPrompt = skillsGroups.stream()
                .map(SkillsGroup::getDescription)
                .toList()
                .stream()
                .collect(Collectors.joining(", ", "[", "]"));   // собираем данные в массив вида ["элемент1", "элемент2", "элемент3"]

        // выполняем сопоставление
        skills = skills.stream()
                .peek(workSkill -> {
                    String prompt = "У тебя есть профессиональный навык " + workSkill.getDescription() + ", к какой группе навыков из списка его можно отнести? Список группы навыков: " + allSkillsPrompt + ". Ответ должен содержать только название группы";
                    
                    // Выбираем LLM провайдера на основе параметра
                    String answer;
                    if ("ollama".equalsIgnoreCase(llmProvider)) {
                        answer = ollamaService.chat(prompt);
                        System.out.println("Используется Ollama для навыка: " + workSkill.getDescription());
                    } else {
                        // По умолчанию используем GigaChat
                        answer = gigaChatService.chat(prompt);
                        System.out.println("Используется GigaChat для навыка: " + workSkill.getDescription());
                    }
                    
                    SkillsGroup skillsGroup = skillsGroups.stream()
                            .filter(group -> answer.contains(group.getDescription()))
                            .findFirst()
                            .orElse(skillsGroupRepository.findByDescription("NO_GROUP"));   // если не найдено задаём дефолтную группу
                    workSkill.setSkillsGroupBySkillsGroupId(skillsGroup);   // устанавливаем группу для навыка
                    System.out.println("Для навыка " + workSkill.getDescription() + " установлена группа " + skillsGroup.getDescription());
                })
                .toList();

        workSkillRepository.saveAllAndFlush(skills);
        return skills;
    }

    public List<Keyword> matchKeywordsToWorkSkills() {
        List<Keyword> keywords = keywordRepository.findAll();
//        List<SkillsGroup> skillsGroups = skillsGroupRepository.findAll();
        List<WorkSkill> workSkills = workSkillRepository.findAll();

        String allKeywordsPrompt = keywords.stream()
                .map(Keyword::getKeyword)
                .toList()
                .stream()
                .collect(Collectors.joining(", ", "[", "]"));   // собираем данные в массив вида ["элемент1", "элемент2", "элемент3"]

        // выполняем сопоставление
        List<Keyword> finalKeywords = keywords;
        workSkills.forEach(workSkill -> {
            String prompt = "У тебя есть технология \"" + workSkill.getDescription() + "\", какое словосочетание из списка для него подходит лучше всего? Список словосочетаний: " + allKeywordsPrompt + ". Ответ должен содержать только подходящее словосочетание из данного списка";
            System.out.println(prompt);
            String answer = ollamaService.chat(prompt);     // получаем рекомендацию от языковой модели по соответствию
            System.out.println(answer);
            finalKeywords.stream()
                    .filter(k -> answer.contains(k.getKeyword()))
                    .toList()
                    .forEach(k -> {
                        List<WorkSkill> workSkillList = k.getWorkSkills();
                        workSkillList.add(workSkill);   // добавляем навык в список
                        k.setWorkSkills(workSkillList);
                        System.out.println("Для навыка \"" + workSkill.getDescription() + "\" сопоставлено ключевое слово \"" + k.getKeyword() + "\"");
                    });
        });

        keywords = keywordRepository.saveAllAndFlush(keywords);
        return keywords;
    }

    public List<VacancyEntity> getAndSaveAllVacancies(String searchText) {
        System.out.println("Search: " + searchText);
        List<VacancyItem> vacancyItemList = hhService.getAllVacancies(searchText);

        // исключаем те вакансии, которые уже сохранены
        List<VacancyItem> vacancyItemListNew = vacancyItemList.stream()
                .filter(vacancyItem -> !vacancyEntityRepository.existsByHhId(vacancyItem.getId()))
                .toList();

        System.out.println("Всего найдено вакансий: " + vacancyItemList.size() + ". Найдено новых вакансий: " + vacancyItemListNew.size());

        // Filter only IT-related vacancies BEFORE fetching full details
        List<VacancyItem> itVacancyItems = vacancyItemListNew.stream()
                .filter(this::isItRelatedVacancyItem)
                .toList();
        
        int nonItCount = vacancyItemListNew.size() - itVacancyItems.size();
        if (nonItCount > 0) {
            System.out.println("Отфильтровано non-IT вакансий: " + nonItCount);
        }
        System.out.println("IT-вакансий для обработки: " + itVacancyItems.size());

        List<Vacancy> vacancies = itVacancyItems.stream()
                .map(vacancyItem -> hhService.getVacancyById(vacancyItem.getId()))
                .toList();  // делаем запрос в hh по каждой вакансии и получаем полную информацию

        List<VacancyEntity> vacancyEntities = vacancies.stream()
                .map(vacancy -> {
                    VacancyEntity vacancyEntity = new VacancyEntity();
                    vacancyEntity.setHhId(vacancy.getId());     // id в HH
                    vacancyEntity.setName(vacancy.getName());
                    vacancyEntity.setDescription(vacancy.getDescription());
                    vacancyEntity.setPublishedAt(vacancy.getPublishedAt());

                    List<WorkSkill> skills = new ArrayList<>();
                    // сначала ищем навыки, которые уже есть в базе
                    skills.addAll(vacancy.getSkills().stream()
                            .filter(workSkillRepository::existsWorkSkillByDescription)
                            .map(workSkillRepository::findByDescription)
                            .toList());
                    // ищем и добавляем навыки, которых нет в базе
                    skills.addAll(vacancy.getSkills().stream()
                            .filter(s -> !workSkillRepository.existsWorkSkillByDescription(s))
                            .map(s -> {
                                SkillsGroup skillsGroup = skillsGroupRepository.findByDescription("NO_GROUP");
                                WorkSkill workSkill = new WorkSkill();
                                workSkill.setDescription(s);
                                workSkill.setSkillsGroupBySkillsGroupId(skillsGroup);
                                workSkill.setMarketDemand(-1.0);
                                workSkill = workSkillRepository.saveAndFlush(workSkill);    // сохраняем в базу данных
                                return workSkill;
                            })
                            .toList());

                    vacancyEntity.setSkills(skills);    // добавляем список навыков в вакансию
                    return vacancyEntity;
                })
                .toList();

        // обновляем данные, если они уже есть
        vacancyEntities.stream()
                .filter(vacancyEntity -> vacancyEntityRepository.existsByHhId(vacancyEntity.getHhId()))
                .peek(vacancyEntity -> {
                    long id = vacancyEntityRepository.findByHhId(vacancyEntity.getHhId()).getId();
                    vacancyEntity.setId(id);
                })
                .forEach(vacancyEntityRepository::saveAndFlush);

        // отображаем новые
        List<VacancyEntity> vacancyEntitiesNew = vacancyEntities.stream()
                .filter(vacancyEntity -> !vacancyEntityRepository.existsByHhId(vacancyEntity.getHhId()))
                .toList();

        System.out.println("Новых вакансий: " + vacancyEntitiesNew.size());

        // сохраняем новые
        vacancyEntitiesNew.forEach(vacancyEntityRepository::saveAndFlush);

        // Automatically process skills for newly added vacancies through GigaChat
        System.out.println("Обработка навыков через GigaChat для новых вакансий...");
        for (VacancyEntity newVacancy : vacancyEntitiesNew) {
            try {
                // Check if vacancy has skills or not
                if (newVacancy.getSkills() != null && !newVacancy.getSkills().isEmpty()) {
                    // Vacancy has skills - normalize them
                    vacancyService.processNewVacancySkills(newVacancy);
                } else {
                    // Vacancy has NO skills - extract from description text
                    vacancyService.processNewVacancyWithoutSkills(newVacancy);
                }
                
                // После обработки навыков через GigaChat, определяем группы для новых навыков
                // Загружаем обновленную вакансию из БД, чтобы получить актуальный список навыков
                VacancyEntity updatedVacancy = vacancyEntityRepository.findById(newVacancy.getId()).orElse(null);
                if (updatedVacancy != null && updatedVacancy.getSkills() != null) {
                    System.out.println("Определение групп технологий для новых навыков вакансии ID: " + updatedVacancy.getId());
                    
                    // Проверяем каждый навык и определяем группу для тех, у кого NO_GROUP
                    for (WorkSkill skill : updatedVacancy.getSkills()) {
                        // Проверяем, является ли навык новым (имеет группу NO_GROUP)
                        if (skill.getSkillsGroupBySkillsGroupId() == null || 
                            "NO_GROUP".equals(skill.getSkillsGroupBySkillsGroupId().getDescription())) {
                            try {
                                System.out.println("Определение группы для нового навыка: " + skill.getDescription());
                                assignSkillGroupWithGigaChat(skill);
                            } catch (Exception groupError) {
                                System.err.println("Ошибка определения группы для навыка \"" + 
                                        skill.getDescription() + "\": " + groupError.getMessage());
                            }
                        }
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Ошибка обработки навыков для вакансии ID: " + newVacancy.getId() + " - " + e.getMessage());
                // Continue processing other vacancies even if one fails
            }
        }
        System.out.println("Завершена обработка навыков через GigaChat");

        return vacancyEntitiesNew;
    }

    private boolean isItRelatedVacancyItem(VacancyItem vacancyItem) {
        String name = vacancyItem.getName().toLowerCase();
        return itKeywordsConfig.getItKeywords().stream().anyMatch(keyword -> name.contains(keyword.toLowerCase()));
    }

    public List<VacancyEntity> getAllVacanciesBySavedSearches() {
        List<SavedSearch> savedSearches = searchRepository.findAll();
        List<VacancyEntity> allVacancies = new ArrayList<>();

        for (SavedSearch search : savedSearches) {
            List<VacancyEntity> vacancies = getAndSaveAllVacancies(search.getSearchQuery());
            allVacancies.addAll(vacancies);
        }

        return allVacancies;
    }

    /**
     * Удаляет все навыки (WorkSkill), которые не привязаны ни к одной вакансии.
     * @return количество удаленных навыков
     */
    @Transactional
    public int deleteUnusedWorkSkills() {
        List<WorkSkill> allSkills = workSkillRepository.findAll();
        List<VacancyEntity> allVacancies = vacancyEntityRepository.findAll();
        
        // Собираем все навыки, которые используются хотя бы в одной вакансии
        List<Long> usedSkillIds = allVacancies.stream()
                .flatMap(vacancy -> {
                    if (vacancy.getSkills() != null) {
                        return vacancy.getSkills().stream();
                    }
                    return java.util.stream.Stream.empty();
                })
                .map(WorkSkill::getId)
                .distinct()
                .toList();
        
        // Находим навыки, которые не используются
        List<WorkSkill> unusedSkills = allSkills.stream()
                .filter(skill -> !usedSkillIds.contains(skill.getId()))
                .toList();
        
        int deletedCount = unusedSkills.size();
        
        if (deletedCount > 0) {
            System.out.println("Найдено неиспользуемых навыков: " + deletedCount);
            unusedSkills.forEach(skill -> 
                System.out.println("  - Удаляется навык: " + skill.getDescription() + " (ID: " + skill.getId() + ")")
            );
            
            // Извлекаем только ID для удаления, чтобы избежать StackOverflowError
            // из-за циклических ссылок в @EqualsAndHashCode
            List<Long> unusedSkillIds = unusedSkills.stream()
                    .map(WorkSkill::getId)
                    .toList();
            
            // Удаляем по ID - безопаснее, чем deleteAllInBatch() для сущностей со связями
            workSkillRepository.deleteAllById(unusedSkillIds);
            System.out.println("Успешно удалено навыков: " + deletedCount);
        } else {
            System.out.println("Неиспользуемых навыков не найдено");
        }
        
        return deletedCount;
    }

    /**
     * Автоматически сопоставляет навык с группой технологий через GigaChat.
     * Используется для новых навыков, чтобы сразу определить их группу.
     * @param workSkill навык для сопоставления
     * @return обновленный навык с назначенной группой
     */
    public WorkSkill assignSkillGroupWithGigaChat(WorkSkill workSkill) {
        try {
            List<SkillsGroup> skillsGroups = skillsGroupRepository.findAll();
            
            String allSkillsPrompt = skillsGroups.stream()
                    .map(SkillsGroup::getDescription)
                    .collect(Collectors.joining(", ", "[", "]"));
            
            String prompt = "У тебя есть профессиональный навык " + workSkill.getDescription() + 
                    ", к какой группе навыков из списка его можно отнести? Список группы навыков: " + 
                    allSkillsPrompt + ". Ответ должен содержать только название группы";
            
            System.out.println("Определение группы для нового навыка через GigaChat: " + workSkill.getDescription());
            String answer = gigaChatService.chat(prompt);
            
            SkillsGroup skillsGroup = skillsGroups.stream()
                    .filter(group -> answer.contains(group.getDescription()))
                    .findFirst()
                    .orElse(skillsGroupRepository.findByDescription("NO_GROUP"));
            
            workSkill.setSkillsGroupBySkillsGroupId(skillsGroup);
            workSkill = workSkillRepository.saveAndFlush(workSkill);
            
            System.out.println("Для нового навыка \"" + workSkill.getDescription() + 
                    "\" установлена группа \"" + skillsGroup.getDescription() + "\"");
            
            return workSkill;
        } catch (Exception e) {
            System.err.println("Ошибка при определении группы для навыка \"" + 
                    workSkill.getDescription() + "\": " + e.getMessage());
            // В случае ошибки оставляем NO_GROUP
            return workSkill;
        }
    }
}
