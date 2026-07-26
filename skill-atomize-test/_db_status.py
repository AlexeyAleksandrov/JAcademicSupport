import psycopg2, os, sys
if hasattr(sys.stdout,'reconfigure'): sys.stdout.reconfigure(encoding='utf-8')
from dotenv import load_dotenv
load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()
tables = [
    'profession', 'profession_cluster', 'skills_group',
    'vacancy', 'vacancy_profession', 'vacancy_cluster_score',
    'skill_canonical', 'work_skill_canonical',
    'skill_dependency', 'vacancy_domain', 'skill_domain_stats'
]
print("Состояние таблиц:")
for t in tables:
    cur.execute(f"SELECT COUNT(*) FROM {t}")
    print(f"  {t:<30} {cur.fetchone()[0]:>8}")
cur.execute("SELECT code FROM profession LIMIT 5")
print("\nКоды профессий:", [r[0] for r in cur.fetchall()])
cur.execute("SELECT COUNT(*) FROM skills_group")
n = cur.fetchone()[0]
if n > 0:
    cur.execute("SELECT id, description FROM skills_group LIMIT 3")
    print("Кластеры (первые 3):", cur.fetchall())
cur.close(); conn.close()
