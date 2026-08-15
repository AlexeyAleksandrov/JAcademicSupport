"""
assign_families_llm.py
----------------------
Двухфазное заполнение поля tech_family для навыков без семейства:
  Фаза 1 (быстро): словарь RAW_RULES из _assign_families.py
  Фаза 2 (GigaChat): оставшиеся навыки → LLM-классификация батчами

Использование:
  python assign_families_llm.py                  # dry-run: показать статистику
  python assign_families_llm.py --save           # сохранить в БД
  python assign_families_llm.py --batch-size 30  # размер батча для LLM (по умолч. 25)
  python assign_families_llm.py --domain GENERAL # только один домен
  python assign_families_llm.py --limit 200      # ограничить кол-во навыков (отладка)
  python assign_families_llm.py --skip-rules     # только LLM, без правил
  python assign_families_llm.py --skip-llm       # только правила, без LLM
"""

import argparse
import os
import re
import sys
import time
import uuid
import warnings
from collections import defaultdict
from datetime import datetime

import psycopg2
import requests
from dotenv import load_dotenv

warnings.filterwarnings("ignore")

load_dotenv()

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

DB_URL         = os.getenv("DB_URL", "postgresql://postgres:1111@localhost:5432/AcademicSupport")
GIGACHAT_TOKEN = os.getenv("GIGACHAT_API_TOKEN", "")
OAUTH_URL      = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth"
API_URL        = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions"
SCOPE          = os.getenv("GIGACHAT_SCOPE", "GIGACHAT_API_PERS")
MODEL          = os.getenv("GIGACHAT_MODEL", "GigaChat-Pro")

# ─── GigaChat auth ────────────────────────────────────────────────────────────

_cached_token = None
_token_expires_at_ms = 0

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
        timeout=30,
    )
    resp.raise_for_status()
    data = resp.json()
    _cached_token = data["access_token"]
    _token_expires_at_ms = int(data.get("expires_at", now_ms + 1_800_000))
    log(f"[AUTH] Получен токен GigaChat (истекает через ~{(_token_expires_at_ms - now_ms) // 60000} мин)")
    return _cached_token


def call_gigachat(system_prompt: str, user_message: str, max_retries: int = 5) -> str:
    for attempt in range(max_retries):
        token = get_access_token()
        payload = {
            "model": MODEL,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user",   "content": user_message},
            ],
            "temperature": 0.0,
            "max_tokens": 2048,
            "profanity_check": False,
        }
        resp = requests.post(
            API_URL,
            headers={"Content-Type": "application/json", "Authorization": f"Bearer {token}"},
            json=payload,
            verify=False,
            timeout=60,
        )
        if resp.status_code == 429:
            wait = 10 * (attempt + 1)
            log(f"  [429] Rate limit, жду {wait}с... (попытка {attempt+1}/{max_retries})")
            time.sleep(wait)
            continue
        resp.raise_for_status()
        return resp.json()["choices"][0]["message"]["content"]
    raise RuntimeError(f"GigaChat 429 после {max_retries} попыток")

# ─── Вспомогательные ──────────────────────────────────────────────────────────

def log(msg: str) -> None:
    ts = datetime.now().strftime("%H:%M:%S")
    print(f"[{ts}] {msg}", flush=True)


def progress_bar(current: int, total: int, width: int = 40) -> str:
    pct = current / total if total else 0
    filled = int(pct * width)
    bar = "█" * filled + "░" * (width - filled)
    return f"|{bar}| {current}/{total} ({pct*100:.1f}%)"

# ─── Правила (из _assign_families.py, упрощённые) ────────────────────────────

