import psycopg2, os
from dotenv import load_dotenv
load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

# Полный список таблиц, ссылающихся на skill_canonical через pg_constraint
cur.execute("""
    SELECT
        ref.relname  AS referencing_table,
        att.attname  AS referencing_col,
        con.conname  AS constraint_name
    FROM pg_constraint con
    JOIN pg_class ref ON ref.oid = con.conrelid
    JOIN pg_class trg ON trg.oid = con.confrelid
    JOIN pg_attribute att ON att.attrelid = con.conrelid
                          AND att.attnum = ANY(con.conkey)
    WHERE con.contype = 'f'
      AND trg.relname = 'skill_canonical'
    ORDER BY ref.relname
""")
print('Все таблицы с FK → skill_canonical:')
for r in cur.fetchall():
    print(' ', r)

cur.execute("SELECT COUNT(*) FROM skill_dependency")
print('\nskill_dependency rows:', cur.fetchone()[0])
cur.execute("SELECT COUNT(*) FROM skill_domain_stats")
print('skill_domain_stats rows:', cur.fetchone()[0])

conn.close()
