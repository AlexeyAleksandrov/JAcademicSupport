"""
test_atomize.py — Итеративная отладка промпта атомизации навыков IT-вакансий.

Использование:
    python test_atomize.py                        # строки из PostgreSQL (GigaChat)
    python test_atomize.py --no-db                # захардкоженные примеры
    python test_atomize.py --local                # локальная модель через Ollama
    python test_atomize.py --local --no-db        # Ollama + без БД
    python test_atomize.py --local-model qwen2.5:7b  # явно указать модель Ollama
    python test_atomize.py --batch-size 10        # строк на один запрос (10 по умолчанию, 25 = быстрее но риск сдвига)
    python test_atomize.py --limit 30 --raw       # лимит из БД + сырые ответы
    python test_atomize.py --all-skills --limit 500 --batch-size 25 --save  # полный прогон + сохранение

Конфигурация через .env:
    GIGACHAT_API_TOKEN  — Base64-токен из кабинета Сбер
    DB_URL              — postgresql://user:pass@host:port/dbname
    GIGACHAT_MODEL      — модель GigaChat (по умолч. GigaChat)
    OLLAMA_URL          — URL Ollama (по умолч. http://localhost:11434)
    OLLAMA_MODEL        — модель Ollama (по умолч. qwen2.5:7b)
"""

import argparse
import json
import os
import re
import sys
import time
import uuid

import requests
import urllib3
from dotenv import load_dotenv

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
load_dotenv()

# Windows cp1251 fix — force UTF-8 output
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

# ─── Настройки ─────────────────────────────────────────────────────────────────

GIGACHAT_TOKEN = os.getenv("GIGACHAT_API_TOKEN", "")
DB_URL         = os.getenv("DB_URL", "")
OAUTH_URL      = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth"
API_URL        = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions"
SCOPE          = os.getenv("GIGACHAT_SCOPE", "GIGACHAT_API_PERS")
MODEL          = os.getenv("GIGACHAT_MODEL", "GigaChat")
OLLAMA_URL        = os.getenv("OLLAMA_URL", "http://localhost:11434")
OLLAMA_MODEL      = os.getenv("OLLAMA_MODEL", "qwen2.5:7b")
OLLAMA_GPU_LAYERS = int(os.getenv("OLLAMA_GPU_LAYERS", "-1"))  # -1 = Ollama решает сам

# ─── OAuth-токен (кэш) ─────────────────────────────────────────────────────────

_cached_token: str | None = None
_token_expires_at_ms: int = 0


def get_access_token() -> str:
    global _cached_token, _token_expires_at_ms
    now_ms = int(time.time() * 1000)
    if _cached_token and now_ms < _token_expires_at_ms - 10_000:
        return _cached_token

    if not GIGACHAT_TOKEN:
        sys.exit("[ERROR] GIGACHAT_API_TOKEN не задан в .env")

    resp = requests.post(
        OAUTH_URL,
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
    print(f"[AUTH] Получен новый токен GigaChat (истекает через ~{(_token_expires_at_ms - now_ms) // 60000} мин)")
    return _cached_token


# ─── Вызов GigaChat ─────────────────────────────────────────────────────────────

def call_gigachat(system_prompt: str, user_message: str, max_retries: int = 5) -> str:
    for attempt in range(max_retries):
        token = get_access_token()
        payload = {
            "model": MODEL,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user",   "content": user_message},
            ],
            "temperature": 0.01,
            "top_p": 0.01,
            "max_tokens": 4096,
            "profanity_check": False,
        }
        resp = requests.post(
            API_URL,
            headers={
                "Content-Type": "application/json",
                "Accept": "application/json",
                "Authorization": f"Bearer {token}",
            },
            json=payload,
            verify=False,
            timeout=90,
        )
        if resp.status_code == 429:
            wait = 2 ** attempt * 3  # 3, 6, 12, 24, 48 с
            print(f" [429 rate limit, ждём {wait}s...]  ", end="", flush=True)
            time.sleep(wait)
            continue
        resp.raise_for_status()
        return resp.json()["choices"][0]["message"]["content"]
    raise RuntimeError(f"GigaChat 429 после {max_retries} попыток")


# ─── Вызов Ollama ──────────────────────────────────────────────────────────────

