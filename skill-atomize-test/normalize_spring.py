"""
normalize_spring.py — нормализация семейства Spring/Hibernate/JPA/JDBC.

Использование:
  python normalize_spring.py            # preview (без изменений)
  python normalize_spring.py --save     # применить к БД
"""

import argparse
import psycopg2
import os
from dotenv import load_dotenv

load_dotenv()

# ──────────────────────────────────────────────────────────────────────────────
# Группы слияний: (winner_name, [loser_names])
# winner_name — имя canonical, которое ОСТАНЕТСЯ в БД
# loser_names — имена, которые будут слиты в winner и удалены
# ──────────────────────────────────────────────────────────────────────────────
MERGE_GROUPS = [

    # ── Spring Data ──────────────────────────────────────────────────────────
    ("Spring Data", [
        "Spring Boot Data",
        "Spring Data Repositories",
        "Spring IO",                     # слишком общий, относится к Spring Data
    ]),
    ("Spring Data JPA", [
        "Spring JPA",
        "Spring Boot Data/JPA",
        "Spring Boot JPA",
        "JPA",                           # в контексте Spring-вакансий JPA = Spring Data JPA
    ]),
    # Spring Data JDBC — фактически победитель Spring JDBC (3 refs),
    # но переименовываем в Spring Data JDBC (современное название модуля)
    ("Spring Data JDBC", [
        "Spring JDBC",
        "Spring JdbcTemplate",
        "JDBC",                          # в контексте Spring-вакансий JDBC = Spring Data JDBC
    ]),

    # ── Spring Security ───────────────────────────────────────────────────────
    ("Spring Security", [
        "Spring Boot Security",
        "Spring Boot Security/JWT",      # JWT — отдельный навык
    ]),

    # ── Spring MVC / Web ─────────────────────────────────────────────────────
    ("Spring MVC", [
        "Spring Web MVC",
        "Spring WebMVC",
        "Spring Boot MVC",
        "Spring REST",                   # REST в Spring делается через MVC
    ]),
    ("Spring Web", [
        "Spring.Web",
        "Spring Boot Web",
    ]),
    ("Spring WebFlux", [
        "Spring Boot Webflux",
        "Spring Reactive",
        "Spring Reactor",                # Project Reactor = основа WebFlux
    ]),

    # ── Spring Boot ───────────────────────────────────────────────────────────
    ("Spring Boot", [
        "Spring Boot Core",
        "Spring Boot Starters",
        "Spring Boot 3.x",              # версионный артефакт
        "Spring Boot Validation",
    ]),

    # ── Spring Core ───────────────────────────────────────────────────────────
    ("Spring Core", [
        "Spring IoC",                    # IoC — и есть суть Spring Core
        "Spring Scope",
    ]),

    # ── Spring Cloud ──────────────────────────────────────────────────────────
    ("Spring Cloud", [
        "Spring Cloud Config",
        "Spring Cloud Stream",
        "Spring Stream",
        "Spring SCGW",                  # Spring Cloud Gateway Wrapper
    ]),
    # Spring Cloud Gateway остаётся отдельным навыком (значимая технология)
    # ("Spring Cloud Gateway", []),

    # ── Spring Test ───────────────────────────────────────────────────────────
    ("Spring Test", [
        "Spring Boot Test",
    ]),

    # ── Spring Kafka ─────────────────────────────────────────────────────────
    ("Spring Kafka", []),               # оставляем, 3 refs, отдельная технология

    # ── Spring Cache ─────────────────────────────────────────────────────────
    ("Spring Cache", [
        "Spring Cache Abstraction",
    ]),

    # ── Spring Integration ────────────────────────────────────────────────────
    ("Spring Integration", [
        "Spring Integration Framework",
    ]),

    # ── SpringDoc OpenAPI ─────────────────────────────────────────────────────
    ("SpringDoc OpenAPI", [
        "Spring Doc OpenAPI",
        "Springdoc",
    ]),

    # ── Spring Web Services (SOAP) ────────────────────────────────────────────
    ("Spring Web Services", [
        "Spring-WS",
        "spring-web-services",
    ]),

    # ── Hibernate ─────────────────────────────────────────────────────────────
    ("Hibernate", [
        "Hibernate ORM",                # Hibernate и Hibernate ORM — одно и то же
        # Hibernate Validator — НЕ сливаем (Bean Validation, отдельная библиотека)
    ]),
]

# ──────────────────────────────────────────────────────────────────────────────
# Переименования победителей:
# winner_name_in_merge_groups → new_name_in_db
# ──────────────────────────────────────────────────────────────────────────────
RENAMES = {
    "Spring Data JDBC":     "Spring Data JDBC",   # Spring JDBC (3 refs) → переименовать
    "Spring Web Services":  "Spring Web Services", # spring-web-services/Spring-WS → переименовать
    "SpringDoc OpenAPI":    "SpringDoc OpenAPI",   # Spring Doc OpenAPI → переименовать
}


# ──────────────────────────────────────────────────────────────────────────────
# Helpers (аналог normalize_skills.py)
# ──────────────────────────────────────────────────────────────────────────────

def get_canonical(cur, name):
    cur.execute("""
        SELECT sc.id, sc.name, COUNT(DISTINCT wsc.work_skill_id) as refs
        FROM skill_canonical sc
        LEFT JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
        WHERE sc.name = %s
        GROUP BY sc.id, sc.name
    """, (name,))
    return cur.fetchone()  # (id, name, refs) or None


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


