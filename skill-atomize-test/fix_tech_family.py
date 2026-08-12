"""
fix_tech_family.py — Заполняет поле tech_family в skill_canonical
по правилам (без LLM), на основе точного совпадения имён.

Использование:
    python fix_tech_family.py              # preview: показать что изменится
    python fix_tech_family.py --save       # применить изменения в БД
    python fix_tech_family.py --family Python   # только одно семейство
    python fix_tech_family.py --domain BACKEND  # ограничить доменом
    python fix_tech_family.py --show-unknown    # навыки без tech_family
"""

import argparse
import os
import sys

from dotenv import load_dotenv

load_dotenv()

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

DB_URL = os.getenv("DB_URL", "")

# ─── Правила: tech_family → список точных имён (case-insensitive) ─────────────
#
# Структура:
#   "Family Name": {
#       "exact": [...],       # точное совпадение (ILIKE без %)
#       "starts": [...],      # name ILIKE 'prefix%'
#       "contains": [...],    # name ILIKE '%substring%'  (осторожно: шире)
#   }

RULES: dict[str, dict] = {

    "Python": {
        "exact": [
            # Web-фреймворки
            "FastAPI", "Django", "Flask", "Tornado", "Starlette", "Sanic",
            "Bottle", "Falcon", "Pyramid", "CherryPy", "Litestar", "BlackSheep",
            "Quart", "aiohttp",
            # REST / API
            "Django REST Framework", "Django Rest Framework", "DRF",
            "django-rest-framework",
            # ORM / миграции
            "SQLAlchemy", "Alembic", "Django ORM", "SQLModel", "Peewee",
            "Tortoise ORM", "Pony ORM", "Beanie",
            # Async / очереди
            "asyncio", "Celery", "Kombu", "aioredis", "aiofiles",
            "Dramatiq", "Huey", "RQ", "Arq", "FastStream", "Propan",
            # HTTP-клиенты
            "requests", "httpx", "urllib3",
            # Сериализация / валидация
            "Pydantic", "Pydantic v2", "Marshmallow", "attrs",
            "dataclasses-json",
            # WSGI / ASGI серверы
            "uvicorn", "gunicorn", "Hypercorn", "Daphne", "Waitress", "uWSGI",
            # Auth
            "python-jose", "PyJWT", "passlib", "authlib",
            # ODM (MongoDB)
            "MongoEngine", "Motor", "PyMongo",
            # Брокеры / message
            "kafka-python", "Pika", "aio-pika",
            # Утилиты
            "Pillow", "python-multipart", "python-dotenv",
            # WebSocket
            "websockets", "channels", "Django Channels",
            # GraphQL
            "Strawberry", "Graphene",
        ],
        "starts": [
            "Pydantic",       # Pydantic, Pydantic v2, Pydantic V2
            "Django ",        # Django ORM, Django REST, Django Channels, ...
            "FastAPI ",       # FastAPI Router и т.п.
            "Flask-",         # Flask-Login, Flask-SQLAlchemy, ...
            "SQLAlchemy",     # SQLAlchemy 2.x и т.п.
            "python-",        # python-jose, python-dotenv и т.п.
            # aio* — только конкретные библиотеки (aiokafka = Message Brokers, не Python)
            "aiohttp", "aioredis", "aiofiles",
        ],
        "contains": [],       # не использовать contains для Python — слишком широко
    },

    ".NET": {
        "exact": [
            ".NET", ".NET Core", ".NET Framework", "ASP.NET", "ASP.NET Core",
            "ASP.NET MVC", "ASP.NET Web API", "Blazor", "MAUI", ".NET MAUI",
            "Entity Framework", "Entity Framework Core", "EF Core",
            "SignalR", "WPF", "WinForms", "Windows Forms",
            "NHibernate", "Dapper", "AutoMapper",
            "MediatR", "FluentValidation", "Polly",
            "Serilog", "NLog", "Log4Net",
            "xUnit", "NUnit", "MSTest", "Moq",
            "Swagger", "Swashbuckle", "NSwag",
            "Hangfire", "Quartz.NET",
            "IdentityServer", "Duende IdentityServer",
            "Carter", "Minimal API",
        ],
        "starts": [
            ".NET", "ASP.NET", "Entity Framework",
        ],
        "contains": [],
    },

    "Java": {
        "exact": [
            "Spring", "Spring Boot", "Spring MVC", "Spring Data", "Spring Security",
            "Spring Cloud", "Spring Batch", "Spring Integration",
            "Hibernate", "JPA", "JDBC", "MyBatis",
            "Maven", "Gradle",
            "JUnit", "Mockito", "TestContainers",
            "Lombok", "MapStruct",
            "Jackson", "Gson",
            "Quarkus", "Micronaut", "Vert.x", "Jakarta EE",
            "Kafka Streams", "Apache Camel",
            "Liquibase", "Flyway",
        ],
        "starts": [
            "Spring ", "Spring.",
        ],
        "contains": [],
    },

    "Go": {
        "exact": [
            "Go", "Golang", "Gin", "Echo", "Fiber", "Chi", "Gorilla Mux",
            "GORM", "sqlx", "pgx",
            "Go Kit", "go-kit",
            "Testify", "GoMock",
            "Goroutines", "gRPC-Go",
            "Cobra", "Viper",
        ],
        "starts": [],
        "contains": [],
    },

    "Node.js": {
        "exact": [
            "Node.js", "NodeJS", "Node JS",
            "Express", "Express.js", "NestJS", "Nest.js", "Fastify",
            "Koa", "Hapi", "Restify", "Sails.js",
            "Sequelize", "TypeORM", "Prisma", "Mongoose",
            "Socket.io", "socket.io",
            "PM2", "Nodemon",
            "Bull", "BullMQ", "Agenda",
            "Passport.js", "jsonwebtoken",
        ],
        "starts": ["Express", "NestJS", "Node"],
        "contains": [],
    },

    "PHP": {
        "exact": [
            "PHP", "Laravel", "Symfony", "Yii", "Yii2", "CodeIgniter",
            "Zend", "CakePHP", "Slim",
            "Doctrine", "Eloquent",
            "Composer",
            "PHPUnit",
            "WordPress", "Bitrix", "1С-Битрикс",
        ],
        "starts": ["PHP", "Laravel", "Symfony", "Yii"],
        "contains": [],
    },

    "Rust": {
        "exact": [
            "Rust", "Actix", "Actix-web", "Axum", "Rocket", "Warp",
            "Tokio", "async-std",
            "Diesel", "SeaORM", "SQLx",
            "Serde", "Cargo",
        ],
        "starts": [],
        "contains": [],
    },

    "Kotlin": {
        "exact": [
            "Kotlin", "Ktor", "Exposed",
            "Kotlin Coroutines", "kotlinx.coroutines",
            "Spring Boot (Kotlin)",
        ],
        "starts": ["Kotlin"],
        "contains": [],
    },

    "Ruby": {
        "exact": [
            "Ruby", "Ruby on Rails", "Rails", "Sinatra", "Hanami",
            "ActiveRecord", "RSpec", "Capybara", "Sidekiq",
            "Bundler",
        ],
        "starts": [],
        "contains": [],
    },
}


