package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.models.DstSettings;

public record DstCalcOptions(
        TreeMode treeMode,
        HoursBase hoursBase,
        BudgetMode budgetMode,
        Integer budgetHours,
        Long disciplineId
) {
    public enum TreeMode { FULL_TREE, EXPLICIT_ONLY }
    public enum CoverageMode { DERIVED, EXPLICIT }
    public enum HoursBase { CURRICULUM, TOUCHED_DISCIPLINES, SINGLE_DISCIPLINE }
    public enum BudgetMode { INDEPENDENT, INHERIT_CURRENT, INHERIT_TARGET }

    public DstCalcOptions(CoverageMode domainMode, CoverageMode familyMode, CoverageMode skillMode,
                          HoursBase hoursBase, BudgetMode budgetMode, Integer budgetHours,
                          Long disciplineId) {
        this(domainMode == CoverageMode.EXPLICIT && familyMode == CoverageMode.EXPLICIT
                        && skillMode == CoverageMode.EXPLICIT ? TreeMode.EXPLICIT_ONLY : TreeMode.FULL_TREE,
                hoursBase, budgetMode, budgetHours, disciplineId);
    }

    public static DstCalcOptions defaults(DstSettings settings) {
        return new DstCalcOptions(parseTreeMode(settings.getTreeMode()), parseHoursBase(settings.getHoursBase()),
                parseBudgetMode(settings.getBudgetMode()), null, null);
    }

    public static DstCalcOptions of(DstSettings settings, String treeMode, String hoursBase,
                                    String budgetMode, Integer budgetHours, Long disciplineId) {
        DstCalcOptions defaults = defaults(settings);
        return new DstCalcOptions(treeMode != null ? parseTreeMode(treeMode) : defaults.treeMode(),
                hoursBase != null ? parseHoursBase(hoursBase) : defaults.hoursBase(),
                budgetMode != null ? parseBudgetMode(budgetMode) : defaults.budgetMode(),
                budgetHours, disciplineId);
    }

    public static DstCalcOptions of(DstSettings settings,
                                    String domainMode, String familyMode, String skillMode,
                                    String hoursBase, String budgetMode, Integer budgetHours,
                                    Long disciplineId) {
        String legacyMode = domainMode != null ? domainMode : familyMode != null ? familyMode : skillMode;
        String treeMode = legacyMode == null ? null
                : "EXPLICIT".equalsIgnoreCase(legacyMode) ? "EXPLICIT_ONLY" : "FULL_TREE";
        return of(settings, treeMode, hoursBase, budgetMode, budgetHours, disciplineId);
    }

    public boolean fullTree() { return treeMode == TreeMode.FULL_TREE; }
    public boolean explicitOnly() { return treeMode == TreeMode.EXPLICIT_ONLY; }
    public boolean explicitDomains() { return explicitOnly(); }
    public boolean explicitFamilies() { return explicitOnly(); }
    public boolean explicitSkills() { return explicitOnly(); }
    public CoverageMode domainMode() { return explicitOnly() ? CoverageMode.EXPLICIT : CoverageMode.DERIVED; }
    public CoverageMode familyMode() { return domainMode(); }
    public CoverageMode skillMode() { return domainMode(); }

    public Integer inheritedBudget() {
        return budgetMode == BudgetMode.INDEPENDENT ? null : budgetHours;
    }

    private static TreeMode parseTreeMode(String value) {
        if (value == null || value.isBlank()) return TreeMode.FULL_TREE;
        String normalized = value.trim().toUpperCase();
        if ("DERIVED".equals(normalized)) return TreeMode.FULL_TREE;
        if ("EXPLICIT".equals(normalized)) return TreeMode.EXPLICIT_ONLY;
        return TreeMode.valueOf(normalized);
    }

    private static HoursBase parseHoursBase(String value) {
        return value == null ? HoursBase.CURRICULUM : HoursBase.valueOf(value.trim().toUpperCase());
    }

    private static BudgetMode parseBudgetMode(String value) {
        return value == null ? BudgetMode.INDEPENDENT : BudgetMode.valueOf(value.trim().toUpperCase());
    }
}
