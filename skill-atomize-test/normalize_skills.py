"""
normalize_skills.py — Донормализация skill_canonical:
  Фаза 1: де-фрагментация версий ("Java 17" → canonical "Java" + skill_version)
  Фаза 2: слияние дубликатов/синонимов + разбивка составных навыков

Использование:
    python normalize_skills.py                     # preview обеих фаз
    python normalize_skills.py --save              # применить изменения
    python normalize_skills.py --phase 1           # только де-фрагментация версий
    python normalize_skills.py --phase 2           # только слияние/разбивка
    python normalize_skills.py --domain LANGUAGES  # ограничить доменом
    python normalize_skills.py --family Java       # ограничить семейством
    python normalize_skills.py --verbose           # все строки (не только сводку)

После применения обязательно пересчитать аналитику:
    python compute_analytics.py
"""

import argparse
import os
import re
import sys

import psycopg2
import psycopg2.extras
from dotenv import load_dotenv

load_dotenv()

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

DB_URL = os.getenv("DB_URL", "postgresql://postgres:1111@localhost:5432/AcademicSupport")

SEP  = "═" * 65
SEP2 = "─" * 65

# ─── Паттерн версии: "BASE VERSION" где VERSION = цифры + точки ──────────────
# Обрабатывает: "Java 17", "C# 8.0", ".NET Core 3.1", "Python 3.12"
# НЕ обрабатывает: "CSS3", "ES6", "HTTP/2" (нет пробела перед цифрой)
VERSION_RE = re.compile(r"^(.+?)\s+(v?[\d]+[\d.]*)$", re.IGNORECASE)


# ═══════════════════════════════════════════════════════════════════════════════
# ФАЗА 2 — Правила слияния и разбивки
# ═══════════════════════════════════════════════════════════════════════════════

