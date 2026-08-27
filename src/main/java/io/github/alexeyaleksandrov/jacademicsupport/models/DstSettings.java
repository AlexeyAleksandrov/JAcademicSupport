package io.github.alexeyaleksandrov.jacademicsupport.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.github.alexeyaleksandrov.jacademicsupport.dto.dst.DstJsonFields;
import io.github.alexeyaleksandrov.jacademicsupport.services.dst.DstSettingsDefaults;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Single-row table holding every editable DST calculation constant.
 * The row always has {@code id = 1}; defaults come from {@link DstSettingsDefaults}.
 */
@Entity
@Table(name = "dst_settings")
@Data
@DstJsonFields
public class DstSettings {

    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    // ── Source reliability ───────────────────────────────────────────────────
    @Column(name = "w_vac") private Double wVac = DstSettingsDefaults.W_VAC;
    @Column(name = "w_exp") private Double wExp = DstSettingsDefaults.W_EXP;
    @Column(name = "w_fc")  private Double wFc  = DstSettingsDefaults.W_FC;
    @Column(name = "w_std") private Double wStd = DstSettingsDefaults.W_STD;

    @Column(name = "vac_enabled") private Boolean vacEnabled = DstSettingsDefaults.VAC_ENABLED;
    @Column(name = "exp_enabled") private Boolean expEnabled = DstSettingsDefaults.EXP_ENABLED;
    @Column(name = "fc_enabled")  private Boolean fcEnabled  = DstSettingsDefaults.FC_ENABLED;
    @Column(name = "std_enabled") private Boolean stdEnabled = DstSettingsDefaults.STD_ENABLED;

    // ── λ on L0 ──────────────────────────────────────────────────────────────
    @Column(name = "lambda_vac_domain") private Double lambdaVacDomain = DstSettingsDefaults.LAMBDA_VAC_DOMAIN;
    @Column(name = "lambda_exp_domain") private Double lambdaExpDomain = DstSettingsDefaults.LAMBDA_EXP_DOMAIN;
    @Column(name = "lambda_fc_domain")  private Double lambdaFcDomain  = DstSettingsDefaults.LAMBDA_FC_DOMAIN;

    // ── λ on L1 (family level). Existing column names preserve DB values. ────
    @JsonAlias("lambdaVac")
    @Column(name = "lambda_vac") private Double lambdaVacL1 = DstSettingsDefaults.LAMBDA_VAC_L1;
    @JsonAlias("lambdaExp")
    @Column(name = "lambda_exp") private Double lambdaExpL1 = DstSettingsDefaults.LAMBDA_EXP_L1;
    @JsonAlias("lambdaFc")
    @Column(name = "lambda_fc")  private Double lambdaFcL1  = DstSettingsDefaults.LAMBDA_FC_L1;
    @JsonAlias("lambdaStd")
    @Column(name = "lambda_std") private Double lambdaStdL1 = DstSettingsDefaults.LAMBDA_STD_L1;

    // ── λ on L2 (canonical skill level) ──────────────────────────────────────
    @Column(name = "lambda_vac_l2") private Double lambdaVacL2 = DstSettingsDefaults.LAMBDA_VAC_L2;
    @Column(name = "lambda_exp_l2") private Double lambdaExpL2 = DstSettingsDefaults.LAMBDA_EXP_L2;
    @Column(name = "lambda_fc_l2")  private Double lambdaFcL2  = DstSettingsDefaults.LAMBDA_FC_L2;
    @Column(name = "lambda_std_l2") private Double lambdaStdL2 = DstSettingsDefaults.LAMBDA_STD_L2;

    /** 0 = count distinct experts from the database. */
    @Column(name = "total_experts") private Integer totalExperts = DstSettingsDefaults.TOTAL_EXPERTS;
    /** 0 = count distinct foresight source_url from the database. */
    @Column(name = "total_sources") private Integer totalSources = DstSettingsDefaults.TOTAL_SOURCES;

    // ── BetP denominator on L0 ───────────────────────────────────────────────
    @Column(name = "n_clusters_l0")      private Integer nClustersL0     = DstSettingsDefaults.N_CLUSTERS_L0;
    @Column(name = "n_clusters_l0_auto") private Boolean nClustersL0Auto = DstSettingsDefaults.N_CLUSTERS_L0_AUTO;

    // ── Server DST decision regulation ───────────────────────────────────────
    @Column(name = "tau_delta")           private Double tauDelta          = DstSettingsDefaults.TAU_DELTA;
    @Column(name = "tau_k")               private Double tauK              = DstSettingsDefaults.TAU_K;
    @Column(name = "tau_theta")           private Double tauTheta          = DstSettingsDefaults.TAU_THETA;
    @Column(name = "strong_signal_delta") private Double strongSignalDelta = DstSettingsDefaults.STRONG_SIGNAL_DELTA;
    @Column(name = "strong_boost_delta")  private Double strongBoostDelta  = DstSettingsDefaults.STRONG_BOOST_DELTA;
    @Column(name = "obsolete_mf")         private Double obsoleteMf        = DstSettingsDefaults.OBSOLETE_MF;
    @Column(name = "obsolete_mt")         private Double obsoleteMt        = DstSettingsDefaults.OBSOLETE_MT;

    // ── Allocation regulation (client tables) ────────────────────────────────
    @Column(name = "tau_alloc") private Double tauAlloc = DstSettingsDefaults.TAU_ALLOC;

    // ── Negative evidence ────────────────────────────────────────────────────
    @Column(name = "negative_evidence_enabled")
    private Boolean negativeEvidenceEnabled = DstSettingsDefaults.NEGATIVE_EVIDENCE_ENABLED;

    // ── Legacy cluster-score contour (article section 3.2) ───────────────────
    @Column(name = "w_loc_title")           private Double  wLocTitle          = DstSettingsDefaults.W_LOC_TITLE;
    @Column(name = "w_loc_skills")          private Double  wLocSkills         = DstSettingsDefaults.W_LOC_SKILLS;
    @Column(name = "w_loc_desc")            private Double  wLocDesc           = DstSettingsDefaults.W_LOC_DESC;
    @Column(name = "dep_edge_threshold")    private Double  depEdgeThreshold   = DstSettingsDefaults.DEP_EDGE_THRESHOLD;
    @Column(name = "dep_min_co_occurrence") private Integer depMinCoOccurrence = DstSettingsDefaults.DEP_MIN_CO_OCCURRENCE;
    @Column(name = "rho_dep")               private Double  rhoDep             = DstSettingsDefaults.RHO_DEP;
    @Column(name = "cluster_min_score")     private Double  clusterMinScore    = DstSettingsDefaults.CLUSTER_MIN_SCORE;

    // ── Default L0-L2 calculation modes ──────────────────────────────────────
    @Column(name = "tree_mode", length = 20) private String treeMode = DstSettingsDefaults.TREE_MODE;
    @Column(name = "domain_mode", length = 20) private String domainMode = DstSettingsDefaults.DOMAIN_MODE;
    @Column(name = "family_mode", length = 20) private String familyMode = DstSettingsDefaults.FAMILY_MODE;
    @Column(name = "skill_mode",  length = 20) private String skillMode  = DstSettingsDefaults.SKILL_MODE;
    @Column(name = "hours_base",  length = 30) private String hoursBase  = DstSettingsDefaults.HOURS_BASE;
    @Column(name = "budget_mode", length = 30) private String budgetMode = DstSettingsDefaults.BUDGET_MODE;
}
