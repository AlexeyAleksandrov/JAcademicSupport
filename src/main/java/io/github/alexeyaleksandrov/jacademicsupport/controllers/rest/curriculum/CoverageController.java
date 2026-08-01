package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.curriculum;

import io.github.alexeyaleksandrov.jacademicsupport.services.curriculum.DisciplineCoverageService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coverage")
@AllArgsConstructor
public class CoverageController {

    private final DisciplineCoverageService coverageService;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        coverageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
