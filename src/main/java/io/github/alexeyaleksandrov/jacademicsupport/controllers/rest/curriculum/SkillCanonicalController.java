package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.curriculum;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillCanonicalRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skill-canonical")
@AllArgsConstructor
public class SkillCanonicalController {

    private final SkillCanonicalRepository skillCanonicalRepository;

    @GetMapping("/search")
    public List<SkillCanonical> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "20") int limit) {
        if (q.isBlank()) return List.of();
        return skillCanonicalRepository.searchByName(q, PageRequest.of(0, limit));
    }

    @GetMapping("/domains")
    public List<String> domains() {
        return skillCanonicalRepository.findDistinctDomains();
    }

    @GetMapping("/families")
    public List<String> families(@RequestParam(required = false) String domain) {
        return skillCanonicalRepository.findDistinctFamilies(domain);
    }
}
