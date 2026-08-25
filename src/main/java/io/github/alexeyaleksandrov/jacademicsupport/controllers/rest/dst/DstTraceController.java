package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level0.DstL0Response;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level1.DstL1DisciplineResponse;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level1.DstL1Response;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level2.DstL2DisciplineResponse;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level2.DstL2Response;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstLevel0Service;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstLevel1Service;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstLevel2Service;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dst")
@AllArgsConstructor
public class DstTraceController {

    private final DstLevel0Service      dstLevel0Service;
    private final DstLevel1Service      dstLevel1Service;
    private final DstLevel2Service      dstLevel2Service;

    /**
     * Level 0 analysis for a curriculum: weighted DST per domain using profession mix.
     */
    @GetMapping("/curriculum/{curriculumId}/level0")
    public DstL0Response level0ByCurriculum(@PathVariable Long curriculumId) {
        return dstLevel0Service.analyzeLevel0(curriculumId);
    }

    @GetMapping("/curriculum/{curriculumId}/domain/{domain}/supply-breakdown")
    public List<Map<String, Object>> supplyBreakdown(@PathVariable Long curriculumId,
                                                      @PathVariable String domain) {
        return dstLevel0Service.getSupplyBreakdown(curriculumId, domain);
    }

    /**
     * Level 1 analysis for a curriculum + domain: weighted DST per tech_family.
     * Budget = total domain hours across all disciplines in curriculum.
     */
    @GetMapping("/curriculum/{curriculumId}/level1")
    public DstL1Response level1ByCurriculumDomain(@PathVariable Long curriculumId,
                                                   @RequestParam String domain) {
        return dstLevel1Service.analyzeLevel1(curriculumId, domain);
    }

    /**
     * Level 1 analysis for a single discipline: DST per tech_family grouped by domain.
     * Budget = discipline.totalHours.
     */
    @GetMapping("/discipline/{disciplineId}/level1")
    public DstL1DisciplineResponse level1ByDiscipline(@PathVariable Long disciplineId) {
        return dstLevel1Service.analyzeLevel1ForDiscipline(disciplineId);
    }

    /**
     * Level 2 analysis for a curriculum + domain + techFamily: weighted DST per canonical skill.
     * Budget = total family hours across all disciplines in curriculum.
     */
    @GetMapping("/curriculum/{curriculumId}/level2")
    public DstL2Response level2ByCurriculumFamily(@PathVariable Long curriculumId,
                                                   @RequestParam String domain,
                                                   @RequestParam String techFamily) {
        return dstLevel2Service.analyzeLevel2(curriculumId, domain, techFamily);
    }

    /**
     * Level 2 analysis for a single discipline + domain + techFamily: weighted DST per canonical skill.
     * Budget = discipline hours allocated to the given domain+techFamily (normalized).
     */
    @GetMapping("/discipline/{disciplineId}/level2")
    public DstL2DisciplineResponse level2ByDisciplineFamily(@PathVariable Long disciplineId,
                                                             @RequestParam String domain,
                                                             @RequestParam String techFamily) {
        return dstLevel2Service.analyzeLevel2ForDisciplineAndFamily(disciplineId, domain, techFamily);
    }
}
