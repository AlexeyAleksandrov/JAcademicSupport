import os, sys
import psycopg2
from dotenv import load_dotenv

load_dotenv()
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

# Прогресс
cur.execute("SELECT COUNT(*) FROM skill_canonical WHERE tech_family IS NULL")
empty_now = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM skill_canonical")
total = cur.fetchone()[0]
assigned = 14731 - empty_now
print(f"Осталось без семейства: {empty_now} / {total}")
print(f"Назначено с начала скрипта: {assigned} из 14731")
print()

# Что назначено: показываем образцы навыков с русскими/описательными именами
cur.execute("""
    SELECT name, domain, tech_family
    FROM skill_canonical
    WHERE tech_family IS NOT NULL
      AND domain IN ('GENERAL', 'TESTING', 'AI_ML', 'DATA_SCIENCE', 'SYSTEMS', 'SECURITY', 'IOT', 'CLOUD')
    ORDER BY id DESC
    LIMIT 80
""")
rows = cur.fetchall()
print(f"=== Недавно назначенные (последние 80 по id) ===")
for name, domain, fam in rows:
    print(f"  [{str(domain or ''):12}] {str(fam):30} <- {name[:55]}")

print()

# Распределение новых семейств (только для доменов которые раньше были пустыми)
cur.execute("""
    SELECT tech_family, COUNT(*) as cnt
    FROM skill_canonical
    WHERE tech_family IS NOT NULL
      AND domain IN ('GENERAL', 'TESTING', 'AI_ML', 'DATA_SCIENCE', 'SYSTEMS', 'SECURITY', 'IOT', 'CLOUD')
    GROUP BY tech_family
    ORDER BY cnt DESC
    LIMIT 30
""")
print("=== Семейства в проблемных доменах (все) ===")
for fam, cnt in cur.fetchall():
    print(f"  {str(fam):35} {cnt:5d}")

conn.close()
