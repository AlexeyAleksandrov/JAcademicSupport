import psycopg2
conn = psycopg2.connect(user="postgres", password="1111", host="localhost", port=5432, dbname="AcademicSupport")
cur = conn.cursor()
cur.execute("""
    SELECT p.code, p.name, COUNT(DISTINCT vp.vacancy_id) as cnt
    FROM profession p
    LEFT JOIN vacancy_profession vp ON vp.profession_id = p.id
    GROUP BY p.code, p.name ORDER BY cnt DESC LIMIT 20
""")
for r in cur.fetchall():
    print(r)
conn.close()
