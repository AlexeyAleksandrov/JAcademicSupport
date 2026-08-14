"""Check final state of redistributed ORM / migration tools."""
import os, sys
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
from dotenv import load_dotenv; load_dotenv()
import psycopg2

conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

TOOLS = [
    "Hibernate", "Hibernate Validator", "JPA", "JPQL", "Spring Data JPA",
    "MyBatis", "iBatis", "Liquibase", "Flyway",
    "SQLAlchemy", "SQLAlchemy ORM", "Alembic", "Peewee", "Tortoise ORM",
    "Sequelize", "TypeORM", "Prisma", "Knex.js", "knex",
    "Eloquent", "Doctrine",
    "GORM", "Gorm", "sqlx",
    "Entity Framework", "EF Core", "Dapper",
    "ActiveRecord",
]

print(f"{'Имя':<35}  {'Домен':<12}  {'Семейство'}")
print("─" * 70)
for name in TOOLS:
    cur.execute("SELECT domain, tech_family FROM skill_canonical WHERE name=%s", (name,))
    row = cur.fetchone()
    if row:
        print(f"{name:<35}  {str(row[0]):<12}  {str(row[1])}")
    else:
        print(f"{name:<35}  NOT FOUND")

# Check remaining DATABASE ORM
print("\n=== ORM family still in DATABASE ===")
cur.execute("SELECT id, name FROM skill_canonical WHERE tech_family='ORM' AND domain='DATABASE'")
for r in cur.fetchall():
    print(f"  {r[0]:>7}  {r[1]}")

conn.close()
