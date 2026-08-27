package io.github.alexeyaleksandrov.jacademicsupport.services.dst;

/**
 * Default values of every DST calculation constant.
 *
 * Operational defaults for the current calculation. Source denominators are
 * derived from the database and negative evidence is enabled. To reproduce the
 * historical article calculation, set {@code totalExperts=12},
 * {@code totalSources=4} and {@code negativeEvidenceEnabled=false}; the other
 * defaults retain the historical constants.
 */
public final class DstSettingsDefaults {

    private DstSettingsDefaults() {}

    // ── Source reliability (Shafer discounting weight) ────────────────────────
    public static final double W_VAC = 0.8;
    public static final double W_EXP = 0.9;
    public static final double W_FC  = 0.6;
    public static final double W_STD = 0.7;

    public static final boolean VAC_ENABLED = true;
    public static final boolean EXP_ENABLED = true;
    public static final boolean FC_ENABLED  = true;
    public static final boolean STD_ENABLED = false;

    // ── λ on L0 (domain level) ───────────────────────────────────────────────
    public static final double LAMBDA_VAC_DOMAIN = 2.0;
    public static final double LAMBDA_EXP_DOMAIN = 1.0;
    public static final double LAMBDA_FC_DOMAIN  = 0.5;

    // ── λ on L1 (family level) ────────────────────────────────────────────────
    public static final double LAMBDA_VAC_L1 = 15.0;
    public static final double LAMBDA_EXP_L1 = 5.0;
    public static final double LAMBDA_FC_L1  = 2.0;
    public static final double LAMBDA_STD_L1 = 3.0;

    // ── λ on L2 (canonical skill level) ──────────────────────────────────────
    public static final double LAMBDA_VAC_L2 = 15.0;
    public static final double LAMBDA_EXP_L2 = 5.0;
    public static final double LAMBDA_FC_L2  = 2.0;
    public static final double LAMBDA_STD_L2 = 3.0;

    /** 0 = derive the denominator from the database (COUNT DISTINCT). */
    public static final int TOTAL_EXPERTS = 0;
    public static final int TOTAL_SOURCES = 0;

    // ── BetP denominator on L0 ───────────────────────────────────────────────
    public static final int     N_CLUSTERS_L0      = 25;
    public static final boolean N_CLUSTERS_L0_AUTO = false;

    // ── Server-side DST decision regulation ──────────────────────────────────
    public static final double TAU_DELTA          = 0.03;
    public static final double TAU_K              = 0.40;
    public static final double TAU_THETA          = 0.50;
    public static final double STRONG_SIGNAL_DELTA = 0.35;
    public static final double STRONG_BOOST_DELTA  = 0.50;
    public static final double OBSOLETE_MF        = 0.80;
    public static final double OBSOLETE_MT        = 0.10;

    // ── Client-side allocation regulation ────────────────────────────────────
    public static final double TAU_ALLOC = 0.03;

    // ── Negative evidence (m(F)) ─────────────────────────────────────────────
    public static final boolean NEGATIVE_EVIDENCE_ENABLED = true;

    // ── Legacy vacancy-cluster-score contour (article section 3.2) ───────────
    public static final double W_LOC_TITLE          = 1.0;
    public static final double W_LOC_SKILLS         = 0.8;
    public static final double W_LOC_DESC           = 0.5;
    public static final double DEP_EDGE_THRESHOLD   = 0.30;
    public static final int    DEP_MIN_CO_OCCURRENCE = 5;
    public static final double RHO_DEP              = 0.30;
    public static final double CLUSTER_MIN_SCORE    = 0.01;

    // ── Default L0-L2 calculation modes ──────────────────────────────────────
    public static final String TREE_MODE   = "FULL_TREE";
    public static final String DOMAIN_MODE = "DERIVED";
    public static final String FAMILY_MODE = "DERIVED";
    public static final String SKILL_MODE  = "DERIVED";
    public static final String HOURS_BASE  = "CURRICULUM";
    public static final String BUDGET_MODE = "INDEPENDENT";
}
