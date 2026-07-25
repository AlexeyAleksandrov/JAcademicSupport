"""Выводит work_skill описания без canonical-связей (оставшиеся ~3%)."""
import psycopg2, os
from dotenv import load_dotenv
load_dotenv()

conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

cur.execute("""
    SELECT DISTINCT TRIM(ws.description)
    FROM work_skill ws
    WHERE NOT EXISTS (
        SELECT 1 FROM work_skill_canonical wsc WHERE wsc.work_skill_id = ws.id
    )
    AND ws.description IS NOT NULL AND LENGTH(TRIM(ws.description)) > 0
    ORDER BY 1
""")
rows = [r[0] for r in cur.fetchall()]
cur.close()
conn.close()

print(f"Всего без canonical-связи: {len(rows)}\n")
for i, desc in enumerate(rows, 1):
    print(f"{i:>4}. {desc}")
