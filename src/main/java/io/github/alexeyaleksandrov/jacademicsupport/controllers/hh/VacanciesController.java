package io.github.alexeyaleksandrov.jacademicsupport.controllers.hh;

import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.models.hh.Vacancy;
import io.github.alexeyaleksandrov.jacademicsupport.models.hh.VacancyItem;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.WorkSkillRepository;
import io.github.alexeyaleksandrov.jacademicsupport.services.hh.HhService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
public class VacanciesController {
    private final HhService hhService;
    private final WorkSkillRepository workSkillRepository;

    @GetMapping("/vac/list")
    private ResponseEntity<List<VacancyItem>> vacanciesList() {
        List<VacancyItem> vacancyItemList = hhService.getAllVacancies("Java Junior Developer");
        return ResponseEntity.ok(vacancyItemList);
    }

    @GetMapping("/vac/{id}")
    private ResponseEntity<Vacancy> vacanciesList(@PathVariable(name = "id") Long id) {
        Vacancy vacancy = hhService.getVacancyById(id);
        return ResponseEntity.ok(vacancy);
    }

    @GetMapping("/vac/list/save")
    private ResponseEntity<String> getAndSaveAllVacancies() {
        List<VacancyItem> vacancyItemList = hhService.getAllVacancies("Java Junior Developer");
        List<Vacancy> vacancies = vacancyItemList.stream()
                .map(vacancyItem -> hhService.getVacancyById(vacancyItem.getId()))
                .toList();  // делаем запрос в hh по каждой вакансии и получаем полную информацию

        List<VacancyEntity> vacancyEntities = vacancies.stream()
                .map(vacancy -> {
                    VacancyEntity vacancyEntity = new VacancyEntity();
                    vacancyEntity.setHhId(vacancy.getId());     // id в HH
                    vacancyEntity.setName(vacancy.getName());
                    vacancyEntity.setDescription(vacancy.getDescription());
                    vacancyEntity.setPublishedAt(vacancyEntity.getPublishedAt());

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
                                WorkSkill workSkill = new WorkSkill();
                                workSkill.setDescription(s);
                                workSkill = workSkillRepository.saveAndFlush(workSkill);    // сохраняем в базу данных
                                return workSkill;
                            })
                            .toList());

                    vacancyEntity.setSkills(skills);    // добавляем список навыков в вакансию
                    return vacancyEntity;
                })
                .toList();


        return ResponseEntity.ok("Ok");
    }
}
