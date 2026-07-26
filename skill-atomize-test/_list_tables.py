import psycopg2, os
from dotenv import load_dotenv
load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()
cur.execute("SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY 1")
print("Таблицы в БД:")
for r in cur.fetchall():
    print(" ", r[0])

# Найти join-таблицу vacancy <-> work_skill
print("\nСтолбцы таблиц содержащих 'vacancy':")
cur.execute("""
    SELECT table_name, column_name, data_type
    FROM information_schema.columns
    WHERE table_schema='public' AND table_name ILIKE '%vacancy%'
    ORDER BY table_name, ordinal_position
""")
for t, c, d in cur.fetchall():
    print(f"  {t}.{c} ({d})")
cur.close(); conn.close()