# Группы синонимов/дубликатов для слияния.
# Победитель = тот, на кого больше ссылается work_skill_canonical.
# Все остальные переключаются на победителя и удаляются.
MERGE_GROUPS: list[list[str]] = [

    # ─── Принципы / General ──────────────────────────────────────────────────
    ["ООП", "OOP", "Объектно-ориентированное программирование",
     "Object-Oriented Programming", "ООП (Объектно-Ориентированное Программирование)",
     "Объектно ориентированное программирование"],
    ["SOLID", "SOLID principles", "Принципы SOLID", "SOLID principle",
     "SOLID-принципы", "Solid Principles", "Принципы SOLID (SRP, OCP, LSP, ISP, DIP)"],
    ["Паттерны проектирования", "Design Patterns", "Design Pattern", "Паттерны",
     "GoF patterns", "Gang of Four", "GoF", "Шаблоны проектирования",
     "Паттерны проектирования (GoF)"],
    ["DRY", "Don't Repeat Yourself", "Принцип DRY", "DRY principle"],
    ["KISS", "Keep It Simple Stupid", "Keep It Simple", "Принцип KISS", "KISS principle"],
    ["YAGNI", "You Aren't Gonna Need It", "Принцип YAGNI", "YAGNI principle"],
    ["GRASP", "GRASP patterns", "GRASP-паттерны", "GRASP principles"],

    # ─── Архитектура ─────────────────────────────────────────────────────────
    ["Микросервисы", "Microservices", "Microservices Architecture",
     "Микросервисная архитектура", "Microservice Architecture"],
    ["Монолит", "Monolith", "Monolithic", "Monolithic Architecture"],
    ["Event Sourcing", "Event-Sourcing", "Event sourcing"],
    ["CQRS", "Command Query Responsibility Segregation",
     "Command-Query Responsibility Segregation"],
    ["Domain-Driven Design", "DDD", "Доменное проектирование",
     "Domain Driven Design", "Доменно-ориентированное проектирование"],
    ["Event-Driven Architecture", "EDA", "Event Driven Architecture",
     "Событийно-ориентированная архитектура"],
    ["Serverless", "Serverless Architecture", "Безсерверная архитектура"],
    ["SOA", "Service-Oriented Architecture", "Service Oriented Architecture"],
    ["Hexagonal Architecture", "Ports and Adapters", "Гексагональная архитектура"],
    ["Clean Architecture", "Чистая архитектура"],

    # ─── Методологии ─────────────────────────────────────────────────────────
    ["Agile", "Agile methodology", "Agile-методология", "Agile Methodology",
     "Методология Agile"],
    ["Scrum", "SCRUM", "Scrum methodology", "Методология Scrum"],
    ["Kanban", "KANBAN", "Методология Kanban"],
    ["Waterfall", "Водопадная модель"],
    ["CI/CD", "CI / CD", "CI-CD", "CI\\CD",
     "Continuous Integration / Continuous Delivery",
     "Continuous Integration/Continuous Deployment",
     "Continuous Integration / Continuous Deployment"],
    ["DevOps", "DevOps practices", "DevOps-практики"],

    # ─── API / Протоколы ─────────────────────────────────────────────────────
    ["REST", "REST API", "RESTful", "RESTful API", "REST API (HTTP)",
     "Restful", "REST/HTTP", "REST API design", "Restful API"],
    ["SOAP", "SOAP API", "SOAP Web Services", "SOAP-сервисы"],
    ["GraphQL", "GraphQL API"],
    ["WebSocket", "WebSockets", "Web Sockets", "WebSocket API"],
    ["gRPC", "GRPC", "gRPC API", "gRPC framework"],
    ["HTTP", "HTTP protocol", "Протокол HTTP", "HTTP/HTTPS"],
    ["HTTPS", "TLS/SSL", "SSL/TLS"],
    ["JSON", "JSON format", "JSON Schema"],
    ["XML", "XML format", "XML Schema"],
    ["YAML", "YML"],
    ["OpenAPI", "OpenAPI Specification", "Swagger/OpenAPI"],
    ["Swagger", "Swagger UI", "Swagger/OpenAPI Specification"],

    # ─── VCS ─────────────────────────────────────────────────────────────────
    ["Git", "GIT", "git"],
    ["GitHub", "Github", "GitHub.com"],
    ["GitLab", "Gitlab", "GitLab CI"],
    ["Bitbucket", "BitBucket"],
    ["SVN", "Subversion", "Apache Subversion"],

    # ─── Языки ───────────────────────────────────────────────────────────────
    ["JavaScript", "JS", "Javascript", "java script"],
    ["TypeScript", "TS", "Typescript"],
    ["Python", "Python 3", "Python3"],
    ["C++", "C/C++", "С++"],
    ["Bash", "Bash Script", "Shell", "Shell Script", "Bash/Shell", "Bash scripting",
     "Shell scripting"],

    # ─── Контейнеры / DevOps ─────────────────────────────────────────────────
    ["Docker", "docker", "Docker container"],
    ["Kubernetes", "K8s", "k8s", "kubernetes", "K8S"],
    ["Helm", "Helm Charts", "Helm chart"],
    ["Terraform", "HashiCorp Terraform"],
    ["Ansible", "Ansible automation"],
    ["Jenkins", "Jenkins CI", "Jenkins CI/CD"],
    ["GitHub Actions", "GitHub Action", "Github Actions"],
    ["GitLab CI/CD", "GitLab CI", "Gitlab CI/CD"],
    ["ArgoCD", "Argo CD", "Argo CD (GitOps)"],

    # ─── Базы данных ─────────────────────────────────────────────────────────
    ["PostgreSQL", "Postgres", "PostgresQL", "Postgresql"],
    ["MySQL", "My SQL", "MySql", "mysql"],
    ["MongoDB", "Mongo DB", "MongoDb", "Mongo", "mongo"],
    ["Redis", "REDIS", "redis"],
    ["Elasticsearch", "ElasticSearch", "Elastic Search", "Elastic"],
    ["SQL", "SQL language", "Язык SQL", "SQL query", "SQL queries"],
    ["NoSQL", "No SQL", "NoSQL databases"],
    ["ClickHouse", "Clickhouse", "Click House"],
    ["Apache Kafka", "Kafka", "kafka", "Apache Kafka (Messaging)"],
    ["RabbitMQ", "Rabbit MQ", "RabbitMQ message broker"],
    ["Memcached", "MemCached"],

    # ─── Облака ──────────────────────────────────────────────────────────────
    ["AWS", "Amazon Web Services", "Amazon AWS"],
    ["GCP", "Google Cloud Platform", "Google Cloud", "Google GCP"],
    ["Azure", "Microsoft Azure", "MS Azure", "Azure Cloud"],
    ["Yandex Cloud", "Яндекс.Облако", "Yandex.Cloud"],
    ["VK Cloud", "Mail.ru Cloud", "MCS"],

    # ─── Тестирование ────────────────────────────────────────────────────────
    ["Unit тестирование", "Unit testing", "Юнит-тестирование", "Unit-тестирование",
     "Модульное тестирование", "Unit tests"],
    ["Интеграционное тестирование", "Integration testing",
     "Integration tests", "Интеграционные тесты"],
    ["Нагрузочное тестирование", "Load testing", "Performance testing",
     "Нагрузочные тесты"],
    ["TDD", "Test-Driven Development", "Test Driven Development",
     "Разработка через тестирование"],
    ["BDD", "Behaviour-Driven Development", "Behavior-Driven Development"],
    ["E2E тестирование", "End-to-End testing", "End-to-End тестирование", "E2E Testing"],

    # ─── Разное ──────────────────────────────────────────────────────────────
    ["Linux", "GNU/Linux", "GNU Linux", "linux"],
    ["Ubuntu", "Ubuntu Linux"],
    ["Windows", "MS Windows", "Microsoft Windows"],
    ["Nginx", "NGINX", "nginx"],
    ["Apache", "Apache HTTP Server", "Apache2"],
    ["OAuth", "OAuth 2", "OAuth 2.0", "OAuth2"],
    ["JWT", "JSON Web Token", "JSON Web Tokens"],
    ["LDAP", "Active Directory/LDAP"],
    ["Prometheus", "Prometheus monitoring"],
    ["Grafana", "Grafana monitoring"],
    ["ELK", "ELK Stack", "Elastic Stack", "ELK stack"],
    ["Jira", "JIRA", "Atlassian Jira"],
    ["Confluence", "Atlassian Confluence"],
    ["Postman", "Postman API"],
]

