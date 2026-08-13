"""
normalize_misc.py — прочие слияния дублей.
python normalize_misc.py          # preview
python normalize_misc.py --save   # применить
"""
import argparse, psycopg2, os
from dotenv import load_dotenv; load_dotenv()

MERGE_GROUPS = [

    # ── pytest / unittest ────────────────────────────────────────────────────
    ("pytest", [
        "Автоматизированное тестирование с использованием Pytest или Unittest",
        "Pytest",           # capitalised variant если есть
    ]),

    # ── Allure ───────────────────────────────────────────────────────────────
    # Allure TestOps — коммерческий продукт, но слишком мало refs
    ("Allure", [
        "Allure Report",
        "Allure-TestOps",
        "Allure TestOps",
        "AllureTestOps",
        "AllureEE",
    ]),

    # ── NUnit ────────────────────────────────────────────────────────────────
    ("NUnit", [
        "Custom NUnit",
    ]),

    # ── SOLID ────────────────────────────────────────────────────────────────
    # SolidWorks, SolidJS, Solidity, Consolidation — НЕ трогаем
    ("SOLID", [
        "SOLID принципы",
    ]),

    # ── event loop ───────────────────────────────────────────────────────────
    ("event loop", [
        "Мониторинг event loop lag (perf_hooks)",
    ]),

    # ── Azure DevOps ─────────────────────────────────────────────────────────
    ("Azure DevOps", [
        "Azure DevOps Services",
    ]),

    # ── DevOps ───────────────────────────────────────────────────────────────
    # Оставляем Azure DevOps отдельно (другой продукт)
    ("DevOps", [
        "DevOps2020",
        "DevOps Principles",
        "DevOps-задачи",
        "DevOps-тестирование",
        "DevOps-инженер",               # это роль, не отдельный навык
        "DevOps Engineer",              # то же — роль
        "Работа с DevOps",
        "DevOps практики в разработке нативных мобильных платформ",
        "Integration of Testing Processes into CI/CD Pipeline and DevOps Processes",
        "Сотрудничество с командами backend и devops",
        "Аналитики и DevOps-команда",
        "Совместная работа DevOps/Backend",
        "Совместная работа с командами DevOps",
    ]),

    # ── Selenium ─────────────────────────────────────────────────────────────
    ("Selenium", [
        "Selenium IDE",                 # конкретный инструмент, 1 ref
    ]),
]


def get_by_name(cur, name):
    cur.execute("""
        SELECT sc.id, sc.name, COUNT(DISTINCT wsc.work_skill_id) as refs
        FROM skill_canonical sc
        LEFT JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
        WHERE sc.name = %s
        GROUP BY sc.id, sc.name
    """, (name,))
    return cur.fetchone()


def relink_wsc(cur, from_id, to_id):
    cur.execute("""
        INSERT INTO work_skill_canonical (work_skill_id, canonical_id)
        SELECT wsc.work_skill_id, %s
        FROM work_skill_canonical wsc
        WHERE wsc.canonical_id = %s
          AND NOT EXISTS (
              SELECT 1 FROM work_skill_canonical x
              WHERE x.work_skill_id = wsc.work_skill_id AND x.canonical_id = %s
          )
    """, (to_id, from_id, to_id))
    cur.execute("DELETE FROM work_skill_canonical WHERE canonical_id = %s", (from_id,))


def delete_canonical(cur, cid, redirect_to):
    cur.execute("DELETE FROM skill_version WHERE canonical_id = %s", (cid,))
    cur.execute("DELETE FROM skill_domain_stats WHERE canonical_id = %s", (cid,))
    cur.execute("DELETE FROM skill_dependency WHERE parent_id = %s OR child_id = %s", (cid, cid))
    for t in ("expert_opinion", "foresight"):
        cur.execute(f"DELETE FROM {t} WHERE canonical_id = %s AND EXISTS "
                    f"(SELECT 1 FROM {t} t2 WHERE t2.canonical_id = %s)", (cid, redirect_to))
        cur.execute(f"UPDATE {t} SET canonical_id = %s WHERE canonical_id = %s", (redirect_to, cid))
    cur.execute("UPDATE work_skill SET canonical_id = %s WHERE canonical_id = %s", (redirect_to, cid))
    cur.execute("DELETE FROM skill_canonical WHERE id = %s", (cid,))


def run(save):
    conn = psycopg2.connect(os.getenv('DB_URL'))
    cur = conn.cursor()
    total = 0

    for winner_name, losers in MERGE_GROUPS:
        winner = get_by_name(cur, winner_name)
        if winner is None:
            print(f"  [SKIP winner] '{winner_name}' — не найден")
            continue
        w_id, _, w_refs = winner

        for lname in losers:
            loser = get_by_name(cur, lname)
            if loser is None:
                continue
            l_id, _, l_refs = loser
            print(f"  MERGE  '{lname}' ({l_refs}x) → '{winner_name}' ({w_refs}x)")
            if save:
                relink_wsc(cur, l_id, w_id)
                delete_canonical(cur, l_id, w_id)
            total += 1

    if save:
        conn.commit()
        print(f"\n✓ Применено: {total} слияний")
    else:
        print(f"\n[PREVIEW] Будет: {total} слияний. Запусти --save")
    conn.close()


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("--save", action="store_true")
    args = p.parse_args()
    print(f"=== normalize_misc {'SAVE' if args.save else 'PREVIEW'} ===\n")
    run(args.save)