def call_ollama(system_prompt: str, user_message: str, model: str) -> str:
    options: dict = {
        "temperature": 0.01,
        "top_p": 0.01,
        "num_predict": 4096,
    }
    if OLLAMA_GPU_LAYERS >= 0:
        options["num_gpu"] = OLLAMA_GPU_LAYERS

    payload = {
        "model": model,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user",   "content": user_message},
        ],
        "stream": False,
        "options": options,
    }
    resp = requests.post(
        f"{OLLAMA_URL}/api/chat",
        json=payload,
        timeout=180,
    )
    resp.raise_for_status()
    return resp.json()["message"]["content"]


# ─── Парсинг ответа ─────────────────────────────────────────────────────────────

def _normalize_skills_list(parsed: list) -> list[list[str]]:
    result = []
    for item in parsed:
        if isinstance(item, list):
            result.append([str(s) for s in item])
        elif isinstance(item, str):
            result.append([item])
        else:
            result.append([str(item)])
    return result


def parse_response(raw: str) -> tuple[list[list[str]] | None, dict[str, str]]:
    """Returns (skills_matrix, domains_dict).
    Supports new format {result, domains} and legacy [[...]] as fallback.
    """
    text = raw.strip()

    # Убираем markdown-блоки ```json ... ``` если есть
    if text.startswith("```"):
        lines = text.splitlines()
        inner = []
        in_block = False
        for line in lines:
            if line.startswith("```"):
                in_block = not in_block
                continue
            if in_block:
                inner.append(line)
        text = "\n".join(inner).strip()

    # Попытка 1: новый формат {"result": ..., "domains": ...}
    obj_start = text.find("{")
    obj_end   = text.rfind("}")
    if obj_start != -1 and obj_end != -1:
        try:
            candidate = json.loads(text[obj_start : obj_end + 1])
            if isinstance(candidate, dict) and "result" in candidate:
                skills = _normalize_skills_list(candidate["result"])
                domains = candidate.get("domains", {})
                if not isinstance(domains, dict):
                    domains = {}
                return skills, domains
        except json.JSONDecodeError:
            pass

    # Попытка 2: старый формат [[...]]
    start = text.find("[[")
    end   = text.rfind("]]")
    if start != -1 and end != -1:
        try:
            parsed = json.loads(text[start : end + 2])
            if isinstance(parsed, list):
                return _normalize_skills_list(parsed), {}
        except json.JSONDecodeError:
            pass

    return None, {}


# ─── Образцы для тестирования без БД ───────────────────────────────────────────

HARDCODED_SAMPLES = [
    # Составные — разные технологии через /
    "C/C++",
    "HTML/CSS/JavaScript",
    "JavaScript/TypeScript",
    "React.js/Vue.js",
    "PostgreSQL/MySQL",
    "MS SQL/PostgreSQL",
    "Node.js/Express.js",
    "Spring Boot/Spring Cloud",
    "Git/GitHub/GitLab",
    # Версионные диапазоны (целые — должны перечислять ВСЕ)
    ".Net 6-8",
    "Java 11-17",
    # Версии через слэш (ОДНА технология, разные версии)
    "1С: Предприятие 8.3/8.2",
    # Слитные версии
    ".net8",
    "java",
    # Версии X+
    "ASP.NET Core 5+",
    "Python 3+",
    # 1С
    "1С Предприятие",
    "1С (Бухгалтерия/ЗУП)",
    # Нормализация регистра/написания
    "python",
    "golang",
    "k8s",
    # Убрать префиксы
    "Знание Python, Django",
    "Опыт работы с Docker и Kubernetes",
    # Мягкие навыки (должны остаться)
    "Работа в команде",
    "Agile/Scrum",
    # NOT_SKILL
    "Умение быстро обучаться",
    "Стрессоустойчивость",
    # 1С аббревиатуры — должны раскрываться
    "1C КА",
    "1С ЗУП",
    "1C ERP",
    "ERP:1C",
    "ERP-системы на базе 1С",
    # Скобки с конкретными инструментами
    "ERP/CRM (SAP, 1C, Oracle)",
    "agent frameworks (LangChain, LlamaIndex, CrewAI)",
    "agentные фреймворки (LangChain, LlamaIndex, CrewAI)",
    # Описательные префиксы
    "Архитектура 1С:Предприятие 8",
    "Архитектура типовых решений 1С",
    "Программирование на платформе 1С",
    "Сертификат 1С: Специалист по платформе",
]


# ─── Вспомогательные утилиты для сохранения ────────────────────────────────────