# Составные навыки, которые нужно разбить на атомарные.
# Ключ — имя compound canonical (case-sensitive, точное имя в БД).
# Значение — список имён целевых canonical.
# Целевые canonical должны существовать в БД (или быть целевыми в MERGE_GROUPS).
SPLIT_RULES: dict[str, list[str]] = {

    # ─── Принципы ────────────────────────────────────────────────────────────
    "ООП и современные паттерны проектирования": ["ООП", "Паттерны проектирования"],
    "ООП и паттерны проектирования":             ["ООП", "Паттерны проектирования"],
    "ООП и паттерны":                            ["ООП", "Паттерны проектирования"],
    "OOP и паттерны проектирования":             ["ООП", "Паттерны проектирования"],
    "Паттерны проектирования SOLID":             ["Паттерны проектирования", "SOLID"],
    "Паттерны проектирования (SOLID, GoF)":      ["Паттерны проектирования", "SOLID"],
    "SOLID и архитектурные паттерны":            ["SOLID", "Паттерны проектирования"],
    "SOLID и паттерны":                          ["SOLID", "Паттерны проектирования"],

    # ─── VCS ─────────────────────────────────────────────────────────────────
    "Git/GitHub":       ["Git", "GitHub"],
    "Git / GitHub":     ["Git", "GitHub"],
    "Git + GitHub":     ["Git", "GitHub"],
    "Git, GitHub":      ["Git", "GitHub"],
    "Git/GitLab":       ["Git", "GitLab"],
    "Git / GitLab":     ["Git", "GitLab"],
    "Git/GitHub/GitLab":["Git", "GitHub", "GitLab"],
    "GitHub/GitLab":    ["GitHub", "GitLab"],

    # ─── Frontend ─────────────────────────────────────────────────────────────
    "HTML/CSS":         ["HTML", "CSS"],
    "HTML / CSS":       ["HTML", "CSS"],
    "HTML5/CSS3":       ["HTML", "CSS"],
    "HTML + CSS":       ["HTML", "CSS"],
    "HTML и CSS":       ["HTML", "CSS"],

    # ─── Languages ────────────────────────────────────────────────────────────
    "C/C++":            ["C", "C++"],
    "C / C++":          ["C", "C++"],
    "C и C++":          ["C", "C++"],

    # ─── Методологии ──────────────────────────────────────────────────────────
    "Agile/Scrum":                   ["Agile", "Scrum"],
    "Agile / Scrum":                 ["Agile", "Scrum"],
    "Scrum/Kanban":                  ["Scrum", "Kanban"],
    "Agile (Scrum, Kanban)":         ["Agile", "Scrum", "Kanban"],
    "Scrum/Kanban/Agile":            ["Agile", "Scrum", "Kanban"],
    "Agile и Scrum":                 ["Agile", "Scrum"],
    "Waterfall/Agile":               ["Waterfall", "Agile"],

    # ─── Разное ───────────────────────────────────────────────────────────────
    "SQL/NoSQL":        ["SQL", "NoSQL"],
    "SQL и NoSQL":      ["SQL", "NoSQL"],
    "REST/SOAP":        ["REST", "SOAP"],
    "REST и SOAP":      ["REST", "SOAP"],
    "REST/GraphQL":     ["REST", "GraphQL"],
    "Docker/Kubernetes":["Docker", "Kubernetes"],
    "Docker и Kubernetes": ["Docker", "Kubernetes"],
    "Linux/Unix":       ["Linux", "Unix"],
    "CI/CD pipeline":   ["CI/CD"],  # → single canonical, not split
    "OAuth 2.0/JWT":    ["OAuth", "JWT"],
    "ELK Stack (Elasticsearch, Logstash, Kibana)": ["ELK"],
}


