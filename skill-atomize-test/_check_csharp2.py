import psycopg2, os
from dotenv import load_dotenv; load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

# C# canonical
cur.execute("SELECT id, name, domain, tech_family FROM skill_canonical WHERE name = 'C#'")
cs = cur.fetchone()
cs_id = cs[0]
print(f'C# canonical: id={cs_id}, domain={cs[2]}, tech_family={cs[3]}')

# wsc refs
cur.execute("SELECT COUNT(*) FROM work_skill_canonical WHERE canonical_id = %s", (cs_id,))
print(f'work_skill_canonical refs: {cur.fetchone()[0]}')

# skill_domain_stats for C#
cur.execute("SELECT * FROM skill_domain_stats WHERE canonical_id = %s", (cs_id,))
stats = cur.fetchall()
print(f'skill_domain_stats entries: {len(stats)}')
for s in stats:
    print(f'  {s}')

# Как C# оказывается в work_skill: сколько ws с description='C#' имеют wsc
cur.execute("""
    SELECT COUNT(DISTINCT ws.id) as total_ws,
           COUNT(DISTINCT wsc.work_skill_id) as linked_ws
    FROM work_skill ws
    LEFT JOIN work_skill_canonical wsc ON wsc.work_skill_id = ws.id AND wsc.canonical_id = %s
    WHERE ws.description ILIKE '%c#%' AND ws.description NOT ILIKE '%c#/%'
      AND ws.description NOT ILIKE '%c++%'
""", (cs_id,))
r = cur.fetchone()
print(f'\nwork_skill WHERE description ILIKE c#: total={r[0]}, linked to C# canonical={r[1]}')

# Что в skill_domain_stats для Languages domain
cur.execute("""
    SELECT sc.name, sds.vacancy_count
    FROM skill_domain_stats sds
    JOIN skill_canonical sc ON sc.id = sds.canonical_id
    WHERE sc.domain = 'LANGUAGES'
    ORDER BY sds.vacancy_count DESC LIMIT 20
""")
print('\nLanguages domain (skill_domain_stats), топ-20:')
for r in cur.fetchall():
    print(f'  {r[1]:5}  {r[0]}')

conn.close()
