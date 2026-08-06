-- =============================================================================
-- foresight_2025.sql
-- Прогнозы рынка труда IT за H2 2025 – начало 2026
-- Источники:
--   SO   = Stack Overflow Developer Survey 2025 (49k respondents, July 2025)
--   JB   = JetBrains Developer Ecosystem 2025 (24k respondents, Oct 2025)
--   HC   = Habr Career IT Salary H1 2025 (57k salaries, Russia)
--   HH   = HH.ru IT Market Analysis 2025 (85k vacancies, Russia)
--
-- Запуск: \i src/main/resources/db/foresight_2025.sql
--   или через pgAdmin / DataGrip (выполнить файл целиком)
--
-- TOTAL_SOURCES = 4  → для max BPA relevantCount = 4
-- confidence 0.0–1.0 → среднее по источникам, влияет на силу сигнала DST
-- direction  = POSITIVE | NEGATIVE
-- profession_code = NULL → применяется ко всем профессиям
-- =============================================================================

BEGIN;

-- =============================================================================
-- УРОВЕНЬ 1: ДОМЕНЫ (domain-level, profession_code = NULL = универсальный)
-- =============================================================================

-- ─── AI_ML ─── 4/4 источников POSITIVE, avg confidence 0.88 ────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.900, 'POSITIVE', NULL, 'AI_ML', NULL, '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.880, 'POSITIVE', NULL, 'AI_ML', NULL, '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.870, 'POSITIVE', NULL, 'AI_ML', NULL, '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.860, 'POSITIVE', NULL, 'AI_ML', NULL, '2025-09-01');

-- ─── DEVOPS ─── 4/4 источников POSITIVE, avg confidence 0.83 ───────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.870, 'POSITIVE', NULL, 'DEVOPS', NULL, '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.840, 'POSITIVE', NULL, 'DEVOPS', NULL, '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.810, 'POSITIVE', NULL, 'DEVOPS', NULL, '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.820, 'POSITIVE', NULL, 'DEVOPS', NULL, '2025-09-01');

-- ─── DATA_SCIENCE ─── 4/4, avg 0.82 ────────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.840, 'POSITIVE', NULL, 'DATA_SCIENCE', NULL, '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.820, 'POSITIVE', NULL, 'DATA_SCIENCE', NULL, '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.830, 'POSITIVE', NULL, 'DATA_SCIENCE', NULL, '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.800, 'POSITIVE', NULL, 'DATA_SCIENCE', NULL, '2025-09-01');

-- ─── CLOUD ─── 4/4, avg 0.78 ────────────────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.820, 'POSITIVE', NULL, 'CLOUD', NULL, '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.780, 'POSITIVE', NULL, 'CLOUD', NULL, '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.760, 'POSITIVE', NULL, 'CLOUD', NULL, '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.750, 'POSITIVE', NULL, 'CLOUD', NULL, '2025-09-01');

-- ─── BACKEND ─── 4/4, avg 0.75 ──────────────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.770, 'POSITIVE', NULL, 'BACKEND', NULL, '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.760, 'POSITIVE', NULL, 'BACKEND', NULL, '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.740, 'POSITIVE', NULL, 'BACKEND', NULL, '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.750, 'POSITIVE', NULL, 'BACKEND', NULL, '2025-09-01');

-- ─── DATABASE ─── 4/4, avg 0.72 ─────────────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.750, 'POSITIVE', NULL, 'DATABASE', NULL, '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.730, 'POSITIVE', NULL, 'DATABASE', NULL, '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.710, 'POSITIVE', NULL, 'DATABASE', NULL, '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.720, 'POSITIVE', NULL, 'DATABASE', NULL, '2025-09-01');

-- ─── SECURITY ─── 3/4 (нет явных данных в JB), avg 0.75 ────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.770, 'POSITIVE', NULL, 'SECURITY', NULL, '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.760, 'POSITIVE', NULL, 'SECURITY', NULL, '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.740, 'POSITIVE', NULL, 'SECURITY', NULL, '2025-09-01');

-- ─── FRONTEND ─── 3/4, avg 0.65 ─────────────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.670, 'POSITIVE', NULL, 'FRONTEND', NULL, '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.660, 'POSITIVE', NULL, 'FRONTEND', NULL, '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.640, 'POSITIVE', NULL, 'FRONTEND', NULL, '2025-09-01');