def delete_canonical(cur, canonical_id, redirect_to_id):
    cur.execute("DELETE FROM skill_version WHERE canonical_id = %s", (canonical_id,))
    cur.execute("DELETE FROM skill_domain_stats WHERE canonical_id = %s", (canonical_id,))
    cur.execute("DELETE FROM skill_dependency WHERE parent_id = %s OR child_id = %s",
                (canonical_id, canonical_id))
    for table in ("expert_opinion", "foresight"):
        cur.execute(
            f"DELETE FROM {table} WHERE canonical_id = %s "
            f"AND EXISTS (SELECT 1 FROM {table} t2 WHERE t2.canonical_id = %s)",
            (canonical_id, redirect_to_id)
        )
        cur.execute(
            f"UPDATE {table} SET canonical_id = %s WHERE canonical_id = %s",
            (redirect_to_id, canonical_id)
        )
    cur.execute(
        "UPDATE work_skill SET canonical_id = %s WHERE canonical_id = %s",
        (redirect_to_id, canonical_id)
    )
    cur.execute("DELETE FROM skill_canonical WHERE id = %s", (canonical_id,))


def normalize_name(name):
    return name.lower().strip()


# ──────────────────────────────────────────────────────────────────────────────
# Main
# ──────────────────────────────────────────────────────────────────────────────

def run(save: bool):
    conn = psycopg2.connect(os.getenv('DB_URL'))
    cur = conn.cursor()

    total_merged = 0
    total_renamed = 0
    not_found = []

    for winner_name, loser_names in MERGE_GROUPS:
        if not loser_names:
            continue

        # Найти winner
        winner = get_canonical(cur, winner_name)

        # Если winner не существует — попробовать найти лосера с наибольшим числом refs как winner
        if winner is None:
            # Найти из лосеров того, кто есть в БД с max refs, стать winner
            candidates = []
            for ln in loser_names:
                r = get_canonical(cur, ln)
                if r:
                    candidates.append(r)
            if not candidates:
                not_found.append(f"GROUP '{winner_name}': ни winner, ни losers не найдены в БД")
                continue
            candidates.sort(key=lambda x: -x[2])
            winner = candidates[0]
            remaining_losers = [c for c in candidates if c[0] != winner[0]]
            print(f"  [AUTO-WINNER] {winner[1]} (нет '{winner_name}', взят по max refs)")
        else:
            remaining_losers = []
            for ln in loser_names:
                r = get_canonical(cur, ln)
                if r:
                    remaining_losers.append(r)
                else:
                    print(f"  [SKIP] '{ln}' — не найден в БД")

        winner_id, winner_current_name, winner_refs = winner

        for loser in remaining_losers:
            loser_id, loser_name, loser_refs = loser
            print(f"  MERGE  '{loser_name}' ({loser_refs} refs) → '{winner_current_name}' ({winner_refs} refs)")
            if save:
                relink_wsc(cur, loser_id, winner_id)
                delete_canonical(cur, loser_id, winner_id)
                total_merged += 1
            else:
                total_merged += 1

    # Применить переименования
    for old_name, new_name in RENAMES.items():
        if old_name == new_name:
            # Найти нынешнего победителя и переименовать, если его имя НЕ совпадает с new_name
            # Ищем тех, кто мог стать winner (по merge group логике)
            # Переименование нужно если в БД есть другое имя, которое должно стать new_name
            pass

    # Отдельная логика переименований: Spring JDBC → Spring Data JDBC
    renames_explicit = [
        # (exact_current_name_in_db, target_name)
        ("Spring JDBC",        "Spring Data JDBC"),
        ("spring-web-services","Spring Web Services"),
        ("Spring-WS",          "Spring Web Services"),
        ("Spring Doc OpenAPI", "SpringDoc OpenAPI"),
        ("Springdoc",          "SpringDoc OpenAPI"),
    ]

    print("\n--- Переименования победителей ---")
    for old_name, new_name in renames_explicit:
        existing = get_canonical(cur, old_name)
        target = get_canonical(cur, new_name)
        if existing is None:
            print(f"  [SKIP] '{old_name}' — не найден (возможно уже переименован)")
            continue
        if target is not None and target[0] != existing[0]:
            # Целевое имя уже занято другим canonical → слить existing в target
            print(f"  MERGE+RENAME  '{old_name}' ({existing[2]} refs) → '{new_name}' ({target[2]} refs)")
            if save:
                relink_wsc(cur, existing[0], target[0])
                delete_canonical(cur, existing[0], target[0])
                total_merged += 1
        else:
            # Просто переименовать
            print(f"  RENAME  '{old_name}' → '{new_name}'")
            if save:
                cur.execute("UPDATE skill_canonical SET name = %s, normalized_name = lower(%s) WHERE id = %s",
                            (new_name, new_name, existing[0]))
                total_renamed += 1

    if save:
        conn.commit()
        print(f"\n✓ Применено: {total_merged} слияний, {total_renamed} переименований")
    else:
        print(f"\n[PREVIEW] Будет: {total_merged} слияний, {total_renamed} переименований")
        print("Запусти с --save для применения")

    if not_found:
        print("\nНе найдены в БД:")
        for m in not_found:
            print(f"  {m}")

    conn.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--save", action="store_true")
    args = parser.parse_args()
    print("=== Spring normalization", "SAVE" if args.save else "PREVIEW", "===\n")
    run(save=args.save)
