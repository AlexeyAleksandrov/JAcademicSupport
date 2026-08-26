import math
import os
import sys
import psycopg2
from collections import defaultdict

DB_URL = os.getenv('DB_URL', 'postgresql://postgres:1111@localhost:5432/AcademicSupport')

W_VAC = 0.8
W_EXP = 0.9
W_FC  = 0.6

LAMBDA_VAC_L2 = 15.0
LAMBDA_EXP_L2 = 5.0
LAMBDA_FC_L2  = 2.0

TAU_K = 0.40


def compute_bpa(relevant, avg, negative, avg_neg, total, lam):
    if total == 0 or (relevant == 0 and negative == 0):
        return 0.0, 0.0, 0.0
    mT = (1.0 - math.exp(-lam * relevant / total)) * avg if relevant > 0 else 0.0
    mF = (1.0 - math.exp(-lam * negative / total)) * avg_neg if negative > 0 else 0.0
    s = mT + mF
    if s > 1.0:
        mT /= s
        mF /= s
    mU = max(0.0, 1.0 - mT - mF)
    return mT, mU, mF


def discount(mT, mU, mF, w):
    mTD = mT * w
    mFD = mF * w
    mUD = mU * w + (1.0 - w)
    s = mTD + mUD + mFD
    if s > 0:
        mTD /= s
        mUD /= s
        mFD /= s
    return mTD, mUD, mFD


def combine_adaptive(m1T, m1U, m1F, m2T, m2U, m2F):
    K = m1T * m2F + m1F * m2T
    if K >= TAU_K:
        mT = m1T * m2T + m1T * m2U + m2T * m1U
        mU = m1U * m2U + K
        mF = m1F * m2F + m1F * m2U + m2F * m1U
    else:
        norm = max(1.0 - K, 0.0001)
        mT = (m1T * m2T + m1T * m2U + m2T * m1U) / norm
        mU = (m1U * m2U) / norm
        mF = (m1F * m2F + m1F * m2U + m2F * m1U) / norm
    s = mT + mU + mF
    if s > 0:
        mT /= s
        mU /= s
        mF /= s
    return mT, mU, mF, K


def combine_three(exp, fc, vac):
    enabled = []
    for name, mT, mU, mF in [('EXP', exp[0], exp[1], exp[2]),
                              ('FC',  fc[0],  fc[1],  fc[2]),
                              ('VAC', vac[0], vac[1], vac[2])]:
        if mT > 0 or mF > 0:
            enabled.append((name, mT, mU, mF))
    if not enabled:
        return 0.0, 'empty'
    curT, curU, curF = enabled[0][1:]
    max_k = 0.0
    used_yager = False
    for i in range(1, len(enabled)):
        nT, nU, nF = enabled[i][1:]
        curT, curU, curF, K = combine_adaptive(curT, curU, curF, nT, nU, nF)
        if K > max_k:
            max_k = K
        if K >= TAU_K:
            used_yager = True
    return max_k, used_yager


