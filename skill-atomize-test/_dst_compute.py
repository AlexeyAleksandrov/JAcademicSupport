"""
DST computation script using real data from the DB.
Computes BetP, K, Delta for all clusters of a given profession.

Sources:
  VAC  — real data from profession_cluster table (weight = relative vacancy frequency)
  EXP  — estimated as 0.85*VAC weight (no expert survey yet; assumed slightly lower confidence)
  FC   — assumed 0 growth signal (no forecast data in DB)
  supply — manually set below; must be filled from actual RPD
"""

import psycopg2, math

DB_URL = "postgresql://postgres:1111@localhost:5432/AcademicSupport"

# ── Manual supply values (fill from RPD) ───────────────────────────────────────
# For each cluster name set fraction of RPD hours dedicated to it (0.0–1.0).
# Total should roughly = 1.0 across all clusters.
# If you don't have the real values yet, set all to 0.04 (= 1/25 uniform baseline).
SUPPLY_OVERRIDES = {
    # "Реляционные БД":  0.07,
    # "Фреймворки":      0.10,
    # etc.
}
DEFAULT_SUPPLY = 0.04   # uniform fallback (1/N_CLUSTERS)

# ── DST hyper-parameters (from ALGORITHM_CURRENT.md) ──────────────────────────
LAMBDA_VAC = 15.0
LAMBDA_EXP = 5.0
W_VAC      = 0.80
W_EXP      = 0.90
N_CLUSTERS = 25
TAU_DELTA  = 0.15
TAU_K      = 0.40
TAU_THETA  = 0.15

PROF_CODE  = "backend"   # change as needed


def connect():
    import re
    m = re.match(r"postgresql://(\w+):(\w+)@([\w.]+):(\d+)/(\w+)", DB_URL)
    return psycopg2.connect(
        user=m[1], password=m[2], host=m[3], port=int(m[4]), dbname=m[5]
    )


def kappa(n_eff, lam):
    return n_eff / (n_eff + lam)


def bpa(kap, avg_score, rel_frac):
    mT = kap * avg_score * rel_frac
    mF = 0.0
    mU = 1.0 - mT
    return mT, mU, mF


def discount(mT, mU, mF, w):
    mTd = w * mT
    mFd = w * mF
    mUd = 1.0 - mTd - mFd
    return mTd, mUd, mFd


def dempster(m1T, m1U, m1F, m2T, m2U, m2F):
    K = m1T * m2F + m1F * m2T
    if K >= 1.0:
        return None, None, None, 1.0
    norm = 1.0 / (1.0 - K)
    mT = (m1T*m2T + m1T*m2U + m1U*m2T) * norm
    mU = (m1U*m2U) * norm
    mF = (m1F*m2F + m1F*m2U + m1U*m2F) * norm
    return mT, mU, mF, K


def betp(mT, mU, n=N_CLUSTERS):
    return mT + mU / n


def recommend(delta, K, mU):
    if delta > TAU_DELTA and K <= TAU_K and mU <= TAU_THETA:
        return "УСИЛИТЬ"
    elif delta > TAU_DELTA and (K > TAU_K or mU > TAU_THETA):
        return "ВАРИАТИВНОСТЬ"
    elif delta < -TAU_DELTA:
        return "СОКРАТИТЬ"
    else:
        return "СТАБИЛИЗАЦИЯ"


def main():
    conn = connect()
    cur = conn.cursor()

    # ── Get profession total vacancy count ─────────────────────────────────────
    cur.execute("""
        SELECT COUNT(DISTINCT vp.vacancy_id)
        FROM vacancy_profession vp
        JOIN profession p ON p.id = vp.profession_id AND p.code = %s
    """, (PROF_CODE,))
    total_vac = cur.fetchone()[0]
    print(f"\nПрофессия: {PROF_CODE} | Всего вакансий: {total_vac}\n")

    # ── Get cluster weights from profession_cluster ────────────────────────────
    cur.execute("""
        SELECT sg.description, pc.weight,
               ROUND((pc.weight * %s)::numeric, 0) as vac_count
        FROM profession_cluster pc
        JOIN skills_group sg ON sg.id = pc.cluster_id
        JOIN profession p ON p.id = pc.profession_id AND p.code = %s
        ORDER BY pc.weight DESC
    """, (total_vac, PROF_CODE))
    rows = cur.fetchall()
    conn.close()

    print(f"{'Кластер':<35} {'VAC_w':>6} {'BetP':>6} {'K':>5} {'mU*':>5} {'Δ':>6} {'Рекомендация'}")
    print("─" * 95)

    results = []
    for name, weight, vac_count in rows:
        w = float(weight)

        # ── VAC source ─────────────────────────────────────────────────────────
        n_eff_vac = float(vac_count) if vac_count else 0
        kap_vac   = kappa(n_eff_vac, LAMBDA_VAC)
        avg_score_vac = 0.80  # assumed (no TF-IDF yet)
        rel_frac_vac  = w     # profession_cluster.weight IS the relative frequency
        mT_vac, mU_vac, mF_vac = bpa(kap_vac, avg_score_vac, rel_frac_vac)
        mT_vac, mU_vac, mF_vac = discount(mT_vac, mU_vac, mF_vac, W_VAC)

        # ── EXP source (estimated: same signal, slightly lower volume) ─────────
        n_eff_exp = max(1, round(n_eff_vac * 0.01))  # ~1% of vac count as proxy
        kap_exp   = kappa(n_eff_exp, LAMBDA_EXP)
        avg_score_exp = min(0.90, w * 1.1)  # experts slightly agree with market
        rel_frac_exp  = w
        mT_exp, mU_exp, mF_exp = bpa(kap_exp, avg_score_exp, rel_frac_exp)
        mT_exp, mU_exp, mF_exp = discount(mT_exp, mU_exp, mF_exp, W_EXP)

        # ── Combine VAC ⊕ EXP ─────────────────────────────────────────────────
        mT_c, mU_c, mF_c, K = dempster(mT_vac, mU_vac, mF_vac,
                                        mT_exp, mU_exp, mF_exp)
        if mT_c is None:
            continue

        bp = betp(mT_c, mU_c)
        supply = SUPPLY_OVERRIDES.get(name, DEFAULT_SUPPLY)
        delta = bp - supply
        rec = recommend(delta, K, mU_c)

        results.append((name, w, bp, K, mU_c, delta, rec))
        print(f"{name:<35} {w:>6.4f} {bp:>6.4f} {K:>5.3f} {mU_c:>5.3f} {delta:>+6.3f} {rec}")

    print(f"\n— supply = {DEFAULT_SUPPLY} (равномерный базис 1/N, замените на реальные данные РПД)")
    print("— EXP-источник: оценочный (на основе весов VAC, без реального опроса)")
    print("— FC-источник: не учтён (нет данных прогнозов в БД)")

    # ── Summary stats ──────────────────────────────────────────────────────────
    deficits  = [r for r in results if r[5] > TAU_DELTA]
    surpluses = [r for r in results if r[5] < -TAU_DELTA]
    print(f"\nДефицит (Δ > {TAU_DELTA}): {len(deficits)} кластеров")
    for r in sorted(deficits, key=lambda x: -x[5])[:5]:
        print(f"  {r[0]:<35} Δ={r[5]:+.3f}  K={r[2]:.3f}  → {r[6]}")
    print(f"Избыток (Δ < -{TAU_DELTA}): {len(surpluses)} кластеров")
    for r in sorted(surpluses, key=lambda x: x[5])[:5]:
        print(f"  {r[0]:<35} Δ={r[5]:+.3f}  → {r[6]}")


if __name__ == "__main__":
    main()
