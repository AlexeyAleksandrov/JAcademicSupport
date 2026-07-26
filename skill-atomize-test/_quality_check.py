"""
_quality_check.py — Выборочная проверка качества skill_canonical и work_skill_canonical.

Запуск:
    python _quality_check.py           # полная проверка
    python _quality_check.py --domain BACKEND  # только один домен
    python _quality_check.py --mappings 20     # показать N случайных work_skill→canonical
"""
import argparse
import os
import sys
import random
import psycopg2
from dotenv import load_dotenv

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

load_dotenv()
conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

parser = argparse.ArgumentParser()
parser.add_argument("--domain",   type=str, default=None)
parser.add_argument("--mappings", type=int, default=30)
parser.add_argument("--sample",   type=int, default=10, help="Навыков на домен для показа")
args = parser.parse_args()

SEP = "─" * 60

# ── 1. Потенциально проблемные записи ───────────────────────────────────────
print(f"\n{'═'*60}")
print("  ПОТЕНЦИАЛЬНЫЕ ПРОБЛЕМЫ")
print(f"{'═'*60}")

# Слишком длинные canonical-имена (>60 символов — скорее всего предложение)
cur.execute("""
    SELECT name, domain, LENGTH(name) AS len
    FROM skill_canonical
    WHERE LENGTH(name) > 60
    ORDER BY len DESC LIMIT 20
""")
long_names = cur.fetchall()
print(f"\n[1] Слишком длинные имена (>60 символов): {len(long_names)} найдено")
for name, domain, ln in long_names[:10]:
    print(f"    [{domain or 'null':12}] ({ln:3} chr) {name[:80]}")

# Имена содержащие «и», «или», «работа с» — признак нераздробленного ввода
cur.execute(r"""
    SELECT name, domain
    FROM skill_canonical
    WHERE name ~* '\y(работа с|знание|умение|навык|опыт работы)\y'
       OR name LIKE '%,%,%,%'
    LIMIT 20
""")
sentence_like = cur.fetchall()
print(f"\n[2] Похожие на предложения / нераздробленные: {len(sentence_like)} найдено")
for name, domain in sentence_like[:10]:
    print(f"    [{domain or 'null':12}] {name[:80]}")

# Дубли по normalized_name (не должно быть из-за UNIQUE, но на всякий случай)
cur.execute("""
    SELECT normalized_name, COUNT(*) AS cnt
    FROM skill_canonical
    GROUP BY normalized_name HAVING COUNT(*) > 1
    LIMIT 10
""")
dupes = cur.fetchall()
print(f"\n[3] Дубли по normalized_name: {len(dupes)} найдено")
for nm, cnt in dupes:
    print(f"    [{cnt}x] {nm}")

# ── 2. Выборка по доменам ────────────────────────────────────────────────────
print(f"\n{'═'*60}")
print("  ВЫБОРКА ПО ДОМЕНАМ")
print(f"{'═'*60}")

if args.domain:
    domains_to_show = [args.domain]
else:
    cur.execute("""
        SELECT COALESCE(domain,'(null)'), COUNT(*)
        FROM skill_canonical GROUP BY domain ORDER BY 2 DESC
    """)
    domains_to_show = [r[0] for r in cur.fetchall()]

for dom in domains_to_show:
    dom_filter = "IS NULL" if dom == "(null)" else f"= '{dom}'"
    cur.execute(f"""
        SELECT name FROM skill_canonical
        WHERE domain {dom_filter}
        ORDER BY RANDOM() LIMIT {args.sample}
    """)
    rows = [r[0] for r in cur.fetchall()]
    cur.execute(f"SELECT COUNT(*) FROM skill_canonical WHERE domain {dom_filter}")
    total = cur.fetchone()[0]
    print(f"\n  {dom} ({total} записей) — {args.sample} случайных:")
    for name in rows:
        print(f"    • {name}")

# ── 3. Случайные маппинги work_skill → canonical ────────────────────────────
print(f"\n{'═'*60}")
print(f"  {args.mappings} СЛУЧАЙНЫХ МАППИНГОВ work_skill → canonical")
print(f"{'═'*60}")

cur.execute(f"""
    SELECT ws.description, sc.name, sc.domain
    FROM work_skill_canonical wsc
    JOIN work_skill ws ON ws.id = wsc.work_skill_id
    JOIN skill_canonical sc ON sc.id = wsc.canonical_id
    ORDER BY RANDOM()
    LIMIT {args.mappings}
""")
mappings = cur.fetchall()
for ws_desc, sc_name, domain in mappings:
    arrow = "→"
    dom_str = f"[{domain}]" if domain else "[null]"
    # Помечаем потенциально подозрительные
    flag = " ⚠" if ws_desc.lower() == sc_name.lower() and len(ws_desc) > 40 else ""
    print(f"  {ws_desc[:45]:<45} {arrow} {sc_name:<30} {dom_str}{flag}")

# ── 4. Итоговая сводка ───────────────────────────────────────────────────────
print(f"\n{'═'*60}")
print("  ИТОГ")
print(f"{'═'*60}")
cur.execute("SELECT COUNT(*) FROM skill_canonical WHERE LENGTH(name) > 60")
n_long = cur.fetchone()[0]
cur.execute(r"SELECT COUNT(*) FROM skill_canonical WHERE name ~* '\y(работа с|знание|умение|навык)\y'")
n_sent = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM skill_canonical WHERE domain IS NULL")
n_null = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM skill_canonical")
n_total = cur.fetchone()[0]

print(f"  Всего canonical:          {n_total:>6}")
print(f"  Длинных имён (>60 chr):   {n_long:>6}  ({n_long*100//n_total}%)")
print(f"  Похожих на предложения:   {n_sent:>6}  ({n_sent*100//n_total}%)")
print(f"  Без домена (null):        {n_null:>6}  ({n_null*100//n_total}%)")

cur.close()
conn.close()