# ═══════════════════════════════════════════════════════════════════════════════
# Вспомогательные функции
# ═══════════════════════════════════════════════════════════════════════════════

def connect() -> psycopg2.extensions.connection:
    return psycopg2.connect(DB_URL)


def load_all_canonicals(cur) -> dict[str, dict]:
    """Загружает все canonical навыки: {normalized_name: row_dict}"""
    cur.execute("""
        SELECT id, name, normalized_name, domain, tech_family, version_group, tech_type
        FROM skill_canonical
        ORDER BY id
    """)
    rows = cur.fetchall()
    cols = [d[0] for d in cur.description]
    by_id   = {}
    by_name = {}
    for row in rows:
        r = dict(zip(cols, row))
        by_id[r["id"]] = r
        by_name[r["name"].lower().strip()] = r
    return by_id, by_name


def count_refs(cur, canonical_id: int) -> int:
    cur.execute(
        "SELECT COUNT(*) FROM work_skill_canonical WHERE canonical_id = %s",
        (canonical_id,)
    )
    return cur.fetchone()[0]


def relink_wsc(cur, from_id: int, to_id: int) -> int:
    """Переключает work_skill_canonical с from_id на to_id. Возвращает кол-во переключённых."""
    cur.execute("""
        INSERT INTO work_skill_canonical(work_skill_id, canonical_id)
        SELECT work_skill_id, %s
        FROM work_skill_canonical
        WHERE canonical_id = %s
        ON CONFLICT DO NOTHING
    """, (to_id, from_id))
    inserted = cur.rowcount
    cur.execute("DELETE FROM work_skill_canonical WHERE canonical_id = %s", (from_id,))
    return inserted


def add_skill_version(cur, canonical_id: int, version_str: str, raw_string: str, is_plus: bool = False):
    """Добавляет skill_version, если такой записи ещё нет."""
    cur.execute("""
        SELECT id FROM skill_version
        WHERE canonical_id = %s AND version_min = %s
    """, (canonical_id, version_str))
    if cur.fetchone() is None:
        cur.execute("""
            INSERT INTO skill_version(canonical_id, raw_string, version_min, is_plus)
            VALUES (%s, %s, %s, %s)
        """, (canonical_id, raw_string, version_str, is_plus))


def move_skill_versions(cur, from_id: int, to_id: int):
    """Перемещает все skill_version записи с from_id на to_id (без дублей)."""
    cur.execute("""
        SELECT version_min, raw_string, version_max, is_plus
        FROM skill_version WHERE canonical_id = %s
    """, (from_id,))
    rows = cur.fetchall()
    for (vmin, raw, vmax, is_plus) in rows:
        cur.execute("""
            SELECT id FROM skill_version
            WHERE canonical_id = %s AND version_min = %s
        """, (to_id, vmin))
        if cur.fetchone() is None:
            cur.execute("""
                INSERT INTO skill_version(canonical_id, raw_string, version_min, version_max, is_plus)
                VALUES (%s, %s, %s, %s, %s)
            """, (to_id, raw, vmin, vmax, is_plus))
    cur.execute("DELETE FROM skill_version WHERE canonical_id = %s", (from_id,))


