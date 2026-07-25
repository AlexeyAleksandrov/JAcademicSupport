"""
fix_domains.py — Классифицирует домены для skill_canonical записей где domain IS NULL.

Использование:
    python fix_domains.py                     # обработать все null-домены
    python fix_domains.py --dry-run           # показать что будет, без записи в БД
    python fix_domains.py --batch-size 20     # размер батча (по умолч. 15)
    python fix_domains.py --model GigaChat-Max
"""

import argparse
import json
import os
import sys
import time
import uuid

import requests
import urllib3
from dotenv import load_dotenv

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
load_dotenv()

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

# ─── Конфиг ─────────────────────────────────────────────────────────────────

GIGACHAT_TOKEN = os.getenv("GIGACHAT_API_TOKEN", "")
DB_URL         = os.getenv("DB_URL", "")
SCOPE          = os.getenv("GIGACHAT_SCOPE", "GIGACHAT_API_PERS")
MODEL          = os.getenv("GIGACHAT_MODEL", "GigaChat-Pro")
API_URL        = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions"
AUTH_URL       = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth"

VALID_DOMAINS = frozenset({
    'BACKEND', 'FRONTEND', 'AI_ML', 'DATA_SCIENCE', 'DEVOPS',
    'DATABASE', 'CLOUD', 'SECURITY', 'TESTING', 'MOBILE',
    '1C', 'IOT', 'SYSTEMS', 'GENERAL',
})

SYSTEM_PROMPT = """Ты классифицируешь IT-навыки по доменам.

Домены:
- BACKEND    — серверная разработка: Spring, FastAPI, Node.js, gRPC, и т.п.
- FRONTEND   — клиентская разработка: React, Vue, Angular, CSS, HTML и т.п.
- AI_ML      — машинное обучение и AI: TensorFlow, PyTorch, LLM, OpenAI API и т.п.
- DATA_SCIENCE — аналитика данных: Pandas, Spark, Tableau, Airflow, Kafka и т.п.
- DEVOPS     — CI/CD, инфраструктура: Docker, Kubernetes, Jenkins, Ansible и т.п.
- DATABASE   — БД и хранилища: PostgreSQL, MongoDB, Redis, Elasticsearch и т.п.
- CLOUD      — облачные платформы: AWS, GCP, Azure, Yandex Cloud и т.п.
- SECURITY   — ИБ: OAuth, LDAP, SIEM, пентест, криптография и т.п.
- TESTING    — тестирование: Selenium, JUnit, Postman, нагрузочное тестирование и т.п.
- MOBILE     — мобильная разработка: Android, iOS, Flutter, React Native и т.п.
- 1C         — платформа 1С и все её конфигурации
- IOT        — встраиваемые системы, IoT, SCADA, PLC и т.п.
- SYSTEMS    — системное программирование: C, C++, Linux kernel, POSIX и т.п.
- GENERAL    — языки и инструменты широкого применения: Python, Java, .NET, Git, SQL, Agile и т.п.

Правила:
1. Отвечай ТОЛЬКО валидным JSON-объектом вида {"навык": "ДОМЕН", ...}
2. Если навык явно не является IT-навыком — верни null как значение
3. Python, Java, .NET, Go, C#, JavaScript, TypeScript → GENERAL
4. SQL → DATABASE, если это именно язык запросов, но PostgreSQL/MySQL/Oracle → DATABASE
5. Не добавляй пояснений, только JSON

Пример:
Вход: ["FastAPI", "Pandas", "Docker", "1С: ЗУП", "чувство юмора"]
Ответ: {"FastAPI": "BACKEND", "Pandas": "DATA_SCIENCE", "Docker": "DEVOPS", "1С: ЗУП": "1C", "чувство юмора": null}
"""

# ─── GigaChat auth ──────────────────────────────────────────────────────────

_cached_token: str = ""
_token_expires_at_ms: int = 0


def get_access_token() -> str:
    global _cached_token, _token_expires_at_ms
    now_ms = int(time.time() * 1000)
    if _cached_token and now_ms < _token_expires_at_ms - 10_000:
        return _cached_token
    if not GIGACHAT_TOKEN:
        sys.exit("[ERROR] GIGACHAT_API_TOKEN не задан в .env")
    resp = requests.post(
        AUTH_URL,
        headers={
            "Content-Type": "application/x-www-form-urlencoded",
            "Accept": "application/json",
            "RqUID": str(uuid.uuid4()),
            "Authorization": f"Basic {GIGACHAT_TOKEN}",
        },
        data=f"scope={SCOPE}",
        verify=False,
        timeout=15,
    )
    resp.raise_for_status()
    data = resp.json()
    _cached_token = data["access_token"]
    _token_expires_at_ms = int(data.get("expires_at", now_ms + 1_800_000))
    print(f"[AUTH] Токен GigaChat получен (~{(_token_expires_at_ms - now_ms)//60000} мин)")
    return _cached_token


