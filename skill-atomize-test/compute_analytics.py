"""
compute_analytics.py — Заполняет аналитические таблицы на основе skill_canonical + work_skill_canonical.

Таблицы:
  skill_dependency   — co-occurrence: сколько раз два навыка встречаются в одной вакансии
  vacancy_domain     — основной домен каждой вакансии (по большинству навыков)
  skill_domain_stats — статистика навыка: в скольких вакансиях, pct_in_domain, top_cooccurrences

Использование:
    python compute_analytics.py              # все три таблицы
    python compute_analytics.py --only deps  # только skill_dependency
    python compute_analytics.py --only vd    # только vacancy_domain
    python compute_analytics.py --only stats # только skill_domain_stats
    python compute_analytics.py --min-cooc 3 # минимальный порог co-occurrence (по умолч. 2)
    python compute_analytics.py --truncate   # очистить таблицы перед заполнением
"""

import argparse
import json
import os
import sys
import time
import psycopg2
import psycopg2.extras
from dotenv import load_dotenv

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

load_dotenv()

SEP = "═" * 60


def step(name: str):
    print(f"\n{SEP}\n  {name}\n{SEP}")


# ─── 1. skill_dependency (co-occurrence) ────────────────────────────────────

DEPS_SQL = """
WITH vacancy_canonical AS (
    -- Для каждой вакансии — уникальный набор canonical_id
    SELECT DISTINCT
        vs.vacancy_entity_id  AS vacancy_id,
        wsc.canonical_id
    FROM vacancy_skills vs
    JOIN work_skill_canonical wsc ON wsc.work_skill_id = vs.skills_id
)
INSERT INTO skill_dependency (parent_id, child_id, co_occurrence_cnt)
SELECT
    LEAST(a.canonical_id,    b.canonical_id)    AS parent_id,
    GREATEST(a.canonical_id, b.canonical_id)    AS child_id,
    COUNT(DISTINCT a.vacancy_id)                AS co_occurrence_cnt
FROM vacancy_canonical a
JOIN vacancy_canonical b
    ON  a.vacancy_id   = b.vacancy_id
    AND a.canonical_id < b.canonical_id
GROUP BY
    LEAST(a.canonical_id,    b.canonical_id),
    GREATEST(a.canonical_id, b.canonical_id)
HAVING COUNT(DISTINCT a.vacancy_id) >= %(min_cooc)s
ON CONFLICT DO NOTHING
"""

# ─── 2. vacancy_domain ──────────────────────────────────────────────────────

VACANCY_DOMAIN_SQL = """
WITH domain_counts AS (
    -- Для каждой вакансии считаем сколько навыков каждого домена
    SELECT
        vs.vacancy_entity_id    AS vacancy_id,
        sc.domain,
        COUNT(DISTINCT wsc.canonical_id) AS skill_cnt
    FROM vacancy_skills vs
    JOIN work_skill_canonical wsc ON wsc.work_skill_id = vs.skills_id
    JOIN skill_canonical sc       ON sc.id = wsc.canonical_id
    WHERE sc.domain IS NOT NULL
    GROUP BY vs.vacancy_entity_id, sc.domain
),
totals AS (
    SELECT vacancy_id, SUM(skill_cnt) AS total_cnt
    FROM domain_counts
    GROUP BY vacancy_id
),
ranked AS (
    SELECT
        dc.vacancy_id,
        dc.domain,
        dc.skill_cnt::numeric / t.total_cnt AS domain_score,
        RANK() OVER (PARTITION BY dc.vacancy_id ORDER BY dc.skill_cnt DESC) AS rn
    FROM domain_counts dc
    JOIN totals t ON t.vacancy_id = dc.vacancy_id
)
INSERT INTO vacancy_domain (vacancy_id, primary_domain, domain_score, computed_at)
SELECT vacancy_id, domain, domain_score, NOW()
FROM ranked
WHERE rn = 1
ON CONFLICT DO NOTHING
"""

# ─── 3. skill_domain_stats ──────────────────────────────────────────────────