RAW_RULES = [
    ("apache maven",       "Java"),
    ("spring tool suite",  "Java"),
    ("spring boot",        "Java"),
    ("spring framework",   "Java"),
    ("spring security",    "Java"),
    ("spring cloud",       "Java"),
    ("spring data",        "Java"),
    ("spring mvc",         "Java"),
    ("spring batch",       "Java"),
    ("spring integration", "Java"),
    ("spring webflux",     "Java"),
    ("spring",             "Java"),
    ("hibernate",          "Java"),
    ("java ee",            "Java"),
    ("java se",            "Java"),
    ("javafx",             "Java"),
    ("maven",              "Java"),
    ("gradle",             "Java"),
    ("junit",              "Java"),
    ("mockito",            "Java"),
    ("testng",             "Java"),
    ("log4j",              "Java"),
    ("slf4j",              "Java"),
    ("logback",            "Java"),
    ("rxjava",             "Java"),
    ("jpa",                "Java"),
    ("jvm",                "Java"),
    ("tomcat",             "Java"),
    ("jetty",              "Java"),
    ("quarkus",            "Java"),
    ("micronaut",          "Java"),
    ("vertx",              "Java"),
    ("dropwizard",         "Java"),
    ("camunda",            "Java"),
    ("liquibase",          "Java"),
    ("flyway",             "Java"),
    ("intellij",           "Java"),
    ("kotlin",             "Kotlin"),
    ("ktor",               "Kotlin"),
    ("koin",               "Kotlin"),
    ("jetpack compose",    "Kotlin"),
    ("jetpack",            "Kotlin"),
    ("coroutines",         "Kotlin"),
    ("room database",      "Kotlin"),
    ("android studio",     "Kotlin"),
    ("scala",              "Scala"),
    ("akka",               "Scala"),
    ("play framework",     "Scala"),
    ("sbt",                "Scala"),
    ("python",             "Python"),
    ("django",             "Python"),
    ("fastapi",            "Python"),
    ("flask",              "Python"),
    ("asyncio",            "Python"),
    ("aiohttp",            "Python"),
    ("celery",             "Python"),
    ("sqlalchemy",         "Python"),
    ("alembic",            "Python"),
    ("pydantic",           "Python"),
    ("uvicorn",            "Python"),
    ("gunicorn",           "Python"),
    ("tornado",            "Python"),
    ("twisted",            "Python"),
    ("pandas",             "Python"),
    ("numpy",              "Python"),
    ("scikit",             "ML/AI"),
    ("matplotlib",         "Python"),
    ("jupyter",            "ML/AI"),
    ("pytest",             "Автотестирование"),
    ("visual studio code", None),
    ("vscode",             None),
    ("vs code",            None),
    ("visual studio",      ".NET"),
    (".net core",          ".NET"),
    (".net framework",     ".NET"),
    ("asp.net",            ".NET"),
    ("entity framework",   ".NET"),
    ("blazor",             ".NET"),
    ("xamarin",            ".NET"),
    ("maui",               ".NET"),
    ("wpf",                ".NET"),
    ("winforms",           ".NET"),
    ("nunit",              ".NET"),
    ("xunit",              ".NET"),
    ("signalr",            ".NET"),
    ("dapper",             ".NET"),
    ("typescript",         "JavaScript"),
    ("javascript",         "JavaScript"),
    ("next.js",            "JavaScript"),
    ("nextjs",             "JavaScript"),
    ("nuxt.js",            "JavaScript"),
    ("nuxtjs",             "JavaScript"),
    ("react native",       "JavaScript"),
    ("react",              "JavaScript"),
    ("vue.js",             "JavaScript"),
    ("angular",            "JavaScript"),
    ("svelte",             "JavaScript"),
    ("express.js",         "JavaScript"),
    ("express",            "JavaScript"),
    ("nest.js",            "JavaScript"),
    ("nestjs",             "JavaScript"),
    ("node.js",            "JavaScript"),
    ("sequelize",          "JavaScript"),
    ("typeorm",            "JavaScript"),
    ("prisma",             "JavaScript"),
    ("webpack",            "JavaScript"),
    ("vite",               "JavaScript"),
    ("eslint",             "JavaScript"),
    ("redux",              "JavaScript"),
    ("laravel",            "PHP"),
    ("symfony",            "PHP"),
    ("yii",                "PHP"),
    ("codeigniter",        "PHP"),
    ("wordpress",          "PHP"),
    ("bitrix",             "PHP"),
    ("doctrine",           "PHP"),
    ("eloquent",           "PHP"),
    ("phpunit",            "PHP"),
    ("composer",           "PHP"),
    ("golang",             "Go"),
    ("gin framework",      "Go"),
    ("echo",               "Go"),
    ("fiber",              "Go"),
    ("grpc",               "Go"),
    ("gorm",               "Go"),
    ("sqlx",               "Go"),
    ("ruby on rails",      "Ruby"),
    ("rails",              "Ruby"),
    ("sinatra",            "Ruby"),
    ("activerecord",       "Ruby"),
    ("rust",               "Rust"),
    ("actix",              "Rust"),
    ("tokio",              "Rust"),
    ("c/c++",              "C/C++"),
    ("cmake",              "C/C++"),
    ("qt framework",       "C/C++"),
    ("swift",              "Swift"),
    ("swiftui",            "Swift"),
    ("objective-c",        "Swift"),
    ("xcode",              "Swift"),
    ("flutter",            "Dart"),
    ("dart",               "Dart"),
    ("kubernetes",         "Контейнеры"),
    ("docker compose",     "Контейнеры"),
    ("docker swarm",       "Контейнеры"),
    ("docker",             "Контейнеры"),
    ("helm",               "Контейнеры"),
    ("podman",             "Контейнеры"),
    ("openshift",          "Контейнеры"),
    ("github actions",     "CI/CD"),
    ("gitlab ci",          "CI/CD"),
    ("gitlab",             "CI/CD"),
    ("jenkins",            "CI/CD"),
    ("teamcity",           "CI/CD"),
    ("circleci",           "CI/CD"),
    ("argocd",             "CI/CD"),
    ("terraform",          "IaC"),
    ("ansible",            "IaC"),
    ("pulumi",             "IaC"),
    ("chef",               "IaC"),
    ("puppet",             "IaC"),
    ("vagrant",            "IaC"),
    ("prometheus",         "Мониторинг"),
    ("grafana",            "Мониторинг"),
    ("kibana",             "Мониторинг"),
    ("logstash",           "Мониторинг"),
    ("elasticsearch",      "Мониторинг"),
    ("zabbix",             "Мониторинг"),
    ("datadog",            "Мониторинг"),
    ("opentelemetry",      "Мониторинг"),
    ("yandex cloud",       "Cloud"),
    ("amazon web services","Cloud"),
    ("google cloud",       "Cloud"),
    ("azure",              "Cloud"),
    ("aws",                "Cloud"),
    ("gcp",                "Cloud"),
    ("cloudflare",         "Cloud"),
    ("digitalocean",       "Cloud"),
    ("nginx",              "Системное ПО"),
    ("apache httpd",       "Системное ПО"),
    ("haproxy",            "Системное ПО"),
    ("linux",              "Системное ПО"),
    ("ubuntu",             "Системное ПО"),
    ("centos",             "Системное ПО"),
    ("debian",             "Системное ПО"),
    ("bash",               "Системное ПО"),
    ("powershell",         "Системное ПО"),
    ("git flow",           "VCS"),
    ("git",                "VCS"),
    ("svn",                "VCS"),
    ("mercurial",          "VCS"),
    ("apache kafka",       "Message Brokers"),
    ("kafka",              "Message Brokers"),
    ("rabbitmq",           "Message Brokers"),
    ("redis streams",      "Message Brokers"),
    ("nats",               "Message Brokers"),
    ("postgresql",         "Реляционные"),
    ("mysql",              "Реляционные"),
    ("microsoft sql",      "Реляционные"),
    ("mssql",              "Реляционные"),
    ("sql server",         "Реляционные"),
    ("mariadb",            "Реляционные"),
    ("sqlite",             "Реляционные"),
    ("oracle db",          "Реляционные"),
    ("oracle",             "Реляционные"),
    ("firebird",           "Реляционные"),
    ("mongodb",            "NoSQL"),
    ("cassandra",          "NoSQL"),
    ("hbase",              "NoSQL"),
    ("couchdb",            "NoSQL"),
    ("neo4j",              "NoSQL"),
    ("dynamodb",           "NoSQL"),
    ("redis",              "In-Memory"),
    ("memcached",          "In-Memory"),
    ("hazelcast",          "In-Memory"),
    ("clickhouse",         "Аналитика"),
    ("greenplum",          "Аналитика"),
    ("vertica",            "Аналитика"),
    ("bigquery",           "Аналитика"),
    ("redshift",           "Аналитика"),
    ("snowflake schema",   "Аналитика"),
    ("dbt",                "Аналитика"),
    ("tensorflow",         "Deep Learning"),
    ("pytorch",            "Deep Learning"),
    ("keras",              "Deep Learning"),
    ("hugging face",       "LLM"),
    ("langchain",          "LLM"),
    ("openai",             "LLM"),
    ("llm",                "LLM"),
    ("chatgpt",            "LLM"),
    ("catboost",           "ML/AI"),
    ("xgboost",            "ML/AI"),
    ("lightgbm",           "ML/AI"),
    ("mlflow",             "ML/AI"),
    ("airflow",            "ML/AI"),
    ("hadoop",             "Big Data"),
    ("hive",               "Big Data"),
    ("spark",              "Big Data"),
    ("tableau",            "Визуализация"),
    ("power bi",           "Визуализация"),
    ("superset",           "Визуализация"),
    ("selenium",           "Автотестирование"),
    ("playwright",         "Автотестирование"),
    ("appium",             "Автотестирование"),
    ("allure",             "Автотестирование"),
    ("cypress",            "Автотестирование"),
    ("postman",            "API Testing"),
    ("soapui",             "API Testing"),
    ("insomnia",           "API Testing"),
    ("rest assured",       "API Testing"),
    ("jmeter",             "Нагрузочное тестирование"),
    ("gatling",            "Нагрузочное тестирование"),
    ("locust",             "Нагрузочное тестирование"),
    ("testrail",           "Тест-менеджмент"),
    ("testit",             "Тест-менеджмент"),
    ("jira",               "PM Tools"),
    ("confluence",         "PM Tools"),
    ("trello",             "PM Tools"),
    ("agile",              "Методологии"),
    ("scrum",              "Методологии"),
    ("kanban",             "Методологии"),
    ("waterfall",          "Методологии"),
    ("figma",              "Дизайн"),
    ("sketch",             "Дизайн"),
    ("swagger",            "Документирование"),
    ("openapi",            "Документирование"),
    ("solid",              "Принципы"),
    ("microservice",       "Архитектура"),
    ("микросервис",        "Архитектура"),
    ("event-driven",       "Архитектура"),
    ("clean architecture", "Архитектура"),
    ("uml",                "Моделирование"),
    ("bpmn",               "Моделирование"),
    ("1с-битрикс",         "1С"),
    ("1c-bitrix",          "1С"),
    ("bitrix24",           "1С"),
    ("1с:",                "1С"),
]