_VERSION_SUFFIX_RE = re.compile(r'^(.+?)\s+([\d]+(?:\.[\d]+)*)$')

_VALID_DOMAINS = frozenset({
    'BACKEND', 'FRONTEND', 'AI_ML', 'DATA_SCIENCE', 'DEVOPS',
    'DATABASE', 'CLOUD', 'SECURITY', 'TESTING', 'MOBILE',
    '1C', 'IOT', 'SYSTEMS', 'GENERAL',
})


def detect_version_group(skill_name: str) -> str | None:
    """Если навык вида 'Python 3.10' или '.NET 8' — вернуть базовое имя группы версий."""
    m = _VERSION_SUFFIX_RE.match(skill_name)
    return m.group(1) if m else None


def save_batch_to_db(conn, batch_inputs: list[str], batch_outputs: list[list[str]],
                     domains: dict[str, str]) -> tuple[int, int, int]:
    """Сохраняет один батч и коммитит. Возвращает (saved_canonical, saved_links, skipped_ws)."""
    cur = conn.cursor()
    saved_canonical = 0
    saved_links     = 0
    skipped_ws      = 0

    for raw_input, skills in zip(batch_inputs, batch_outputs):
        canonical_ids: list[int] = []

        for skill in skills:
            if skill.upper() == "NOT_SKILL":
                continue

            normalized = skill.strip().lower()
            raw_domain = domains.get(skill)
            domain     = raw_domain if raw_domain in _VALID_DOMAINS else None
            vg         = detect_version_group(skill)

            cur.execute("""
                INSERT INTO skill_canonical (name, normalized_name, version_group, domain, domain_source)
                VALUES (%s, %s, %s, %s, 'llm')
                ON CONFLICT (normalized_name) DO UPDATE
                    SET name          = EXCLUDED.name,
                        version_group = COALESCE(EXCLUDED.version_group, skill_canonical.version_group),
                        domain        = CASE
                            WHEN skill_canonical.domain_source = 'manual' THEN skill_canonical.domain
                            WHEN EXCLUDED.domain IS NOT NULL              THEN EXCLUDED.domain
                            ELSE skill_canonical.domain
                        END,
                        domain_source = CASE
                            WHEN skill_canonical.domain_source = 'manual' THEN 'manual'
                            WHEN EXCLUDED.domain IS NOT NULL              THEN 'llm'
                            ELSE skill_canonical.domain_source
                        END
                RETURNING id
            """, (skill.strip(), normalized, vg, domain))
            row = cur.fetchone()
            if row:
                canonical_ids.append(row[0])
                saved_canonical += 1

        if not canonical_ids:
            continue

        cur.execute(
            "SELECT id FROM work_skill WHERE TRIM(description) = %s",
            (raw_input.strip(),)
        )
        ws_rows = cur.fetchall()
        if not ws_rows:
            skipped_ws += 1
            continue

        for (ws_id,) in ws_rows:
            for cid in canonical_ids:
                cur.execute("""
                    INSERT INTO work_skill_canonical (work_skill_id, canonical_id)
                    VALUES (%s, %s)
                    ON CONFLICT DO NOTHING
                """, (ws_id, cid))
                saved_links += 1

    conn.commit()
    cur.close()
    return saved_canonical, saved_links, skipped_ws


def save_to_db(conn, samples: list[str], outputs: list[list[str]], domains: dict[str, str]) -> None:
    """Сохраняет все результаты. Используется для финального вывода статистики."""
    sc, sl, sw = save_batch_to_db(conn, samples, outputs, domains)
    print(f"[SAVE] skill_canonical: +{sc} upserts | work_skill_canonical: +{sl} links | not found: {sw}")


# ─── Загрузка из БД ─────────────────────────────────────────────────────────────