SKILL_STATS_SQL = """
WITH skill_vacancies AS (
    -- Для каждого canonical skill — список вакансий
    SELECT DISTINCT wsc.canonical_id, vs.vacancy_entity_id AS vacancy_id
    FROM work_skill_canonical wsc
    JOIN vacancy_skills vs ON vs.skills_id = wsc.work_skill_id
),
skill_counts AS (
    SELECT canonical_id, COUNT(DISTINCT vacancy_id) AS vacancy_count
    FROM skill_vacancies
    GROUP BY canonical_id
),
domain_vacancies AS (
    -- Для каждого домена — общее число вакансий с хотя бы одним навыком этого домена
    SELECT vd.primary_domain AS domain, COUNT(DISTINCT vd.vacancy_id) AS domain_vacancy_count
    FROM vacancy_domain vd
    GROUP BY vd.primary_domain
)
SELECT
    sc.id            AS canonical_id,
    sc.domain,
    sk.vacancy_count,
    COALESCE(dv.domain_vacancy_count, 0) AS domain_vacancy_count,
    CASE WHEN COALESCE(dv.domain_vacancy_count, 0) > 0
         THEN LEAST(1.0, ROUND(sk.vacancy_count::numeric / dv.domain_vacancy_count, 6))
         ELSE 0
    END AS pct_in_domain
FROM skill_canonical sc
JOIN skill_counts sk    ON sk.canonical_id = sc.id
LEFT JOIN domain_vacancies dv ON dv.domain = sc.domain
WHERE sc.domain IS NOT NULL
"""

TOP_COOC_SQL = """
SELECT
    sd.parent_id AS other_id,
    sc.name      AS other_name,
    sd.co_occurrence_cnt
FROM skill_dependency sd
JOIN skill_canonical sc ON sc.id = sd.parent_id
WHERE sd.child_id = %(cid)s
UNION ALL
SELECT
    sd.child_id  AS other_id,
    sc.name      AS other_name,
    sd.co_occurrence_cnt
FROM skill_dependency sd
JOIN skill_canonical sc ON sc.id = sd.child_id
WHERE sd.parent_id = %(cid)s
ORDER BY co_occurrence_cnt DESC
LIMIT 10
"""


def compute_deps(cur, min_cooc: int):
    step("1/3  skill_dependency  (co-occurrence)")
    t0 = time.time()
    cur.execute(DEPS_SQL, {"min_cooc": min_cooc})
    n = cur.rowcount
    elapsed = time.time() - t0
    print(f"  Вставлено пар: {n:,}  ({elapsed:.1f}с)")


def compute_vacancy_domain(cur):
    step("2/3  vacancy_domain  (основной домен вакансии)")
    t0 = time.time()
    cur.execute(VACANCY_DOMAIN_SQL)
    n = cur.rowcount
    elapsed = time.time() - t0
    print(f"  Вставлено вакансий: {n:,}  ({elapsed:.1f}с)")


def compute_skill_stats(cur):
    step("3/3  skill_domain_stats  (статистика навыков)")
    t0 = time.time()

    cur.execute(SKILL_STATS_SQL)
    rows = cur.fetchall()
    print(f"  Навыков для обработки: {len(rows):,}")

    inserted = 0
    for (canonical_id, domain, vacancy_count, domain_vacancy_count, pct_in_domain) in rows:
        # top co-occurrences
        cur.execute(TOP_COOC_SQL, {"cid": canonical_id})
        top = [{"id": r[0], "name": r[1], "count": r[2]} for r in cur.fetchall()]

        cur.execute("""
            INSERT INTO skill_domain_stats
                (canonical_id, domain, vacancy_count, domain_vacancy_count, pct_in_domain, top_cooccurrences, computed_at)
            VALUES (%s, %s, %s, %s, %s, %s, NOW())
            ON CONFLICT DO NOTHING
        """, (
            canonical_id, domain, vacancy_count, domain_vacancy_count,
            pct_in_domain, json.dumps(top, ensure_ascii=False)
        ))
        inserted += 1
        if inserted % 1000 == 0:
            print(f"    ... {inserted}/{len(rows)}")

    elapsed = time.time() - t0
    print(f"  Вставлено записей: {inserted:,}  ({elapsed:.1f}с)")


# ─── main ────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--only",      choices=["deps", "vd", "stats"], default=None)
    parser.add_argument("--min-cooc",  type=int, default=2,
                        help="Мин. кол-во вакансий для записи в skill_dependency (по умолч. 2)")
    parser.add_argument("--truncate",  action="store_true",
                        help="Очистить таблицы перед заполнением")
    args = parser.parse_args()

    conn = psycopg2.connect(os.getenv("DB_URL"))
    cur  = conn.cursor()

    if args.truncate:
        print("[TRUNCATE] Очищаем skill_dependency, vacancy_domain, skill_domain_stats...")
        cur.execute("TRUNCATE skill_dependency, vacancy_domain, skill_domain_stats")
        conn.commit()

    t_total = time.time()

    run_all = args.only is None
    if run_all or args.only == "deps":
        compute_deps(cur, args.min_cooc)
        conn.commit()

    if run_all or args.only == "vd":
        compute_vacancy_domain(cur)
        conn.commit()

    if run_all or args.only == "stats":
        compute_skill_stats(cur)
        conn.commit()

    cur.close()
    conn.close()

    print(f"\n{SEP}")
    print(f"  ГОТОВО  (общее время: {time.time() - t_total:.1f}с)")
    print(SEP)


if __name__ == "__main__":
    main()