def delete_canonical(cur, canonical_id: int, redirect_to_id: int = None):
    """Удаляет canonical и все его аналитические записи.
    redirect_to_id: если задан, перенаправляет ссылки (work_skill, expert_opinion, foresight) на этот id.
    """
    # Таблицы с простым DELETE (pre-computed, будут пересчитаны)
    cur.execute("DELETE FROM skill_version WHERE canonical_id = %s", (canonical_id,))
    cur.execute("DELETE FROM work_skill_canonical WHERE canonical_id = %s", (canonical_id,))
    cur.execute("DELETE FROM skill_dependency WHERE parent_id = %s OR child_id = %s",
                (canonical_id, canonical_id))
    cur.execute("DELETE FROM skill_domain_stats WHERE canonical_id = %s", (canonical_id,))

    # Таблицы с данными — перенаправить на победителя или обнулить
    for table in ("expert_opinion", "foresight"):
        if redirect_to_id is not None:
            # Переключить на победителя; конфликты (дубли по canonical_id) — удалить
            cur.execute(
                f"DELETE FROM {table} WHERE canonical_id = %s "
                f"AND EXISTS (SELECT 1 FROM {table} t2 WHERE t2.canonical_id = %s)",
                (canonical_id, redirect_to_id)
            )
            cur.execute(
                f"UPDATE {table} SET canonical_id = %s WHERE canonical_id = %s",
                (redirect_to_id, canonical_id)
            )
        else:
            cur.execute(f"DELETE FROM {table} WHERE canonical_id = %s", (canonical_id,))

    # work_skill.canonical_id (denormalised legacy ссылка)
    if redirect_to_id is not None:
        cur.execute(
            "UPDATE work_skill SET canonical_id = %s WHERE canonical_id = %s",
            (redirect_to_id, canonical_id)
        )
    else:
        cur.execute("UPDATE work_skill SET canonical_id = NULL WHERE canonical_id = %s",
                    (canonical_id,))

    cur.execute("DELETE FROM skill_canonical WHERE id = %s", (canonical_id,))


# ═══════════════════════════════════════════════════════════════════════════════
# ФАЗА 1 — Де-фрагментация версий
# ═══════════════════════════════════════════════════════════════════════════════

def _parse_version(name: str):
    """Возвращает (base, version_str) или None если не подходит под паттерн."""
    m = VERSION_RE.match(name.strip())
    if not m:
        return None
    base = m.group(1).strip()
    ver  = m.group(2).strip().lstrip("v").lstrip("V")
    return base, ver


