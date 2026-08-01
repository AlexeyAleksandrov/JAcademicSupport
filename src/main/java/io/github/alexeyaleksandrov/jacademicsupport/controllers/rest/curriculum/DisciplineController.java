package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.curriculum;

import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.DisciplineCoverageDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.DisciplineCoverageResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.DisciplineDto;
import io.github.alexeyaleksandrov.jacademicsupport.dto.curriculum.DisciplineResponseDto;
import io.github.alexeyaleksandrov.jacademicsupport.services.curriculum.DisciplineCoverageService;
import io.github.alexeyaleksandrov.jacademicsupport.services.curriculum.DisciplineService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disciplines")
@AllArgsConstructor
public class DisciplineController {

    private final DisciplineService disciplineService;
    private final DisciplineCoverageService coverageService;

    @PostMapping
    public DisciplineResponseDto create(@RequestBody DisciplineDto dto) {
        return disciplineService.create(dto);
    }

    @GetMapping("/{id}")
    public DisciplineResponseDto getById(@PathVariable Long id) {
        return disciplineService.getById(id);
    }

    @PutMapping("/{id}")
    public DisciplineResponseDto update(@PathVariable Long id, @RequestBody DisciplineDto dto) {
        return disciplineService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        disciplineService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/coverage")
    public List<DisciplineCoverageResponseDto> getCoverage(@PathVariable Long id) {
        return coverageService.getByDisciplineId(id);
    }

    @PostMapping("/{id}/coverage")
    public DisciplineCoverageResponseDto addCoverage(@PathVariable Long id,
                                                      @RequestBody DisciplineCoverageDto dto) {
        return coverageService.create(id, dto);
    }
}
