# DST-слой: Атомизация и аналитика навыков — отчёт о проделанной работе

> Дата: 25–26 июля 2026  
> Статус: **Этап 1 завершён** — данные в БД, аналитика готова

---

## Что было до

| Таблица | Состояние |
|---|---|
| `work_skill` | 25 503 строк с сырыми описаниями навыков (дубли, аббревиатуры, пробелы) |
| `skill_canonical` | пустая |
| `work_skill_canonical` | пустая |
| `skill_dependency` | пустая |
| `vacancy_domain` | пустая |
| `skill_domain_stats` | пустая |

Навыки в `work_skill` хранились как есть из HH.ru: `"Python 3+, pandas, sklearn"`, `"*nix-системы"`, `"чтение-логов"`, `"1С ЗУП"` и т.д.

---

## Что сделано

### 1. Атомизация и нормализация навыков (`test_atomize.py`)

Скрипт отправляет батчи `work_skill.description` в **GigaChat-Max** с промптом, который:
- **SPLIT** — разбивает составные записи: `"React, Vue, Angular"` → `["React", "Vue", "Angular"]`
- **NORM** — нормализует: `"*nix системы"` → `"Linux"`, `"airflow dag"` → `"Airflow DAG"`
- **SAME** — оставляет как есть: `"Docker"` → `"Docker"`
- **NOT_SKILL** — отбрасывает нерелевантное: `"чувство юмора"`, `"опыт от 3 лет"`

Результат сохраняется в:
- `skill_canonical(id, name, normalized_name, domain, domain_source, version_group)`
- `work_skill_canonical(work_skill_id, canonical_id)`

**Ключевые технические решения:**

| Проблема | Решение |
|---|---|
| Алфавитный порядок → однородные батчи → сдвиг позиций | `--shuffle` (авто-включён для `--all-skills`) |
| Модель вернула N-1 ответов → весь батч сдвинулся | Retry при `len(outputs) != len(inputs)` |
| 429 Rate Limit | Экспоненциальный backoff: 5→15→30→60→120с |
| Потеря прогресса при прерывании | Per-batch commit после каждого батча |
| `NOT_SKILL` в поле домена → FK violation | Валидация через `_VALID_DOMAINS` frozenset |
| Зависшая транзакция после ошибки | `conn.rollback()` в except-блоке |

**Параметры запуска:**
```bash
python test_atomize.py --all-skills --limit 21000 --batch-size 10 \
    --model GigaChat-Max --skip-done --save --delay 1
```

**Результат (2 прохода):**

| Метрика | Значение |
|---|---|
| `skill_canonical` | **17 107** уникальных навыков |
| `work_skill_canonical` | **34 558** связей |
| Покрытие | **97%** (24 689 / 25 502 уникальных descriptions) |
| Оставшиеся 813 (3%) | Подтверждённый NOT_SKILL: soft skills, мусор, кривая кодировка |

---

### 2. Разметка доменов (`fix_domains.py`)

После атомизации 1 160 `skill_canonical` записей имели `domain = NULL` — GigaChat возвращал невалидные коды (например `NOT_SKILL` в поле домена).

Скрипт отправляет имена навыков в GigaChat с упрощённым промптом: «Классифицируй в один из 14 доменов или null».

```bash
python fix_domains.py --batch-size 15 --model GigaChat-Max --delay 2
```

**Результат:**
- 215 навыков получили домен
- 945 остались `NULL` — подтверждены как не-IT (правильно)

---

### 3. Аналитические таблицы (`compute_analytics.py`)

Все три таблицы заполнены одним скриптом через чистый SQL:

```bash
python compute_analytics.py --min-cooc 2
```

#### `skill_dependency` — co-occurrence пар навыков

Для каждой пары навыков (A, B) считает сколько вакансий содержат оба.  
Алгоритм: self-join `vacancy_canonical` (CTE из `vacancy_skills → work_skill_canonical`).

**144 471 пара**, топ-5:

| Пара | Вакансий |
|---|---|
| Python + SQL | 2 196 |
| Python + PostgreSQL | 1 581 |
| PostgreSQL + Docker | 1 576 |
| Python + Docker | 1 337 |
| Kubernetes + Docker | 1 260 |

#### `vacancy_domain` — основной домен вакансии

Для каждой вакансии определяет домен по большинству навыков (rank по count).

**13 417 вакансий**, распределение:

| Домен | Вакансий | Ср. уверенность |
|---|---|---|
| GENERAL | 4 189 | 54% |
| FRONTEND | 2 132 | 65% |
| DEVOPS | 1 507 | 44% |
| DATABASE | 1 285 | 42% |
| BACKEND | 1 095 | 38% |
| DATA_SCIENCE | 1 080 | 50% |
| TESTING | 890 | 50% |

#### `skill_domain_stats` — статистика навыка

Для каждого навыка: `vacancy_count`, `pct_in_domain`, `top_cooccurrences` (JSONB top-10).

**15 875 записей**. Пример Python:
```json
top_cooccurrences: [
  {"name": "SQL",        "count": 2196},
  {"name": "PostgreSQL", "count": 1581},
  {"name": "Docker",     "count": 1337},
  {"name": "Git",        "count": 1091},
  {"name": "FastAPI",    "count": 703}
]
```

---

## Файлы в `skill-atomize-test/`

| Файл | Назначение |
|---|---|
| `test_atomize.py` | Основной скрипт атомизации |
| `prompt.txt` | Системный промпт для GigaChat |
| `fix_domains.py` | Дозаполнение доменов для null-записей |
| `compute_analytics.py` | Расчёт skill_dependency, vacancy_domain, skill_domain_stats |
| `_check_db.py` | Статистика покрытия БД |
| `_quality_check.py` | Выборочная проверка качества данных |
| `_analytics_preview.py` | Просмотр co-occurrence и domain stats |
| `_show_remaining.py` | Вывод необработанных work_skill |
| `_schema_check.py` | Структура аналитических таблиц |
| `_list_tables.py` | Список таблиц в БД |
| `_cleanup_and_restart.py` | Очистка llm-записей (использовался при перезапуске) |

---

## Схема данных (цепочка)

```
vacancy
  └─► vacancy_skills (140 404 строки)
          vacancy_entity_id → vacancy.id
          skills_id         → work_skill.id
               └─► work_skill_canonical
                       work_skill_id → work_skill.id
                       canonical_id  → skill_canonical.id
                                           ├─ name
                                           ├─ normalized_name
                                           ├─ domain          (14 доменов)
                                           ├─ domain_source   ('llm')
                                           └─ version_group

skill_dependency
  parent_id → skill_canonical.id
  child_id  → skill_canonical.id
  co_occurrence_cnt

vacancy_domain
  vacancy_id    → vacancy.id
  primary_domain
  domain_score

skill_domain_stats
  canonical_id       → skill_canonical.id
  domain, vacancy_count, pct_in_domain
  top_cooccurrences  (JSONB)
```

---

## Следующий шаг — Java-интеграция

Данные готовы, нужно подключить к Spring Boot:

1. **JPA-сущности**: `SkillCanonical`, `WorkSkillCanonical`, `SkillDependency`, `VacancyDomain`, `SkillDomainStats`
2. **REST API**:
   - `GET /api/skills/canonical?domain=BACKEND` — список канонических навыков
   - `GET /api/skills/{id}/related` — связанные навыки из `skill_dependency`
   - `GET /api/vacancies/{id}/domain` — домен вакансии
3. **Использование в существующем `SkillNormalizationService`** — вместо вызова GigaChat на каждый навык брать из `skill_canonical` по `normalized_name`