def run_phase1(conn, preview: bool, domain_filter=None, family_filter=None, verbose=False):
    print(f"\n{SEP}")
    print("  ФАЗА 1 — Де-фрагментация версий")
    print(SEP)

    with conn.cursor() as cur:
        by_id, by_name = load_all_canonicals(cur)

        # Кандидаты: canonical, чьё имя содержит версию
        candidates = []
        for sc in by_id.values():
            parsed = _parse_version(sc["name"])
            if parsed is None:
                continue
            base, ver = parsed
            # Применить фильтры
            if domain_filter and sc.get("domain", "").upper() != domain_filter.upper():
                continue
            if family_filter and sc.get("tech_family", "").lower() != family_filter.lower():
                continue
            parent = by_name.get(base.lower())
            candidates.append({
                "id":     sc["id"],
                "name":   sc["name"],
                "base":   base,
                "ver":    ver,
                "parent": parent,
                "refs":   None,
            })

        # Подсчитать ссылки для кандидатов
        if candidates:
            ids = tuple(c["id"] for c in candidates)
            cur.execute("""
                SELECT canonical_id, COUNT(*) as cnt
                FROM work_skill_canonical
                WHERE canonical_id = ANY(%s)
                GROUP BY canonical_id
            """, (list(ids),))
            ref_map = {r[0]: r[1] for r in cur.fetchall()}
            for c in candidates:
                c["refs"] = ref_map.get(c["id"], 0)

        found     = [c for c in candidates if c["parent"] is not None]
        orphans   = [c for c in candidates if c["parent"] is None]

        # Сводка
        print(f"\nКандидатов с версией в имени: {len(candidates)}")
        print(f"  Найден родитель:              {len(found)}")
        print(f"  Родитель НЕ найден (пропуск): {len(orphans)}")

        if orphans and verbose:
            print(f"\n{SEP2}")
            print("  ORPHANS (родитель не найден — пропускаются):")
            for o in orphans:
                print(f"    [{o['id']:>6}] {o['name']!r:45} base={o['base']!r}")

        if not found:
            print("\n  Нет изменений для Фазы 1.")
            return 0

        print(f"\n{SEP2}")
        print(f"  {'CANON':>6}  {'REFS':>5}  {'VERSIONED NAME':45}  {'→ PARENT'}")
        print(SEP2)

        merged = 0
        for c in sorted(found, key=lambda x: x["name"]):
            pid   = c["parent"]["id"]
            pname = c["parent"]["name"]
            line  = f"  [{c['id']:>6}] {c['refs']:>5}  {c['name']:45}  → [{pid}] {pname}  (ver={c['ver']})"
            if verbose or c["refs"] > 0:
                print(line)

            if not preview:
                # 1. Добавить версию родителю
                add_skill_version(cur, pid, c["ver"], c["name"])
                # 2. Перенести skill_version от versioned к родителю
                move_skill_versions(cur, c["id"], pid)
                # 3. Переключить work_skill_canonical
                relink_wsc(cur, c["id"], pid)
                # 4. Удалить versioned canonical, перенаправить ссылки на родителя
                delete_canonical(cur, c["id"], redirect_to_id=pid)
                # Обновить локальный by_name (чтобы следующие итерации не использовали удалённый)
                del by_name[c["name"].lower().strip()]
                del by_id[c["id"]]
                merged += 1

        if preview:
            print(f"\n  [PREVIEW] Будет слито: {len(found)} versioned canonical в родителей")
            print(f"  Orphans (нет родителя, пропускаются): {len(orphans)}")
            print("  Запустите с --save для применения.")
        else:
            conn.commit()
            print(f"\n  ✓ Слито: {merged} versioned canonical.")

        return len(found)


# ═══════════════════════════════════════════════════════════════════════════════
# ФАЗА 2а — Слияние дубликатов/синонимов
# ═══════════════════════════════════════════════════════════════════════════════

def run_phase2_merges(conn, preview: bool, domain_filter=None, family_filter=None, verbose=False):
    print(f"\n{SEP}")
    print("  ФАЗА 2а — Слияние дубликатов/синонимов")
    print(SEP)

    changes = 0

    with conn.cursor() as cur:
        by_id, by_name = load_all_canonicals(cur)

        # Подсчитать ссылки для всех canonical
        cur.execute("""
            SELECT canonical_id, COUNT(*) as cnt
            FROM work_skill_canonical
            GROUP BY canonical_id
        """)
        ref_map = {r[0]: r[1] for r in cur.fetchall()}

        for group in MERGE_GROUPS:
            # Найти существующие canonical из группы
            existing = []
            for name in group:
                sc = by_name.get(name.lower().strip())
                if sc is not None:
                    sc["refs"] = ref_map.get(sc["id"], 0)
                    existing.append(sc)

            if len(existing) < 2:
                continue  # Только 1 или 0 — нечего сливать

            # Убрать дубликаты (одно имя с разным регистром → один canonical)
            seen_ids: set = set()
            deduped = []
            for e in existing:
                if e["id"] not in seen_ids:
                    seen_ids.add(e["id"])
                    deduped.append(e)
            existing = deduped

            if len(existing) < 2:
                continue

            # Применить фильтры домена/семейства
            if domain_filter:
                existing = [e for e in existing if (e.get("domain") or "").upper() == domain_filter.upper()]
                if len(existing) < 2:
                    continue
            if family_filter:
                existing = [e for e in existing if (e.get("tech_family") or "").lower() == family_filter.lower()]
                if len(existing) < 2:
                    continue

            # Победитель — максимум ссылок; при равенстве — наименьший id
            winner = max(existing, key=lambda x: (x["refs"], -x["id"]))
            losers = [e for e in existing if e["id"] != winner["id"]]

            print(f"\n  Группа: {[e['name'] for e in existing]}")
            print(f"    ✓ Победитель: [{winner['id']:>6}] {winner['name']!r} ({winner['refs']} refs)")
            for loser in losers:
                print(f"    ✗ Проигравший: [{loser['id']:>6}] {loser['name']!r} ({loser['refs']} refs) → удалить")

            changes += len(losers)

            if not preview:
                for loser in losers:
                    relink_wsc(cur, loser["id"], winner["id"])
                    move_skill_versions(cur, loser["id"], winner["id"])
                    delete_canonical(cur, loser["id"], redirect_to_id=winner["id"])
                    by_name.pop(loser["name"].lower().strip(), None)
                    by_id.pop(loser["id"], None)

        if preview:
            print(f"\n  [PREVIEW] Будет удалено дубликатов: {changes}")
            print(f"  Запустите с --save для применения.")
        else:
            conn.commit()
            print(f"\n  ✓ Удалено дубликатов: {changes}")

    return changes