def main():
    conn = psycopg2.connect(DB_URL)
    cur = conn.cursor()

    prof_code = 'backend'
    domain = 'LANGUAGES'

    cur.execute("""SELECT COUNT(DISTINCT eo.expert_id) FROM expert_opinion eo JOIN expert e ON e.id=eo.expert_id""")
    total_experts = cur.fetchone()[0] or 1

    cur.execute("""SELECT COUNT(DISTINCT source_url) FROM foresight""")
    total_sources = cur.fetchone()[0] or 1

    # All backend canonicals with a tech_family (L2 context)
    cur.execute("""
        SELECT sc.id, sc.name, sc.tech_family
        FROM skill_canonical sc
        WHERE sc.domain = %s AND sc.tech_family IS NOT NULL AND sc.tech_family <> ''
        ORDER BY sc.id
    """, (domain,))
    skills = cur.fetchall()

    results = []
    for cid, name, family in skills:
        # VAC L2: relevant = skill count, total = family count
        cur.execute("""
            SELECT COUNT(DISTINCT vp.vacancy_id)
            FROM work_skill_canonical wsc
            JOIN work_skill ws ON ws.id = wsc.work_skill_id
            JOIN vacancy_skills vs ON vs.skills_id = ws.id
            JOIN vacancy_profession vp ON vp.vacancy_id = vs.vacancy_entity_id
            JOIN profession p ON p.id = vp.profession_id AND p.code = %s
            WHERE wsc.canonical_id = %s
        """, (prof_code, cid))
        rel = cur.fetchone()[0]

        cur.execute("""
            SELECT COUNT(DISTINCT vp.vacancy_id)
            FROM skill_canonical sc
            JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
            JOIN work_skill ws ON ws.id = wsc.work_skill_id
            JOIN vacancy_skills vs ON vs.skills_id = ws.id
            JOIN vacancy_profession vp ON vp.vacancy_id = vs.vacancy_entity_id
            JOIN profession p ON p.id = vp.profession_id AND p.code = %s
            WHERE sc.domain = %s AND sc.tech_family = %s
        """, (prof_code, domain, family))
        total_vac = cur.fetchone()[0]

        # EXP L2
        cur.execute("""
            SELECT COUNT(DISTINCT eo.expert_id), AVG(eo.skill_importance)
            FROM expert_opinion eo
            WHERE eo.canonical_id = %s AND eo.direction = 'POSITIVE'
            AND (%s IS NULL OR eo.profession_code = %s OR COALESCE(eo.profession_code,'') = '')
        """, (cid, prof_code, prof_code))
        exp_pos = cur.fetchone()
        exp_rel = int(exp_pos[0] or 0)
        exp_avg = float(exp_pos[1] or 0.0)

        cur.execute("""
            SELECT COUNT(DISTINCT eo.expert_id), AVG(eo.skill_importance)
            FROM expert_opinion eo
            WHERE eo.canonical_id = %s AND eo.direction = 'NEGATIVE'
            AND (%s IS NULL OR eo.profession_code = %s OR COALESCE(eo.profession_code,'') = '')
        """, (cid, prof_code, prof_code))
        exp_neg = cur.fetchone()
        exp_neg_cnt = int(exp_neg[0] or 0)
        exp_neg_avg = float(exp_neg[1] or 0.0)

        # FC L2
        cur.execute("""
            SELECT COUNT(DISTINCT f.source_url), AVG(f.confidence)
            FROM foresight f
            WHERE f.canonical_id = %s AND f.direction = 'POSITIVE'
            AND (%s IS NULL OR f.profession_code = %s OR COALESCE(f.profession_code,'') = '')
        """, (cid, prof_code, prof_code))
        fc_pos = cur.fetchone()
        fc_rel = int(fc_pos[0] or 0)
        fc_avg = float(fc_pos[1] or 0.0)

        cur.execute("""
            SELECT COUNT(DISTINCT f.source_url), AVG(f.confidence)
            FROM foresight f
            WHERE f.canonical_id = %s AND f.direction = 'NEGATIVE'
            AND (%s IS NULL OR f.profession_code = %s OR COALESCE(f.profession_code,'') = '')
        """, (cid, prof_code, prof_code))
        fc_neg = cur.fetchone()
        fc_neg_cnt = int(fc_neg[0] or 0)
        fc_neg_avg = float(fc_neg[1] or 0.0)

        # avoid VAC BPA if family total is zero or relevant is zero
        if total_vac == 0 or rel == 0:
            continue

        vac_mT, vac_mU, vac_mF = compute_bpa(rel, 1.0, 0, 0.0, total_vac, LAMBDA_VAC_L2)
        exp_mT, exp_mU, exp_mF = compute_bpa(exp_rel, exp_avg, exp_neg_cnt, exp_neg_avg, total_experts, LAMBDA_EXP_L2)
        fc_mT, fc_mU, fc_mF = compute_bpa(fc_rel, fc_avg, fc_neg_cnt, fc_neg_avg, total_sources, LAMBDA_FC_L2)

        exp_d = discount(exp_mT, exp_mU, exp_mF, W_EXP)
        fc_d = discount(fc_mT, fc_mU, fc_mF, W_FC)
        vac_d = discount(vac_mT, vac_mU, vac_mF, W_VAC)

        max_k, used_yager = combine_three(exp_d, fc_d, vac_d)

        results.append({
            'id': cid,
            'name': name,
            'family': family,
            'vac_rel': rel, 'vac_total': total_vac,
            'exp_rel': exp_rel, 'exp_avg': exp_avg,
            'exp_neg': exp_neg_cnt, 'exp_neg_avg': exp_neg_avg,
            'fc_rel': fc_rel, 'fc_avg': fc_avg,
            'fc_neg': fc_neg_cnt, 'fc_neg_avg': fc_neg_avg,
            'mF_fc': fc_d[2],
            'mT_vac': vac_d[0],
            'mT_exp': exp_d[0],
            'max_k': max_k,
            'yager': used_yager
        })

    results.sort(key=lambda x: x['max_k'], reverse=True)

    print(f"Profession: {prof_code}, domain: {domain}")
    print(f"Total experts: {total_experts}, total sources: {total_sources}")
    print("=" * 120)
    print(f"{'name':<30} {'family':<20} {'vac_rel/total':<15} {'fc_neg/avg':<18} {'mF_fc':<8} {'mT_vac':<8} {'mT_exp':<8} {'maxK':<8} {'Yager':<6}")
    for r in results[:30]:
        print(f"{r['name']:<30} {r['family']:<20} {r['vac_rel']}/{r['vac_total']:<8} "
              f"{r['fc_neg']}/{r['fc_neg_avg']:.2f}     "
              f"{r['mF_fc']:.4f}  {r['mT_vac']:.4f}  {r['mT_exp']:.4f}  "
              f"{r['max_k']:.4f}  {r['yager']}")

    # Simulation: how many extra negative FC sources (avg conf 0.9) to reach Yager for top candidates
    for target in [results[0]] + [r for r in results if r['name'] in ('PHP', 'Objective-C')]:
        print(f"\nSimulation for: {target['name']} (fc_neg={target['fc_neg']}, mT_vac={target['mT_vac']:.3f})")
        print("Adding NEW distinct negative foresight sources (avg_conf=0.9, new sources increase total).")
        for extra in [0, 5, 10, 20, 30, 50, 75, 100, 150, 200, 500]:
            new_total = total_sources + extra
            new_fc_neg = target['fc_neg'] + extra
            # weighted avg of confidence for negative
            old_neg_score = target['fc_neg'] * target['fc_neg_avg'] if target['fc_neg'] > 0 else 0.0
            add_neg_score = extra * 0.9
            new_avg_neg = (old_neg_score + add_neg_score) / new_fc_neg if new_fc_neg > 0 else 0.0
            _, _, fc_mF = compute_bpa(target['fc_rel'], target['fc_avg'], new_fc_neg, new_avg_neg, new_total, LAMBDA_FC_L2)
            _, _, fc_d_mF = discount(0.0, 1.0 - fc_mF, fc_mF, W_FC)
            # first EXP+FC: K1 = mT_exp * mF_fc
            k1 = target['mT_exp'] * fc_d_mF
            # combined mF_12 after Dempster with EXP mF=0
            mU_exp = 1 - target['mT_exp'] - target['mF_fc']
            # approximate mF_12 = mU_exp * fc_d_mF / (1 - k1)
            if k1 < 1.0:
                mF_12 = (mU_exp * fc_d_mF) / (1 - k1)
                k2 = mF_12 * target['mT_vac']
                max_k_est = max(k1, k2)
            else:
                max_k_est = 1.0
            print(f"  +{extra:>3} new neg sources -> total={new_total:>3}, neg={new_fc_neg:>3}, "
                  f"mF_fc_d={fc_d_mF:.4f}, maxK~={max_k_est:.4f}, Yager={max_k_est>=TAU_K}")

    cur.close()
    conn.close()


if __name__ == '__main__':
    main()
