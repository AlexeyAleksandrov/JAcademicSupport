import psycopg2

conn = psycopg2.connect("postgresql://postgres:1111@localhost:5432/AcademicSupport")
cur = conn.cursor()

cur.execute("""
    SELECT id, name, version_group FROM skill_canonical
    WHERE lower(name) LIKE 'java%' AND version_group IS NOT NULL
    ORDER BY name LIMIT 15
""")
print("=== Java versioned ===")
for r in cur.fetchall():
    print(r)

cur.execute("""
    SELECT id, name, version_group FROM skill_canonical
    WHERE lower(name) LIKE 'python%' AND version_group IS NOT NULL
    ORDER BY name LIMIT 10
""")
print("\n=== Python versioned ===")
for r in cur.fetchall():
    print(r)

cur.execute("""
    SELECT id, name, version_group FROM skill_canonical
    WHERE version_group = 'Java'
    ORDER BY name
""")
print("\n=== version_group='Java' ===")
for r in cur.fetchall():
    print(r)

cur.execute("""
    SELECT id, name, version_group FROM skill_canonical
    WHERE version_group = '.NET'
    ORDER BY name
""")
print("\n=== version_group='.NET' ===")
for r in cur.fetchall():
    print(r)

cur.execute("""
    SELECT version_group, COUNT(*) FROM skill_canonical
    WHERE version_group IS NOT NULL
    GROUP BY version_group
    ORDER BY COUNT(*) DESC
    LIMIT 15
""")
print("\n=== Top version groups ===")
for r in cur.fetchall():
    print(r)

conn.close()