# ═══════════════════════════════════════════════════════════════════════════════
# ФАЗА 2б — Разбивка составных навыков
# ═══════════════════════════════════════════════════════════════════════════════

def run_phase2_splits(conn, preview: bool, domain_filter=None, family_filter=None, verbose=False):
    print(f"\n{SEP}")
    print("  ФАЗА 2б — Разбивка составных навыков")
    print(SEP)

    changes = 0

    with conn.cursor() as cur:
        by_id, by_name = load_all_canonicals(cur)

        for compound_name, targets in SPLIT_RULES.items():
            compound = by_name.get(compound_name.lower().strip())
            if compound is None:
                continue  # Нет такого canonical в БД

            # Применить фильтры
            if domain_filter and (compound.get("domain") or "").upper() != domain_filter.upper():
                continue
            if family_filter and (compound.get("tech_family") or "").lower() != family_filter.lower():
                continue

            # Найти целевые canonical
            target_rows = []
            missing = []
            for t in targets:
                tr = by_name.get(t.lower().strip())
                if tr:
                    target_rows.append(tr)
                else:
                    missing.append(t)

            cur.execute(
                "SELECT COUNT(*) FROM work_skill_canonical WHERE canonical_id = %s",
                (compound["id"],)
            )
            refs = cur.fetchone()[0]

            status = "⚠ ПРОПУСК" if missing else "✓"
            print(f"\n  {status} {compound_name!r} ({refs} refs) → {targets}")
            if missing:
                print(f"    Не найдены в БД: {missing} — пропускаем")
                continue

            changes += 1

            if not preview:
                # Получить все work_skill_id ссылающиеся на compound
                cur.execute(
                    "SELECT work_skill_id FROM work_skill_canonical WHERE canonical_id = %s",
                    (compound["id"],)
                )
                ws_ids = [r[0] for r in cur.fetchall()]

                for ws_id in ws_ids:
                    for tr in target_rows:
                        cur.execute("""
                            INSERT INTO work_skill_canonical(work_skill_id, canonical_id)
                            VALUES (%s, %s)
                            ON CONFLICT DO NOTHING
                        """, (ws_id, tr["id"]))

                # Удалить compound canonical, перенаправить на первый целевой
                delete_canonical(cur, compound["id"], redirect_to_id=target_rows[0]["id"])
                by_name.pop(compound_name.lower().strip(), None)
                by_id.pop(compound["id"], None)

    if preview:
        print(f"\n  [PREVIEW] Будет разбито составных: {changes}")
        print(f"  Запустите с --save для применения.")
    else:
        conn.commit()
        print(f"\n  ✓ Разбито составных: {changes}")

    return changes


# ═══════════════════════════════════════════════════════════════════════════════
# Авто-обнаружение потенциальных дубликатов (для информации)
# ═══════════════════════════════════════════════════════════════════════════════

