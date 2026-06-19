# Целевая архитектура системы поддержки разработки ОП Топ-ИТ

> **Статус:** концептуальный документ — описывает целевое разбиение на микросервисы
> и потоки данных между ними.
> Смежные файлы: `ALGORITHM_IDEAL.md` (DST-алгоритм), `ROADMAP_CONSTRUCTOR.md` (конструктор роадмапа).

---

## Обзор: два микросервиса и слой маппинга

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        ВНЕШНИЕ ИСТОЧНИКИ ДАННЫХ                         │
│   HH.ru API  │  Habr Career  │  GitHub Trends  │  StackOverflow Survey  │
└──────────────────────────────┬──────────────────────────────────────────┘
                               │
              ┌────────────────▼──────────────────┐
              │  MIS — Market Intelligence Service  │
              │  (Микросервис 1: рынок труда)        │
              │                                     │
              │  • Парсинг и хранение вакансий      │
              │  • Агрегация навыков и частот        │
              │  • domain_markers: технология→домен  │
              │  • DST-расчёт: κ, BPA, BetP          │
              │  • База: JAcademicSupport DB         │
              └────────────────┬──────────────────--┘
                               │
                      REST API / gRPC
                   (BetP, m(T/U/F), Δ по кластерам)
                               │
              ┌────────────────▼──────────────────┐
              │  MAPPING LAYER                     │
              │  КРМ-компетенция → рыночный кластер │
              │  (Python/AI, Python/Backend, ...)    │
              └────────────────┬──────────────────--┘
                               │
              ┌────────────────▼──────────────────┐
              │  TAS — Top-IT Academic Service     │
              │  (Микросервис 2: учебные программы) │
              │                                     │
              │  • Хранение КРМ (роли, компетенции, │
              │    индикаторы, примеры технологий)   │
              │  • Хранение ЦКМ и учебных планов    │
              │  • Граф обучения (индикаторы как     │
              │    узлы, семестры как слои)          │
              │  • Конструктор роадмапа              │
              │  • Pre-validation РПД               │
              │  • Gap analysis                     │
              └────────────────┬──────────────────--┘
                               │
              ┌────────────────▼──────────────────┐
              │  UI: Конструктор роадмапа           │
              │  (см. ROADMAP_CONSTRUCTOR.md)       │
              └───────────────────────────────────--┘
```

---

## Микросервис 1: Market Intelligence Service (MIS)

### Назначение

Единственный источник правды о рынке труда. Предоставляет другим сервисам
агрегированные, статистически обоснованные оценки важности технологий и ролей.
Не знает ничего о КРМ, компетенциях, Топ-ИТ — это чисто рыночный сервис.

### Что хранит

На базе существующих таблиц JAcademicSupport:

```
vacancy          — вакансии с метаданными (дата, зарплата, компания, город)
vacancy_skill    — навыки из вакансии (с позицией: required/preferred)
work_skill       — справочник навыков (name, skill_type: language/framework/tool)
skills_group     — кластеры навыков (technology + domain: Python/Backend)
employer         — работодатели-эксперты (индустриальные партнёры)
employer_skill_group — оценки работодателей по кластерам
forecast         — прогнозы (нужно добавить: direction, confidence)

-- Новые таблицы --
domain_markers   — сигнальные навыки, указывающие домен кластера
technology_ontology — граф зависимостей (Flask → Python)
dst_results      — история DST-расчётов с параметрами
job_title_mapping — маппинг ролей КРМ на реальные тайтлы вакансий
```

### Таблица job_title_mapping (ключевая для связи с TAS)

```sql
CREATE TABLE job_title_mapping (
    id            SERIAL PRIMARY KEY,
    krm_role_code VARCHAR(50),        -- 'engineer-programmer' (из КРМ)
    job_title     VARCHAR(200),       -- 'Backend Developer'
    weight        DECIMAL(3,2),       -- 1.0 = точное совпадение; 0.6 = частичное
    source        VARCHAR(30)         -- 'manual', 'nlp_clustering'
);
```

### API, который предоставляет MIS

```
GET /api/v1/roles/{krm_role_code}/betp
  → BetP(роль), m(T), m(U), K_conflict
  → Используется: TAS, Шаг 2 (выбор ролей)

GET /api/v1/clusters/{domain_cluster}/betp
  → BetP(кластер), Δ относительно supply, история тренда
  → Используется: TAS, Шаг 3 (ЦКМ)

GET /api/v1/skills/{skill_name}/relevance
  → BetP(навык), m(F) (признак устаревания), топ-3 замены
  → Используется: TAS, конструктор роадмапа (подсказки)

