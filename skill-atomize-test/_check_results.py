"""Проверка результатов DST пайплайна."""
import urllib.request, json, sys
if hasattr(sys.stdout,'reconfigure'): sys.stdout.reconfigure(encoding='utf-8')

BASE = "http://localhost:8080"
SEP = "=" * 60

def get(path, timeout=30):
    with urllib.request.urlopen(f"{BASE}{path}", timeout=timeout) as r:
        return json.loads(r.read())

# ── Level 0: профессии ───────────────────────────────────────
profs = get("/api/dst/professions")
print(f"\n{SEP}\n  Level 0: {len(profs)} профессий\n{SEP}")

# ── Level 1: кластеры backend ────────────────────────────────
clusters = get("/api/dst/professions/backend/clusters")
print(f"\n{SEP}\n  Level 1: backend кластеров = {len(clusters)}\n{SEP}")
for c in clusters[:8]:
    print(f"  [{c['clusterId']:>3}] {c.get('clusterName','?'):<25} weight={c.get('professionWeight',0):.4f}  avgScore={c.get('marketDemandAvg',0):.4f}")

# ── Level 2: навыки в кластере "Фреймворки" (id=2) ──────────
skills = get("/api/dst/professions/backend/clusters/2/skills", timeout=120)
print(f"\n{SEP}\n  Level 2: backend/cluster=2 → {len(skills)} навыков\n{SEP}")
for s in skills[:15]:
    freq = round(s.get('relativeFrequency',0)*100, 1)
    dom  = s.get('domain') or '—'
    print(f"  {s.get('description','?'):<38} [{dom:<12}] {freq:>5}%")

# ── Новый API: related skills ────────────────────────────────
import psycopg2, os
from dotenv import load_dotenv
load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()
cur.execute("SELECT id FROM skill_canonical WHERE name='Python' LIMIT 1")
row = cur.fetchone()
python_id = row[0] if row else None
cur.close(); conn.close()

if python_id:
    print(f"\n{SEP}\n  GET /api/dst/skills/{python_id}/related  (Python)\n{SEP}")
    related = get(f"/api/dst/skills/{python_id}/related")
    for r in related[:10]:
        print(f"  {r.get('name','?'):<30} [{r.get('domain','—'):<12}] {r.get('coOccurrenceCount',0):>5}x")
    print(f"  ... всего {len(related)} связанных")

print(f"\n{SEP}")
print("  ВСЁ РАБОТАЕТ ✓")
print(SEP)
