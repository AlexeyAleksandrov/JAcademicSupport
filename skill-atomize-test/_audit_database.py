"""Audit DATABASE domain: show all skills grouped by tech_family."""
import os, sys
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
from dotenv import load_dotenv; load_dotenv()
import psycopg2

conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

# All DATABASE skills grouped by family
cur.execute("""
    SELECT tech_family, id, name
    FROM skill_canonical
    WHERE domain = 'DATABASE'
    ORDER BY tech_family NULLS LAST, name
""")
rows = cur.fetchall()

current_fam = "__NONE__"
for fam, sid, name in rows:
    if fam != current_fam:
        current_fam = fam
        cur2 = conn.cursor()
        cur2.execute(
            "SELECT COUNT(*) FROM skill_canonical WHERE domain='DATABASE' AND tech_family IS NOT DISTINCT FROM %s",
            (fam,)
        )
        cnt = cur2.fetchone()[0]
        print(f"\n{'─'*60}")
        print(f"  [{fam or 'NULL'}]  ({cnt} skills)")
        print(f"{'─'*60}")
    print(f"    {sid:>7}  {name}")

conn.close()