SHORT_RULES = [
    ("java",   "Java"),
    ("ruby",   "Ruby"),
    ("node",   "JavaScript"),
    ("go",     "Go"),
    ("php",    "PHP"),
    ("c#",     ".NET"),
    ("r",      "R"),
    ("sql",    "Реляционные"),
    ("css",    "CSS"),
    ("html",   "HTML"),
    ("1с ",    "1С"),
]

KNOWN_FAMILIES = [
    # ── Языки и фреймворки ──────────────────────────────────────────────
    "Java", "Python", ".NET", "Go", "Node.js", "PHP", "Kotlin", "Ruby", "Rust", "Scala",
    "C/C++", "Swift", "Dart", "JavaScript", "R",
    # ── Frontend ────────────────────────────────────────────────────────
    "CSS", "HTML", "HTML/CSS", "Дизайн", "3D/Графика", "Computer Vision",
    # ── DevOps ──────────────────────────────────────────────────────────
    "Контейнеры", "CI/CD", "IaC", "Мониторинг", "Cloud", "VCS", "Системное ПО",
    "AWS", "GCP", "Azure", "MLOps", "Data Engineering",
    # ── Database ────────────────────────────────────────────────────────
    "Реляционные", "NoSQL", "In-Memory", "Аналитика", "BI/Analytics", "Аналитика/BI",
    "Message Brokers", "Vector DB",
    # ── AI/ML ───────────────────────────────────────────────────────────
    "ML/AI", "Deep Learning", "Big Data", "LLM", "Визуализация", "NLP",
    "Computer Vision", "MLOps",
    # ── Testing ─────────────────────────────────────────────────────────
    "Автотестирование", "Нагрузочное тестирование", "API Testing",
    "Тест-менеджмент", "Методологии тестирования",
    # ── Security ────────────────────────────────────────────────────────
    "Пентест/AppSec", "Криптография", "IAM", "Compliance", "Сетевая безопасность",
    # ── IoT / Systems ───────────────────────────────────────────────────
    "Протоколы IoT", "Микроконтроллеры", "Встраиваемые системы", "Радиотехника",
    # ── General / Methodology ───────────────────────────────────────────
    "Архитектура", "Принципы", "Методологии", "Документирование",
    "Моделирование", "PM Tools", "1С", "Soft Skills", "gRPC/Протоколы",
    "Mobile", "Cross-platform", "iOS", "Android",
]

