"""
_assign_families.py
-------------------
Фаза 0: version_group IS NOT NULL → tech_family = version_group
Фаза 1: словарь ключевых слов (lowercase + фрагмент / \b word \b для коротких)
         одновременно исправляет domain для навыков из GENERAL

Запуск:
  python _assign_families.py            # dry-run: показывает статистику без записи
  python _assign_families.py --save     # реальная запись в БД

После запуска — проверить покрытие:
  SELECT domain, COUNT(*) FROM skill_canonical
  WHERE tech_family IS NULL GROUP BY domain ORDER BY 2 DESC;
"""

import argparse
import re
import sys
from collections import defaultdict

import psycopg2
from dotenv import load_dotenv
import os

load_dotenv()

DB_URL = os.getenv("DB_URL", "postgresql://postgres:1111@localhost:5432/AcademicSupport")

# ─────────────────────────────────────────────────────────────────────────────
# Словарь правил: (keyword, tech_family_or_None, new_domain_or_None)
#
# Правила применяются в порядке убывания длины keyword — длинные раньше.
# Первое совпавшее правило выигрывает.
# new_domain=None означает «оставить текущий домен».
# tech_family=None означает «не задавать семейство (soft-skills / tools)».
# ─────────────────────────────────────────────────────────────────────────────