# ─── Утилиты ─────────────────────────────────────────────────────────────────

def build_where_clause(rules: dict, param_offset: int = 1):
    """Строит SQL WHERE условие и список параметров для одного семейства."""
    conditions = []
    params = []
    idx = param_offset

    for name in rules.get("exact", []):
        conditions.append(f"LOWER(sc.name) = LOWER(%s)")
        params.append(name)
        idx += 1

    for prefix in rules.get("starts", []):
        conditions.append(f"LOWER(sc.name) LIKE LOWER(%s)")
        params.append(prefix.rstrip("%") + "%")
        idx += 1

    for sub in rules.get("contains", []):
        conditions.append(f"LOWER(sc.name) LIKE LOWER(%s)")
        params.append("%" + sub.strip("%") + "%")
        idx += 1

    return conditions, params


def preview_family(cur, family: str, rules: dict, domain_filter: str | None):
    conditions, params = build_where_clause(rules)
    if not conditions:
        return []

    domain_clause = ""
    if domain_filter:
        domain_clause = f" AND sc.domain = %s"
        params.append(domain_filter)

    sql = f"""
        SELECT sc.id, sc.name, sc.domain, sc.tech_family
        FROM skill_canonical sc
        WHERE ({' OR '.join(conditions)}){domain_clause}
        ORDER BY sc.domain, sc.name
    """
    cur.execute(sql, params)
    return cur.fetchall()


