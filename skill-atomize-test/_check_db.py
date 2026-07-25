import psycopg2, os
from dotenv import load_dotenv
load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

print("=" * 55)
print("  СТАТИСТИКА БД ПОСЛЕ ПРОГОНА")
print("=" * 55)

cur.execute("SELECT COUNT(*) FROM skill_canonical")
print(f"skill_canonical (всего):      {cur.fetchone()[0]:>7}")

cur.execute("SELECT domain_source, COUNT(*) FROM skill_canonical GROUP BY domain_source ORDER BY 2 DESC")
for src, cnt in cur.fetchall():
    print(f"  source={src:<10}          {cnt:>7}")

cur.execute("SELECT COUNT(*) FROM work_skill_canonical")
print(f"work_skill_canonical (всего): {cur.fetchone()[0]:>7}")

cur.execute("SELECT COUNT(DISTINCT work_skill_id) FROM work_skill_canonical")
print(f"  уникальных work_skill:      {cur.fetchone()[0]:>7}")

cur.execute("SELECT COUNT(*) FROM work_skill WHERE description IS NOT NULL AND TRIM(description)<>''")
total_ws = cur.fetchone()[0]
print(f"  всего work_skill в БД:      {total_ws:>7}")

cur.execute("""
    SELECT COUNT(DISTINCT ws.id) FROM work_skill ws
    WHERE NOT EXISTS (SELECT 1 FROM work_skill_canonical wsc WHERE wsc.work_skill_id = ws.id)
      AND ws.description IS NOT NULL AND TRIM(ws.description) <> ''
""")
unlinked = cur.fetchone()[0]
print(f"  без canonical-связи:        {unlinked:>7}  ({unlinked*100//total_ws}% от всех)")

print()
print("Топ-10 доменов в skill_canonical:")
cur.execute("""
    SELECT COALESCE(domain,'(null)'), COUNT(*)
    FROM skill_canonical GROUP BY domain ORDER BY 2 DESC LIMIT 10
""")
for domain, cnt in cur.fetchall():
    print(f"  {domain:<15} {cnt:>5}")

print()
print("Топ-5 самых частых canonical навыков:")
cur.execute("""
    SELECT sc.name, COUNT(wsc.work_skill_id) AS cnt
    FROM skill_canonical sc
    JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
    GROUP BY sc.name ORDER BY cnt DESC LIMIT 5
""")
for name, cnt in cur.fetchall():
    print(f"  {name:<35} {cnt:>5} упоминаний")

print()
print("Покрытие уникальных descriptions:")
cur.execute(
    "SELECT COUNT(DISTINCT TRIM(description)) FROM work_skill "
    "WHERE description IS NOT NULL AND LENGTH(TRIM(description)) > 0"
)
total_desc = cur.fetchone()[0]
print(f"  Уникальных descriptions в БД: {total_desc:>6}")

cur.execute("""
    SELECT COUNT(DISTINCT TRIM(ws.description))
    FROM work_skill ws
    WHERE NOT EXISTS (
        SELECT 1 FROM work_skill_canonical wsc WHERE wsc.work_skill_id = ws.id
    )
    AND ws.description IS NOT NULL AND LENGTH(TRIM(ws.description)) > 0
""")
remaining_desc = cur.fetchone()[0]
done_desc = total_desc - remaining_desc
print(f"  Обработано descriptions:      {done_desc:>6}  ({done_desc*100//total_desc}%)")
print(f"  Осталось необработанных:      {remaining_desc:>6}  ({remaining_desc*100//total_desc}%)")

cur.close(); conn.close()
print("=" * 55)
