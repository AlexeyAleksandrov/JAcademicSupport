import psycopg2, os
from dotenv import load_dotenv; load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

# Find Java canonical id
cur.execute("SELECT id FROM skill_canonical WHERE name = 'Java' LIMIT 1")
java_id = cur.fetchone()[0]
print(f'Java canonical id: {java_id}')

# Count skill_version entries for Java
cur.execute("SELECT COUNT(*) FROM skill_version WHERE canonical_id = %s", (java_id,))
print(f'skill_version entries for Java: {cur.fetchone()[0]}')

# Show version counts via new query logic
cur.execute("""
    SELECT sv.version_min, sv.raw_string,
           COUNT(DISTINCT vs.vacancy_entity_id) as cnt
    FROM skill_version sv
    LEFT JOIN work_skill ws ON lower(trim(ws.description)) = lower(trim(sv.raw_string))
    LEFT JOIN vacancy_skills vs ON vs.skills_id = ws.id
    WHERE sv.canonical_id = %s
    GROUP BY sv.version_min, sv.raw_string
    ORDER BY cnt DESC, sv.version_min DESC NULLS LAST
    LIMIT 15
""", (java_id,))
print('\nJava version counts (new query):')
for r in cur.fetchall():
    print(f'  {r}')

# Also check a few raw work_skill descriptions for Java
cur.execute("""
    SELECT ws.description, COUNT(DISTINCT vs.vacancy_entity_id) as cnt
    FROM work_skill_canonical wsc
    JOIN work_skill ws ON ws.id = wsc.work_skill_id
    JOIN vacancy_skills vs ON vs.skills_id = ws.id
    WHERE wsc.canonical_id = %s AND ws.description ILIKE '%java %'
    GROUP BY ws.description
    ORDER BY cnt DESC LIMIT 10
""", (java_id,))
print('\nSample work_skill descriptions for Java (containing "java "):')
for r in cur.fetchall():
    print(f'  {r}')

conn.close()