def apply_family(cur, family: str, rules: dict, domain_filter: str | None) -> int:
    conditions, where_params = build_where_clause(rules)
    if not conditions:
        return 0

    domain_clause = ""
    if domain_filter:
        domain_clause = f" AND sc.domain = %s"
        where_params.append(domain_filter)

    sql = f"""
        UPDATE skill_canonical sc
        SET tech_family = %s
        WHERE ({' OR '.join(conditions)}){domain_clause}
    """
    # family идёт ПЕРВЫМ (для SET), затем условия WHERE
    all_params = [family] + where_params
    cur.execute(sql, all_params)
    return cur.rowcount


def show_unknown(cur, domain_filter: str | None):
    domain_clause = ""
    params = []
    if domain_filter:
        domain_clause = "AND domain = %s"
        params.append(domain_filter)

    cur.execute(f"""
        SELECT id, name, domain, version_group
        FROM skill_canonical
        WHERE tech_family IS NULL {domain_clause}
        ORDER BY domain, name
        LIMIT 200
    """, params)
    rows = cur.fetchall()
    print(f"\n{'ID':>8}  {'Домен':<15}  {'version_group':<20}  name")
    print("─" * 80)
    for row in rows:
        sid, name, dom, vg = row
        print(f"{sid:>8}  {(dom or '—'):<15}  {(vg or '—'):<20}  {name}")
    print(f"\nИтого: {len(rows)} навыков без tech_family (лимит 200)")


# ─── Main ─────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="Заполнить tech_family в skill_canonical (rule-based)")
    parser.add_argument("--save",         action="store_true",  help="Применить изменения (иначе только preview)")
    parser.add_argument("--family",       default=None,         help="Обработать только одно семейство")
    parser.add_argument("--domain",       default=None,         help="Ограничить доменом (BACKEND, FRONTEND, ...)")
    parser.add_argument("--show-unknown", action="store_true",  help="Показать навыки без tech_family")
    args = parser.parse_args()

    if not DB_URL:
        sys.exit("[ERROR] DB_URL не задан в .env")

    import psycopg2
    conn = psycopg2.connect(DB_URL)
    cur  = conn.cursor()

    if args.show_unknown:
        show_unknown(cur, args.domain)
        cur.close()
        conn.close()
        return

    families_to_process = (
        {args.family: RULES[args.family]} if args.family and args.family in RULES
        else RULES
    )

    if args.family and args.family not in RULES:
        sys.exit(f"[ERROR] Семейство '{args.family}' не найдено. Доступны: {list(RULES.keys())}")

    total_updated = 0

    for family, rules in families_to_process.items():
        rows = preview_family(cur, family, rules, args.domain)

        already    = [r for r in rows if r[3] == family]
        to_update  = [r for r in rows if r[3] != family]
        other_fam  = [r for r in to_update if r[3] is not None]
        new_assign = [r for r in to_update if r[3] is None]

        print(f"\n{'═'*70}")
        print(f"  Семейство: {family}")
        print(f"  Совпадений по правилам: {len(rows)}")
        print(f"  ├─ уже имеют tech_family='{family}': {len(already)}")
        print(f"  ├─ будут назначены (tech_family IS NULL): {len(new_assign)}")
        print(f"  └─ перезапись другого значения: {len(other_fam)}")

        if to_update:
            print(f"\n  {'ID':>8}  {'Домен':<15}  {'Старый tech_family':<20}  name")
            print(f"  {'─'*65}")
            for sid, name, dom, old_fam in to_update[:50]:
                action = "→ assign" if old_fam is None else f"→ overwrite '{old_fam}'"
                print(f"  {sid:>8}  {(dom or '—'):<15}  {(old_fam or '—'):<20}  {name}  [{action}]")
            if len(to_update) > 50:
                print(f"  ... ещё {len(to_update) - 50} строк")

        if args.save and to_update:
            n = apply_family(cur, family, rules, args.domain)
            conn.commit()
            total_updated += n
            print(f"\n  ✅ Обновлено: {n} строк")
        elif not args.save and to_update:
            print(f"\n  ℹ️  Запустите с --save чтобы применить")

    if args.save:
        print(f"\n{'═'*70}")
        print(f"Итого обновлено: {total_updated} строк")
    else:
        print(f"\n{'═'*70}")
        print("Preview-режим. Для применения добавьте --save")

    cur.close()
    conn.close()


if __name__ == "__main__":
    main()
