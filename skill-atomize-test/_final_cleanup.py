"""Final small cleanups after multi-domain fix."""
import os, sys
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
from dotenv import load_dotenv; load_dotenv()
import psycopg2

SAVE = "--save" in sys.argv
conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

fixes = [
    # Thread (IoT protocol) was in SYSTEMS → move to IOT
    ("UPDATE skill_canonical SET domain='IOT' WHERE name='Thread' AND domain='SYSTEMS' AND tech_family='Протоколы IoT'",
     "Thread: SYSTEMS → IOT"),
    # Bugzilla: Ручное тестирование → Тест-менеджмент
    ("UPDATE skill_canonical SET tech_family='Тест-менеджмент' WHERE id=38218",
     "Bugzilla: Ручное тестирование → Тест-менеджмент"),
]

for sql, desc in fixes:
    if SAVE:
        cur.execute(sql)
        print(f"  {desc}: {cur.rowcount} rows")
    else:
        print(f"  Preview: {desc}")

if SAVE:
    conn.commit()
    print("\n✅ Done")
else:
    print("\nRun with --save to apply")
conn.close()
