import psycopg2, os
from dotenv import load_dotenv; load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

print('--- Поиск C# в skill_canonical ---')
cur.execute("""
    SELECT sc.id, sc.name, sc.domain, sc.tech_family,
           COUNT(DISTINCT wsc.work_skill_id) as refs
    FROM skill_canonical sc
    LEFT JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
    WHERE sc.name ILIKE '%c#%' OR sc.name ILIKE '%csharp%'
    GROUP BY sc.id, sc.name, sc.domain, sc.tech_family
    ORDER BY refs DESC
""")
rows = cur.fetchall()
if rows:
    for r in rows:
        print(f'  id={r[0]}  refs={r[4]:4}  name={r[1]!r:30}  domain={r[2]}  family={r[3]}')
else:
    print('  НЕ НАЙДЕН вообще!')

print('\n--- Что в семействе Languages (топ-30) ---')
cur.execute("""
    SELECT sc.name, COUNT(DISTINCT wsc.work_skill_id) as refs
    FROM skill_canonical sc
    LEFT JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
    WHERE sc.tech_family = 'Languages'
    GROUP BY sc.name
    ORDER BY refs DESC LIMIT 30
""")
for r in cur.fetchall():
    print(f'  {r[1]:5}  {r[0]}')

print('\n--- work_skill descriptions содержащие "c#" ---')
cur.execute("""
    SELECT ws.description, ws.canonical_id,
           COUNT(DISTINCT vs.vacancy_entity_id) as cnt
    FROM work_skill ws
    LEFT JOIN vacancy_skills vs ON vs.skills_id = ws.id
    WHERE ws.description ILIKE '%c#%'
    GROUP BY ws.description, ws.canonical_id
    ORDER BY cnt DESC LIMIT 15
""")
for r in cur.fetchall():
    print(f'  {r[2]:4}x  canonical_id={r[1]}  desc={r[0]!r}')

conn.close()
