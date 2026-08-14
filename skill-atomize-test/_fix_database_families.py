"""
Fix DATABASE domain: correct wrong tech_family and domain assignments.
Run with --save to apply changes.
"""
import os, sys
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
from dotenv import load_dotenv; load_dotenv()
import psycopg2

SAVE = "--save" in sys.argv

# (id, new_domain, new_tech_family, reason)
FIXES = [
    # ── Message Brokers не относятся к DATABASE ──────────────────────
    (37825, "BACKEND",   "Message Brokers", "Artemis MQ — брокер сообщений"),
    (36876, "BACKEND",   "Message Brokers", "Message Queue — брокер сообщений"),

    # ── eBS — блочное хранилище AWS, не СУБД ─────────────────────────
    (38435, "CLOUD",     "AWS",             "eBS — блочное хранилище AWS"),

    # ── MS Dynamics AX, HP StorageWorks — не IT-база данных ──────────
    (44653, "GENERAL",   None,              "MS Dynamics AX — ERP-система"),
    (51263, "SYSTEMS",   None,              "HP StorageWorks — аппаратное хранилище"),

    # ── Flyway/Liquibase — инструменты миграции БД ────────────────────
    (44407, "DATABASE",  "Миграции БД",     "Liquibase — миграции схем БД"),
    (44408, "DATABASE",  "Миграции БД",     "Flyway — миграции схем БД"),

    # ── Vector DB — отдельный класс СУБД, не NLP ─────────────────────
    (34631, "DATABASE",  "Vector DB",       "Chroma — векторная БД"),
    (33795, "DATABASE",  "Vector DB",       "Qdrant — векторная БД"),
    (53007, "DATABASE",  "Vector DB",       "Vector Database — векторная БД"),
    (37479, "DATABASE",  "Vector DB",       "Weaviate — векторная БД"),

    # ── PHP ORM/инструменты → правильное семейство внутри DATABASE ───
    (47765, "DATABASE",  "ORM",             "Eloquent — PHP ORM"),
    (44083, "DATABASE",  "Реляционные",     "phpMyAdmin — инструмент MySQL"),

    # ── ACID — принцип БД, не Архитектура ────────────────────────────
    (52068, "DATABASE",  "Реляционные",     "ACID — принцип транзакционности"),

    # ── AWS managed databases → правильные семейства ─────────────────
    (58203, "DATABASE",  "NoSQL",           "DynamoDB — NoSQL БД AWS"),
    (33687, "DATABASE",  "Реляционные",     "RDS — managed relational DB"),
    (49566, "DATABASE",  "BI/Analytics",    "Redshift — data warehouse AWS"),

    # ── GCP databases → правильные семейства ─────────────────────────
    (39118, "DATABASE",  "Реляционные",     "Cloud SQL — managed relational DB"),
    (37407, "DATABASE",  "BI/Analytics",    "BigQuery — data warehouse GCP"),
    (34500, "DATABASE",  "NoSQL",           "Firestore — NoSQL БД GCP"),
]

conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

print(f"\n{'ID':>7}  {'Имя':<28}  {'Домен':<12}  {'Семейство':<20}  Причина")
print("─" * 100)

total = 0
for sid, new_domain, new_family, reason in FIXES:
    cur.execute("SELECT name, domain, tech_family FROM skill_canonical WHERE id=%s", (sid,))
    row = cur.fetchone()
    if not row:
        print(f"  {sid}: NOT FOUND")
        continue
    name, old_domain, old_family = row
    domain_str  = f"{old_domain} → {new_domain}" if old_domain != new_domain else old_domain
    family_str  = f"{old_family} → {new_family}" if old_family != new_family else str(new_family)
    print(f"{sid:>7}  {name:<28}  {domain_str:<22}  {family_str:<30}  {reason}")
    total += 1

print(f"\nTotal: {total} fixes")

if SAVE:
    for sid, new_domain, new_family, reason in FIXES:
        cur.execute(
            "UPDATE skill_canonical SET domain=%s, tech_family=%s WHERE id=%s",
            (new_domain, new_family, sid)
        )
    conn.commit()
    print(f"\n✅ Applied {total} fixes")
else:
    print("\nℹ️  Run with --save to apply")

conn.close()
