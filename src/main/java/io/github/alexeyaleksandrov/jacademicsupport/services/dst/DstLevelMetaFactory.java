package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstLevelMeta;
import io.github.alexeyaleksandrov.jacademicsupport.models.DstSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Fills the normalisation metadata block shared by all L0–L2 responses. */
@Component
@RequiredArgsConstructor
public class DstLevelMetaFactory {

    private final DstSettingsService settingsService;

    public DstLevelMeta build(DstCalcOptions options, int budgetHours, int coveredHours, String budgetSource) {
        return build(options, budgetHours, coveredHours, budgetSource, 0);
    }

    public DstLevelMeta build(DstCalcOptions options, int budgetHours, int coveredHours,
                              String budgetSource, int nClusters) {
        DstSettings s = settingsService.get();
        DstLevelMeta meta = new DstLevelMeta();
        meta.setNClusters(nClusters);
        meta.setBudgetHours(budgetHours);
        meta.setBudgetSource(budgetSource);
        meta.setCoveredHours(coveredHours);
        meta.setUnallocatedHours(Math.max(0, budgetHours - coveredHours));
        meta.setOverallocatedHours(Math.max(0, coveredHours - budgetHours));
        meta.setTreeMode(options.treeMode().name());
        meta.setDomainMode(options.domainMode().name());
        meta.setFamilyMode(options.familyMode().name());
        meta.setSkillMode(options.skillMode().name());
        meta.setHoursBase(options.hoursBase().name());
        meta.setBudgetMode(options.budgetMode().name());
        meta.setTauAlloc(s.getTauAlloc() != null ? s.getTauAlloc() : DstSettingsDefaults.TAU_ALLOC);
        meta.setNegativeEvidenceEnabled(!Boolean.FALSE.equals(s.getNegativeEvidenceEnabled()));
        return meta;
    }
}
