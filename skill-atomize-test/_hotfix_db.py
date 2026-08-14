"""Clear wrong tech_family for specific DATABASE-domain skills that belong to SYSTEMS."""
import os, sys
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
from dotenv import load_dotenv; load_dotenv()
import psycopg2

conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

fixes = [
    ("iSCSI",    "DATABASE"),   # storage protocol → SYSTEMS
    ("CoreData", "DATABASE"),   # iOS framework misclassified
    ("HTTP",     "BACKEND"),    # HTTP in BACKEND with Системное ПО
    ("HTTPS",    "BACKEND"),    # same
    ("HAProxy",  "DEVOPS"),     # load balancer in DEVOPS with Системное ПО
    ("Nginx",    "DEVOPS"),     # web server in DEVOPS with Системное ПО
]

total = 0
for name, domain in fixes:
    cur.execute(
        "UPDATE skill_canonical SET tech_family = NULL WHERE name = %s AND domain = %s",
        (name, domain)
    )
    n = cur.rowcount
    total += n
    print(f"  {name:20s} ({domain}): cleared {n}")

conn.commit()
print(f"\nTotal cleared: {total}")
conn.close()