RAW_RULES = [
    # ── Java ──────────────────────────────────────────────────────────────
    ("apache maven",       "Java",         "BACKEND"),
    ("spring tool suite",  "Java",         "BACKEND"),
    ("spring boot",        "Java",         "BACKEND"),
    ("spring framework",   "Java",         "BACKEND"),
    ("spring security",    "Java",         "BACKEND"),
    ("spring cloud",       "Java",         "BACKEND"),
    ("spring data",        "Java",         "BACKEND"),
    ("spring mvc",         "Java",         "BACKEND"),
    ("spring batch",       "Java",         "BACKEND"),
    ("spring integration", "Java",         "BACKEND"),
    ("spring webflux",     "Java",         "BACKEND"),
    ("spring",             "Java",         "BACKEND"),
    ("hibernate",          "Java",         "BACKEND"),
    ("java ee",            "Java",         "BACKEND"),
    ("java se",            "Java",         "BACKEND"),
    ("java ee",            "Java",         "BACKEND"),
    ("java fx",            "Java",         "BACKEND"),
    ("javafx",             "Java",         "BACKEND"),
    ("maven",              "Java",         "BACKEND"),
    ("gradle",             "Java",         "BACKEND"),
    ("junit",              "Java",         "BACKEND"),
    ("mockito",            "Java",         "BACKEND"),
    ("testng",             "Java",         "BACKEND"),
    ("log4j",              "Java",         "BACKEND"),
    ("slf4j",              "Java",         "BACKEND"),
    ("logback",            "Java",         "BACKEND"),
    ("rxjava",             "Java",         "BACKEND"),
    ("jvm",                "Java",         "BACKEND"),
    ("jpa",                "Java",         "BACKEND"),
    ("jaxb",               "Java",         "BACKEND"),
    ("jax-rs",             "Java",         "BACKEND"),
    ("jax-ws",             "Java",         "BACKEND"),
    ("tomcat",             "Java",         "BACKEND"),
    ("jetty",              "Java",         "BACKEND"),
    ("quarkus",            "Java",         "BACKEND"),
    ("micronaut",          "Java",         "BACKEND"),
    ("vertx",              "Java",         "BACKEND"),
    ("vertx",              "Java",         "BACKEND"),
    ("dropwizard",         "Java",         "BACKEND"),
    ("camunda",            "Java",         "BACKEND"),
    ("activemq",           "Java",         "BACKEND"),
    ("active mq",          "Message Brokers", "BACKEND"),
    ("apache activemq",    "Message Brokers", "BACKEND"),
    ("highload",           "Архитектура",  "BACKEND"),
    ("высоконагруженн",    "Архитектура",  "BACKEND"),
    ("intellij",           "Java",         "GENERAL"),
    ("eclipse",            "Java",         "GENERAL"),
    ("netbeans",           "Java",         "GENERAL"),
    ("jdk",                "Java",         "GENERAL"),
    ("jre",                "Java",         "GENERAL"),

    # ── Kotlin ───────────────────────────────────────────────────────────
    ("android studio",     "Kotlin",       "MOBILE"),
    ("kotlin",             "Kotlin",       "BACKEND"),
    ("ktor",               "Kotlin",       "BACKEND"),
    ("koin",               "Kotlin",       "BACKEND"),
    ("jetpack compose",    "Kotlin",       "MOBILE"),
    ("jetpack",            "Kotlin",       "MOBILE"),
    ("coroutines",         "Kotlin",       "BACKEND"),
    ("room database",      "Kotlin",       "MOBILE"),

    # ── Scala ────────────────────────────────────────────────────────────
    ("scala",              "Scala",        "BACKEND"),
    ("akka",               "Scala",        "BACKEND"),
    ("play framework",     "Scala",        "BACKEND"),
    ("sbt",                "Scala",        "BACKEND"),
    ("spark",              "Scala",        "DATA_SCIENCE"),  # Apache Spark

    # ── Python ───────────────────────────────────────────────────────────
    ("python",             "Python",       "BACKEND"),
    ("django",             "Python",       "BACKEND"),
    ("fastapi",            "Python",       "BACKEND"),
    ("flask",              "Python",       "BACKEND"),
    ("asyncio",            "Python",       "BACKEND"),
    ("aiohttp",            "Python",       "BACKEND"),
    ("celery",             "Python",       "BACKEND"),
    ("sqlalchemy",         "Python",       "BACKEND"),
    ("alembic",            "Python",       "BACKEND"),
    ("pydantic",           "Python",       "BACKEND"),
    ("uvicorn",            "Python",       "BACKEND"),
    ("gunicorn",           "Python",       "BACKEND"),
    ("tornado",            "Python",       "BACKEND"),
    ("twisted",            "Python",       "BACKEND"),
    ("pytest",             "Python",       "TESTING"),
    ("pandas",             "Python",       "DATA_SCIENCE"),
    ("numpy",              "Python",       "DATA_SCIENCE"),
    ("scikit",             "Python",       "DATA_SCIENCE"),
    ("matplotlib",         "Python",       "DATA_SCIENCE"),
    ("seaborn",            "Python",       "DATA_SCIENCE"),
    ("jupyter",            "Python",       "DATA_SCIENCE"),
    ("anaconda",           "Python",       "DATA_SCIENCE"),
    ("spyder",             "Python",       "DATA_SCIENCE"),
    ("colab",              "Python",       "DATA_SCIENCE"),
    ("pycharm",            "Python",       "GENERAL"),
    ("pip",                "Python",       "GENERAL"),
    ("pipenv",             "Python",       "GENERAL"),
    ("poetry",             "Python",       "GENERAL"),
    ("pypi",               "Python",       "GENERAL"),

    # ── .NET / C# ────────────────────────────────────────────────────────
    ("visual studio code", None,           None),      # универсальный редактор
    ("vscode",             None,           None),
    ("vs code",            None,           None),
    ("visual studio",      ".NET",         "GENERAL"),
    (".net core",          ".NET",         "BACKEND"),
    (".net framework",     ".NET",         "BACKEND"),
    ("asp.net",            ".NET",         "BACKEND"),
    ("entity framework",   ".NET",         "BACKEND"),
    ("blazor",             ".NET",         "FRONTEND"),
    ("xamarin",            ".NET",         "MOBILE"),
    ("maui",               ".NET",         "MOBILE"),
    ("wpf",                ".NET",         "BACKEND"),
    ("winforms",           ".NET",         "BACKEND"),
    ("nunit",              ".NET",         "TESTING"),
    ("xunit",              ".NET",         "TESTING"),
    ("signalr",            ".NET",         "BACKEND"),
    ("nuget",              ".NET",         "GENERAL"),
    ("resharper",          ".NET",         "GENERAL"),
    ("dotcover",           ".NET",         "GENERAL"),
    ("rider",              ".NET",         "GENERAL"),
    (".net",               ".NET",         "BACKEND"),

    # ── JavaScript / TypeScript ───────────────────────────────────────────
    ("typescript",         "JavaScript",   "FRONTEND"),
    ("javascript",         "JavaScript",   "FRONTEND"),
    ("next.js",            "JavaScript",   "FRONTEND"),
    ("nextjs",             "JavaScript",   "FRONTEND"),
    ("nuxt.js",            "JavaScript",   "FRONTEND"),
    ("nuxtjs",             "JavaScript",   "FRONTEND"),
    ("react native",       "JavaScript",   "MOBILE"),
    ("react",              "JavaScript",   "FRONTEND"),
    ("vue.js",             "JavaScript",   "FRONTEND"),
    ("angular",            "JavaScript",   "FRONTEND"),
    ("svelte",             "JavaScript",   "FRONTEND"),
    ("express.js",         "JavaScript",   "BACKEND"),
    ("express",            "JavaScript",   "BACKEND"),
    ("nest.js",            "JavaScript",   "BACKEND"),
    ("nestjs",             "JavaScript",   "BACKEND"),
    ("node.js",            "JavaScript",   "BACKEND"),
    ("node",               "JavaScript",   "BACKEND"),
    ("webpack",            "JavaScript",   "FRONTEND"),
    ("vite",               "JavaScript",   "FRONTEND"),
    ("eslint",             "JavaScript",   "FRONTEND"),
    ("prettier",           "JavaScript",   "FRONTEND"),
    ("babel",              "JavaScript",   "FRONTEND"),
    ("rollup",             "JavaScript",   "FRONTEND"),
    ("redux",              "JavaScript",   "FRONTEND"),
    ("mobx",               "JavaScript",   "FRONTEND"),
    ("storybook",          "JavaScript",   "FRONTEND"),
    ("jest",               "JavaScript",   "TESTING"),
    ("cypress",            "JavaScript",   "TESTING"),
    ("webstorm",           "JavaScript",   "GENERAL"),
    ("npm",                "JavaScript",   "FRONTEND"),
    ("yarn",               "JavaScript",   "FRONTEND"),
    ("pnpm",               "JavaScript",   "FRONTEND"),

    # ── PHP ───────────────────────────────────────────────────────────────
    ("laravel",            "PHP",          "BACKEND"),
    ("symfony",            "PHP",          "BACKEND"),
    ("yii",                "PHP",          "BACKEND"),
    ("codeigniter",        "PHP",          "BACKEND"),
    ("wordpress",          "PHP",          "BACKEND"),
    ("bitrix",             "PHP",          "BACKEND"),
    ("zend",               "PHP",          "BACKEND"),
    ("phpunit",            "PHP",          "TESTING"),
    ("composer",           "PHP",          "GENERAL"),
    ("phpstorm",           "PHP",          "GENERAL"),

    # ── Go ────────────────────────────────────────────────────────────────
    ("golang",             "Go",           "BACKEND"),
    ("gin framework",      "Go",           "BACKEND"),
    ("gin",                "Go",           "BACKEND"),
    ("echo",               "Go",           "BACKEND"),
    ("fiber",              "Go",           "BACKEND"),
    ("grpc",               "Go",           "BACKEND"),
    ("goland",             "Go",           "GENERAL"),

    # ── Ruby ──────────────────────────────────────────────────────────────
    ("ruby on rails",      "Ruby",         "BACKEND"),
    ("rails",              "Ruby",         "BACKEND"),
    ("sinatra",            "Ruby",         "BACKEND"),
    ("rubygems",           "Ruby",         "GENERAL"),
    ("bundler",            "Ruby",         "GENERAL"),

    # ── Rust ──────────────────────────────────────────────────────────────
    ("rust",               "Rust",         "BACKEND"),
    ("cargo",              "Rust",         "GENERAL"),
    ("actix",              "Rust",         "BACKEND"),
    ("tokio",              "Rust",         "BACKEND"),

    # ── C/C++ ─────────────────────────────────────────────────────────────
    ("c/c++",              "C/C++",        "SYSTEMS"),
    ("clion",              "C/C++",        "SYSTEMS"),
    ("cmake",              "C/C++",        "SYSTEMS"),
    ("conan",              "C/C++",        "SYSTEMS"),
    ("qt framework",       "C/C++",        "SYSTEMS"),

    # ── Swift / iOS ───────────────────────────────────────────────────────
    ("swift",              "Swift",        "MOBILE"),
    ("swiftui",            "Swift",        "MOBILE"),
    ("objective-c",        "Swift",        "MOBILE"),
    ("objc",               "Swift",        "MOBILE"),
    ("xcode",              "Swift",        "MOBILE"),
    ("appcode",            "Swift",        "MOBILE"),
    ("cocoapods",          "Swift",        "MOBILE"),

    # ── Dart / Flutter ───────────────────────────────────────────────────
    ("flutter",            "Dart",         "MOBILE"),
    ("dart",               "Dart",         "MOBILE"),

    # ── DevOps: Контейнеры ─────────────────────────────────────────────────
    ("kubernetes",         "Контейнеры",   "DEVOPS"),
    ("docker compose",     "Контейнеры",   "DEVOPS"),
    ("docker swarm",       "Контейнеры",   "DEVOPS"),
    ("docker",             "Контейнеры",   "DEVOPS"),
    ("helm",               "Контейнеры",   "DEVOPS"),
    ("podman",             "Контейнеры",   "DEVOPS"),
    ("containerd",         "Контейнеры",   "DEVOPS"),
    ("openshift",          "Контейнеры",   "DEVOPS"),

    # ── DevOps: CI/CD ──────────────────────────────────────────────────────
    ("github actions",     "CI/CD",        "DEVOPS"),
    ("gitlab ci",          "CI/CD",        "DEVOPS"),
    ("gitlab",             "CI/CD",        "DEVOPS"),
    ("jenkins",            "CI/CD",        "DEVOPS"),
    ("teamcity",           "CI/CD",        "DEVOPS"),
    ("circleci",           "CI/CD",        "DEVOPS"),
    ("travis",             "CI/CD",        "DEVOPS"),
    ("argocd",             "CI/CD",        "DEVOPS"),
    ("flux",               "CI/CD",        "DEVOPS"),

    # ── DevOps: IaC ────────────────────────────────────────────────────────
    ("terraform",          "IaC",          "DEVOPS"),
    ("ansible",            "IaC",          "DEVOPS"),
    ("pulumi",             "IaC",          "DEVOPS"),
    ("chef",               "IaC",          "DEVOPS"),
    ("puppet",             "IaC",          "DEVOPS"),
    ("saltstack",          "IaC",          "DEVOPS"),
    ("vagrant",            "IaC",          "DEVOPS"),

    # ── DevOps: Мониторинг ─────────────────────────────────────────────────
    ("prometheus",         "Мониторинг",   "DEVOPS"),
    ("grafana",            "Мониторинг",   "DEVOPS"),
    ("kibana",             "Мониторинг",   "DEVOPS"),
    ("logstash",           "Мониторинг",   "DEVOPS"),
    ("elasticsearch",      "Мониторинг",   "DEVOPS"),  # дополнительно как DB
    ("zabbix",             "Мониторинг",   "DEVOPS"),
    ("datadog",            "Мониторинг",   "DEVOPS"),
    ("jaeger",             "Мониторинг",   "DEVOPS"),
    ("opentelemetry",      "Мониторинг",   "DEVOPS"),
    ("fluentd",            "Мониторинг",   "DEVOPS"),

    # ── DevOps: Cloud ─────────────────────────────────────────────────────
    ("yandex cloud",       "Cloud",        "DEVOPS"),
    ("amazon web services","Cloud",        "DEVOPS"),
    ("google cloud",       "Cloud",        "DEVOPS"),
    ("azure",              "Cloud",        "DEVOPS"),
    ("aws",                "Cloud",        "DEVOPS"),
    ("gcp",                "Cloud",        "DEVOPS"),
    ("cloudflare",         "Cloud",        "DEVOPS"),
    ("digitalocean",       "Cloud",        "DEVOPS"),

    # ── DevOps: Системное ПО ──────────────────────────────────────────────
    ("nginx",              "Системное ПО", "DEVOPS"),
    ("apache httpd",       "Системное ПО", "DEVOPS"),
    ("haproxy",            "Системное ПО", "DEVOPS"),
    ("linux",              "Системное ПО", "SYSTEMS"),
    ("ubuntu",             "Системное ПО", "SYSTEMS"),
    ("centos",             "Системное ПО", "SYSTEMS"),
    ("debian",             "Системное ПО", "SYSTEMS"),
    ("bash",               "Системное ПО", "SYSTEMS"),
    ("shell",              "Системное ПО", "SYSTEMS"),
    ("powershell",         "Системное ПО", "SYSTEMS"),

    # ── DevOps: Git / VCS ─────────────────────────────────────────────────
    ("git flow",           "VCS",          "DEVOPS"),
    ("gitflow",            "VCS",          "DEVOPS"),
    ("git",                "VCS",          "DEVOPS"),
    ("svn",                "VCS",          "DEVOPS"),
    ("mercurial",          "VCS",          "DEVOPS"),

    # ── MQ / Брокеры сообщений ────────────────────────────────────────────
    ("apache kafka",       "Message Brokers", "BACKEND"),
    ("kafka",              "Message Brokers", "BACKEND"),
    ("rabbitmq",           "Message Brokers", "BACKEND"),
    ("redis streams",      "Message Brokers", "BACKEND"),
    ("nats",               "Message Brokers", "BACKEND"),
    ("pulsar",             "Message Brokers", "BACKEND"),

    # ── Database: Реляционные ────────────────────────────────────────────
    ("postgresql",         "Реляционные",  "DATABASE"),
    ("mysql",              "Реляционные",  "DATABASE"),
    ("microsoft sql",      "Реляционные",  "DATABASE"),
    ("mssql",              "Реляционные",  "DATABASE"),
    ("sql server",         "Реляционные",  "DATABASE"),
    ("mariadb",            "Реляционные",  "DATABASE"),
    ("sqlite",             "Реляционные",  "DATABASE"),
    ("oracle db",          "Реляционные",  "DATABASE"),
    ("oracle",             "Реляционные",  "DATABASE"),
    ("firebird",           "Реляционные",  "DATABASE"),
    ("datagrip",           "Реляционные",  "DATABASE"),

    # ── Database: NoSQL ──────────────────────────────────────────────────
    ("mongodb",            "NoSQL",        "DATABASE"),
    ("cassandra",          "NoSQL",        "DATABASE"),
    ("hbase",              "NoSQL",        "DATABASE"),
    ("couchdb",            "NoSQL",        "DATABASE"),
    ("couchbase",          "NoSQL",        "DATABASE"),
    ("neo4j",              "NoSQL",        "DATABASE"),
    ("dynamodb",           "NoSQL",        "DATABASE"),

    # ── Database: In-Memory ──────────────────────────────────────────────
    ("redis",              "In-Memory",    "DATABASE"),
    ("memcached",          "In-Memory",    "DATABASE"),
    ("hazelcast",          "In-Memory",    "DATABASE"),
    ("ignite",             "In-Memory",    "DATABASE"),

    # ── Database: Аналитика / NewSQL ──────────────────────────────────────
    ("clickhouse",         "Аналитика",    "DATABASE"),
    ("greenplum",          "Аналитика",    "DATABASE"),
    ("vertica",            "Аналитика",    "DATABASE"),
    ("bigquery",           "Аналитика",    "DATABASE"),
    ("redshift",           "Аналитика",    "DATABASE"),
    ("snowflake",          "Аналитика",    "DATABASE"),
    ("dbt",                "Аналитика",    "DATABASE"),

    # ── Data Science / ML ─────────────────────────────────────────────────
    ("tensorflow",         "ML/AI",        "AI_ML"),
    ("pytorch",            "ML/AI",        "AI_ML"),
    ("keras",              "ML/AI",        "AI_ML"),
    ("hugging face",       "ML/AI",        "AI_ML"),
    ("transformers",       "ML/AI",        "AI_ML"),
    ("langchain",          "ML/AI",        "AI_ML"),
    ("openai",             "ML/AI",        "AI_ML"),
    ("llm",                "ML/AI",        "AI_ML"),
    ("catboost",           "ML/AI",        "AI_ML"),
    ("xgboost",            "ML/AI",        "AI_ML"),
    ("lightgbm",           "ML/AI",        "AI_ML"),
    ("airflow",            "ML/AI",        "DATA_SCIENCE"),
    ("luigi",              "ML/AI",        "DATA_SCIENCE"),
    ("mlflow",             "ML/AI",        "DATA_SCIENCE"),
    ("hadoop",             "Big Data",     "DATA_SCIENCE"),
    ("hive",               "Big Data",     "DATA_SCIENCE"),
    ("tableau",            "Визуализация", "DATA_SCIENCE"),
    ("power bi",           "Визуализация", "DATA_SCIENCE"),
    ("superset",           "Визуализация", "DATA_SCIENCE"),

    # ── Методологии ──────────────────────────────────────────────────────
    ("agile",              "Методологии",  "GENERAL"),
    ("scrum",              "Методологии",  "GENERAL"),
    ("kanban",             "Методологии",  "GENERAL"),
    ("waterfall",          "Методологии",  "GENERAL"),
    ("lean",               "Методологии",  "GENERAL"),
    ("devops culture",     "Методологии",  "GENERAL"),
    ("devops",             "Методологии",  "GENERAL"),

    # ── Инструменты PM / командной работы ────────────────────────────────
    ("jira",               "PM Tools",     "GENERAL"),
    ("confluence",         "PM Tools",     "GENERAL"),
    ("trello",             "PM Tools",     "GENERAL"),
    ("asana",              "PM Tools",     "GENERAL"),
    ("miro",               "PM Tools",     "GENERAL"),
    ("figma",              "Дизайн",       "FRONTEND"),
    ("sketch",             "Дизайн",       "FRONTEND"),

    # ── Принципы разработки ──────────────────────────────────────────────
    ("solid",              "Принципы",     "GENERAL"),
    ("dry",                "Принципы",     "GENERAL"),
    ("kiss",               "Принципы",     "GENERAL"),
    ("yagni",              "Принципы",     "GENERAL"),
    ("grasp",              "Принципы",     "GENERAL"),
    ("ооп",                "Принципы",     "GENERAL"),
    ("oop",                "Принципы",     "GENERAL"),

    # ── Архитектурные паттерны ────────────────────────────────────────────
    ("design pattern",     "Архитектура",  "GENERAL"),
    ("паттерн проектирования", "Архитектура", "GENERAL"),
    ("шаблон проектирования",  "Архитектура", "GENERAL"),
    ("clean architecture", "Архитектура",  "GENERAL"),
    ("clean code",         "Архитектура",  "GENERAL"),
    ("microservice",       "Архитектура",  "BACKEND"),
    ("микросервис",        "Архитектура",  "BACKEND"),
    ("event-driven",       "Архитектура",  "BACKEND"),
    ("event driven",       "Архитектура",  "BACKEND"),
    ("high load",          "Архитектура",  "BACKEND"),
    ("высоконагруженн",    "Архитектура",  "BACKEND"),

    # ── Моделирование / документирование ─────────────────────────────────
    ("uml",                "Моделирование","GENERAL"),
    ("bpmn",               "Моделирование","GENERAL"),
    ("swagger",            "Документирование", "BACKEND"),
    ("openapi",            "Документирование", "BACKEND"),

    # ── Тестирование ─────────────────────────────────────────────────────
    ("selenium",           "Автотестирование", "TESTING"),
    ("playwright",         "Автотестирование", "TESTING"),
    ("appium",             "Автотестирование", "TESTING"),
    ("testng",             "Автотестирование", "TESTING"),
    ("allure",             "Автотестирование", "TESTING"),
    ("testcafe",           "Автотестирование", "TESTING"),
    ("testrail",           "Ручное тестирование", "TESTING"),
    ("bugzilla",           "Ручное тестирование", "TESTING"),
    ("soapui",             "API Testing",  "TESTING"),
    ("postman",            "API Testing",  "TESTING"),
    ("insomnia",           "API Testing",  "TESTING"),
    ("jmeter",             "Нагрузочное",  "TESTING"),
    ("gatling",            "Нагрузочное",  "TESTING"),
    ("locust",             "Нагрузочное",  "TESTING"),

    # ── Soft skills ──────────────────────────────────────────────────────
    ("английский",         None,           "GENERAL"),
    ("немецкий",           None,           "GENERAL"),
    ("русский язык",       None,           "GENERAL"),
    ("китайский",          None,           "GENERAL"),
    ("работа в команде",   None,           "GENERAL"),
    ("командная работа",   None,           "GENERAL"),
    ("управление командой",None,           "GENERAL"),
    ("обучение и развитие",None,           "GENERAL"),
    ("аналитическое мышление", None,       "GENERAL"),
    ("коммуникация",       None,           "GENERAL"),

    # ── 1С ───────────────────────────────────────────────────────────────
    ("1с-битрикс",         "1С",           "1C"),
    ("1c-bitrix",          "1С",           "1C"),
    ("bitrix24",           "1С",           "1C"),
    ("1с:",                "1С",           "1C"),
    ("1с ",                "1С",           "1C"),
]