-- ─── MOBILE ─── 2/4, avg 0.62 ───────────────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.640, 'POSITIVE', NULL, 'MOBILE', NULL, '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.610, 'POSITIVE', NULL, 'MOBILE', NULL, '2025-10-01');

-- ─── TESTING ─── 2/4, avg 0.58 ──────────────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.600, 'POSITIVE', NULL, 'TESTING', NULL, '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.570, 'POSITIVE', NULL, 'TESTING', NULL, '2025-09-01');

-- ─── 1C ─── 2/4 (только RU-источники), avg 0.65 ────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.670, 'POSITIVE', NULL, '1C', NULL, '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.640, 'POSITIVE', NULL, '1C', NULL, '2025-09-01');

-- ─── IOT ─── 1/4, avg 0.55 ──────────────────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.550, 'POSITIVE', NULL, 'IOT', NULL, '2025-07-29');

-- ─── GENERAL ─── 2/4, avg 0.55 ──────────────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.560, 'POSITIVE', NULL, 'GENERAL', NULL, '2025-09-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.540, 'POSITIVE', NULL, 'GENERAL', NULL, '2025-07-01');

-- ─── SYSTEMS ─── 2/4, avg 0.60 ──────────────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.620, 'POSITIVE', NULL, 'SYSTEMS', NULL, '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.590, 'POSITIVE', NULL, 'SYSTEMS', NULL, '2025-10-01');

-- =============================================================================
-- УРОВЕНЬ 2: ТЕХНОЛОГИЧЕСКИЕ СЕМЕЙСТВА (domain + tech_family)
-- =============================================================================

-- ─── Python in BACKEND ─── 4/4, avg 0.88 ───────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.900, 'POSITIVE', NULL, 'BACKEND', 'Python', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.880, 'POSITIVE', NULL, 'BACKEND', 'Python', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.860, 'POSITIVE', NULL, 'BACKEND', 'Python', '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.870, 'POSITIVE', NULL, 'BACKEND', 'Python', '2025-09-01');

-- ─── Python in AI_ML ─── 4/4, avg 0.90 ─────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.920, 'POSITIVE', NULL, 'AI_ML', 'Python', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.900, 'POSITIVE', NULL, 'AI_ML', 'Python', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.890, 'POSITIVE', NULL, 'AI_ML', 'Python', '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.890, 'POSITIVE', NULL, 'AI_ML', 'Python', '2025-09-01');

-- ─── Python in DATA_SCIENCE ─── 4/4, avg 0.88 ──────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.890, 'POSITIVE', NULL, 'DATA_SCIENCE', 'Python', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.870, 'POSITIVE', NULL, 'DATA_SCIENCE', 'Python', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.880, 'POSITIVE', NULL, 'DATA_SCIENCE', 'Python', '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.880, 'POSITIVE', NULL, 'DATA_SCIENCE', 'Python', '2025-09-01');

-- ─── TypeScript in BACKEND ─── 4/4, avg 0.83 ────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.840, 'POSITIVE', NULL, 'BACKEND', 'TypeScript', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.850, 'POSITIVE', NULL, 'BACKEND', 'TypeScript', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.810, 'POSITIVE', NULL, 'BACKEND', 'TypeScript', '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.820, 'POSITIVE', NULL, 'BACKEND', 'TypeScript', '2025-09-01');

-- ─── TypeScript in FRONTEND ─── 4/4, avg 0.85 ───────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.860, 'POSITIVE', NULL, 'FRONTEND', 'TypeScript', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.870, 'POSITIVE', NULL, 'FRONTEND', 'TypeScript', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.830, 'POSITIVE', NULL, 'FRONTEND', 'TypeScript', '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.840, 'POSITIVE', NULL, 'FRONTEND', 'TypeScript', '2025-09-01');

-- ─── Go in BACKEND ─── 4/4, avg 0.80 ────────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.810, 'POSITIVE', NULL, 'BACKEND', 'Go', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.820, 'POSITIVE', NULL, 'BACKEND', 'Go', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.790, 'POSITIVE', NULL, 'BACKEND', 'Go', '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.790, 'POSITIVE', NULL, 'BACKEND', 'Go', '2025-09-01');

-- ─── Rust in BACKEND ─── 3/4, avg 0.75 ──────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.780, 'POSITIVE', NULL, 'BACKEND', 'Rust', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.770, 'POSITIVE', NULL, 'BACKEND', 'Rust', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.720, 'POSITIVE', NULL, 'BACKEND', 'Rust', '2025-07-01');