SYSTEM_PROMPT = f"""Ты — классификатор IT-навыков. Твоя задача — определить tech_family (технологическое семейство) для КОНКРЕТНОГО инструмента или технологии.

РАЗРЕШЁННЫЕ СЕМЕЙСТВА:
{chr(10).join("  " + f for f in KNOWN_FAMILIES)}

Входные данные — список навыков:
  ID|ДОМЕН|НАЗВАНИЕ

Выходные данные — ровно столько же строк:
  ID|FAMILY

═══ КРИТИЧЕСКИ ВАЖНЫЕ ПРАВИЛА ═══

0. ГЛАВНОЕ ПРАВИЛО: классифицируй по НАЗВАНИЮ навыка, домен — только подсказка.
   Если название не соответствует домену — доверяй НАЗВАНИЮ.
   Пример: навык "Navision" в домене 1С → это Microsoft ERP, не 1С → ".NET" или "—"

1. ВЕРНИ ID|— (прочерк) для:
   - Описаний роли или домена: "backend разработка", "frontend development", "fullstack", "back-end", "разработчик"
   - Общих компетенций без конкретного инструмента: "разработка сервисов", "работа с API", "CRUD", "REST-сервисы"
   - HTTP-методов как отдельных навыков: GET, POST, DELETE, PATCH, PUT
   - Слишком общих терминов: "Бухгалтерия", "Учёт", "Аналитика", "Отчётность" (без конкретного продукта)
   - Мусора или опечаток: однобуквенные термины, URL-фрагменты, случайные слова
   - Soft-skills и языков: "английский", "коммуникация", "командная работа"

2. СЕМЕЙСТВО — это КОНКРЕТНЫЙ ИНСТРУМЕНТ/ЭКОСИСТЕМА, а НЕ язык реализации и НЕ домен:
   - "FastAPI" → Python, "Spring" → Java, "React" → JavaScript
   - "4-bit QLoRA", "LoRA", "PEFT" → Deep Learning (техники дообучения, не MLOps!)
   - "ADK", "LangGraph", "LangChain" → LLM (Agent frameworks)
   - "Navision", "SAP", "Oracle ERP" → .NET или — (НЕ 1С!)
   - НО: "1С:Бухгалтерия", "1С:ERP", "СКД", "БСП" → 1С (это инструменты платформы 1С)
   - НО: "backend разработка" → — (это описание, не инструмент)
   - НО: "Payroll" без уточнения → — (слишком общий термин)

3. Для конкретных ИНСТРУМЕНТОВ без языковой принадлежности:
   - Docker, Kubernetes → Контейнеры
   - Git, GitHub → VCS
   - Jenkins, GitLab CI → CI/CD
   - PostgreSQL, MySQL → Реляционные
   - Jira, Confluence → PM Tools
   - TensorFlow, PyTorch → Deep Learning
   - OpenVAS, Nessus → Пентест/AppSec
   - gRPC, WebSocket → gRPC/Протоколы
   - OPC-UA, Modbus → Протоколы IoT

4. НЕ добавляй пояснений — только строки ID|FAMILY
5. Каждый входной ID должен быть в выводе ровно один раз
"""


