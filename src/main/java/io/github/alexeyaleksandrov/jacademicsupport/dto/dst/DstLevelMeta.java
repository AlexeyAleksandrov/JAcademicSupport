package io.github.alexeyaleksandrov.jacademicsupport.dto.dst;

import lombok.Data;

/**
 * Normalisation metadata attached to every L0–L2 response so that the UI can
 * render the BetP normalisation explicitly instead of guessing the base.
 *
 * budgetHours is the T in {@code targetHours_i = nBetP_i × T}; budgetSource says
 * where T came from. The under/over-allocation fields make incomplete explicit
 * coverage and inherited budgets auditable without reconstructing them client-side.
 */
@Data
@DstJsonFields
public class DstLevelMeta {
    /** T — the hours the level's target hours are normalised against. */
    private int    budgetHours;
    /** Human-readable origin of T, shown next to the formula. */
    private String budgetSource;
    /** Σ of the level objects' current hours. */
    private int    coveredHours;
    /** budgetHours − coveredHours; > 0 when coverage does not fill the budget. */
    private int    unallocatedHours;
    /** coveredHours − budgetHours; > 0 when current coverage exceeds the selected budget. */
    private int    overallocatedHours;

    private String  treeMode;
    private String  domainMode;
    private String  familyMode;
    private String  skillMode;
    private String  hoursBase;
    private String  budgetMode;

    /** BetP denominator N actually used on this level. */
    private int nClusters;

    /** Allocation threshold (Δ_norm) the client regulation must use. */
    private double tauAlloc;
    /** Whether negative evidence (m(F)) participated in this computation. */
    private boolean negativeEvidenceEnabled;
}
