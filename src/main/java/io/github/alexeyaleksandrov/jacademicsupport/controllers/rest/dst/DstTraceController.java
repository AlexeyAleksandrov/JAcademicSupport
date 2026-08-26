package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level0.DstL0Response;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level1.DstL1DisciplineResponse;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level1.DstL1Response;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level2.DstL2DisciplineResponse;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level2.DstL2Response;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstCalcOptions;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstLevel0Service;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstLevel1Service;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstLevel2Service;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstSettingsService;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * L0–L2 DST endpoints.
 *
 * Every endpoint accepts the optional calculation switches
 * ({@code domainMode}, {@code familyMode}, {@code skillMode}, {@code hoursBase},
 * {@code budgetMode}, {@code budgetHours}, {@code disciplineId}). When they are omitted the values
 * stored in {@code dst_settings} are used, so existing clients remain API
 * compatible while calculation policy can be changed centrally.
 */
@RestController
@RequestMapping("/api/dst")
@AllArgsConstructor
public class DstTraceController {

    private final DstLevel0Service   dstLevel0Service;
    private final DstLevel1Service   dstLevel1Service;
    private final DstLevel2Service   dstLevel2Service;
    private final DstSettingsService settingsService;

    private DstCalcOptions options(String domainMode, String familyMode, String skillMode,
                                   String hoursBase, String budgetMode, Integer budgetHours,
                                   Long disciplineId) {
        if (budgetHours != null && budgetHours < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "budgetHours: ожидается целое число не меньше 0");
        }
        if (disciplineId != null && disciplineId < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "disciplineId: ожидается положительный идентификатор");
        }
        try {
            return DstCalcOptions.of(settingsService.get(),
                    domainMode, familyMode, skillMode, hoursBase, budgetMode, budgetHours, disciplineId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Недопустимое значение режима расчёта", ex);
        }
    }

    /**
     * Level 0 analysis for a curriculum: weighted DST per domain using profession mix.
     */
    @GetMapping("/curriculum/{curriculumId}/level0")
    public DstL0Response level0ByCurriculum(@PathVariable Long curriculumId,
                                             @RequestParam(required = false) String domainMode,
                                             @RequestParam(required = false) String familyMode,
                                             @RequestParam(required = false) String skillMode,
                                             @RequestParam(required = false) String hoursBase,
                                             @RequestParam(required = false) String budgetMode,
                                             @RequestParam(required = false) Integer budgetHours,
                                             @RequestParam(required = false) Long disciplineId) {
        return dstLevel0Service.analyzeLevel0(curriculumId,
                options(domainMode, familyMode, skillMode, hoursBase, budgetMode, budgetHours, disciplineId));
    }

    @GetMapping("/curriculum/{curriculumId}/domain/{domain}/supply-breakdown")
    public List<Map<String, Object>> supplyBreakdown(@PathVariable Long curriculumId,
                                                      @PathVariable String domain) {
        return dstLevel0Service.getSupplyBreakdown(curriculumId, domain);
    }

    /**
     * Level 1 analysis for a curriculum + domain: weighted DST per tech_family.
     * Budget depends on hoursBase/budgetMode; by default it is the conditional
     * volume of the disciplines related to the domain.
     */
    @GetMapping("/curriculum/{curriculumId}/level1")
    public DstL1Response level1ByCurriculumDomain(@PathVariable Long curriculumId,
                                                   @RequestParam String domain,
                                                   @RequestParam(required = false) String domainMode,
                                                   @RequestParam(required = false) String familyMode,
                                                   @RequestParam(required = false) String skillMode,
                                                   @RequestParam(required = false) String hoursBase,
                                                   @RequestParam(required = false) String budgetMode,
                                                   @RequestParam(required = false) Integer budgetHours,
                                                   @RequestParam(required = false) Long disciplineId) {
        return dstLevel1Service.analyzeLevel1(curriculumId, domain,
                options(domainMode, familyMode, skillMode, hoursBase, budgetMode, budgetHours, disciplineId));
    }

    /**
     * Level 1 analysis for a single discipline: DST per tech_family grouped by domain.
     * Budget = discipline.totalHours.
     */
    @GetMapping("/discipline/{disciplineId}/level1")
    public DstL1DisciplineResponse level1ByDiscipline(@PathVariable Long disciplineId,
                                                       @RequestParam(required = false) String domainMode,
                                                       @RequestParam(required = false) String familyMode,
                                                       @RequestParam(required = false) String skillMode,
                                                       @RequestParam(required = false) String hoursBase,
                                                       @RequestParam(required = false) String budgetMode,
                                                       @RequestParam(required = false) Integer budgetHours) {
        return dstLevel1Service.analyzeLevel1ForDiscipline(disciplineId,
                options(domainMode, familyMode, skillMode, hoursBase, budgetMode, budgetHours, disciplineId));
    }

    /**
     * Level 2 analysis for a curriculum + domain + techFamily: weighted DST per canonical skill.
     */
    @GetMapping("/curriculum/{curriculumId}/level2")
    public DstL2Response level2ByCurriculumFamily(@PathVariable Long curriculumId,
                                                   @RequestParam String domain,
                                                   @RequestParam String techFamily,
                                                   @RequestParam(required = false) String domainMode,
                                                   @RequestParam(required = false) String familyMode,
                                                   @RequestParam(required = false) String skillMode,
                                                   @RequestParam(required = false) String hoursBase,
                                                   @RequestParam(required = false) String budgetMode,
                                                   @RequestParam(required = false) Integer budgetHours,
                                                   @RequestParam(required = false) Long disciplineId) {
        return dstLevel2Service.analyzeLevel2(curriculumId, domain, techFamily,
                options(domainMode, familyMode, skillMode, hoursBase, budgetMode, budgetHours, disciplineId));
    }

    /**
     * Level 2 analysis for a single discipline + domain + techFamily: weighted DST per canonical skill.
     */
    @GetMapping("/discipline/{disciplineId}/level2")
    public DstL2DisciplineResponse level2ByDisciplineFamily(@PathVariable Long disciplineId,
                                                             @RequestParam String domain,
                                                             @RequestParam String techFamily,
                                                             @RequestParam(required = false) String domainMode,
                                                             @RequestParam(required = false) String familyMode,
                                                             @RequestParam(required = false) String skillMode,
                                                             @RequestParam(required = false) String hoursBase,
                                                             @RequestParam(required = false) String budgetMode,
                                                             @RequestParam(required = false) Integer budgetHours) {
        return dstLevel2Service.analyzeLevel2ForDisciplineAndFamily(disciplineId, domain, techFamily,
                options(domainMode, familyMode, skillMode, hoursBase, budgetMode, budgetHours, disciplineId));
    }
}