def build_rules():
    rules = [(kw.lower().strip(), fam) for kw, fam in RAW_RULES]
    rules.sort(key=lambda x: -len(x[0]))
    return rules


def matches_short(name_lc: str, kw_lc: str) -> bool:
    return bool(re.search(r'\b' + re.escape(kw_lc) + r'\b', name_lc))


def apply_rules(name: str, rules, short_rules) -> str | None:
    """Вернуть family или None если нет совпадения. False если совпало и нет семейства (soft-skill)."""
    name_lc = name.lower().strip()
    for kw, fam in rules:
        if kw in name_lc:
            return fam  # None означает soft-skill, совпадение найдено
    for kw, fam in short_rules:
        if matches_short(name_lc, kw):
            return fam
    return "___NO_MATCH___"


# Мусорные «семейства» которые GigaChat иногда придумывает — отклоняем
_REJECT_FAMILIES = {
    "", "—", "-", "–", "null", "none", "n/a", "не определено", "unknown",
    "не указано", "другое", "general", "прочее",
}


def parse_llm_response(response: str, batch_ids: list[int]) -> dict[int, str | None]:
    """Парсит ответ LLM (строки ID|FAMILY) → словарь {id: family}.

    Принимаем ЛЮБОЕ непустое семейство, кроме заведомо мусорных.
    Если LLM вернул явный мусор или прочерк → None (soft-skill, пропустить).
    """
    result = {}
    for line in response.strip().splitlines():
        line = line.strip()
        if not line or "|" not in line:
            continue
        parts = line.split("|", 1)
        try:
            sid = int(parts[0].strip())
            fam = parts[1].strip()
            fam_lc = fam.lower()
            if fam_lc in _REJECT_FAMILIES:
                fam = None  # soft-skill / неопределено
            # Остальное принимаем как есть (GigaChat знает специфику домена)
            result[sid] = fam
        except (ValueError, IndexError):
            continue

    missing = [i for i in batch_ids if i not in result]
    if missing:
        log(f"  [WARN] LLM не вернул ответ для {len(missing)} навыков: {missing[:5]}...")

    return result


