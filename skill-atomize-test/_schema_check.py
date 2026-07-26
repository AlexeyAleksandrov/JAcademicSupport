import psycopg2, os, sys
if hasattr(sys.stdout, "reconfigure"): sys.stdout.reconfigure(encoding="utf-8")
from dotenv import load_dotenv
load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

tables = ['vacancy_skills', 'skill_dependency', 'skill_domain_stats', 'vacancy_domain']
for t in tables:
    cur.execute(f"""
        SELECT column_name, data_type, is_nullable
        FROM information_schema.columns
        WHERE table_schema='public' AND table_name='{t}'
        ORDER BY ordinal_position
    """)
    cols = cur.fetchall()
    cur.execute(f"SELECT COUNT(*) FROM {t}")
    cnt = cur.fetchone()[0]
    print(f"\n[{t}]  ({cnt} rows)")
    for col, dtype, nullable in cols:
        print(f"  {col:<35} {dtype:<25} {'NULL' if nullable=='YES' else 'NOT NULL'}")

cur.close(); conn.close()
