import psycopg2, os, sys
if hasattr(sys.stdout,'reconfigure'): sys.stdout.reconfigure(encoding='utf-8')
from dotenv import load_dotenv; load_dotenv()
c = psycopg2.connect(os.getenv('DB_URL')); cur = c.cursor()
cur.execute("""
    SELECT sg.id, sg.description, COUNT(ws.id) as cnt
    FROM skills_group sg
    LEFT JOIN work_skill ws ON ws.skills_group_id = sg.id
    GROUP BY sg.id, sg.description
    ORDER BY cnt DESC
""")
for row in cur.fetchall():
    print(f"  [{row[0]:>3}] {str(row[1]):<30} {row[2]} work_skills")
c.close()
