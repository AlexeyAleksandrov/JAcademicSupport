package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.curriculum;

import io.github.alexeyaleksandrov.jacademicsupport.models.SkillCanonical;
import io.github.alexeyaleksandrov.jacademicsupport.repositories.SkillCanonicalRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skill-canonical")
@AllArgsConstructor
public class SkillCanonicalController {

    private final SkillCanonicalRepository skillCanonicalRepository;

    @GetMapping("/search")
    public List<SkillCanonical> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String family,
            @RequestParam(defaultValue = "false") boolean familyMissing) {
        if (q.isBlank() && domain == null && family == null && !familyMissing) return List.of();
        if (familyMissing) {
            if (domain == null || domain.isBlank()) return List.of();
            return skillCanonicalRepository.searchByNameWithoutFamily(q, domain, PageRequest.of(0, limit));
        }
        return skillCanonicalRepository.searchByNameFiltered(q, domain, family, PageRequest.of(0, limit));
    }

    @GetMapping("/domains")
    public List<String> domains() {
        return skillCanonicalRepository.findDistinctDomains();
    }

    @GetMapping("/families")
    public List<String> families(@RequestParam(required = false) String domain) {
        return skillCanonicalRepository.findDistinctFamilies(domain);
    }

    @GetMapping("/families-grouped")
    public Map<String, List<String>> familiesGrouped() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (String domain : skillCanonicalRepository.findDistinctDomains()) {
            result.put(domain, skillCanonicalRepository.findDistinctFamilies(domain));
        }
        return result;
    }
}
