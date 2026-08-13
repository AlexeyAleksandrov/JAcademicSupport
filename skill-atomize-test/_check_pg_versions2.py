import psycopg2, os
from dotenv import load_dotenv; load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

cur.execute("SELECT id FROM skill_canonical WHERE name = 'PostgreSQL' LIMIT 1")
pg_id = cur.fetchone()[0]

cur.execute("""
    SELECT sv.raw_string, sv.version_min,
           COUNT(DISTINCT vs.vacancy_entity_id) AS vacancy_count
    FROM skill_version sv
    LEFT JOIN work_skill_canonical wsc ON wsc.canonical_id = sv.canonical_id
    LEFT JOIN work_skill ws
           ON ws.id = wsc.work_skill_id
          AND ws.description ~ ('(^|[^0-9])' || sv.version_min || '([^0-9]|$)')
    LEFT JOIN vacancy_skills vs ON vs.skills_id = ws.id
    WHERE sv.canonical_id = %s
    GROUP BY sv.id, sv.raw_string, sv.version_min
    ORDER BY vacancy_count DESC
""", (pg_id,))
print('PostgreSQL versions (new query):')
for r in cur.fetchall():
    print(f'  {r[1]:>6}  {r[2]:>4}x  {r[0]}')

# Also check Java for comparison
cur.execute("SELECT id FROM skill_canonical WHERE name = 'Java' LIMIT 1")
java_id = cur.fetchone()[0]
cur.execute("""
    SELECT sv.raw_string, sv.version_min,
           COUNT(DISTINCT vs.vacancy_entity_id) AS cnt
    FROM skill_version sv
    LEFT JOIN work_skill_canonical wsc ON wsc.canonical_id = sv.canonical_id
    LEFT JOIN work_skill ws
           ON ws.id = wsc.work_skill_id
          AND ws.description ~ ('(^|[^0-9])' || sv.version_min || '([^0-9]|$)')
    LEFT JOIN vacancy_skills vs ON vs.skills_id = ws.id
    WHERE sv.canonical_id = %s
    GROUP BY sv.id, sv.raw_string, sv.version_min
    ORDER BY cnt DESC LIMIT 8
""", (java_id,))
print('\nJava versions (new query):')
for r in cur.fetchall():
    print(f'  {r[1]:>6}  {r[2]:>4}x  {r[0]}')

conn.close()