def get_db_samples(limit: int) -> list[str]:
    if not DB_URL:
        print("[WARN] DB_URL не задан в .env, используем захардкоженные примеры")
        return []

    try:
        import psycopg2
    except ImportError:
        print("[WARN] psycopg2 не установлен. Запустите: pip install psycopg2-binary")
        return []

    queries = [
        f"SELECT DISTINCT description FROM work_skill WHERE description LIKE '%/%' ORDER BY description LIMIT {limit}",
        f"SELECT DISTINCT description FROM work_skill WHERE description ~ '\\d+\\s*[-\u2013]\\s*\\d+' OR description ~ '\\d+\\s*\\+' ORDER BY description LIMIT {limit}",
        f"SELECT DISTINCT description FROM work_skill WHERE LOWER(description) LIKE '%1\u0441%' OR description LIKE '%1C%' ORDER BY description LIMIT {limit}",
        f"SELECT DISTINCT description FROM work_skill WHERE description ~ ',\\s*\\S' ORDER BY description LIMIT {limit}",
    ]

    results: set[str] = set()
    try:
        conn = psycopg2.connect(DB_URL)
        cur  = conn.cursor()
        for q in queries:
            cur.execute(q)
            for (desc,) in cur.fetchall():
                if desc and desc.strip():
                    results.add(desc.strip())
        cur.close()
        conn.close()
        print(f"[DB] Загружено {len(results)} уникальных проблемных строк из PostgreSQL")
    except Exception as e:
        print(f"[WARN] Ошибка подключения к БД: {e}")
        return []

    return sorted(results)


def get_all_db_samples(limit: int, offset: int = 0, skip_done: bool = False) -> list[str]:
    """Загружает ВСЕ уникальные work_skill.description (для полного прогона --all-skills).
    skip_done=True: пропускает work_skill, у которых уже есть запись в work_skill_canonical.
    """
    if not DB_URL:
        print("[WARN] DB_URL не задан в .env")
        return []
    try:
        import psycopg2
    except ImportError:
        print("[WARN] psycopg2 не установлен")
        return []

    try:
        conn = psycopg2.connect(DB_URL)
        cur  = conn.cursor()

        if skip_done:
            cur.execute(
                """
                SELECT DISTINCT TRIM(ws.description)
                FROM work_skill ws
                WHERE ws.description IS NOT NULL
                  AND TRIM(ws.description) <> ''
                  AND NOT EXISTS (
                      SELECT 1 FROM work_skill_canonical wsc
                      WHERE wsc.work_skill_id = ws.id
                  )
                ORDER BY 1
                LIMIT %s OFFSET %s
                """,
                (limit, offset)
            )
        else:
            cur.execute(
                """
                SELECT DISTINCT TRIM(description)
                FROM work_skill
                WHERE description IS NOT NULL AND TRIM(description) <> ''
                ORDER BY 1
                LIMIT %s OFFSET %s
                """,
                (limit, offset)
            )

        rows = [r[0] for r in cur.fetchall()]
        cur.close()
        conn.close()
        mode = "skip-done" if skip_done else "all"
        print(f"[DB] {len(rows)} строк (all-skills, {mode}, offset={offset})")
        return rows
    except Exception as e:
        print(f"[WARN] Ошибка БД: {e}")
        return []


# ─── Вывод результатов ─────────────────────────────────────────────────────────

def print_results(inputs: list[str], outputs: list[list[str]], domains: dict[str, str]) -> None:
    split_count     = 0
    norm_count      = 0
    same_count      = 0
    not_skill_count = 0
    mismatch        = len(inputs) != len(outputs)
    has_domains     = bool(domains)

    print(f"\n{'='*65}")
    print(f"  РЕЗУЛЬТАТЫ АТОМИЗАЦИИ  ({len(inputs)} вход → {len(outputs)} выход)")
    if mismatch:
        print(f"  [!] НЕСООТВЕТСТВИЕ КОЛИЧЕСТВА — GigaChat пропустил часть")
    if not has_domains:
        print(f"  [!] ДОМЕНЫ: отсутствуют (модель вернула старый формат)")
    print(f"{'='*65}\n")

    for i, inp in enumerate(inputs):
        if i >= len(outputs):
            print(f"[MISS   ] {inp!r}")
            print()
            continue

        out = outputs[i]

        is_not_skill = (len(out) == 1 and out[0].upper() == "NOT_SKILL")
        is_same      = (len(out) == 1 and out[0] == inp.strip())   # exact match (case-sensitive)
        is_norm      = (len(out) == 1 and not is_same and not is_not_skill)
        is_split     = len(out) > 1

        if is_not_skill:
            tag = "[NOT_SK ]"
            not_skill_count += 1
        elif is_split:
            tag = "[SPLIT  ]"
            split_count += 1
        elif is_norm:
            tag = "[NORM   ]"
            norm_count += 1
        else:
            tag = "[SAME   ]"
            same_count += 1

        def _dom(skill: str) -> str:
            d = domains.get(skill, "")
            return f"  [{d}]" if d else ""

        if is_not_skill:
            print(f"{tag} {inp!r}")
        elif is_same:
            print(f"{tag} {inp!r}{_dom(out[0])}")
        elif is_norm:
            print(f"{tag} {inp!r}  ->  {out[0]!r}{_dom(out[0])}")
        else:
            print(f"{tag} {inp!r}")
            for item in out:
                print(f"           -> {item!r}{_dom(item)}")
        print()

    print(f"{'='*65}")
    print(f"  ИТОГ: SPLIT={split_count}  NORM={norm_count}  SAME={same_count}  NOT_SKILL={not_skill_count}")
    if mismatch:
        missed = len(inputs) - len(outputs)
        print(f"  MISS={missed}  (GigaChat не вернул ответ для {missed} строк)")
    print(f"{'='*65}\n")


