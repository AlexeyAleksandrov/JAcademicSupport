\pset pager off
\pset format unaligned

BEGIN;

-- 1. Базовые имена языков программирования — из любого домена → LANGUAGES
UPDATE skill_canonical
SET domain = 'LANGUAGES'
WHERE domain != 'LANGUAGES'
  AND name IN (
    'Java','Python','C#','Go','PHP','Ruby','Kotlin','Rust','Scala',
    'Swift','Dart','TypeScript','JavaScript','Objective-C',
    'COBOL','Elixir','F#','Groovy','Haskell','Julia','Lua',
    'Perl','Visual Basic','VB.NET','Erlang','1С',
    'C','C++'
  );

-- 2. Версии языков (по version_group) — из любого домена → LANGUAGES
-- Исключение: R, MATLAB оставить в DATA_SCIENCE; SQL-диалекты в DATABASE;
-- Bash/Shell/PowerShell в SYSTEMS
UPDATE skill_canonical
SET domain = 'LANGUAGES'
WHERE domain != 'LANGUAGES'
  AND version_group IN (
    'C#','Java','Python','PHP','Ruby','Kotlin','Swift',
    'Go','Rust','Scala','Dart','TypeScript','JavaScript',
    'Groovy','Lua','Perl','Elixir','F#','Haskell','C++','C'
  );

-- Итог по каждому языку
SELECT name, domain, version_group
FROM skill_canonical
WHERE name IN (
  'Java','Python','C#','Go','PHP','Ruby','Kotlin','Rust','Scala',
  'Swift','Dart','TypeScript','JavaScript','Objective-C',
  'C++','C','COBOL','Elixir','F#','Groovy','Haskell','Julia',
  'Lua','Perl','Visual Basic','VB.NET','Erlang'
)
ORDER BY name;

-- Статистика: сколько записей стало LANGUAGES по version_group
SELECT version_group, COUNT(*) as cnt
FROM skill_canonical
WHERE domain = 'LANGUAGES' AND version_group IS NOT NULL
GROUP BY version_group
ORDER BY cnt DESC
LIMIT 30;

COMMIT;
