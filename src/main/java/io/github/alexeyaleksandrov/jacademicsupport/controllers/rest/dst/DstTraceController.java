package io.github.alexeyaleksandrov.jacademicsupport.controllers.rest.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.level0.DstL0Response;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.trace.DstTraceResponse;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstCombinationService;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstLevel0Service;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.bpa.DstContext;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dst")
@AllArgsConstructor
public class DstTraceController {

    private final DstCombinationService dstCombinationService;
    private final DstLevel0Service      dstLevel0Service;

    /**
     * Runs a full DST trace for a given context (domain / techFamily / canonicalId / professionCode).
     * supply = fraction of RPD hours assigned to this context (0..1), defaults to 0 if not provided.
     */
    @GetMapping("/trace")
    public DstTraceResponse trace(
            @RequestParam(required = false) String  profCode,
            @RequestParam(required = false) String  domain,
            @RequestParam(required = false) String  techFamily,
            @RequestParam(required = false) Long    canonicalId,
            @RequestParam(required = false, defaultValue = "0") double supply
    ) {
        DstContext ctx = new DstContext(profCode, domain, techFamily, canonicalId);
        return dstCombinationService.compute(ctx, supply);
    }

    /**
     * Level 0 analysis for a curriculum: weighted DST per domain using profession mix.
     */
    @GetMapping("/curriculum/{curriculumId}/level0")
    public DstL0Response level0ByCurriculum(@PathVariable Long curriculumId) {
        return dstLevel0Service.analyzeLevel0(curriculumId);
    }
}