# ─── main ───────────────────────────────────────────────────────────────────────

def main() -> None:
    parser = argparse.ArgumentParser(
        description="Тестирует промпт атомизации навыков против GigaChat"
    )
    parser.add_argument("--no-db",       action="store_true", help="Использовать захардкоженные примеры")
    parser.add_argument("--all-skills",  action="store_true", help="Загрузить ВСЕ work_skill, не только проблемные")
    parser.add_argument("--limit",       type=int, default=20, help="Макс. строк на SQL-запрос (по умолч. 20)")
    parser.add_argument("--batch-size",  type=int, default=10, help="Строк в одном запросе к LLM (по умолч. 10)")
    parser.add_argument("--raw",         action="store_true", help="Вывести сырой ответ LLM")
    parser.add_argument("--local",       action="store_true", help="Использовать локальную модель через Ollama")
    parser.add_argument("--local-model", type=str, default=None, help="Модель Ollama (по умолч. из .env или qwen2.5:7b)")
    parser.add_argument("--model",       type=str, default=None, help="Модель GigaChat (напр. GigaChat-Max; по умолч. из .env)")
    parser.add_argument("--shuffle",     action="store_true", help="Перемешать строки из БД перед батчингом")
    parser.add_argument("--save",        action="store_true", help="Сохранить результаты в skill_canonical + work_skill_canonical")
    parser.add_argument("--skip-done",   action="store_true", help="(с --all-skills) Пропускать work_skill с уже существующими canonical-связями")
    parser.add_argument("--offset",      type=int, default=0,   help="(с --all-skills) Смещение SQL OFFSET для ручной пагинации")
    parser.add_argument("--delay",        type=float, default=0, help="Задержка между батчами в секундах (по умолч. 0)")
    args = parser.parse_args()

    # Загружаем промпт
    prompt_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "prompt.txt")
    if not os.path.exists(prompt_path):
        sys.exit(f"[ERROR] Файл промпта не найден: {prompt_path}")
    with open(prompt_path, encoding="utf-8") as f:
        system_prompt = f.read()
    print(f"[PROMPT] Загружен {prompt_path} ({len(system_prompt)} символов)")

    # Переопределяем модель GigaChat если задан --model
    global MODEL
    if args.model:
        MODEL = args.model

    # Проверка: --save несовместим с --no-db
    if args.save and args.no_db:
        sys.exit("[ERROR] --save несовместим с --no-db: нет work_skill для привязки")

    # Получаем образцы
    if args.no_db:
        samples = HARDCODED_SAMPLES
        print(f"[MODE] --no-db: используем {len(samples)} захардкоженных примеров")
    elif args.all_skills:
        samples = get_all_db_samples(args.limit, offset=args.offset, skip_done=args.skip_done)
        if not samples:
            sys.exit("[ERROR] Нет данных из БД")
        print(f"[MODE] --all-skills: {len(samples)} строк из work_skill")
    else:
        samples = get_db_samples(args.limit)
        if not samples:
            print("[MODE] Нет данных из БД, используем захардкоженные примеры")
            samples = HARDCODED_SAMPLES

    # При --all-skills включаем shuffle по умолчанию (если явно не отключено --no-shuffle)
    # Это критично: алфавитная сортировка даёт однородные батчи вида «25 строк на чтение…»
    # и GigaChat теряет связь между входом и выходом.
    do_shuffle = args.shuffle or (args.all_skills and not getattr(args, 'no_shuffle', False))
    if do_shuffle and not args.no_db:
        import random
        random.shuffle(samples)
        print(f"[SHUFFLE] Строки перемешаны (алфавитная однородность устранена)")

    use_local = args.local
    local_model = args.local_model or OLLAMA_MODEL
    active_model = local_model if use_local else MODEL
    gpu_info = f" [GPU layers: {OLLAMA_GPU_LAYERS}]" if use_local and OLLAMA_GPU_LAYERS >= 0 else (" [GPU: auto]" if use_local else "")
    backend = f"Ollama/{local_model}{gpu_info}" if use_local else f"GigaChat/{MODEL}"

    batch_size = args.batch_size
    n_batches = (len(samples) + batch_size - 1) // batch_size
    print(f"\n[INPUT] {len(samples)} строк → {n_batches} батч(а) по {batch_size} (бэкенд: {backend})")

    all_outputs: list[list[str]] = []
    all_domains: dict[str, str]  = {}
    total_elapsed = 0.0

    # Открываем соединение заранее (если --save)
    db_conn = None
    total_saved_sc = total_saved_links = total_skipped_ws = 0
    if args.save and DB_URL:
        try:
            import psycopg2
            db_conn = psycopg2.connect(DB_URL)
        except Exception as e:
            print(f"[ERROR] Не удалось подключиться к БД для --save: {e}")

    for b_idx in range(n_batches):
        batch = samples[b_idx * batch_size : (b_idx + 1) * batch_size]
        user_message = json.dumps(batch, ensure_ascii=False, indent=None)
        print(f"[BATCH {b_idx+1}/{n_batches}] {len(batch)} строк...", end=" ", flush=True)
        t0 = time.time()
        try:
            if use_local:
                raw_response = call_ollama(system_prompt, user_message, local_model)
            else:
                raw_response = call_gigachat(system_prompt, user_message)
        except requests.HTTPError as e:
            if hasattr(e, 'response') and e.response.status_code == 402:
                sys.exit("\n[ERROR] 402 Payment Required — лимит токенов GigaChat исчерпан. Пождите обновления квоты.")
            sys.exit(f"\n[ERROR] HTTP {e.response.status_code}: {e.response.text}")
        except Exception as e:
            sys.exit(f"\n[ERROR] LLM: {e}")
        elapsed = time.time() - t0
        total_elapsed += elapsed
        print(f"{elapsed:.1f} с", end="")

        if args.raw:
            print(f"\n--- RAW BATCH {b_idx+1} ---\n{raw_response}\n--- END RAW ---\n")

        if args.delay > 0 and b_idx < n_batches - 1:
            time.sleep(args.delay)

        batch_outputs, batch_domains = parse_response(raw_response)
        # Валидация: количество ответов должно совпадать с количеством входов
        if batch_outputs is None or len(batch_outputs) != len(batch):
            mismatch_reason = "не удалось разобрать JSON" if batch_outputs is None \
                else f"длина {len(batch_outputs)} ≠ {len(batch)}"
            print(f"  [RETRY] Батч {b_idx+1}: {mismatch_reason}, повторяем...")
            try:
                if use_local:
                    raw_response = call_ollama(system_prompt, user_message, local_model)
                else:
                    raw_response = call_gigachat(system_prompt, user_message)
                batch_outputs, batch_domains = parse_response(raw_response)
            except Exception:
                batch_outputs = None
            if batch_outputs is None or len(batch_outputs) != len(batch):
                print(f"  [FALLBACK] Батч {b_idx+1}: оставляем как есть (identity)")
                batch_outputs = [[inp] for inp in batch]
                batch_domains = {}
        all_outputs.extend(batch_outputs)
        all_domains.update(batch_domains)

        # Пер-батч сохранение: коммит после каждого батча
        if args.save and db_conn:
            try:
                sc, sl, sw = save_batch_to_db(db_conn, batch, batch_outputs, batch_domains)
                total_saved_sc    += sc
                total_saved_links += sl
                total_skipped_ws  += sw
                print(f"  [сохр. +{sc} canonical, +{sl} links]")
            except Exception as e:
                db_conn.rollback()
                print(f"  [ERROR] --save батч {b_idx+1}: {e}")
        else:
            print()

    if db_conn:
        db_conn.close()

    print(f"[OK] Итого за {total_elapsed:.1f} с")
    if args.save and DB_URL:
        print(f"[SAVE ИТОГ] skill_canonical: +{total_saved_sc} | links: +{total_saved_links} | not found: {total_skipped_ws}")

    # Выводим результаты
    print_results(samples, all_outputs, all_domains)


if __name__ == "__main__":
    main()
