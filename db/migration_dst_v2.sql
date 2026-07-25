\encoding UTF8
-- =============================================================================
-- DST Algorithm Data Structure Migration v2
-- Adds domain classification tables and LLM-based atomization support.
-- Safe to run multiple times (all statements use IF NOT EXISTS / DO blocks).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. DOMAIN REGISTRY
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skill_domain (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(30)  NOT NULL,
    name        VARCHAR(100),
    examples    TEXT,
    parent_code VARCHAR(30)  REFERENCES skill_domain(code),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_skill_domain_code UNIQUE (code)
);

COMMENT ON TABLE  skill_domain IS 'Registry of technology domains; expandable without schema changes';
COMMENT ON COLUMN skill_domain.code     IS 'Short code used in skill_canonical.domain: BACKEND, AI_ML, etc.';
COMMENT ON COLUMN skill_domain.examples IS 'Comma-separated examples injected into LLM prompt at runtime';

INSERT INTO skill_domain (code, name, examples) VALUES
    ('BACKEND',      'Бэкенд-разработка',                'Flask, FastAPI, Django, Spring Boot, Express, ASP.NET Core, gRPC, GraphQL'),
    ('FRONTEND',     'Фронтенд-разработка',              'React, Vue.js, Angular, TypeScript, HTML, CSS, Webpack, Vite, Next.js, Nuxt.js'),
    ('AI_ML',        'ИИ и машинное обучение',           'TensorFlow, PyTorch, scikit-learn, NumPy, Keras, LangChain, LlamaIndex, OpenCV'),
    ('DATA_SCIENCE', 'Анализ данных',                    'pandas, Jupyter, Apache Spark, Tableau, Power BI, Airflow, dbt, ClickHouse'),
    ('DEVOPS',       'DevOps / инфраструктура',          'Docker, Kubernetes, Jenkins, Ansible, Terraform, GitLab CI, GitHub Actions, Prometheus, Grafana, Zabbix'),
    ('DATABASE',     'Базы данных',                      'PostgreSQL, MySQL, MongoDB, Redis, Elasticsearch, Cassandra, Oracle DB, MS SQL'),
    ('CLOUD',        'Облачные платформы',               'AWS, Azure, GCP, S3, Lambda, Yandex Cloud'),
    ('SECURITY',     'Информационная безопасность',      'OAuth, LDAP, Active Directory, GPO, SSL/TLS, WSUS, WDS, DNS, PKI'),
    ('TESTING',      'Тестирование и QA',                'JUnit, Pytest, Selenium, Postman, Cypress, k6, Gatling, Allure'),
    ('MOBILE',       'Мобильная разработка',             'Android, iOS, Flutter, React Native, Swift, Kotlin'),
    ('1C',           '1С-разработка',                    '1С: Предприятие, 1С-Битрикс, ЗУП, УТ, БП, 1С: ERP, 1С-Битрикс: Управление сайтом'),
    ('IOT',          'IoT / встраиваемые системы',       'Arduino, MQTT, Raspberry Pi, RTOS, Modbus, ZigBee'),
    ('SYSTEMS',      'Системное программирование',       'C, C++, Rust, POSIX, Linux kernel, WinAPI'),
    ('GENERAL',      'Общие / контекстно-зависимые',     'Python, Java, Go, C#, .NET, JavaScript, Git, Linux, Agile, Scrum, Jira')
ON CONFLICT (code) DO UPDATE
    SET name     = EXCLUDED.name,
        examples = EXCLUDED.examples;

-- -----------------------------------------------------------------------------
-- 2. ADD domain + domain_source TO skill_canonical
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'skill_canonical' AND column_name = 'domain'
    ) THEN
        ALTER TABLE skill_canonical
            ADD COLUMN domain        VARCHAR(30) REFERENCES skill_domain(code),
            ADD COLUMN domain_source VARCHAR(10) NOT NULL DEFAULT 'unknown';
        COMMENT ON COLUMN skill_canonical.domain        IS 'Technology domain code from skill_domain.code';
        COMMENT ON COLUMN skill_canonical.domain_source IS 'Source of domain assignment: llm | cooccur | manual | unknown';
    END IF;
