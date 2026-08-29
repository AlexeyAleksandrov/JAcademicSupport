package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.curriculum;

import io.github.alexeyaleksandrov.jacademicsupport.models.CurriculumProfession;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.CurriculumProfessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/curriculum/{curriculumId}/professions")
@RequiredArgsConstructor
public class CurriculumProfessionController {

    private final CurriculumProfessionRepository repo;

    @GetMapping
    public List<CurriculumProfession> list(@PathVariable Long curriculumId) {
        return repo.findByCurriculumId(curriculumId);
    }

    @PostMapping
    @Transactional
    public CurriculumProfession add(@PathVariable Long curriculumId,
                                    @RequestBody Map<String, Object> body) {
        String code   = (String) body.get("professionCode");
        String name   = (String) body.getOrDefault("professionName", code);
        double weight = body.containsKey("weight")
                        ? ((Number) body.get("weight")).doubleValue() : 1.0;
        if (!Double.isFinite(weight) || weight < 0.0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Profession weight must be finite and non-negative");
        }

        CurriculumProfession cp = repo.findByCurriculumIdAndProfessionCode(curriculumId, code)
                .orElse(new CurriculumProfession(null, curriculumId, code, name, weight));
        cp.setProfessionName(name);
        cp.setWeight(weight);
        return repo.save(cp);
    }

    @DeleteMapping("/{professionCode}")
    @Transactional
    public ResponseEntity<Void> remove(@PathVariable Long curriculumId,
                                       @PathVariable String professionCode) {
        repo.deleteByCurriculumIdAndProfessionCode(curriculumId, professionCode);
        return ResponseEntity.noContent().build();
    }
}
