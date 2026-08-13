import psycopg2, os
from dotenv import load_dotenv; load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

print('=== DDL skill_version ===')
cur.execute("""
    SELECT column_name, data_type, is_nullable, column_default
    FROM information_schema.columns
    WHERE table_name = 'skill_version'
    ORDER BY ordinal_position
""")
for r in cur.fetchall():
    print(f'  {r[0]:20} {r[1]:20} nullable={r[2]}  default={r[3]}')

print('\n=== Пример: Java ===')
cur.execute("""
    SELECT sc.name as canonical, sv.raw_string, sv.version_min, sv.version_max, sv.is_plus
    FROM skill_version sv
    JOIN skill_canonical sc ON sc.id = sv.canonical_id
    WHERE sc.name = 'Java'
    ORDER BY sv.version_min::numeric NULLS LAST
""")
for r in cur.fetchall():
    print(f'  canonical={r[0]:12} raw={r[1]:20} min={r[2]:8} max={r[3] or "null":8} plus={r[4]}')

print('\n=== Статистика ===')
cur.execute("SELECT COUNT(*) FROM skill_version")
print(f'  Всего skill_version записей: {cur.fetchone()[0]}')
cur.execute("SELECT COUNT(DISTINCT canonical_id) FROM skill_version")
print(f'  Canonical-навыков с версиями: {cur.fetchone()[0]}')
cur.execute("""
    SELECT sc.name, COUNT(sv.id) as cnt
    FROM skill_version sv JOIN skill_canonical sc ON sc.id = sv.canonical_id
    GROUP BY sc.name ORDER BY cnt DESC LIMIT 10
""")
print('\n  Топ-10 навыков по кол-ву версий:')
for r in cur.fetchall():
    print(f'    {r[1]:3}  {r[0]}')

conn.close()