-- ─── Java in BACKEND ─── 3/4, avg 0.70 ──────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.710, 'POSITIVE', NULL, 'BACKEND', 'Java', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.700, 'POSITIVE', NULL, 'BACKEND', 'Java', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.690, 'POSITIVE', NULL, 'BACKEND', 'Java', '2025-09-01');

-- ─── Kotlin in BACKEND & MOBILE ─── 3/4, avg 0.72 ──────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.730, 'POSITIVE', NULL, 'BACKEND', 'Kotlin', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.750, 'POSITIVE', NULL, 'BACKEND', 'Kotlin', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.700, 'POSITIVE', NULL, 'BACKEND', 'Kotlin', '2025-09-01');

INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.720, 'POSITIVE', NULL, 'MOBILE', 'Kotlin', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.740, 'POSITIVE', NULL, 'MOBILE', 'Kotlin', '2025-10-01');

-- ─── Docker in DEVOPS ─── 4/4, avg 0.90 ─────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.940, 'POSITIVE', NULL, 'DEVOPS', 'Docker', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.900, 'POSITIVE', NULL, 'DEVOPS', 'Docker', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.880, 'POSITIVE', NULL, 'DEVOPS', 'Docker', '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.880, 'POSITIVE', NULL, 'DEVOPS', 'Docker', '2025-09-01');

-- ─── Kubernetes in DEVOPS ─── 4/4, avg 0.85 ─────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.870, 'POSITIVE', NULL, 'DEVOPS', 'Kubernetes', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.850, 'POSITIVE', NULL, 'DEVOPS', 'Kubernetes', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.830, 'POSITIVE', NULL, 'DEVOPS', 'Kubernetes', '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.840, 'POSITIVE', NULL, 'DEVOPS', 'Kubernetes', '2025-09-01');

-- ─── Terraform in DEVOPS ─── 3/4, avg 0.75 ──────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.770, 'POSITIVE', NULL, 'DEVOPS', 'Terraform', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.750, 'POSITIVE', NULL, 'DEVOPS', 'Terraform', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.740, 'POSITIVE', NULL, 'DEVOPS', 'Terraform', '2025-09-01');

-- ─── Ansible in DEVOPS ─── 2/4, avg 0.65 ────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.660, 'POSITIVE', NULL, 'DEVOPS', 'Ansible', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.650, 'POSITIVE', NULL, 'DEVOPS', 'Ansible', '2025-09-01');

-- ─── PostgreSQL in DATABASE ─── 4/4, avg 0.88 ───────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.910, 'POSITIVE', NULL, 'DATABASE', 'PostgreSQL', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.890, 'POSITIVE', NULL, 'DATABASE', 'PostgreSQL', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.860, 'POSITIVE', NULL, 'DATABASE', 'PostgreSQL', '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.870, 'POSITIVE', NULL, 'DATABASE', 'PostgreSQL', '2025-09-01');

-- ─── Redis in DATABASE ─── 3/4, avg 0.78 ────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.800, 'POSITIVE', NULL, 'DATABASE', 'Redis', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.780, 'POSITIVE', NULL, 'DATABASE', 'Redis', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.760, 'POSITIVE', NULL, 'DATABASE', 'Redis', '2025-09-01');

-- ─── MongoDB in DATABASE ─── 2/4, avg 0.62 ──────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.630, 'POSITIVE', NULL, 'DATABASE', 'MongoDB', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.610, 'POSITIVE', NULL, 'DATABASE', 'MongoDB', '2025-10-01');

-- ─── React in FRONTEND ─── 3/4, avg 0.72 ────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.740, 'POSITIVE', NULL, 'FRONTEND', 'React', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.720, 'POSITIVE', NULL, 'FRONTEND', 'React', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.700, 'POSITIVE', NULL, 'FRONTEND', 'React', '2025-09-01');

-- ─── Vue in FRONTEND ─── 2/4, avg 0.62 ──────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.630, 'POSITIVE', NULL, 'FRONTEND', 'Vue', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.610, 'POSITIVE', NULL, 'FRONTEND', 'Vue', '2025-09-01');

-- ─── AWS in CLOUD ─── 4/4, avg 0.82 ─────────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.850, 'POSITIVE', NULL, 'CLOUD', 'AWS', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.820, 'POSITIVE', NULL, 'CLOUD', 'AWS', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.800, 'POSITIVE', NULL, 'CLOUD', 'AWS', '2025-07-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.810, 'POSITIVE', NULL, 'CLOUD', 'AWS', '2025-09-01');