GET /api/v1/diff/krm?from_version=2024&to_version=2025
  → Список компетенций с изменёнными технологиями → FC-источник DST
  → Используется: TAS, актуализация РПД
```

### DST внутри MIS

MIS — основное место запуска DST-алгоритма. Схема расчёта остаётся как в
`ALGORITHM_CURRENT.md` / `ALGORITHM_IDEAL.md`, только данные берутся из БД:

```
Level 0: вакансии группируются по job_title_mapping → BetP(кРМ-роль)
Level 1: вакансии по кластеру → BetP(кластер) → Δ
Level 2: частота навыка в вакансиях кластера → BetP(навык), m(F)
```

---

## Микросервис 2: Top-IT Academic Service (TAS)

### Назначение

Хранит нормативную структуру Топ-ИТ (КРМ, ЦКМ, учебные планы, РПД) и предоставляет
инструменты для работы преподавателя: конструктор роадмапа, gap analysis, pre-validation.
Запрашивает рыночные данные из MIS — сам не знает о вакансиях напрямую.

### Что хранит (новые таблицы)

```sql
-- Роли из КРМ
CREATE TABLE krm_role (
    id            SERIAL PRIMARY KEY,
    code          VARCHAR(50) UNIQUE,       -- 'engineer-programmer'
    name          VARCHAR(200),             -- 'Инженер-программист'
    role_group    INT,                      -- 1=основная, 2=опережающая, 3=дополнительная
    krm_version   VARCHAR(20),             -- '2025'
    updated_at    TIMESTAMP
);

-- Компетенции КРМ
CREATE TABLE krm_competency (
    id            SERIAL PRIMARY KEY,
    code          VARCHAR(20) UNIQUE,       -- 'ПК-1'
    comp_type     VARCHAR(20),             -- 'ПК','ПРПК','ПРОК','УНК'
    level_type    VARCHAR(20),             -- 'basic','top','cross','specialized'
    description   TEXT,
    role_id       INT REFERENCES krm_role(id),
    krm_version   VARCHAR(20)
);

-- Индикаторы компетенций (узлы графа обучения)
CREATE TABLE krm_indicator (
    id            SERIAL PRIMARY KEY,
    code          VARCHAR(20) UNIQUE,       -- 'ИД-1.2'
    competency_id INT REFERENCES krm_competency(id),
    level         VARCHAR(20),             -- 'basic','medium','advanced'
    description   TEXT,
    example_technologies TEXT[]            -- ['FastAPI','Spring Boot','JWT']
);

-- Маппинг: индикатор → рыночный кластер MIS
CREATE TABLE indicator_cluster_mapping (
    indicator_id  INT REFERENCES krm_indicator(id),
    cluster_key   VARCHAR(100),            -- 'Python/Backend' (ключ в MIS)
    confidence    DECIMAL(3,2),            -- уверенность маппинга
    mapped_by     VARCHAR(20)             -- 'auto_nlp', 'manual'
);

-- Целевая компетентностная модель выпускника (ЦКМ)
CREATE TABLE target_competency_model (
    id            SERIAL PRIMARY KEY,
    program_id    INT,
    competency_id INT REFERENCES krm_competency(id),
    included      BOOLEAN,
    inclusion_reason TEXT,                 -- 'required'/'partner'/'market'
    created_at    TIMESTAMP
);

-- Учебный план: дисциплины
CREATE TABLE discipline (
    id            SERIAL PRIMARY KEY,
    program_id    INT,
    name          VARCHAR(200),
    semester      INT,
    hours_total   INT,
    hours_lecture INT,
    hours_practice INT
);

-- Граф обучения: назначение индикаторов дисциплинам
CREATE TABLE indicator_assignment (
    id              SERIAL PRIMARY KEY,
    indicator_id    INT REFERENCES krm_indicator(id),
    discipline_id   INT REFERENCES discipline(id),
    hours_allocated INT,
    technologies    TEXT[]                -- конкретные технологии в этой дисциплине
);

-- Граф обучения: рёбра (prereq / co-req)
CREATE TABLE learning_graph_edge (
    id            SERIAL PRIMARY KEY,
    from_indicator INT REFERENCES krm_indicator(id),
    to_indicator   INT REFERENCES krm_indicator(id),
    edge_type      VARCHAR(20),           -- 'prerequisite','co-requisite'
    auto_generated BOOLEAN               -- true = из уровней КРМ, false = ручное
);
```

### API, который предоставляет TAS

```
POST /api/v1/roadmap/validate
  → Запускает gap analysis: uncovered, broken prerequisites, market deficit
  → Тело: program_id

