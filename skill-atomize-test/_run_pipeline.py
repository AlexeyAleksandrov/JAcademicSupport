"""Запускает DST-пайплайн и проверяет состояние данных."""
import urllib.request, json, sys, time
import psycopg2, os
from dotenv import load_dotenv

if hasattr(sys.stdout, 'reconfigure'): sys.stdout.reconfigure(encoding='utf-8')
load_dotenv()

BASE = "http://localhost:8080"
SEP = "=" * 55

def post(path, timeout=300):
    req = urllib.request.Request(f"{BASE}{path}", method="POST", data=b"",
                                  headers={"Content-Type": "application/json"})
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            return r.status, json.loads(r.read())
    except urllib.error.HTTPError as e:
        return e.code, json.loads(e.read())
    except Exception as e:
        return 0, {"error": str(e)}

def get(path):
    try:
        with urllib.request.urlopen(f"{BASE}{path}", timeout=10) as r:
            return r.status, json.loads(r.read())
    except Exception as e:
        return 0, {"error": str(e)}

# ── Проверка состояния БД ────────────────────────────────────
print(f"\n{SEP}\n  СОСТОЯНИЕ БД\n{SEP}")
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()
tables = ['skills_group', 'profession', 'vacancy_profession',
          'profession_cluster', 'vacancy_cluster_score']
for t in tables:
    cur.execute(f"SELECT COUNT(*) FROM {t}")
    print(f"  {t:<30} {cur.fetchone()[0]:>8}")

cur.execute("SELECT COUNT(*) FROM work_skill WHERE skills_group_id IS NOT NULL")
ws_with_group = cur.fetchone()[0]
cur.execute("SELECT COUNT(*) FROM work_skill")
ws_total = cur.fetchone()[0]
print(f"\n  work_skill с group:        {ws_with_group:>8} / {ws_total}")
cur.close(); conn.close()

# ── Проверяем сервер ─────────────────────────────────────────
print(f"\n{SEP}\n  ПРОВЕРКА СЕРВЕРА\n{SEP}")
code, data = get("/api/dst/professions")
if code == 200:
    print(f"  Сервер OK, профессий: {len(data)}")
    for p in data[:3]:
        print(f"    [{p['code']}] {p['name']}")
else:
    print(f"  Сервер недоступен: {data}")
    sys.exit(1)

# ── Запуск шагов пайплайна ───────────────────────────────────
print(f"\n{SEP}\n  ЗАПУСК ПАЙПЛАЙНА\n{SEP}")

import argparse
parser = argparse.ArgumentParser()
parser.add_argument("--only", choices=["classify","weights","scores","weights-scores"], default=None,
                    help="Запустить только конкретный шаг(и)")
cli = parser.parse_args()

all_steps = [
    ("classify-professions",      "POST /api/admin/dst/classify-professions"),
    ("compute-profession-weights", "POST /api/admin/dst/compute-profession-weights"),
    ("compute-cluster-scores",    "POST /api/admin/dst/compute-cluster-scores"),
]
if cli.only == "classify":
    steps = [all_steps[0]]
elif cli.only == "weights":
    steps = [all_steps[1]]
elif cli.only == "scores":
    steps = [all_steps[2]]
elif cli.only == "weights-scores":
    steps = [all_steps[1], all_steps[2]]
else:
    steps = all_steps

for step_id, desc in steps:
    print(f"\n  {desc}")
    t0 = time.time()
    code, result = post(f"/api/admin/dst/{step_id}", timeout=300)
    elapsed = time.time() - t0
    print(f"  HTTP {code}  ({elapsed:.1f}с)")
    if isinstance(result, dict):
        for k, v in result.items():
            print(f"    {k}: {v}")
    else:
        print(f"  {result}")

# ── Проверяем результат ──────────────────────────────────────
print(f"\n{SEP}\n  ПРОВЕРКА РЕЗУЛЬТАТОВ\n{SEP}")
code, clusters = get("/api/dst/professions/backend/clusters")
print(f"  backend кластеров: {len(clusters) if isinstance(clusters, list) else 'ошибка'}")
if isinstance(clusters, list):
    for c in clusters[:5]:
        print(f"    [{c.get('clusterId')}] {c.get('clusterName','?')[:40]}")

print(f"\n{SEP}")
