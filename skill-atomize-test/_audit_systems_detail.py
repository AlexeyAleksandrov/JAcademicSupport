"""Show SYSTEMS wrong-family skills + TESTING family breakdown."""
import os, sys
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
from dotenv import load_dotenv; load_dotenv()
import psycopg2

conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

SYSTEMS_WRONG = {"Cloud", "Message Brokers", "Altera Max", "AMD/Xilinx Kintex",
                 "3D/Графика", "3PAR StoreServ", "iOS"}

print("=== SYSTEMS: wrong families ===")
for fam in sorted(SYSTEMS_WRONG):
    cur.execute(
        "SELECT id, name FROM skill_canonical WHERE domain='SYSTEMS' AND tech_family IS NOT DISTINCT FROM %s ORDER BY name",
        (fam,)
    )
    rows = cur.fetchall()
    if rows:
        print(f"\n  [{fam}]")
        for sid, name in rows:
            print(f"    {sid:>7}  {name}")

print("\n\n=== TESTING: all families ===")
cur.execute("""
    SELECT tech_family, COUNT(*) as n FROM skill_canonical
    WHERE domain='TESTING' GROUP BY tech_family ORDER BY n DESC
""")
for fam, n in cur.fetchall():
    print(f"  {n:>5}  {str(fam)}")

conn.close()
