"""Fix CoreData: move from DATABASE to MOBILE domain."""
import os, sys
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
from dotenv import load_dotenv; load_dotenv()
import psycopg2

conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()
cur.execute(
    "UPDATE skill_canonical SET domain='MOBILE', tech_family='iOS' WHERE name='CoreData' AND domain='DATABASE'"
)
print(f"CoreData moved to MOBILE/iOS: {cur.rowcount} rows")
conn.commit()
conn.close()
