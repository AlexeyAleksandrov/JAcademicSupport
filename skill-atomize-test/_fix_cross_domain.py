"""
Fix cross-domain tech_family contamination.
For each skill, if its tech_family is primarily associated with a different domain,
clear the tech_family (set to NULL) so it doesn't pollute domain inference.
"""
import os, sys
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
from dotenv import load_dotenv; load_dotenv()
import psycopg2

SAVE = "--save" in sys.argv

conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

# Find the "canonical" domain for each tech_family (majority vote)
cur.execute("""
    WITH family_domain_counts AS (
        SELECT tech_family, domain, COUNT(*) as n
        FROM skill_canonical
        WHERE tech_family IS NOT NULL AND domain IS NOT NULL
        GROUP BY tech_family, domain
    ),
    family_canonical_domain AS (
        SELECT DISTINCT ON (tech_family) tech_family, domain as canonical_domain, n
        FROM family_domain_counts
        ORDER BY tech_family, n DESC
    )
    SELECT sc.id, sc.name, sc.domain, sc.tech_family, fcd.canonical_domain
    FROM skill_canonical sc
    JOIN family_canonical_domain fcd ON sc.tech_family = fcd.tech_family
    WHERE sc.domain IS NOT NULL
      AND sc.domain != fcd.canonical_domain
      AND fcd.n >= 5   -- only reset if the family has a clear majority domain (>=5 skills)
    ORDER BY sc.tech_family, sc.domain, sc.name
""")
rows = cur.fetchall()

print(f"\nSkills with tech_family from wrong domain: {len(rows)}")
print(f"{'ID':>8}  {'Domain':<14}  {'Family':<22}  {'CanonDom':<14}  Name")
print("─" * 90)
for sid, name, dom, fam, canon_dom in rows:
    print(f"{sid:>8}  {dom:<14}  {fam:<22}  {canon_dom:<14}  {name}")

if SAVE and rows:
    ids = [r[0] for r in rows]
    cur.execute(
        "UPDATE skill_canonical SET tech_family = NULL WHERE id = ANY(%s)",
        (ids,)
    )
    conn.commit()
    print(f"\n✅ Cleared tech_family for {cur.rowcount} skills")
elif rows:
    print(f"\nℹ️  Run with --save to clear {len(rows)} wrong assignments")
else:
    print("✅ No cross-domain contamination found")

conn.close()
