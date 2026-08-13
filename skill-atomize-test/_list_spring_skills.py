import psycopg2, os
from dotenv import load_dotenv; load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

cur.execute("""
    SELECT sc.id, sc.name, COUNT(DISTINCT wsc.work_skill_id) as refs
    FROM skill_canonical sc
    LEFT JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
    WHERE sc.name ILIKE 'Spring%'
       OR sc.name ILIKE 'Hibernate%'
       OR sc.name IN ('JPA', 'JDBC')
    GROUP BY sc.id, sc.name
    ORDER BY sc.name
""")
rows = cur.fetchall()
print(f'Всего Spring/Hibernate/JPA/JDBC навыков: {len(rows)}\n')
for r in rows:
    print(f'  {r[2]:4}  {r[1]}')

conn.close()
