"""
run_migrations.py — Применяет SQL-миграции к PostgreSQL через psycopg2.
Использование: python run_migrations.py
"""

import os
import sys
import psycopg2
from dotenv import load_dotenv

load_dotenv()

DB_URL = os.getenv("DB_URL", "")
if not DB_URL:
    sys.exit("[ERROR] DB_URL не задан в .env")

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

MIGRATIONS = [
    os.path.join(BASE_DIR, "db", "migration_dst_v1.sql"),
    os.path.join(BASE_DIR, "db", "migration_dst_v2.sql"),
]


def apply_migration(conn, path: str) -> None:
    sql = open(path, encoding="utf-8").read()
    # Убираем psql мета-команды вида \encoding, \set и т.п.
    filtered_lines = []
    for line in sql.splitlines():
        stripped = line.strip()
        if stripped.startswith("\\"):
            continue
        filtered_lines.append(line)
    clean_sql = "\n".join(filtered_lines)

    cur = conn.cursor()
    cur.execute(clean_sql)
    conn.commit()
    cur.close()
    print(f"[OK] Applied: {os.path.basename(path)}")


def main() -> None:
    try:
        conn = psycopg2.connect(DB_URL)
        print(f"[DB] Connected to {DB_URL.split('@')[-1]}")
    except Exception as e:
        sys.exit(f"[ERROR] Cannot connect to DB: {e}")

    for path in MIGRATIONS:
        if not os.path.exists(path):
            print(f"[SKIP] Not found: {path}")
            continue
        try:
            apply_migration(conn, path)
        except psycopg2.Error as e:
            conn.rollback()
            print(f"[ERROR] {os.path.basename(path)}: {e}")
            if input("Continue with next migration? (y/N): ").strip().lower() != "y":
                break

    conn.close()
    print("[DONE]")


if __name__ == "__main__":
    main()