-- ─── GCP in CLOUD ─── 3/4, avg 0.72 ─────────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.740, 'POSITIVE', NULL, 'CLOUD', 'GCP', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.720, 'POSITIVE', NULL, 'CLOUD', 'GCP', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.710, 'POSITIVE', NULL, 'CLOUD', 'GCP', '2025-09-01');

-- ─── Azure in CLOUD ─── 3/4, avg 0.72 ───────────────────────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.740, 'POSITIVE', NULL, 'CLOUD', 'Azure', '2025-07-29');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.720, 'POSITIVE', NULL, 'CLOUD', 'Azure', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.710, 'POSITIVE', NULL, 'CLOUD', 'Azure', '2025-09-01');

-- ─── NEGATIVE: PHP (долгосрочное снижение по JetBrains) ──────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.700, 'NEGATIVE', NULL, 'BACKEND', 'PHP', '2025-10-01');
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.660, 'NEGATIVE', NULL, 'BACKEND', 'PHP', '2025-07-29');

-- ─── NEGATIVE: Ruby (долгосрочное снижение по JetBrains) ─────────────────────
INSERT INTO foresight (source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
VALUES ('JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.650, 'NEGATIVE', NULL, 'BACKEND', 'Ruby', '2025-10-01');

-- =============================================================================
-- УРОВЕНЬ 3: КОНКРЕТНЫЕ НАВЫКИ (canonical_id via subquery по skill_canonical.name)
-- Строки с NULL canonical_id (не найден) не будут вставлены — ON CONFLICT DO NOTHING
-- =============================================================================

-- ─── Python ──────────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.900, 'POSITIVE', NULL, 'BACKEND', 'Python', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'python' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.880, 'POSITIVE', NULL, 'BACKEND', 'Python', '2025-10-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'python' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.860, 'POSITIVE', NULL, 'BACKEND', 'Python', '2025-07-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'python' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.870, 'POSITIVE', NULL, 'BACKEND', 'Python', '2025-09-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'python' LIMIT 1;

-- ─── PostgreSQL ───────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.910, 'POSITIVE', NULL, 'DATABASE', 'PostgreSQL', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'postgresql' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.890, 'POSITIVE', NULL, 'DATABASE', 'PostgreSQL', '2025-10-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'postgresql' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.860, 'POSITIVE', NULL, 'DATABASE', 'PostgreSQL', '2025-07-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'postgresql' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.870, 'POSITIVE', NULL, 'DATABASE', 'PostgreSQL', '2025-09-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'postgresql' LIMIT 1;

-- ─── Docker ───────────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.940, 'POSITIVE', NULL, 'DEVOPS', 'Docker', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'docker' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.900, 'POSITIVE', NULL, 'DEVOPS', 'Docker', '2025-10-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'docker' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.880, 'POSITIVE', NULL, 'DEVOPS', 'Docker', '2025-07-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'docker' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.880, 'POSITIVE', NULL, 'DEVOPS', 'Docker', '2025-09-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'docker' LIMIT 1;

-- ─── Kubernetes ───────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.870, 'POSITIVE', NULL, 'DEVOPS', 'Kubernetes', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'kubernetes' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.850, 'POSITIVE', NULL, 'DEVOPS', 'Kubernetes', '2025-10-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'kubernetes' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.830, 'POSITIVE', NULL, 'DEVOPS', 'Kubernetes', '2025-07-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'kubernetes' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.840, 'POSITIVE', NULL, 'DEVOPS', 'Kubernetes', '2025-09-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'kubernetes' LIMIT 1;

-- ─── Git ─────────────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.850, 'POSITIVE', NULL, 'DEVOPS', NULL, '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'git' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.860, 'POSITIVE', NULL, 'DEVOPS', NULL, '2025-09-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'git' LIMIT 1;

-- ─── SQL ─────────────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.780, 'POSITIVE', NULL, 'DATABASE', NULL, '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'sql' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.800, 'POSITIVE', NULL, 'DATABASE', NULL, '2025-09-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'sql' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.790, 'POSITIVE', NULL, 'DATABASE', NULL, '2025-07-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'sql' LIMIT 1;

-- ─── Linux ───────────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.790, 'POSITIVE', NULL, 'DEVOPS', NULL, '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'linux' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.810, 'POSITIVE', NULL, 'DEVOPS', NULL, '2025-09-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'linux' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.800, 'POSITIVE', NULL, 'DEVOPS', NULL, '2025-07-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'linux' LIMIT 1;

-- ─── Redis ───────────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.800, 'POSITIVE', NULL, 'DATABASE', 'Redis', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'redis' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.780, 'POSITIVE', NULL, 'DATABASE', 'Redis', '2025-10-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'redis' LIMIT 1;

-- ─── TypeScript ───────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.860, 'POSITIVE', NULL, 'FRONTEND', 'TypeScript', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'typescript' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.870, 'POSITIVE', NULL, 'FRONTEND', 'TypeScript', '2025-10-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'typescript' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'HH.ru IT Market Analysis 2025', 'https://forpes.ru/post/204443', 0.840, 'POSITIVE', NULL, 'FRONTEND', 'TypeScript', '2025-09-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'typescript' LIMIT 1;

-- ─── FastAPI ──────────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.870, 'POSITIVE', NULL, 'BACKEND', 'Python', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'fastapi' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.830, 'POSITIVE', NULL, 'BACKEND', 'Python', '2025-10-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'fastapi' LIMIT 1;

-- ─── Spring Boot ──────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.730, 'POSITIVE', NULL, 'BACKEND', 'Java', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'spring boot' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.720, 'POSITIVE', NULL, 'BACKEND', 'Java', '2025-10-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'spring boot' LIMIT 1;

-- ─── Terraform ────────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.770, 'POSITIVE', NULL, 'DEVOPS', 'Terraform', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'terraform' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.750, 'POSITIVE', NULL, 'DEVOPS', 'Terraform', '2025-10-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'terraform' LIMIT 1;

-- ─── Kafka ────────────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.750, 'POSITIVE', NULL, 'BACKEND', NULL, '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'kafka' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.730, 'POSITIVE', NULL, 'BACKEND', NULL, '2025-10-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'kafka' LIMIT 1;

-- ─── Elasticsearch ────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.720, 'POSITIVE', NULL, 'BACKEND', NULL, '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'elasticsearch' LIMIT 1;

-- ─── React ────────────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.740, 'POSITIVE', NULL, 'FRONTEND', 'React', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'react' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.720, 'POSITIVE', NULL, 'FRONTEND', 'React', '2025-10-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'react' LIMIT 1;

-- ─── Go (Golang) ──────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.810, 'POSITIVE', NULL, 'BACKEND', 'Go', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) IN ('go', 'golang') LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.820, 'POSITIVE', NULL, 'BACKEND', 'Go', '2025-10-01'
FROM skill_canonical sc WHERE LOWER(sc.name) IN ('go', 'golang') LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Habr Career IT Salary H1 2025', 'https://habr.com/ru/specials/936618/', 0.790, 'POSITIVE', NULL, 'BACKEND', 'Go', '2025-07-01'
FROM skill_canonical sc WHERE LOWER(sc.name) IN ('go', 'golang') LIMIT 1;

-- ─── Rust ─────────────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.780, 'POSITIVE', NULL, 'BACKEND', 'Rust', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'rust' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.770, 'POSITIVE', NULL, 'BACKEND', 'Rust', '2025-10-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'rust' LIMIT 1;

-- ─── MongoDB ──────────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.630, 'POSITIVE', NULL, 'DATABASE', 'MongoDB', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'mongodb' LIMIT 1;

-- ─── Ansible ──────────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.660, 'POSITIVE', NULL, 'DEVOPS', 'Ansible', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'ansible' LIMIT 1;

-- ─── Kotlin ───────────────────────────────────────────────────────────────────
INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'Stack Overflow Developer Survey 2025', 'https://survey.stackoverflow.co/2025/technology/', 0.730, 'POSITIVE', NULL, 'BACKEND', 'Kotlin', '2025-07-29'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'kotlin' LIMIT 1;

INSERT INTO foresight (canonical_id, source_name, source_url, confidence, direction, profession_code, domain, tech_family, forecast_date)
SELECT sc.id, 'JetBrains Developer Ecosystem 2025', 'https://devecosystem-2025.jetbrains.com/', 0.750, 'POSITIVE', NULL, 'BACKEND', 'Kotlin', '2025-10-01'
FROM skill_canonical sc WHERE LOWER(sc.name) = 'kotlin' LIMIT 1;

-- =============================================================================
-- Итог: ~200 строк, 4 уникальных source_url
-- Запустите SELECT COUNT(*), COUNT(DISTINCT source_url) FROM foresight; для проверки
-- =============================================================================

COMMIT;
