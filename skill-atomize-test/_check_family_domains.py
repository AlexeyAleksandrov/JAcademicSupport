"""Check which families appear in multiple domains (cross-domain contamination)."""
import os
import sys
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

from dotenv import load_dotenv
load_dotenv()
import psycopg2

conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

# 1. Families that appear in more than one domain
cur.execute("""
    SELECT tech_family, array_agg(domain || ':' || cnt::text ORDER BY cnt DESC) as domains
    FROM (
        SELECT tech_family, domain, COUNT(*) as cnt
        FROM skill_canonical
        WHERE tech_family IS NOT NULL AND domain IS NOT NULL
        GROUP BY tech_family, domain
    ) sub
    GROUP BY tech_family
    HAVING COUNT(DISTINCT domain) > 1
    ORDER BY tech_family
""")
rows = cur.fetchall()
print(f"\n=== Families in multiple domains ({len(rows)}) ===")
for fam, domains in rows:
    print(f"  {fam:25s}  {', '.join(domains)}")

# 2. Families that are NOT their domain's majority (potential misassignments)
print("\n=== DATABASE skills with unexpected families ===")
cur.execute("""
    SELECT tech_family, name FROM skill_canonical
    WHERE domain='DATABASE'
      AND tech_family IN ('Системное ПО','Микроконтроллеры','Протоколы IoT','iOS','Android')
    ORDER BY tech_family, name
""")
for row in cur.fetchall():
    print(f"  {row[0]:20s}  {row[1]}")

# 3. Check findDomainsByTechFamilies logic
print("\n=== Системное ПО by domain ===")
cur.execute("""
    SELECT domain, COUNT(*) FROM skill_canonical
    WHERE tech_family='Системное ПО'
    GROUP BY domain ORDER BY COUNT(*) DESC
""")
for row in cur.fetchall():
    print(f"  {row[0]}: {row[1]}")

# 4. Check what families DATABASE domain has
print("\n=== DATABASE tech_family distribution (top 20) ===")
cur.execute("""
    SELECT tech_family, COUNT(*) as n FROM skill_canonical
    WHERE domain='DATABASE'
    GROUP BY tech_family ORDER BY n DESC LIMIT 20
""")
for row in cur.fetchall():
    print(f"  {str(row[0]):25s}: {row[1]}")

conn.close()
