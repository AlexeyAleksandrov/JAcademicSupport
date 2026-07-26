import psycopg2, os, sys, json
if hasattr(sys.stdout, "reconfigure"): sys.stdout.reconfigure(encoding="utf-8")
from dotenv import load_dotenv
load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

print("═"*60)
print("  ТОП-20 CO-OCCURRENCE ПАР НАВЫКОВ")
print("═"*60)
cur.execute("""
    SELECT a.name, b.name, sd.co_occurrence_cnt
    FROM skill_dependency sd
    JOIN skill_canonical a ON a.id = sd.parent_id
    JOIN skill_canonical b ON b.id = sd.child_id
    ORDER BY sd.co_occurrence_cnt DESC LIMIT 20
""")
for a, b, cnt in cur.fetchall():
    print(f"  {a:<30} + {b:<30} = {cnt:>5} вакансий")

print("\n" + "═"*60)
print("  РАСПРЕДЕЛЕНИЕ ВАКАНСИЙ ПО ДОМЕНАМ")
print("═"*60)
cur.execute("""
    SELECT primary_domain, COUNT(*) AS cnt, ROUND(AVG(domain_score)*100,1) AS avg_score_pct
    FROM vacancy_domain
    GROUP BY primary_domain ORDER BY cnt DESC
""")
for domain, cnt, avg_score in cur.fetchall():
    print(f"  {domain or '(null)':<15} {cnt:>5} вакансий  (ср. уверенность {avg_score}%)")

print("\n" + "═"*60)
print("  ТОП-10 НАВЫКОВ ПО ОХВАТУ ВАКАНСИЙ")
print("═"*60)
cur.execute("""
    SELECT sc.name, sc.domain, sds.vacancy_count, ROUND(sds.pct_in_domain*100,1) AS pct
    FROM skill_domain_stats sds
    JOIN skill_canonical sc ON sc.id = sds.canonical_id
    ORDER BY sds.vacancy_count DESC LIMIT 10
""")
for name, domain, vc, pct in cur.fetchall():
    print(f"  {name:<30} [{domain or 'null':<12}] {vc:>5} вакансий ({pct}% в домене)")

print("\n" + "═"*60)
print("  ПРИМЕР: навыки связанные с Python (top_cooccurrences)")
print("═"*60)
cur.execute("""
    SELECT sc.name, sds.top_cooccurrences
    FROM skill_domain_stats sds
    JOIN skill_canonical sc ON sc.id = sds.canonical_id
    WHERE sc.name = 'Python' LIMIT 1
""")
row = cur.fetchone()
if row:
    print(f"  Python → top co-occurrence:")
    data = row[1] if isinstance(row[1], list) else json.loads(row[1])
    for item in data[:10]:
        print(f"    {item['name']:<30} {item['count']:>5} раз")

cur.close(); conn.close()
