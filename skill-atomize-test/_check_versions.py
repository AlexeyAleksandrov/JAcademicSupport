import psycopg2

conn = psycopg2.connect("postgresql://postgres:1111@localhost:5432/AcademicSupport")
cur = conn.cursor()

cur.execute("""
    SELECT id, name, tech_type, version_group
    FROM skill_canonical
    WHERE lower(name) IN ('java','python','.net','docker','kotlin')
    ORDER BY name
""")
print("=== Sample skills ===")
for r in cur.fetchall():
    print(r)

cur.execute("SELECT COUNT(*) FROM skill_canonical WHERE tech_type IS NOT NULL")
print("\nWith tech_type:", cur.fetchone()[0])

cur.execute("SELECT COUNT(*) FROM skill_canonical WHERE version_group IS NOT NULL")
print("With version_group:", cur.fetchone()[0])

cur.execute("SELECT COUNT(*) FROM skill_version")
print("skill_version rows:", cur.fetchone()[0])

cur.execute("""
    SELECT id, name, version_group FROM skill_canonical
    WHERE version_group IS NOT NULL
    ORDER BY name LIMIT 20
""")
print("\n=== Sample version_group ===")
for r in cur.fetchall():
    print(r)

cur.execute("""
    SELECT id, name FROM skill_canonical
    WHERE name ~ '[0-9]'
    AND lower(name) IN ('java 8','java 17','.net 6','.net 8','python 3','node.js 18','react 18','spring boot 3')
    LIMIT 10
""")
print("\n=== Skills with version in name ===")
for r in cur.fetchall():
    print(r)

conn.close()
