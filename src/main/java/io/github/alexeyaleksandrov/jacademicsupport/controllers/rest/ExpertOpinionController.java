package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest;

import io.github.alexeyaleksandrov.jacademicsupport.models.ExpertOpinionEntity;
import io.github.alexeyaleksandrov.jacademicsupport.services.ExpertOpinionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expert-opinions")
public class ExpertOpinionController {
    private final ExpertOpinionService expertOpinionService;

    public ExpertOpinionController(ExpertOpinionService expertOpinionService) {
        this.expertOpinionService = expertOpinionService;
    }

    @GetMapping
    public ResponseEntity<List<ExpertOpinionEntity>> getAllExpertOpinions() {
        List<ExpertOpinionEntity> expertOpinions = expertOpinionService.findAll();
        return ResponseEntity.ok(expertOpinions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpertOpinionEntity> getExpertOpinionById(@PathVariable Long id) {
        ExpertOpinionEntity expertOpinion = expertOpinionService.findById(id);
        if (expertOpinion == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(expertOpinion);
    }

    @PostMapping
    public ResponseEntity<ExpertOpinionEntity> createExpertOpinion(@RequestBody ExpertOpinionEntity expertOpinionEntity) {
        ExpertOpinionEntity savedExpertOpinion = expertOpinionService.save(expertOpinionEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedExpertOpinion);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpertOpinionEntity> updateExpertOpinion(@PathVariable Long id, @RequestBody ExpertOpinionEntity expertOpinionEntity) {
        ExpertOpinionEntity existingExpertOpinion = expertOpinionService.findById(id);
        if (existingExpertOpinion == null) {
            return ResponseEntity.notFound().build();
        }
        expertOpinionEntity.setId(id);
        ExpertOpinionEntity updatedExpertOpinion = expertOpinionService.save(expertOpinionEntity);
        return ResponseEntity.ok(updatedExpertOpinion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpertOpinion(@PathVariable Long id) {
        ExpertOpinionEntity expertOpinion = expertOpinionService.findById(id);
        if (expertOpinion == null) {
            return ResponseEntity.notFound().build();
        }
        expertOpinionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
