package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.expert;

import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertEntity;
import io.github.alexeyaleksandrov.jacademicsupport.services.ExpertService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/experts")
public class ExpertController {
    private final ExpertService expertService;

    public ExpertController(ExpertService expertService) {
        this.expertService = expertService;
    }

    @GetMapping
    public ResponseEntity<List<ExpertEntity>> getAllExperts() {
        List<ExpertEntity> experts = expertService.findAll();
        return ResponseEntity.ok(experts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpertEntity> getExpertById(@PathVariable Long id) {
        ExpertEntity expert = expertService.findById(id);
        if (expert == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(expert);
    }

    @PostMapping
    public ResponseEntity<ExpertEntity> createExpert(@RequestBody ExpertEntity expertEntity) {
        ExpertEntity savedExpert = expertService.save(expertEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedExpert);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpertEntity> updateExpert(@PathVariable Long id, @RequestBody ExpertEntity expertEntity) {
        ExpertEntity existingExpert = expertService.findById(id);
        if (existingExpert == null) {
            return ResponseEntity.notFound().build();
        }
        expertEntity.setId(id);
        ExpertEntity updatedExpert = expertService.save(expertEntity);
        return ResponseEntity.ok(updatedExpert);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpert(@PathVariable Long id) {
        ExpertEntity expert = expertService.findById(id);
        if (expert == null) {
            return ResponseEntity.notFound().build();
        }
        expertService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
