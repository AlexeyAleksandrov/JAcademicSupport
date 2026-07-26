# Java-интеграция DST-слоя — отчёт

> Дата: 26 июля 2026  
> Ветка/модуль: `src/main/java/.../services/dst`, `models`, `repositories`, `controllers`  
> Статус: **Реализовано, собирается (BUILD SUCCESS)**

---

## Контекст: что было до

| Компонент | Было |
|---|---|
| `SkillCanonical.java` | Есть, но без полей `domain` и `domain_source` |
| `WorkSkillCanonical.java` | Отсутствовал |
| `VacancyDomain.java` | Отсутствовал |
| `SkillDomainStats.java` | Отсутствовал |
| DST Level 2 | Работал через `work_skill.canonical_id` (1:1 связь, старая) |
| `SkillNormalizationService` | Regex-пайплайн (3 прохода: скобки → разбивка → классификация) |
| `/api/admin/dst/normalize-skills` | Активный эндпоинт |

---

## Что изменено

### Фаза 1 — Модели

#### `SkillCanonical.java` — добавлены поля
```java
@Column(name = "domain", length = 20)
private String domain;           // BACKEND, FRONTEND, AI_ML, DATA_SCIENCE, DEVOPS, ...

@Column(name = "domain_source", length = 20)
private String domainSource;     // "llm" — заполнено Python-пайплайном
```

#### Новый: `WorkSkillCanonical.java`
Маппинг таблицы `work_skill_canonical` (M:N: work_skill ↔ skill_canonical).  
Composite PK: `(workSkillId, canonicalId)`.  
Содержит `@ManyToOne` на `SkillCanonical` для удобного JOIN.

#### Новый: `VacancyDomain.java`
Маппинг таблицы `vacancy_domain`.  
PK: `vacancyId`. Поля: `primaryDomain`, `domainScore`, `computedAt`.

#### Новый: `SkillDomainStats.java`
Маппинг таблицы `skill_domain_stats`.  
PK: `canonicalId`. Поля: `domain`, `vacancyCount`, `domainVacancyCount`, `pctInDomain`, `topCooccurrences` (JSONB → `List<Map<String,Object>>`), `computedAt`.

---

### Фаза 2 — Репозитории

| Файл | Ключевые методы |
|---|---|
| `WorkSkillCanonicalRepository` | `findByWorkSkillId(Long)`, `findByCanonicalId(Long)`, `findCanonicalIdsByWorkSkillIds(List<Long>)` |
| `VacancyDomainRepository` | `findByVacancyId(Long)` |
| `SkillDomainStatsRepository` | `findByCanonicalId(Long)`, `findByDomainOrderByVacancyCountDesc(String)` |

---

### Фаза 3 — Сервисы

#### `DstQueryService` — Level 2 переписан

**До:** для каждой вакансии брал `work_skill.canonical_id` (1:1 связь, только первый canonical от regex-нормализации).

**После:** полная M:N цепочка через `work_skill_canonical`:
```
vacancy → vacancy_skills → work_skill → work_skill_canonical → skill_canonical
```

Алгоритм:
1. Загружает все вакансии для profession + cluster (по score)
2. Для каждого `work_skill` каждой вакансии запрашивает `WorkSkillCanonical.findByWorkSkillId()`
3. Собирает уникальные `canonical_id` в пределах одной вакансии (Set → нет дублей)
4. Считает частоту по всем вакансиям кластера
5. Обогащает каждый `SkillInfo`: добавляет `domain` из `SkillCanonical` и `topCooccurrences` из `SkillDomainStats`

#### `SkillInfo` record — расширен
```java
// Было:
record SkillInfo(long skillId, String description, Long canonicalId,
                 double relativeFrequency, long absoluteCount, boolean isImplied)

// Стало:
record SkillInfo(long skillId, String description, Long canonicalId,
                 double relativeFrequency, long absoluteCount, boolean isImplied,
                 String domain,                          // новое
                 List<Map<String,Object>> topCooccurrences) // новое
```

#### Новые методы в `DstQueryService`

```java
// Топ co-occurring навыков (из skill_dependency)
List<RelatedSkillInfo> getRelatedSkills(Long canonicalId)

// Домен вакансии (из vacancy_domain)
Optional<VacancyDomainInfo> getVacancyDomain(Long vacancyId)
```

