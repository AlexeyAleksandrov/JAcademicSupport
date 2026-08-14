"""
Redistribute ORM, Миграции БД, Аналитика families:
- ORM tools    → BACKEND domain, language-specific family
- Миграции БД  → BACKEND domain, language-specific family (generic ones → DATABASE/Реляционные)
- Аналитика    → DATA_SCIENCE domain
Run with --save to apply.
"""
import os, sys
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
from dotenv import load_dotenv; load_dotenv()
import psycopg2

SAVE = "--save" in sys.argv

# ── ORM: skill_name → (new_domain, new_family) ────────────────────────────
ORM_MAP = {
    # Java
    "Hibernate":          ("BACKEND", "Java"),
    "Hibernate Validator":("BACKEND", "Java"),
    "JPA":                ("BACKEND", "Java"),
    "JPQL":               ("BACKEND", "Java"),
    "Spring Data JPA":    ("BACKEND", "Java"),
    "MyBatis":            ("BACKEND", "Java"),
    "iBatis":             ("BACKEND", "Java"),
    # Python
    "SQLAlchemy":         ("BACKEND", "Python"),
    "SQLAlchemy ORM":     ("BACKEND", "Python"),
    "Alembic":            ("BACKEND", "Python"),
    "Peewee":             ("BACKEND", "Python"),
    "Tortoise ORM":       ("BACKEND", "Python"),
    # JavaScript / TypeScript
    "Sequelize":          ("BACKEND", "JavaScript"),
    "TypeORM":            ("BACKEND", "JavaScript"),
    "Prisma":             ("BACKEND", "JavaScript"),
    "Knex.js":            ("BACKEND", "JavaScript"),
    "knex":               ("BACKEND", "JavaScript"),
    # PHP
    "Eloquent":           ("BACKEND", "PHP"),
    "Doctrine":           ("BACKEND", "PHP"),
    # Go
    "Gorm":               ("BACKEND", "Go"),
    "GORM":               ("BACKEND", "Go"),
    "sqlx":               ("BACKEND", "Go"),
    # .NET / C#
    "Entity Framework":   ("BACKEND", ".NET"),
    "EF Core":            ("BACKEND", ".NET"),
    "Dapper":             ("BACKEND", ".NET"),
    # Ruby
    "ActiveRecord":       ("BACKEND", "Ruby"),
}

# ── Миграции БД: skill_name → (new_domain, new_family) ────────────────────
MIGRATION_MAP = {
    # Java-based (but language-agnostic migration tools used most in Java projects)
    "Flyway":                    ("BACKEND", "Java"),
    "Liquibase":                 ("BACKEND", "Java"),
    # Python
    "Alembic":                   ("BACKEND", "Python"),   # duplicate of ORM, same target
    "Django migrations":         ("BACKEND", "Python"),
    # JavaScript
    "db-migrate":                ("BACKEND", "JavaScript"),
    "TypeORM migrations":        ("BACKEND", "JavaScript"),
    "Prisma Migrate":            ("BACKEND", "JavaScript"),
    # Go
    "golang-migrate":            ("BACKEND", "Go"),
    # Ruby
    "Rails migrations":          ("BACKEND", "Ruby"),
    # Generic / language-agnostic → stay in DATABASE with cleaner family
    "Migrations":                ("DATABASE", "Реляционные"),
    "Data migration automation": ("DATABASE", "Реляционные"),
    "Database schema migration": ("DATABASE", "Реляционные"),
    "Database schema migrations via XML changelogs": ("DATABASE", "Реляционные"),
    "Восстановление и миграция баз данных":          ("DATABASE", "Реляционные"),
    "миграция баз данных":       ("DATABASE", "Реляционные"),
}

conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

# ── Collect all changes ────────────────────────────────────────────────────
changes = []  # (id, name, old_domain, old_family, new_domain, new_family)

def collect(name_map, source_family):
    for name, (new_dom, new_fam) in name_map.items():
        cur.execute(
            "SELECT id, domain, tech_family FROM skill_canonical WHERE name=%s",
            (name,)
        )
        row = cur.fetchone()
        if not row:
            continue
        sid, old_dom, old_fam = row
        if old_dom == new_dom and old_fam == new_fam:
            continue  # already correct
        changes.append((sid, name, old_dom, old_fam, new_dom, new_fam))

collect(ORM_MAP, "ORM")
collect(MIGRATION_MAP, "Миграции БД")

# Аналитика → DATABASE/BI/Analytics (data warehouse modeling concepts)
cur.execute(
    "SELECT id, name, domain FROM skill_canonical WHERE tech_family='Аналитика' AND domain='DATABASE'"
)
for sid, name, old_dom in cur.fetchall():
    changes.append((sid, name, old_dom, "Аналитика", "DATABASE", "BI/Analytics"))

# ── Print preview ──────────────────────────────────────────────────────────
print(f"\n{'ID':>7}  {'Имя':<35}  {'Домен':<25}  {'Семейство'}")
print("─" * 110)
for sid, name, old_dom, old_fam, new_dom, new_fam in sorted(changes, key=lambda x: (x[4], x[5] or "", x[1])):
    dom_s = f"{old_dom} → {new_dom}" if old_dom != new_dom else old_dom
    fam_s = f"{str(old_fam)} → {str(new_fam)}" if old_fam != new_fam else str(new_fam)
    print(f"{sid:>7}  {name:<35}  {dom_s:<25}  {fam_s}")

print(f"\nTotal: {len(changes)} changes")

if SAVE:
    for sid, name, old_dom, old_fam, new_dom, new_fam in changes:
        cur.execute(
            "UPDATE skill_canonical SET domain=%s, tech_family=%s WHERE id=%s",
            (new_dom, new_fam, sid)
        )
    conn.commit()
    print("✅ Saved")
else:
    print("ℹ️  Run with --save to apply")

conn.close()