def call_gigachat(skill_names: list[str], max_retries: int = 6) -> dict[str, str | None]:
    """Отправляет список навыков, возвращает {name: domain_or_None}."""
    user_message = json.dumps(skill_names, ensure_ascii=False)
    for attempt in range(max_retries):
        token = get_access_token()
        resp = requests.post(
            API_URL,
            headers={
                "Content-Type": "application/json",
                "Accept": "application/json",
                "Authorization": f"Bearer {token}",
            },
            json={
                "model": MODEL,
                "messages": [
                    {"role": "system", "content": SYSTEM_PROMPT},
                    {"role": "user",   "content": user_message},
                ],
                "temperature": 0.01,
                "top_p": 0.01,
                "max_tokens": 2048,
            },
            verify=False,
            timeout=90,
        )
        if resp.status_code == 429:
            wait = [5, 15, 30, 60, 120, 120][min(attempt, 5)]
            print(f" [429, ждём {wait}s]", end="", flush=True)
            time.sleep(wait)
            continue
        resp.raise_for_status()
        raw = resp.json()["choices"][0]["message"]["content"]
        try:
            # Вырезаем JSON из ответа
            start = raw.index("{")
            end   = raw.rindex("}") + 1
            result = json.loads(raw[start:end])
            return result
        except (ValueError, json.JSONDecodeError):
            if attempt < max_retries - 1:
                print(f" [parse-retry]", end="", flush=True)
                continue
            return {}
    return {}


# ─── main ────────────────────────────────────────────────────────────────────

def main() -> None:
    parser = argparse.ArgumentParser(description="Классифицирует домены для skill_canonical WHERE domain IS NULL")
    parser.add_argument("--batch-size", type=int,   default=15,    help="Навыков в одном запросе к GigaChat (по умолч. 15)")
    parser.add_argument("--delay",      type=float, default=1.0,   help="Задержка между батчами в секундах (по умолч. 1)")
    parser.add_argument("--model",      type=str,   default=None,  help="Модель GigaChat (по умолч. из .env)")
    parser.add_argument("--dry-run",    action="store_true",       help="Показать результаты без записи в БД")
    args = parser.parse_args()

    global MODEL
    if args.model:
        MODEL = args.model

    if not DB_URL:
        sys.exit("[ERROR] DB_URL не задан в .env")

    import psycopg2
    conn = psycopg2.connect(DB_URL)
    cur  = conn.cursor()

    # Загружаем все skill_canonical с domain IS NULL
    cur.execute("""
        SELECT id, name FROM skill_canonical
        WHERE domain IS NULL
        ORDER BY name
    """)
    rows = cur.fetchall()
    print(f"[DB] Найдено {len(rows)} записей с domain=NULL (модель: {MODEL})")

    if not rows:
        print("[OK] Нечего исправлять.")
        cur.close(); conn.close()
        return

    batch_size = args.batch_size
    n_batches  = (len(rows) + batch_size - 1) // batch_size
    print(f"[INPUT] {len(rows)} навыков → {n_batches} батч(а) по {batch_size}\n")

    updated = 0
    set_null = 0
    total_elapsed = 0.0

    for b_idx in range(n_batches):
        batch = rows[b_idx * batch_size : (b_idx + 1) * batch_size]
        ids   = [r[0] for r in batch]
        names = [r[1] for r in batch]

        print(f"[BATCH {b_idx+1}/{n_batches}] {len(batch)} навыков...", end=" ", flush=True)
        t0 = time.time()

        try:
            result = call_gigachat(names)
        except Exception as e:
            print(f"[ERROR] {e}")
            continue

        elapsed = time.time() - t0
        total_elapsed += elapsed
        print(f"{elapsed:.1f}с", end="")

        # Применяем результаты
        batch_updated = 0
        for sk_id, sk_name in zip(ids, names):
            raw_domain = result.get(sk_name)
            domain = raw_domain if raw_domain in VALID_DOMAINS else None

            if args.dry_run:
                status = f"→ {domain}" if domain else "→ (не IT-навык)"
                print(f"\n  {sk_name}: {status}", end="")
            else:
                if domain:
                    cur.execute(
                        "UPDATE skill_canonical SET domain=%s, domain_source='llm' WHERE id=%s",
                        (domain, sk_id)
                    )
                    batch_updated += 1
                    updated += 1
                else:
                    set_null += 1

        if not args.dry_run:
            conn.commit()
            print(f"  [обновлено: {batch_updated}]")
        else:
            print()

        if args.delay > 0 and b_idx < n_batches - 1:
            time.sleep(args.delay)

    cur.close()
    conn.close()

    print(f"\n[ИТОГ] Время: {total_elapsed:.1f}с")
    if args.dry_run:
        print("[DRY-RUN] Изменения не сохранены")
    else:
        print(f"[ИТОГ] Домен назначен: {updated} | Остались null (не IT): {set_null}")


if __name__ == "__main__":
    main()
