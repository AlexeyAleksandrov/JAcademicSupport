import psycopg2, os
from dotenv import load_dotenv; load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

print('=== Различные значения tech_family в БД ===')
cur.execute("""
    SELECT tech_family, COUNT(*) as cnt
    FROM skill_canonical
    GROUP BY tech_family
    ORDER BY cnt DESC
    LIMIT 30
""")
for r in cur.fetchall():
    print(f'  {r[1]:5}  {r[0]!r}')

print('\n=== Java, Kotlin, C#, PHP - их tech_family ===')
cur.execute("""
    SELECT name, tech_family, domain
    FROM skill_canonical
    WHERE name IN ('Java', 'Kotlin', 'C#', 'PHP', 'Python', 'Go', 'Swift', '.NET', 'C++', 'Ruby')
    ORDER BY name
""")
for r in cur.fetchall():
    print(f'  {r[0]:12}  tech_family={r[1]!r:20}  domain={r[2]}')

conn.close()
