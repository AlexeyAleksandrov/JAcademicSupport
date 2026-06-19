-- =============================================================================
-- DST Algorithm Data Structure Migration v1
-- Adds new tables for ideal DST algorithm WITHOUT modifying existing tables.
-- Only addition: work_skill.canonical_id FK column.
-- Safe to run multiple times (all statements use IF NOT EXISTS / DO blocks).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. CANONICAL SKILLS  (нормализованные имена технологий)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skill_canonical (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    normalized_name VARCHAR(200) NOT NULL,
    tech_type       VARCHAR(50),      -- 'language','framework','tool','db','platform','unknown'
    version_group   VARCHAR(100),     -- базовая группа версий, напр. '.NET', 'Python'
    CONSTRAINT uq_skill_canonical_name UNIQUE (normalized_name)
);

COMMENT ON TABLE  skill_canonical IS 'Canonical (normalised) skill names — one row per unique technology';
COMMENT ON COLUMN skill_canonical.normalized_name IS 'Lowercase trimmed base name without version, used for deduplication';
COMMENT ON COLUMN skill_canonical.version_group   IS 'Root technology for version grouping, e.g. ".NET" for ".NET 6", ".NET 8"';

-- -----------------------------------------------------------------------------
-- 2. ADD canonical_id TO work_skill  (не трогаем остальные поля)
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'work_skill' AND column_name = 'canonical_id'
    ) THEN
        ALTER TABLE work_skill ADD COLUMN canonical_id INT REFERENCES skill_canonical(id);
        COMMENT ON COLUMN work_skill.canonical_id IS 'FK to skill_canonical; NULL = not yet normalised';
    END IF;
END;
$$;

-- -----------------------------------------------------------------------------
-- 3. SKILL VERSIONS  (извлечённые версии из сырых строк)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skill_version (
    id           SERIAL PRIMARY KEY,
    canonical_id INT NOT NULL REFERENCES skill_canonical(id) ON DELETE CASCADE,
    raw_string   VARCHAR(255),    -- исходная строка из work_skill.description
    version_min  VARCHAR(20),     -- '6', '3.10'
    version_max  VARCHAR(20),     -- '8' (NULL если точная или is_plus)
    is_plus      BOOLEAN NOT NULL DEFAULT FALSE  -- ".NET 6+" → min=6, is_plus=TRUE
);

COMMENT ON TABLE skill_version IS 'Version variants extracted from raw work_skill descriptions';

-- -----------------------------------------------------------------------------
-- 4. SKILL DEPENDENCY GRAPH  (граф зависимостей, построенный из co-occurrence)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skill_dependency (
    parent_id         INT NOT NULL REFERENCES skill_canonical(id) ON DELETE CASCADE,
    child_id          INT NOT NULL REFERENCES skill_canonical(id) ON DELETE CASCADE,
    co_occurrence_cnt INT NOT NULL DEFAULT 0,
    weight            DECIMAL(6,5),   -- P(parent | child) = co_occ / total_vacancies_with_child
    PRIMARY KEY (parent_id, child_id),
    CONSTRAINT chk_no_self_dependency CHECK (parent_id <> child_id)
);

COMMENT ON TABLE  skill_dependency IS 'Directed skill dependency graph: child implies parent (e.g. FastAPI→Python)';
COMMENT ON COLUMN skill_dependency.weight IS 'Conditional probability P(parent|child): fraction of child-vacancies that also require parent';

-- -----------------------------------------------------------------------------
-- 5. PROFESSIONS
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS profession (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    keywords    TEXT,        -- comma-separated keyword list for classification
    CONSTRAINT uq_profession_code UNIQUE (code)
);

COMMENT ON TABLE  profession IS 'IT profession/specialisation catalogue';
COMMENT ON COLUMN profession.keywords IS 'Comma-separated lowercase keywords used for vacancy title matching';

