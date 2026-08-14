"""Show DATABASE domain skills with families that don't belong there."""
import os, sys
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
from dotenv import load_dotenv; load_dotenv()
import psycopg2

conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

# Families that are legitimate in DATABASE
OK_FAMILIES = {
    None, "Реляционные", "NoSQL", "In-Memory", "ORM",
    "BI/Analytics", "Data Engineering", "Аналитика/BI",
    "Аналитика", "Vector DB", "Миграции БД",
}

cur.execute("""
    SELECT tech_family, id, name
    FROM skill_canonical
    WHERE domain = 'DATABASE'
    ORDER BY tech_family NULLS LAST, name
""")
rows = cur.fetchall()

# Filter to only non-OK families
wrong = [(f, sid, n) for f, sid, n in rows if f not in OK_FAMILIES]

current_fam = "__NONE__"
for fam, sid, name in wrong:
    if fam != current_fam:
        current_fam = fam
        cnt = sum(1 for f2, _, _ in wrong if f2 == fam)
        print(f"\n{'═'*60}")
        print(f"  FAMILY: [{fam}]  ({cnt} skills in DATABASE)")
        print(f"{'═'*60}")
    print(f"    {sid:>7}  {name}")

print(f"\nTotal non-standard: {len(wrong)} skills")
conn.close()
