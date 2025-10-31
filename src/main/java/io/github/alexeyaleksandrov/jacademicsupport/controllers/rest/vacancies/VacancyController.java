package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.vacancies;

import io.github.alexeyaleksandrov.jacademicsupport.dto.vacancies.SkillRequest;
import io.github.alexeyaleksandrov.jacademicsupport.dto.vacancies.VacancyDto;
import io.github.alexeyaleksandrov.jacademicsupport.models.VacancyEntity;
import io.github.alexeyaleksandrov.jacademicsupport.models.WorkSkill;
import io.github.alexeyaleksandrov.jacademicsupport.services.vacancies.VacancyService;
import io.github.alexeyaleksandrov.jacademicsupport.services.workskills.WorkSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
public class VacancyController {

    private final VacancyService vacancyService;
    private final WorkSkillService workSkillService;

    @GetMapping
    public ResponseEntity<List<VacancyEntity>> getAllVacancies(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "50") int limit) {
        Page<VacancyEntity> vacanciesPage = vacancyService.findAllPaginated(offset, limit);
        return ResponseEntity.ok(vacanciesPage.getContent());
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getVacanciesCount() {
        long count = vacancyService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VacancyEntity> getVacancyById(@PathVariable Long id) {
        return vacancyService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/hh/{hhId}")
    public ResponseEntity<VacancyEntity> getVacancyByHhId(@PathVariable Long hhId) {
        return vacancyService.findByHhId(hhId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<VacancyEntity> createVacancy(@RequestBody @Valid VacancyDto vacancyDto) {
        VacancyEntity vacancy = convertToEntity(vacancyDto);
        VacancyEntity savedVacancy = vacancyService.save(vacancy);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedVacancy);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VacancyEntity> updateVacancy(
            @PathVariable Long id,
            @RequestBody @Valid VacancyDto vacancyDto) {

        if (!vacancyService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        VacancyEntity vacancy = convertToEntity(vacancyDto);
        vacancy.setId(id);
        VacancyEntity updatedVacancy = vacancyService.save(vacancy);
        return ResponseEntity.ok(updatedVacancy);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVacancy(@PathVariable Long id) {
        if (!vacancyService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        vacancyService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/skills")
    public ResponseEntity<List<WorkSkill>> getVacancySkills(@PathVariable Long id) {
        return vacancyService.findById(id)
                .map(vacancy -> ResponseEntity.ok(vacancy.getSkills()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/skills")
    public ResponseEntity<VacancyEntity> addSkillToVacancy(
            @PathVariable Long id,
            @RequestBody SkillRequest request) {

        return vacancyService.findById(id)
                .map(vacancy -> {
                    WorkSkill skill = workSkillService.findById(request.getSkillId())
                            .orElseThrow(() -> new RuntimeException("Skill not found"));
                    vacancy.getSkills().add(skill);
                    return ResponseEntity.ok(vacancyService.save(vacancy));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/skills/{skillId}")
    public ResponseEntity<VacancyEntity> removeSkillFromVacancy(
            @PathVariable Long id,
            @PathVariable Long skillId) {

        return vacancyService.findById(id)
                .map(vacancy -> {
                    vacancy.setSkills(
                            vacancy.getSkills().stream()
                                    .filter(skill -> skill.getId() != skillId)
                                    .collect(Collectors.toList())
                    );
                    return ResponseEntity.ok(vacancyService.save(vacancy));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Process vacancy skills using GigaChat AI to normalize and extract proper skill names.
     * This endpoint processes the first 5 vacancies that have at least one skill.
     * For each skill in these vacancies, it sends the description to GigaChat with a custom
     * system prompt to extract and normalize skill names, then replaces old skills with the
     * normalized ones.
     * 
     * @return ResponseEntity with success message
     */
    @PostMapping("/process-skills")
    public ResponseEntity<String> processVacancySkills() {
        try {
            vacancyService.processVacancySkillsWithGigaChat();
            return ResponseEntity.ok("Successfully processed vacancy skills with GigaChat");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing vacancy skills: " + e.getMessage());
        }
    }

    private VacancyEntity convertToEntity(VacancyDto dto) {
        VacancyEntity vacancy = new VacancyEntity();
        vacancy.setHhId(dto.getHhId());
        vacancy.setName(dto.getName());
        vacancy.setPublishedAt(dto.getPublishedAt());
        vacancy.setDescription(dto.getDescription());

        if (dto.getSkillIds() != null) {
            List<WorkSkill> skills = workSkillService.findAllById(dto.getSkillIds());
            vacancy.setSkills(skills);
        }

        return vacancy;
    }

}