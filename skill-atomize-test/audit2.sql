\pset pager off
\pset format unaligned
\pset fieldsep ' | '

-- 1. Найти C# специально
SELECT id, name, domain, tech_type, version_group, tech_family
FROM skill_canonical
WHERE name ILIKE 'C#%' OR name ILIKE 'C sharp%' OR name ILIKE 'C-sharp%'
ORDER BY name;

-- 2. Проверить tech_type = 'LANGUAGE' вне LANGUAGES домена
SELECT name, domain, version_group, tech_family
FROM skill_canonical
WHERE tech_type ILIKE '%language%' AND (domain != 'LANGUAGES' OR domain IS NULL)
ORDER BY domain NULLS FIRST, name
LIMIT 50;

-- 3. Найти одиночные языки (не версии) в неправильных доменах - по имени
SELECT name, domain, version_group, tech_family
FROM skill_canonical
WHERE name IN (
  'C#','Java','Python','JavaScript','TypeScript','PHP','Ruby','Kotlin',
  'Swift','Go','Rust','Scala','Dart','Groovy','Lua','R','Perl','Erlang',
  'Haskell','Elixir','F#','OCaml','Julia','MATLAB','Bash','Shell',
  'PowerShell','VB.NET','Visual Basic','COBOL','Fortran','Assembly',
  '1С','PL/SQL','T-SQL','Objective-C'
) AND (domain IS NULL OR domain != 'LANGUAGES')
ORDER BY domain NULLS FIRST, name;
