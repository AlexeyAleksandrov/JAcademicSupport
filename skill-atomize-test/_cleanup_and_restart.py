"""Удаляет все llm-записи из skill_canonical (каскадно чистит work_skill_canonical)."""
import psycopg2, os
from dotenv import load_dotenv
load_dotenv()

conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

cur.execute("SELECT COUNT(*) FROM work_skill_canonical")
wsc_before = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM skill_canonical WHERE domain_source = 'llm'")
sc_before = cur.fetchone()[0]

print(f"До очистки: skill_canonical={sc_before}, work_skill_canonical={wsc_before}")

cur.execute("DELETE FROM skill_canonical WHERE domain_source = 'llm'")
conn.commit()

cur.execute("SELECT COUNT(*) FROM work_skill_canonical")
wsc_after = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM skill_canonical")
sc_after = cur.fetchone()[0]

print(f"После очистки: skill_canonical={sc_after}, work_skill_canonical={wsc_after}")
cur.close()
conn.close()
print("OK")
