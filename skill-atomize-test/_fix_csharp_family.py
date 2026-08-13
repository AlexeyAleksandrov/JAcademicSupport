import psycopg2, os
from dotenv import load_dotenv; load_dotenv()
conn = psycopg2.connect(os.getenv('DB_URL'))
cur = conn.cursor()

# Найти все LANGUAGES canonical с tech_family = NULL
cur.execute("""
    SELECT id, name, domain FROM skill_canonical
    WHERE domain = 'LANGUAGES' AND tech_family IS NULL
    ORDER BY name
""")
missing = cur.fetchall()
print(f'LANGUAGES skills без tech_family: {len(missing)}')
for r in missing:
    print(f'  id={r[0]}  {r[1]!r}')

# Применить: для каждого языкового canonical без family → family = name
print('\nПрименяю tech_family = name для каждого...')
for r in missing:
    skill_id, skill_name = r[0], r[1]
    # Для составных вроде "автотесты на C#" → 'C#'
    # Для базовых ("C#") → "C#"
    family = skill_name  # по умолчанию = имя
    # Особые случаи
    if 'c#' in skill_name.lower() and skill_name.lower() != 'c#':
        family = 'C#'
    cur.execute(
        "UPDATE skill_canonical SET tech_family = %s WHERE id = %s",
        (family, skill_id)
    )
    print(f'  UPDATE {skill_name!r} → tech_family={family!r}')

conn.commit()
print('\nГотово!')
conn.close()
