"""Откат: сбрасывает tech_family = NULL для всех записей, затем
предлагает перезапустить fix_tech_family.py для восстановления
правил-базированных назначений.
Запуск: python _rollback_families.py [--save]
"""
import os, sys, argparse
import psycopg2
from dotenv import load_dotenv

load_dotenv()
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

parser = argparse.ArgumentParser()
parser.add_argument("--save", action="store_true", help="Применить откат")
args = parser.parse_args()

conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

cur.execute("SELECT COUNT(*) FROM skill_canonical WHERE tech_family IS NOT NULL")
count_before = cur.fetchone()[0]
print(f"Записей с tech_family: {count_before}")

if not args.save:
    print("\n[DRY-RUN] Запустите с --save для применения отката")
else:
    cur.execute("UPDATE skill_canonical SET tech_family = NULL")
    conn.commit()
    cur.execute("SELECT COUNT(*) FROM skill_canonical WHERE tech_family IS NOT NULL")
    count_after = cur.fetchone()[0]
    print(f"Сброшено: {count_before - count_after} записей")
    print(f"Осталось с tech_family: {count_after}")
    print()
    print("Теперь запустите fix_tech_family.py чтобы восстановить правило-базированные назначения:")
    print("  python fix_tech_family.py --save")

conn.close()