-- Seed: 16 профессий
INSERT INTO profession (code, name, description, keywords) VALUES
    ('backend',        'Backend Developer',
     'Серверная разработка, API, базы данных',
     'backend,java developer,python developer,.net developer,php developer,go developer,node.js developer,разработчик'),
    ('frontend',       'Frontend Developer',
     'Клиентская разработка, UI, браузерные технологии',
     'frontend,react developer,vue developer,angular developer,верстальщик,ui developer,веб-разработчик'),
    ('fullstack',      'Full-Stack Developer',
     'Полный цикл разработки, клиент + сервер',
     'full stack,full-stack,fullstack'),
    ('devops',         'DevOps / Cloud Engineer',
     'CI/CD, облачные платформы, инфраструктура как код',
     'devops,sre,site reliability,platform engineer,cloud engineer,infrastructure engineer,devops-инженер'),
    ('data_scientist', 'Data Scientist / ML Engineer',
     'Машинное обучение, нейронные сети, NLP, CV',
     'data scientist,ml engineer,machine learning,deep learning,computer vision,nlp engineer,data science'),
    ('data_analyst',   'Data Analyst / BI',
     'Анализ данных, визуализация, бизнес-аналитика данных',
     'аналитик данных,data analyst,bi developer,бизнес-аналитик данных,tableau,power bi,аналитик'),
    ('data_engineer',  'Data Engineer',
     'Построение пайплайнов данных, ETL, хранилища данных',
     'data engineer,etl developer,инженер данных,spark developer,airflow,hadoop,kafka developer'),
    ('mobile',         'Mobile Developer',
     'Разработка мобильных приложений для Android и iOS',
     'android developer,ios developer,flutter developer,react native,kotlin developer,swift developer,mobile developer'),
    ('qa',             'QA Engineer',
     'Тестирование ПО, автоматизация тестов',
     'qa engineer,tester,quality assurance,автоматизатор тестирования,sdet,инженер по тестированию,тестировщик'),
    ('1c',             '1С-разработчик',
     'Разработка и внедрение конфигураций 1С',
     '1с-разработчик,1с программист,разработчик 1с,1c developer,специалист 1с,внедренец 1с'),
    ('sys_analyst',    'Системный / Бизнес-аналитик',
     'Сбор требований, проектирование систем',
     'системный аналитик,system analyst,бизнес-аналитик,business analyst,аналитик требований'),
    ('security',       'Security Engineer',
     'Информационная безопасность, пентест, защита систем',
     'информационная безопасность,security engineer,пентест,pentest,cybersecurity,кибербезопасность,ибшник'),
    ('dba',            'DBA / Database Engineer',
     'Администрирование и оптимизация баз данных',
     'database administrator,dba,администратор баз данных,database engineer,администратор бд'),
    ('embedded',       'Embedded / Firmware Developer',
     'Встраиваемые системы, микроконтроллеры, прошивки',
     'embedded developer,firmware,microcontroller,программист микроконтроллеров,rtos,arduino,stm32,embedded systems'),
    ('game_dev',       'Game Developer',
     'Разработка компьютерных и мобильных игр',
     'game developer,unity developer,unreal developer,разработчик игр,геймдев,game dev'),
    ('architect',      'Solution / Enterprise Architect',
     'Проектирование архитектуры программных систем',
     'architect,архитектор программного обеспечения,software architect,enterprise architect,solution architect')
ON CONFLICT (code) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 6. VACANCY → PROFESSION  (многие-ко-многим с уверенностью)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vacancy_profession (
    vacancy_id    BIGINT NOT NULL REFERENCES vacancy(id) ON DELETE CASCADE,
    profession_id INT    NOT NULL REFERENCES profession(id) ON DELETE CASCADE,
    confidence    DECIMAL(4,3) NOT NULL CHECK (confidence > 0 AND confidence <= 1),
    PRIMARY KEY (vacancy_id, profession_id)
);