GET /api/v1/roadmap/{program_id}/gaps
  → Список пробелов трёх типов с приоритетами (из DST Δ)

GET /api/v1/competencies/check-proportions
  → Доля ПК топ-уровня, соответствие нормативам Топ-ИТ

POST /api/v1/technologies/map
  → Принимает: список технологий из дисциплины
  → Возвращает: маппинг на индикаторы + BetP из MIS (через mapping layer)
```

---

## Слой маппинга: КРМ-компетенция → рыночный кластер

Слой маппинга — не отдельный сервис, а **компонент TAS** (может быть выделен в lib/module).

### Задача

Перевести абстрактное описание компетенции КРМ в конкретный контекстный кластер MIS,
чтобы корректно запросить DST-данные.

```
ПК-1 "разработка серверных приложений" + примеры [FastAPI, Spring Boot]
  → сигналы из domain_markers: FastAPI→Backend, Spring Boot→Backend
  → кластер MIS: "Python/Backend" или "Java/Backend"
  → запрос MIS: GET /clusters/Python-Backend/betp
```

### Алгоритм маппинга

```
1. Взять example_technologies из krm_indicator
2. Для каждой технологии найти домен из domain_markers (запрос в MIS)
3. Голосование: какой домен встречается чаще
4. Если ничья или уверенность < 0.6: пометить как "требует ручного маппинга"
5. Сохранить результат в indicator_cluster_mapping
```

### Пример

```
Индикатор ИД-3.1: "применяет алгоритмы ML"
  Примеры: Python, TensorFlow, scikit-learn, Jupyter

domain_markers ответ:
  TensorFlow  → AI/ML  (weight 1.0)
  scikit-learn → AI/ML  (weight 1.0)
  Jupyter     → DataScience (weight 0.8) / AI/ML (weight 0.7)
  Python      → [Backend 0.6, DataScience 0.8, AI/ML 0.9] — контекстный

Голосование: AI/ML × 3, DataScience × 1 → кластер "Python/AI"
→ Запрос MIS: GET /clusters/Python-AI/betp → BetP=0.85
```

---

## Поток данных: полный жизненный цикл

### Сценарий 1: Выбор ролей (Шаг 2 РПД)

```
1. Преподаватель открывает TAS, создаёт новую программу
2. TAS запрашивает список ролей КРМ из krm_role
3. Для каждой роли: TAS → MIS: GET /roles/{code}/betp
4. MIS считает DST Level 0 по вакансиям через job_title_mapping
5. TAS получает BetP(роль) → показывает ранжированный список
6. Преподаватель выбирает 1–3 роли + опережающую
7. TAS сравнивает выбор с BetP: если выбранная роль BetP < 0.4 → предупреждение
8. TAS сохраняет выбор в program_roles, запускает формирование ЦКМ
```

### Сценарий 2: Формирование ЦКМ (Шаг 3)

```
1. TAS загружает все компетенции из KRM для выбранных ролей
2. Для каждой компетенции: TAS → mapping layer → кластер MIS
3. TAS → MIS: GET /clusters/{cluster}/betp → Δ = BetP − 0 (нет РПД ещё)
4. TAS автоматически формирует предложение ЦКМ:
   - все обязательные ПК базового уровня
   - ПК топ-уровня с Δ > τΔ (до достижения 70%)
5. TAS проверяет пропорции (≥50% топ-уровня) → флаг если нарушено
6. Преподаватель согласует + партнёры вносят оценки (EXP-источник в MIS)
7. TAS сохраняет согласованную ЦКМ
```

### Сценарий 3: Конструктор роадмапа (Шаг 4)

```
1. Преподаватель создаёт дисциплины (название, семестр, часы)
2. Для каждой дисциплины добавляет технологии/темы
3. TAS → mapping layer → маппинг технологий на индикаторы
4. TAS → MIS: GET /skills/{skill}/relevance → BetP, m(F) → показывает подсказки
5. TAS строит граф (indicator_assignment + learning_graph_edge)
6. TAS запускает gap analysis → возвращает три типа пробелов
7. Преподаватель корректирует → граф обновляется в реальном времени
8. TAS генерирует: карту компетенций (матрица), ромашку (граф → радиальная схема)
```

### Сценарий 4: Pre-validation РПД (Шаг 5)

```
1. Преподаватель нажимает "Проверить РПД"
2. TAS считает supply по каждой компетенции из indicator_assignment:
   supply = сумма(hours_allocated) / hours_total программы
3. TAS → MIS: GET /clusters/{cluster}/betp → BetP
4. TAS считает Δ = BetP − supply → флаги где Δ > τΔ
5. TAS → MIS: GET /skills/{skill}/relevance для каждой технологии в РПД
   → список устаревших (m(F) > 0.5)