def main():
    parser = argparse.ArgumentParser(description="Заполнение tech_family: правила + GigaChat LLM")
    parser.add_argument("--save",       action="store_true", help="Сохранить изменения в БД")
    parser.add_argument("--batch-size", type=int, default=25, help="Батч для LLM (по умолч. 25)")
    parser.add_argument("--domain",     help="Обработать только один домен")
    parser.add_argument("--limit",      type=int, help="Ограничить кол-во навыков (отладка)")
    parser.add_argument("--skip-rules", action="store_true", help="Пропустить фазу правил")
    parser.add_argument("--skip-llm",   action="store_true", help="Пропустить фазу LLM")
    parser.add_argument("--verbose",    action="store_true", help="Выводить каждый навык → семейство")
    args = parser.parse_args()

    mode = "SAVE" if args.save else "DRY-RUN"
    log(f"{'='*65}")
    log(f"  assign_families_llm  [{mode}]  модель: {MODEL}")
    log(f"{'='*65}")

    rules = build_rules()
    short_rules = [(kw.lower().strip(), fam) for kw, fam in SHORT_RULES]

    conn = psycopg2.connect(DB_URL)
    conn.autocommit = False
    cur = conn.cursor()

    # ── Загрузка навыков без семейства ────────────────────────────────────────
    domain_filter = f"AND domain = '{args.domain}'" if args.domain else ""
    limit_clause  = f"LIMIT {args.limit}" if args.limit else ""

    cur.execute(f"""
        SELECT id, name, domain
        FROM skill_canonical
        WHERE tech_family IS NULL
        {domain_filter}
        ORDER BY domain NULLS LAST, name
        {limit_clause}
    """)
    all_rows = cur.fetchall()
    log(f"Навыков без tech_family: {len(all_rows)}")

    if not all_rows:
        log("Нечего делать — все навыки уже имеют семейство!")
        conn.close()
        return

    # ── Фаза 1: Правила ───────────────────────────────────────────────────────
    rule_updates = []    # (family, id)
    rule_soft    = []    # (id, name) — soft-skills (family=None)
    llm_queue    = []    # (id, name, domain) — для LLM

    if not args.skip_rules:
        log(f"\n{'─'*65}")
        log(f"ФАЗА 1: Словарь правил ({len(all_rows)} навыков)")
        log(f"{'─'*65}")

        rule_family_counts = defaultdict(int)
        for i, (sid, name, domain) in enumerate(all_rows):
            fam = apply_rules(name, rules, short_rules)
            if fam == "___NO_MATCH___":
                llm_queue.append((sid, name, domain))
            elif fam is None:
                rule_soft.append((sid, name))
                # soft-skill — не устанавливаем family, но можем пометить
            else:
                rule_family_counts[fam] += 1
                rule_updates.append((fam, sid))

            if (i + 1) % 500 == 0 or (i + 1) == len(all_rows):
                log(f"  {progress_bar(i+1, len(all_rows))}")

        log(f"\n  Совпало правилами:  {len(rule_updates)}")
        log(f"  Soft-skills:        {len(rule_soft)}")
        log(f"  Остаток для LLM:    {len(llm_queue)}")
        log(f"\n  Топ-15 семейств (правила):")
        for fam, cnt in sorted(rule_family_counts.items(), key=lambda x: -x[1])[:15]:
            log(f"    {fam:35s} {cnt:5d}")
    else:
        llm_queue = [(sid, name, dom) for sid, name, dom in all_rows]
        log("  [skip-rules] Фаза правил пропущена")

    # ── Сохранить результаты правил ───────────────────────────────────────────
    if args.save and rule_updates:
        log(f"\n  Запись {len(rule_updates)} обновлений (правила)...")
        cur.executemany("UPDATE skill_canonical SET tech_family = %s WHERE id = %s", rule_updates)
        conn.commit()
        log(f"  ✓ Правила сохранены")

    # ── Фаза 2: LLM (GigaChat) ────────────────────────────────────────────────
    llm_updates = []   # (family, id)
    llm_family_counts = defaultdict(int)
    llm_unresolved = []

    if llm_queue and not args.skip_llm:
        n_batches = (len(llm_queue) + args.batch_size - 1) // args.batch_size
        log(f"\n{'─'*65}")
        log(f"ФАЗА 2: GigaChat LLM ({len(llm_queue)} навыков → {n_batches} батчей × {args.batch_size})")
        log(f"{'─'*65}")

        t_start_llm = time.time()

        for batch_idx in range(n_batches):
            batch = llm_queue[batch_idx * args.batch_size : (batch_idx + 1) * args.batch_size]
            batch_ids = [r[0] for r in batch]

            user_msg = "\n".join(f"{sid}|{dom or 'UNKNOWN'}|{name}" for sid, name, dom in batch)

            t_batch = time.time()
            log(f"\n  Батч {batch_idx+1}/{n_batches} ({len(batch)} навыков)...")

            try:
                raw = call_gigachat(SYSTEM_PROMPT, user_msg)
            except Exception as e:
                log(f"  [ERROR] Батч {batch_idx+1} провален: {e}")
                llm_unresolved.extend(batch)
                continue

            elapsed = time.time() - t_batch
            parsed = parse_llm_response(raw, batch_ids)

            batch_saves = []
            resolved = 0
            verbose_lines = []
            for sid, name, dom in batch:
                fam = parsed.get(sid, "___NOT_FOUND___")
                if fam == "___NOT_FOUND___":
                    llm_unresolved.append((sid, name, dom))
                    if args.verbose:
                        verbose_lines.append(f"    [?] {name[:45]}")
                elif fam is None:
                    if args.verbose:
                        verbose_lines.append(f"    [—] {name[:45]}")
                else:
                    llm_family_counts[fam] += 1
                    llm_updates.append((fam, sid))
                    batch_saves.append((fam, sid))
                    resolved += 1
                    if args.verbose:
                        verbose_lines.append(f"    {str(fam):30} <- {name[:45]}")

            if args.verbose and verbose_lines:
                for vl in verbose_lines:
                    print(vl, flush=True)

            # Сохраняем каждый батч сразу — защита от краша
            if args.save and batch_saves:
                cur.executemany("UPDATE skill_canonical SET tech_family = %s WHERE id = %s", batch_saves)
                conn.commit()

            # Прогресс
            done_batches = batch_idx + 1
            elapsed_total = time.time() - t_start_llm
            avg_per_batch = elapsed_total / done_batches
            eta_sec = avg_per_batch * (n_batches - done_batches)
            saved_mark = f" 💾{len(batch_saves)}" if args.save and batch_saves else ""
            log(f"  ✓ Батч {done_batches}/{n_batches} — {resolved}/{len(batch)} назначено{saved_mark} | "
                f"{elapsed:.1f}с | ETA: {eta_sec/60:.1f} мин")
            log(f"    {progress_bar(done_batches, n_batches)}")

        log(f"\n  Итог LLM:")
        log(f"    Назначено:     {len(llm_updates)}")
        log(f"    Не разрешено:  {len(llm_unresolved)}")
        log(f"\n  Топ-15 семейств (LLM):")
        for fam, cnt in sorted(llm_family_counts.items(), key=lambda x: -x[1])[:15]:
            log(f"    {fam:35s} {cnt:5d}")

        if llm_unresolved:
            log(f"\n  Не разрешено LLM (первые 30):")
            for sid, name, dom in llm_unresolved[:30]:
                log(f"    [{dom or '?':12s}] {name}")
    else:
        if args.skip_llm:
            log("  [skip-llm] Фаза LLM пропущена")
        elif not llm_queue:
            log("\n  Все навыки покрыты правилами — LLM не нужен!")

    # ── Итоговая статистика БД ────────────────────────────────────────────────
    if not args.save:
        conn.rollback()
        log(f"\n{'─'*65}")
        log("[dry-run] Изменения НЕ сохранены. Запустите с --save для применения.")
        log(f"  Правила покроют:  {len(rule_updates)}")
        log(f"  LLM покроет:      {len(llm_updates)}")
        log(f"  Итого:            {len(rule_updates) + len(llm_updates)}")
    else:
        cur.execute("""
            SELECT
                COUNT(*) FILTER (WHERE tech_family IS NOT NULL) AS filled,
                COUNT(*) FILTER (WHERE tech_family IS NULL)     AS empty,
                COUNT(*)                                        AS total
            FROM skill_canonical
        """)
        filled, empty, total = cur.fetchone()
        log(f"\n{'='*65}")
        log(f"ИТОГОВАЯ СТАТИСТИКА БД")
        log(f"  Заполнено:  {filled:6d} ({filled/total*100:.1f}%)")
        log(f"  Пусто:      {empty:6d} ({empty/total*100:.1f}%)")
        log(f"  Всего:      {total:6d}")

        cur.execute("""
            SELECT domain, COUNT(*) AS cnt
            FROM skill_canonical
            WHERE tech_family IS NULL
            GROUP BY domain ORDER BY cnt DESC
            LIMIT 15
        """)
        rows = cur.fetchall()
        if rows:
            log(f"\n  Оставшиеся пустые по доменам:")
            for row in rows:
                log(f"    {str(row[0]):20s} {row[1]:5d}")
        else:
            log("\n  🎉 Все навыки получили семейство!")

    conn.close()
    log(f"\n{'='*65}")
    log("  Готово!")


if __name__ == "__main__":
    main()