END;
$$;

-- -----------------------------------------------------------------------------
-- 3. WORK_SKILL → CANONICAL  (M2M — fixes "only first canonical" bug)
-- One work_skill.description can split into multiple canonicals after atomization.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS work_skill_canonical (
    work_skill_id  BIGINT NOT NULL REFERENCES work_skill(id)      ON DELETE CASCADE,
    canonical_id   INT    NOT NULL REFERENCES skill_canonical(id) ON DELETE CASCADE,
    PRIMARY KEY (work_skill_id, canonical_id)
);

COMMENT ON TABLE work_skill_canonical IS 'M:N link between raw work_skill rows and normalised skill_canonical entries';

-- Migrate existing single canonical_id links from work_skill to the new M2M table
INSERT INTO work_skill_canonical (work_skill_id, canonical_id)
SELECT id, canonical_id FROM work_skill WHERE canonical_id IS NOT NULL
ON CONFLICT DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_wsc_work_skill  ON work_skill_canonical (work_skill_id);
CREATE INDEX IF NOT EXISTS idx_wsc_canonical   ON work_skill_canonical (canonical_id);

-- -----------------------------------------------------------------------------
-- 4. VACANCY → DOMINANT DOMAIN  (filled by SQL analytics job)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vacancy_domain (
    vacancy_id      BIGINT NOT NULL REFERENCES vacancy(id) ON DELETE CASCADE,
    primary_domain  VARCHAR(30) REFERENCES skill_domain(code),
    domain_score    DECIMAL(5,4),  -- fraction of non-GENERAL skills matching primary_domain
    computed_at     TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (vacancy_id)
);

COMMENT ON TABLE  vacancy_domain IS 'Dominant domain of a vacancy, computed from co-occurrence of its canonical skills';
COMMENT ON COLUMN vacancy_domain.domain_score IS 'Share of domain-specific skills among all non-GENERAL skills in vacancy';

-- -----------------------------------------------------------------------------
-- 5. SKILL × DOMAIN STATISTICS  (filled by SQL analytics job)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skill_domain_stats (
    canonical_id          INT    NOT NULL REFERENCES skill_canonical(id) ON DELETE CASCADE,
    domain                VARCHAR(30) NOT NULL REFERENCES skill_domain(code),
    vacancy_count         INT    NOT NULL DEFAULT 0,  -- vacancies mentioning this skill in this domain
    domain_vacancy_count  INT    NOT NULL DEFAULT 0,  -- total vacancies classified as this domain
    pct_in_domain         DECIMAL(7,6) NOT NULL DEFAULT 0,  -- vacancy_count / domain_vacancy_count
    top_cooccurrences     JSONB,   -- [{"skill": "TensorFlow", "count": 812}, ...]
    computed_at           TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (canonical_id, domain)
);

COMMENT ON TABLE  skill_domain_stats IS 'Analytics snapshot: how often a skill appears in domain-specific vacancies';
COMMENT ON COLUMN skill_domain_stats.pct_in_domain IS 'Direct input for DST BPA: relevant/total';

CREATE INDEX IF NOT EXISTS idx_sds_domain       ON skill_domain_stats (domain);
CREATE INDEX IF NOT EXISTS idx_sds_pct          ON skill_domain_stats (domain, pct_in_domain DESC);
CREATE INDEX IF NOT EXISTS idx_skill_can_domain ON skill_canonical (domain);

-- -----------------------------------------------------------------------------
-- Done
-- -----------------------------------------------------------------------------
-- Next steps after migration:
--   1. Run python test_atomize.py --limit 500 --batch-size 25 --save
--        → fills skill_canonical (with domain) + work_skill_canonical
--   2. Run SQL analytics job (compute_domain_stats.sql)
--        → fills vacancy_domain + skill_domain_stats
--   3. DST Level 2 queries skill_domain_stats instead of hardcoded averageScore