6. TAS формирует отчёт: покрытие, дефициты, устаревания
7. Если критических ошибок нет → разрешает отправку оператору
```

### Сценарий 5: Ежегодная актуализация (Шаг 5, подшаг 5.5)

```
1. Оператор публикует новую версию КРМ
2. TAS парсит новый КРМ Excel → обновляет krm_role, krm_competency, krm_indicator
3. TAS → MIS: GET /diff/krm?from=2024&to=2025 → список изменений
4. MIS пересчитывает DST для изменённых кластеров (новые технологии как FC-сигналы)
5. TAS получает новые BetP, пересчитывает Δ по существующим программам
6. TAS генерирует отчёт актуализации:
   ┌──────────────────────────────────────────────────┐
   │ Программа "ИТ-бакалавриат 2024"                  │
   │ Новая версия КРМ: 2025                           │
   │                                                  │
   │ ПК-3 (ML-алгоритмы): PyTorch BetP ↑ 0.71→0.87  │
   │   → Рекомендуется: увеличить часы в Семестре 5   │
   │ ПК-1 (Backend): Spring XML m(F) ↑ 0.21→0.54     │
   │   → Рекомендуется: заменить на Spring Annotations│
   │ ПРОК-2 (LLM-интеграция): новая в КРМ 2025       │
   │   → BetP = 0.79, не включена в ЦКМ              │
   │   → Рекомендуется: рассмотреть включение         │
   └──────────────────────────────────────────────────┘
```

---

## Порядок реализации

### Фаза 1 — Данные и инфраструктура

| Задача | Сервис | Зависимости |
|--------|--------|-------------|
| Создать `job_title_mapping` (ручной маппинг ролей КРМ → тайтлы вакансий) | MIS | — |
| Парсер КРМ Excel → `krm_role`, `krm_competency`, `krm_indicator` | TAS | — |
| Таблица `domain_markers` (сигналы технологий по доменам) | MIS | work_skill |
| REST API MIS: `/roles`, `/clusters`, `/skills` | MIS | DST-алгоритм |

### Фаза 2 — Маппинг и ЦКМ

| Задача | Сервис | Зависимости |
|--------|--------|-------------|
| Алгоритм маппинга индикатор → кластер MIS | TAS (mapping) | domain_markers, MIS API |
| Формирование ЦКМ с автопроверкой пропорций | TAS | indicator_cluster_mapping |
| DST Level 0: BetP(роль) | MIS | job_title_mapping |
| DST Level 1: BetP(кластер), Δ | MIS | vacancy_skill, employer_skill_group |

### Фаза 3 — Конструктор роадмапа

| Задача | Сервис | Зависимости |
|--------|--------|-------------|
| Граф обучения (indicator_assignment, learning_graph_edge) | TAS | krm_indicator |
| Автогенерация prereq-рёбер из уровней КРМ | TAS | krm_indicator.level |
| Gap analysis: три типа пробелов | TAS | граф + MIS API |
| Подсказки при добавлении технологий | TAS | MIS /skills API |
| DST Level 2: BetP(навык), m(F) | MIS | vacancy_skill |

### Фаза 4 — Pre-validation и актуализация

| Задача | Сервис | Зависимости |
|--------|--------|-------------|
| Pre-validation RPD (supply из графа + BetP из MIS) | TAS | Фазы 1–3 |
| Детектор устаревших технологий в дисциплинах | TAS + MIS | m(F) из Level 2 |
| КРМ-диффинг при выходе новой версии | TAS | krm_* tables + MIS forecast |
| Генерация карты компетенций и ромашки | TAS | indicator_assignment |

---

## Что уже есть в JAcademicSupport

| Компонент | Статус | Что нужно |
|-----------|--------|-----------|
| vacancy, vacancy_skill | ✅ Есть | — |
| work_skill, skills_group | ✅ Есть | Добавить `domain` поле |
| employer, employer_skill_group | ✅ Частично | Маппинг на KRM-роли |
| forecast | ⚠️ Есть | Добавить `direction`, `confidence` |
| domain_markers | ❌ Нет | Новая таблица |
| technology_ontology | ❌ Нет | Новая таблица (онтология) |
| job_title_mapping | ❌ Нет | Новая таблица |
| krm_* (роли, компетенции, индикаторы) | ❌ Нет | Новые таблицы (TAS) |
| indicator_assignment, learning_graph | ❌ Нет | Новые таблицы (TAS) |
| dst_results (история расчётов) | ❌ Нет | Новая таблица |
