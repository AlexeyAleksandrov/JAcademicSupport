package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

import io.github.alexeyaleksandrov.jacademicsupport.models.DstSettings;

/**
 * Per-request switches for the L0–L2 aggregation.
 *
 * All fields default to the values stored in {@code dst_settings}, so calling the
 * endpoints without query parameters keeps the historical behaviour.
 */
public record DstCalcOptions(
        CoverageMode domainMode,
        CoverageMode familyMode,
        CoverageMode skillMode,
        HoursBase    hoursBase,
        BudgetMode   budgetMode,
        Integer      budgetHours,
        Long         disciplineId
) {

    /** How coverage rows are mapped onto a level's objects. */
    public enum CoverageMode {
        /** Current behaviour: the domain/family is derived from families and skills when not stated. */
        DERIVED,
        /** Only rows where the level's attribute is filled in explicitly are counted. */
        EXPLICIT
    }

    /** What total the shares and target hours are normalised against. */
    public enum HoursBase {
        /** Σ totalHours of every discipline in the curriculum (current behaviour). */
        CURRICULUM,
        /** Σ totalHours of the disciplines that actually cover the level's scope. */
        TOUCHED_DISCIPLINES,
        /** totalHours of the discipline selected by {@code disciplineId}. */
        SINGLE_DISCIPLINE
    }

    /** Whether a child level inherits its budget from the parent level. */
    public enum BudgetMode {
        /** Each level re-normalises the full hours of the related disciplines (current behaviour). */
        INDEPENDENT,
        /** Child budget = parent's current (supply) hours. */
        INHERIT_CURRENT,
        /** Child budget = parent's target (nBetP × T) hours. */
        INHERIT_TARGET
    }

    public static DstCalcOptions defaults(DstSettings s) {
        return new DstCalcOptions(
                parseCoverage(s.getDomainMode()),
                parseCoverage(s.getFamilyMode()),
                parseCoverage(s.getSkillMode()),
                parseHoursBase(s.getHoursBase()),
                parseBudgetMode(s.getBudgetMode()),
                null,
                null);
    }

    /** Builds options from raw request parameters, falling back to stored defaults. */
    public static DstCalcOptions of(DstSettings s,
                                    String domainMode, String familyMode, String skillMode,
                                    String hoursBase, String budgetMode, Integer budgetHours,
                                    Long disciplineId) {
        DstCalcOptions d = defaults(s);
        return new DstCalcOptions(
                domainMode != null ? parseCoverage(domainMode)   : d.domainMode(),
                familyMode != null ? parseCoverage(familyMode)   : d.familyMode(),
                skillMode  != null ? parseCoverage(skillMode)    : d.skillMode(),
                hoursBase  != null ? parseHoursBase(hoursBase)   : d.hoursBase(),
                budgetMode != null ? parseBudgetMode(budgetMode) : d.budgetMode(),
                budgetHours,
                disciplineId);
    }

    public boolean explicitDomains()  { return domainMode == CoverageMode.EXPLICIT; }
    public boolean explicitFamilies() { return familyMode == CoverageMode.EXPLICIT; }
    public boolean explicitSkills()   { return skillMode  == CoverageMode.EXPLICIT; }

    /** Parent budget to use, or null when the level computes its own. */
    public Integer inheritedBudget() {
        return budgetMode == BudgetMode.INDEPENDENT ? null : budgetHours;
    }

    private static CoverageMode parseCoverage(String v) {
        return v == null ? CoverageMode.DERIVED : CoverageMode.valueOf(v.trim().toUpperCase());
    }

    private static HoursBase parseHoursBase(String v) {
        return v == null ? HoursBase.CURRICULUM : HoursBase.valueOf(v.trim().toUpperCase());
    }

    private static BudgetMode parseBudgetMode(String v) {
        return v == null ? BudgetMode.INDEPENDENT : BudgetMode.valueOf(v.trim().toUpperCase());
    }
}
