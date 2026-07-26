"""Быстрая проверка новых DST API эндпоинтов."""
import sys, json
import urllib.request
import urllib.error

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

BASE = "http://localhost:8080"

def get(path):
    try:
        with urllib.request.urlopen(f"{BASE}{path}", timeout=10) as r:
            return json.loads(r.read())
    except urllib.error.HTTPError as e:
        return {"http_error": e.code, "reason": e.reason}
    except Exception as e:
        return {"error": str(e)}

SEP = "═" * 60

# ── Professions ──────────────────────────────────────────────
print(f"\n{SEP}\n  GET /api/dst/professions\n{SEP}")
profs = get("/api/dst/professions")
if isinstance(profs, list):
    for p in profs[:5]:
        print(f"  [{p.get('code','?')}] {p.get('name','?')}")
    if len(profs) > 5:
        print(f"  ... и ещё {len(profs)-5}")
else:
    print(profs)

# ── normalize-skills (должен вернуть 410) ────────────────────
print(f"\n{SEP}\n  POST /api/admin/dst/normalize-skills  (ожидаем 410)\n{SEP}")
try:
    req = urllib.request.Request(f"{BASE}/api/admin/dst/normalize-skills", method="POST")
    with urllib.request.urlopen(req, timeout=10) as r:
        print(f"  HTTP {r.status}: {r.read().decode()}")
except urllib.error.HTTPError as e:
    body = e.read().decode()
    print(f"  HTTP {e.code} Gone ✓")
    try:
        data = json.loads(body)
        print(f"  message: {data.get('message','')[:80]}")
    except Exception:
        print(f"  body: {body[:80]}")

# ── Skills related ────────────────────────────────────────────
print(f"\n{SEP}\n  GET /api/dst/skills/1/related  (Python's canonical ID)\n{SEP}")
import psycopg2, os
from dotenv import load_dotenv
load_dotenv()
conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()
cur.execute("SELECT id FROM skill_canonical WHERE name='Python' LIMIT 1")
row = cur.fetchone()
python_id = row[0] if row else 1
cur.close(); conn.close()

print(f"  Python canonical_id = {python_id}")
related = get(f"/api/dst/skills/{python_id}/related")
if isinstance(related, list):
    print(f"  Связанных навыков: {len(related)}")
    for r in related[:10]:
        print(f"    {r.get('name','?'):<30} [{r.get('domain','?'):<12}] {r.get('coOccurrenceCount',0):>5} раз")
else:
    print(related)

# ── Vacancy domain ────────────────────────────────────────────
print(f"\n{SEP}\n  GET /api/dst/vacancies/{{}}/domain  (первая вакансия с доменом)\n{SEP}")
conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()
cur.execute("SELECT vacancy_id, primary_domain, domain_score FROM vacancy_domain ORDER BY domain_score DESC LIMIT 1")
row = cur.fetchone()
cur.close(); conn.close()

if row:
    vac_id, domain, score = row
    print(f"  Тест вакансии id={vac_id} (ожидаем домен={domain}, score={score})")
    result = get(f"/api/dst/vacancies/{vac_id}/domain")
    print(f"  Ответ: {result}")
else:
    print("  Нет данных в vacancy_domain")

# ── Level 2 skills (spot check) ──────────────────────────────
print(f"\n{SEP}\n  GET /api/dst/professions (берём первую для Level 2)\n{SEP}")
if isinstance(profs, list) and profs:
    prof_code = profs[0].get('code')
    clusters = get(f"/api/dst/professions/{prof_code}/clusters")
    if isinstance(clusters, list) and clusters:
        cluster_id = clusters[0].get('clusterId')
        print(f"  Профессия: {prof_code}, кластер: {cluster_id}")
        skills = get(f"/api/dst/professions/{prof_code}/clusters/{cluster_id}/skills")
        if isinstance(skills, list):
            print(f"  Level 2 вернул {len(skills)} навыков (через work_skill_canonical):")
            for s in skills[:8]:
                freq = round(s.get('relativeFrequency',0)*100, 1)
                dom  = s.get('domain') or '—'
                print(f"    {s.get('description','?'):<35} [{dom:<12}] {freq:>5}%")
        else:
            print(f"  Ошибка: {skills}")
    else:
        print(f"  Нет кластеров для {prof_code}")

print(f"\n{SEP}")
print("  ГОТОВО")
print(SEP)
