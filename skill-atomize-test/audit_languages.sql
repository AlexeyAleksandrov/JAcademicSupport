\pset pager off
\pset format unaligned
\pset fieldsep ' | '

-- Аудит: языки программирования не в LANGUAGES
SELECT domain, version_group, count(*) as cnt
FROM skill_canonical
WHERE version_group IN (
  'C#','.NET','Java','Python','JavaScript','TypeScript','PHP',
  'Ruby','Kotlin','Swift','Go','Rust','Scala','Dart','Groovy',
  'Lua','R','C++','C','Perl','Erlang','Haskell','Elixir',
  'F#','Julia','Nim','Zig','Crystal','MATLAB','Bash','Shell',
  'PowerShell','VB.NET','Visual Basic','COBOL','Fortran','Assembly',
  '1С','1C','SQL'
)
AND (domain != 'LANGUAGES' OR domain IS NULL)
GROUP BY domain, version_group
ORDER BY domain NULLS FIRST, cnt DESC;