# ─────────────────────────────────────────────────────────────────────────────
# Специальные короткие ключевые слова (≤ 3 символа после strip) — word-boundary
# Обрабатываются отдельно через \b...\b
# ─────────────────────────────────────────────────────────────────────────────
SHORT_RULES = [
    # (keyword, tech_family, new_domain) — \bword\b matching (word boundary)
    # Используем для слов, которые являются подстроками других технологий:
    # java → javascript, ruby → ruby on rails, lean → clean, node → electrode
    ("java",   "Java",         "BACKEND"),     # \bjava\b не совпадёт с "javascript"
    ("ruby",   "Ruby",         "BACKEND"),
    ("lean",   "Методологии",  "GENERAL"),   # \blean\b не совпадёт с "clean"
    ("node",   "JavaScript",  "BACKEND"),     # \bnode\b
    ("go",     "Go",           "BACKEND"),
    ("php",    "PHP",          "BACKEND"),
    ("c#",     ".NET",         "BACKEND"),
    ("r",      "R",            "DATA_SCIENCE"),
    ("sql",    "Реляционные", "DATABASE"),
]


def build_rules():
    """Возвращает список правил, отсортированных по убыванию длины ключевого слова."""
    rules = [(kw.lower().strip(), fam, dom) for kw, fam, dom in RAW_RULES]
    rules.sort(key=lambda x: -len(x[0]))
    return rules