COMMENT ON TABLE  vacancy_profession IS 'M:N with confidence: a vacancy can match multiple professions';
COMMENT ON COLUMN vacancy_profession.confidence IS '0–1 match strength; full-stack = {backend:0.7, frontend:0.7}';

-- -----------------------------------------------------------------------------
-- 7. PROFESSION → CLUSTER WEIGHTS  (Уровень 0 → Уровень 1)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS profession_cluster (
    profession_id INT NOT NULL REFERENCES profession(id) ON DELETE CASCADE,
    cluster_id    INT NOT NULL REFERENCES skills_group(id) ON DELETE CASCADE,
    weight        DECIMAL(5,4) NOT NULL DEFAULT 0,  -- рассчитывается из vacancy_profession + vacancy_skill
    PRIMARY KEY (profession_id, cluster_id)
);

COMMENT ON TABLE  profession_cluster IS 'Weight of a cluster for a given profession, computed from vacancy statistics';

-- -----------------------------------------------------------------------------
-- 8. VACANCY → CLUSTER SCORE  (для расчёта BPA источника VAC)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vacancy_cluster_score (
    vacancy_id    BIGINT NOT NULL REFERENCES vacancy(id) ON DELETE CASCADE,
    cluster_id    INT    NOT NULL REFERENCES skills_group(id) ON DELETE CASCADE,
    score         DECIMAL(6,5) NOT NULL DEFAULT 0,  -- нормализованная взвешенная уверенность
    from_title    BOOLEAN NOT NULL DEFAULT FALSE,    -- навык кластера найден в названии вакансии
    from_skills   BOOLEAN NOT NULL DEFAULT FALSE,    -- навык кластера найден в списке навыков
    from_desc     BOOLEAN NOT NULL DEFAULT FALSE,    -- навык кластера найден в описании вакансии
    via_dependency BOOLEAN NOT NULL DEFAULT FALSE,   -- включён через граф зависимостей
    PRIMARY KEY (vacancy_id, cluster_id)
);

COMMENT ON TABLE  vacancy_cluster_score IS 'Pre-computed cluster relevance score per vacancy for fast DST BPA calculation';
COMMENT ON COLUMN vacancy_cluster_score.score IS 'Weighted: title=1.0, skills=0.8, description=0.5; normalised to [0,1]';

-- -----------------------------------------------------------------------------
-- 9. INDEXES
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_skill_canonical_normalized  ON skill_canonical (normalized_name);
CREATE INDEX IF NOT EXISTS idx_work_skill_canonical_id     ON work_skill (canonical_id);
CREATE INDEX IF NOT EXISTS idx_skill_dependency_child      ON skill_dependency (child_id);
CREATE INDEX IF NOT EXISTS idx_skill_dependency_weight     ON skill_dependency (weight DESC);
CREATE INDEX IF NOT EXISTS idx_vacancy_profession_vacancy  ON vacancy_profession (vacancy_id);
CREATE INDEX IF NOT EXISTS idx_vacancy_profession_prof     ON vacancy_profession (profession_id);
CREATE INDEX IF NOT EXISTS idx_vcs_cluster                 ON vacancy_cluster_score (cluster_id);
CREATE INDEX IF NOT EXISTS idx_vcs_score                   ON vacancy_cluster_score (cluster_id, score DESC);
CREATE INDEX IF NOT EXISTS idx_profession_cluster_prof     ON profession_cluster (profession_id);

-- -----------------------------------------------------------------------------
-- Done
-- -----------------------------------------------------------------------------
-- Next steps:
--   Run SkillNormalizationService  → fills skill_canonical + work_skill.canonical_id
--   Run VacancyProfessionService   → fills vacancy_profession
--   Run SkillDependencyService     → fills skill_dependency
--   Run ProfessionClusterService   → fills profession_cluster
--   Run VacancyClusterScoreService → fills vacancy_cluster_score