def report_potential_duplicates(conn, domain_filter=None, family_filter=None):
    """Показывает canonical с похожими normalized_name, которых нет в MERGE_GROUPS."""
    print(f"\n{SEP}")
    print("  АВТО-ОБНАРУЖЕНИЕ: пары с похожими именами (не в MERGE_GROUPS)")
    print(SEP)

    known = set()
    for group in MERGE_GROUPS:
        for name in group:
            known.add(name.lower().strip())

    with conn.cursor() as cur:
        query = """
            SELECT a.id, a.name, a.domain, a.tech_family,
                   b.id as b_id, b.name as b_name,
                   COUNT(wa.work_skill_id) as refs_a,
                   COUNT(wb.work_skill_id) as refs_b
            FROM skill_canonical a
            JOIN skill_canonical b
                ON a.id < b.id
                AND (
                    lower(a.normalized_name) = lower(b.normalized_name)
                    OR (
                        length(a.normalized_name) > 3
                        AND length(b.normalized_name) > 3
                        AND (
                            lower(a.normalized_name) LIKE lower(b.normalized_name) || '%%'
                            OR lower(b.normalized_name) LIKE lower(a.normalized_name) || '%%'
                        )
                    )
                )
            LEFT JOIN work_skill_canonical wa ON wa.canonical_id = a.id
            LEFT JOIN work_skill_canonical wb ON wb.canonical_id = b.id
        """
        params = []
        if domain_filter:
            query += " WHERE a.domain = %s AND b.domain = %s"
            params += [domain_filter.upper(), domain_filter.upper()]
        query += " GROUP BY a.id, a.name, a.domain, a.tech_family, b.id, b.name ORDER BY refs_a+refs_b DESC LIMIT 50"

        cur.execute(query, params)
        rows = cur.fetchall()

    if not rows:
        print("  Дубликатов не найдено.")
        return

    print(f"  {'A_ID':>6}  {'A_NAME':40}  {'B_ID':>6}  {'B_NAME':40}  REFS")
    print(SEP2)
    for row in rows:
        aid, aname, dom, fam, bid, bname, ra, rb = row
        al = aname.lower().strip()
        bl = bname.lower().strip()
        if al in known and bl in known:
            continue  # уже в MERGE_GROUPS
        print(f"  [{aid:>6}] {aname:40}  [{bid:>6}] {bname:40}  {ra+rb}")


# ═══════════════════════════════════════════════════════════════════════════════
# main
# ═══════════════════════════════════════════════════════════════════════════════

def main():
    parser = argparse.ArgumentParser(
        description="Донормализация skill_canonical: версии и дубликаты"
    )
    parser.add_argument("--save",     action="store_true",
                        help="Применить изменения (по умолчанию — preview)")
    parser.add_argument("--phase",    choices=["1", "2", "all"], default="all",
                        help="Фаза: 1=версии, 2=дубликаты, all=обе (по умолчанию all)")
    parser.add_argument("--domain",   help="Ограничить доменом (LANGUAGES, BACKEND, ...)")
    parser.add_argument("--family",   help="Ограничить семейством (Java, Python, ...)")
    parser.add_argument("--verbose",  action="store_true",
                        help="Показать все строки, не только сводку")
    parser.add_argument("--suggest",  action="store_true",
                        help="Показать авто-обнаруженные потенциальные дубликаты")
    args = parser.parse_args()

    preview = not args.save

    print(SEP)
    print("  normalize_skills.py")
    print(f"  Режим: {'PREVIEW (только отчёт)' if preview else '⚠ SAVE (изменения в БД)'}")
    print(f"  Фаза:  {args.phase}")
    if args.domain:
        print(f"  Домен: {args.domain}")
    if args.family:
        print(f"  Семейство: {args.family}")
    print(SEP)

    conn = connect()

    try:
        if args.suggest:
            report_potential_duplicates(conn, args.domain, args.family)

        total = 0

        if args.phase in ("1", "all"):
            total += run_phase1(
                conn, preview,
                domain_filter=args.domain,
                family_filter=args.family,
                verbose=args.verbose,
            )

        if args.phase in ("2", "all"):
            total += run_phase2_merges(
                conn, preview,
                domain_filter=args.domain,
                family_filter=args.family,
                verbose=args.verbose,
            )
            total += run_phase2_splits(
                conn, preview,
                domain_filter=args.domain,
                family_filter=args.family,
                verbose=args.verbose,
            )

        print(f"\n{SEP}")
        if preview:
            print(f"  [PREVIEW] Всего изменений: {total}")
            print("  Добавьте --save для применения.")
        else:
            print(f"  ✓ Готово. Применено изменений: {total}")
            print()
            print("  Не забудьте пересчитать аналитику:")
            print("    python compute_analytics.py --truncate")
        print(SEP)

    except Exception as e:
        conn.rollback()
        print(f"\n  ✗ ОШИБКА: {e}", file=sys.stderr)
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    main()
