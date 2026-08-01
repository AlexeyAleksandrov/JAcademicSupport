package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.curriculum;

import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.CurriculumDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.CurriculumResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.DisciplineResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.services.curriculum.CurriculumService;
import io.github.alexeyaleksandrov.jacademicsupport.services.curriculum.DisciplineService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/curriculum")
@AllArgsConstructor
public class CurriculumController {

    private final CurriculumService curriculumService;
    private final DisciplineService disciplineService;

    @GetMapping
    public List<CurriculumResponseDto> getAll() {
        return curriculumService.getAll();
    }

    @GetMapping("/{id}")
    public CurriculumResponseDto getById(@PathVariable Long id) {
        return curriculumService.getById(id);
    }

    @GetMapping("/{id}/disciplines")
    public List<DisciplineResponseDto> getDisciplines(@PathVariable Long id) {
        return disciplineService.getByCurriculum(id);
    }

    @PostMapping
    public CurriculumResponseDto create(@RequestBody CurriculumDto dto) {
        return curriculumService.create(dto);
    }

    @PutMapping("/{id}")
    public CurriculumResponseDto update(@PathVariable Long id, @RequestBody CurriculumDto dto) {
        return curriculumService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        curriculumService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
