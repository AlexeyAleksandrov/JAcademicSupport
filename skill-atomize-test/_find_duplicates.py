"""
_find_duplicates.py — поиск дублей в skill_canonical.
Выводит:
 1. Конкретные навыки по запросу пользователя
 2. Общий поиск: пары, где одно имя является подстрокой другого
"""
import psycopg2, os
from dotenv import load_dotenv; load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

def get_skill(name_pattern):
    cur.execute("""
        SELECT sc.id, sc.name, sc.domain, sc.tech_family,
               COUNT(DISTINCT wsc.work_skill_id) as refs
        FROM skill_canonical sc
        LEFT JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
        WHERE lower(sc.name) LIKE lower(%s)
        GROUP BY sc.id, sc.name, sc.domain, sc.tech_family
        ORDER BY refs DESC
    """, (f'%{name_pattern}%',))
    return cur.fetchall()

# ─── 1. Конкретные случаи из UI ─────────────────────────────────────────────
checks = [
    'pytest', 'unittest', 'Автоматизирован',
    'allure',
    'nunit', 'xunit',
    'solid',
    'event loop',
    'devops',
    'selenium ide',
    'Custom NUnit',
]
print('=== Конкретные навыки ===\n')
for pattern in checks:
    rows = get_skill(pattern)
    if rows:
        print(f'  [{pattern}]')
        for r in rows:
            print(f'    {r[4]:4}x  id={r[0]}  {r[1]!r}  domain={r[2]}  family={r[3]}')

# ─── 2. Общий поиск дублей: substring containment (в Python, не в SQL) ──────
print('\n\n=== Потенциальные дубли (substring containment, refs ≥ 1) ===\n')
cur.execute("""
    SELECT sc.id, sc.name,
           COUNT(DISTINCT wsc.work_skill_id) as refs
    FROM skill_canonical sc
    LEFT JOIN work_skill_canonical wsc ON wsc.canonical_id = sc.id
    GROUP BY sc.id, sc.name
    HAVING COUNT(DISTINCT wsc.work_skill_id) >= 1
    ORDER BY sc.name
""")
all_skills = cur.fetchall()   # (id, name, refs)
print(f'Всего навыков с ≥1 ref: {len(all_skills)}')

pairs = []
for i, (id_a, name_a, refs_a) in enumerate(all_skills):
    na = name_a.lower().strip()
    if len(na) < 4:
        continue
    for id_b, name_b, refs_b in all_skills[i+1:]:
        nb = name_b.lower().strip()
        if len(nb) < 4:
            continue
        if na in nb or nb in na:
            pairs.append((refs_a, name_a, id_a, refs_b, name_b, id_b))

pairs.sort(key=lambda x: -max(x[0], x[3]))
print(f'Найдено {len(pairs)} пар:\n')
for refs_a, name_a, _, refs_b, name_b, _ in pairs[:80]:
    print(f'  {refs_a:4}x {name_a!r:55s}  ←→  {refs_b:4}x {name_b!r}')

conn.close()