def matches_short(name_lc: str, kw_lc: str) -> bool:
    return bool(re.search(r'\b' + re.escape(kw_lc) + r'\b', name_lc))


def apply_rules(name: str, rules, short_rules):
    """Вернуть (tech_family, new_domain) или (sentinel, None) если нет совпадения."""
    name_lc = name.lower().strip()

    for kw, fam, dom in rules:
        if kw in name_lc:
            return fam, dom

    for kw, fam, dom in short_rules:
        if matches_short(name_lc, kw):
            return fam, dom

    return "___NO_MATCH___", None


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--save", action="store_true", help="Записать изменения в БД")
    parser.add_argument("--domain", help="Обработать только указанный домен (debug)")
    parser.add_argument("--limit", type=int, help="Ограничить количество навыков (debug)")
    args = parser.parse_args()

    rules = build_rules()
    short_rules = [(kw.lower().strip(), fam, dom) for kw, fam, dom in SHORT_RULES]

    conn = psycopg2.connect(DB_URL)
    conn.autocommit = False
    cur = conn.cursor()

    # ── Фаза 0: version_group IS NOT NULL → tech_family = version_group ──
    print("=== Фаза 0: version_group → tech_family ===")
    cur.execute("""
        UPDATE skill_canonical
        SET tech_family = version_group
        WHERE version_group IS NOT NULL AND (tech_family IS NULL OR tech_family = '')
    """)
    phase0_count = cur.rowcount
    print(f"  Обновлено: {phase0_count} записей")

    # ── Фаза 1: словарь ────────────────────────────────────────────────────
    print("\n=== Фаза 1: словарь (lowercase + фрагмент) ===")
    domain_filter = f"AND domain = '{args.domain}'" if args.domain else ""
    limit_clause  = f"LIMIT {args.limit}" if args.limit else ""

    cur.execute(f"""
        SELECT id, name, domain
        FROM skill_canonical
        WHERE tech_family IS NULL
        {domain_filter}
        ORDER BY id
        {limit_clause}
    """)
    rows = cur.fetchall()
    print(f"  Навыков без tech_family: {len(rows)}")

    updates = []
    domain_fix_count = 0
    family_counts = defaultdict(int)
    unmatched_examples = []

    for skill_id, name, current_domain in rows:
        fam, new_dom = apply_rules(name, rules, short_rules)

        if fam == "___NO_MATCH___":
            if len(unmatched_examples) < 20:
                unmatched_examples.append((skill_id, name, current_domain))
            continue

        effective_domain = new_dom if new_dom else current_domain
        domain_changed = (new_dom is not None and new_dom != current_domain)
        if domain_changed:
            domain_fix_count += 1

        family_counts[fam or "null"] += 1
        updates.append((fam if fam else None, effective_domain, skill_id))

    print(f"\n  Совпало: {len(updates)} навыков")
    print(f"  Из них исправлен домен: {domain_fix_count}")
    print(f"  Нет совпадения: {len(rows) - len(updates)}")

    print("\n  Топ-20 семейств:")
    for fam, cnt in sorted(family_counts.items(), key=lambda x: -x[1])[:20]:
        print(f"    {fam:30s} {cnt:5d}")

    if unmatched_examples:
        print("\n  Примеры без совпадения (первые 20):")
        for sid, nm, dom in unmatched_examples:
            print(f"    [{dom}] {nm}")

    if args.save and updates:
        print(f"\n  Запись {len(updates)} обновлений...")
        cur.executemany("""
            UPDATE skill_canonical
            SET tech_family = %s, domain = %s
            WHERE id = %s
        """, updates)
        conn.commit()
        print("  ✓ Сохранено")
    elif not args.save:
        conn.rollback()
        print("\n  [dry-run] Изменения НЕ сохранены. Запустите с --save")
    else:
        conn.commit()
        print("  Нечего сохранять")

    # ── Итоговая статистика ────────────────────────────────────────────────
    cur.execute("""
        SELECT
            COUNT(*) FILTER (WHERE tech_family IS NOT NULL) AS filled,
            COUNT(*) FILTER (WHERE tech_family IS NULL)     AS empty,
            COUNT(*)                                         AS total
        FROM skill_canonical
    """)
    filled, empty, total = cur.fetchone()
    print(f"\n=== Итог (текущее состояние БД) ===")
    print(f"  Заполнено:  {filled:6d} ({filled/total*100:.1f}%)")
    print(f"  Пусто:      {empty:6d} ({empty/total*100:.1f}%)")
    print(f"  Всего:      {total:6d}")

    cur.execute("""
        SELECT domain, COUNT(*) AS cnt
        FROM skill_canonical
        WHERE tech_family IS NULL
        GROUP BY domain ORDER BY cnt DESC
    """)
    print("\n  Пустые по доменам (после скрипта):")
    for row in cur.fetchall():
        print(f"    {str(row[0]):20s} {row[1]:5d}")

    conn.close()


if __name__ == "__main__":
    main()