#### `SkillNormalizationService.normalizeAll()` — deprecated
```java
@Deprecated(since = "2.0", forRemoval = false)
public NormalizationReport normalizeAll() {
    log.warn("Deprecated. Use Python pipeline: test_atomize.py");
    return new NormalizationReport(0, 0, 0, 0);
}
```
Весь regex-код (`parseRawSkill`, `classifyToken` и т.д.) **сохранён** — может использоваться для отладки через `parseRawSkill()`.

---

### Фаза 4 — Контроллеры

#### `DstAdminController` — `normalize-skills` → 410 Gone
```
POST /api/admin/dst/normalize-skills
→ HTTP 410 Gone
→ {"status":"gone","message":"Use Python pipeline: test_atomize.py --all-skills --save"}
```
Из `run-full-pipeline` нормализация убрана, вместо неё возвращается:
```json
"phase2_normalize": {"status": "skipped", "reason": "Handled by Python LLM pipeline"}
```

#### `DstQueryController` — 2 новых эндпоинта
```
GET /api/dst/skills/{canonicalId}/related
→ List<RelatedSkillInfo>: топ co-occurring навыков из skill_dependency
→ Сортировка по co_occurrence_cnt DESC
→ 404 если canonical не найден

GET /api/dst/vacancies/{vacancyId}/domain
→ VacancyDomainInfo: primaryDomain, domainScore, computedAt
→ 404 если вакансия не в vacancy_domain
```

---

## Архитектурный комментарий: `skills_group` vs `skill_canonical.domain`

| | `skills_group` (старая) | `skill_canonical.domain` (новая) |
|---|---|---|
| Гранулярность | Грубая (~20-50 групп) | 14 доменов (фиксированных) |
| Заполнение | GigaChat по одному навыку | Python batch (GigaChat-Max) |
| Использование | DST Level 1, RecommendationService, RPD | DST Level 2, analytics API |
| Статус | Активен (не трогали) | Новый слой |

DST Level 1 (кластеры = `skills_group`) и RecommendationService по-прежнему работают через `skills_group`. Level 2 теперь использует `skill_canonical.domain`.

---

## Данные в БД (на момент интеграции)

| Таблица | Строк | Заполнено |
|---|---|---|
| `skill_canonical` | 17,107 | Python GigaChat-Max |
| `work_skill_canonical` | 34,558 | Python GigaChat-Max |
| `skill_dependency` | 144,471 | Python (compute_analytics.py) |
| `vacancy_domain` | 13,417 | Python (compute_analytics.py) |
| `skill_domain_stats` | 15,875 | Python (compute_analytics.py) |

---

## Верификация (26 июля 2026)

Пайплайн запущен и все эндпоинты проверены:

```
POST /api/admin/dst/classify-professions      → 10909 classified, 2570 other
POST /api/admin/dst/compute-profession-weights → 309 profession_cluster rows
POST /api/admin/dst/compute-cluster-scores    → 276006 vacancy_cluster_score rows
```

**Level 1** — `/api/dst/professions/backend/clusters` (23 кластера):
```
[2] Фреймворки          weight=0.7658  avgScore=0.3543
[4] Реляционные БД      weight=0.5889  avgScore=0.2098
[7] Общее               weight=0.4272  avgScore=0.7981
[5] Система контроля версий weight=0.3773
```

**Level 2** — `/api/dst/professions/backend/clusters/2/skills` (7028 навыков):
```
PostgreSQL  [DATABASE]   38.1%
Git         [DEVOPS]     32.2%
Docker      [DEVOPS]     28.5%
Python      [GENERAL]    26.9%
Java        [GENERAL]    26.4%
REST API    [BACKEND]    16.2%
```

**Новые эндпоинты**:
```
GET /api/dst/skills/33506/related  (Python)
→ SQL (2196x), PostgreSQL (1581x), Docker (1337x), Git (1091x) ... 3294 навыка

GET /api/dst/vacancies/{id}/domain
→ {"primaryDomain": "...", "domainScore": ...}
```

**Исправления в ходе верификации:**
- `VacancyClusterScoreService` — заменён N+1 на bulk pre-load из `work_skill_canonical`
- `DstQueryService.getSkillsForProfessionAndCluster` — заменён N+1 на `findByWorkSkillIdIn()`
- `WorkSkillCanonicalRepository` — добавлен `findByWorkSkillIdIn(List<Long>)`

## Pending

1. **Миграция RPD-рекомендаций** с `skills_group` на `skill_canonical.domain` — отдельная задача
2. **Пересчёт после завершения `test_atomize.py`** — когда батч-пайплайн добавит новые canonical записи, стоит перезапустить `compute-cluster-scores` для актуализации
