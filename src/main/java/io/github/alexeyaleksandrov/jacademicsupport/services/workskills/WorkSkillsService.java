package io.github.alexeyaleksandrov.jacademicsupport.services.workskills;

import io.github.alexeyaleksandrov.jacademicsupport.dto.hh.Vacancy;
import io.github.alexeyaleksandrov.jacademicsupport.dto.hh.VacancyItem;
import io.github.alexeyaleksandrov.jacademicsupport.models.*;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.*;
import io.github.alexeyaleksandrov.jacademicsupport.services.hh.HhService;
import io.github.alexeyaleksandrov.jacademicsupport.services.ollama.OllamaService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WorkSkillsService {
    final WorkSkillRepository workSkillRepository;
    final SkillsGroupRepository skillsGroupRepository;
    private final VacancyEntityRepository vacancyEntityRepository;
    final OllamaService ollamaService;
    private final HhService hhService;
    private KeywordRepository keywordRepository;
    private final SavedSearchRepository searchRepository;

    public List<WorkSkill> matchWorkSkillsToSkillsGroups() {
        List<WorkSkill> skills = workSkillRepository.findAll();
        List<SkillsGroup> skillsGroups = skillsGroupRepository.findAll();

        String allSkillsPrompt = skillsGroups.stream()
                .map(SkillsGroup::getDescription)
                .toList()
                .stream()
                .collect(Collectors.joining(", ", "[", "]"));   // собираем данные в массив вида ["элемент1", "элемент2", "элемент3"]

        // выполняем cопоставление
        skills = skills.stream()
                .peek(workSkill -> {
                    String prompt = "У тебя есть профессиональный навык " + workSkill.getDescription() + ", к какой группе навыков из списка его можно отнести? Список группы навыков: " + allSkillsPrompt + ". Ответ должен содержать только название группы";
                    String answer = ollamaService.chat(prompt);     // получаем рекомендацию от языковой модели по группе технологий
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

        // выполняем cопоставление
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
//        int vacanciesCount = vacancyItemList.size();
//        int lastVacancyIndex = 0;
        List<Vacancy> vacancies = vacancyItemList.stream()
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

        // сохраняем новые
        vacancyEntities.stream()
                .filter(vacancyEntity -> !vacancyEntityRepository.existsByHhId(vacancyEntity.getHhId()))
                .forEach(vacancyEntityRepository::saveAndFlush);

        return vacancyEntities;
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
}
