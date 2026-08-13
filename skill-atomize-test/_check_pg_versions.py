import psycopg2, os
from dotenv import load_dotenv; load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

cur.execute("SELECT id FROM skill_canonical WHERE name = 'PostgreSQL' LIMIT 1")
pg_id = cur.fetchone()[0]
print(f'PostgreSQL canonical id: {pg_id}')

print('\n--- skill_version entries ---')
cur.execute("SELECT raw_string, version_min FROM skill_version WHERE canonical_id = %s ORDER BY version_min DESC", (pg_id,))
for r in cur.fetchall():
    print(f'  raw_string={r[0]!r:30}  version_min={r[1]}')

print('\n--- sample work_skill descriptions pointing to PostgreSQL (containing digit) ---')
cur.execute("""
    SELECT ws.description, COUNT(DISTINCT vs.vacancy_entity_id) as cnt
    FROM work_skill_canonical wsc
    JOIN work_skill ws ON ws.id = wsc.work_skill_id
    JOIN vacancy_skills vs ON vs.skills_id = ws.id
    WHERE wsc.canonical_id = %s
      AND ws.description ~ '[0-9]'
    GROUP BY ws.description
    ORDER BY cnt DESC LIMIT 20
""", (pg_id,))
for r in cur.fetchall():
    print(f'  {r[1]:5}x  {r[0]!r}')

conn.close()
