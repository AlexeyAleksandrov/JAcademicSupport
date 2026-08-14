"""Audit multiple domains: show tech_family distribution and flag anomalies."""
import os, sys
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
from dotenv import load_dotenv; load_dotenv()
import psycopg2

DOMAINS = ["SYSTEMS", "TESTING", "MOBILE", "SECURITY", "AI_ML", "CLOUD"]

# Families that are expected within each domain
OK_FAMILIES = {
    "SYSTEMS":  {None, "Системное ПО", "Build Systems", "Языки", "C/C++", "Go",
                 "Rust", "Python", "Архитектура", "VCS", "Сети", "Микроконтроллеры"},
    "TESTING":  {None, "Автотестирование", "API Testing", "Нагрузочное тестирование",
                 "Тест-менеджмент", "Тест-дизайн", "CI/CD", "JavaScript", "Python",
                 "Mobile Testing", "Performance Testing"},
    "MOBILE":   {None, "iOS", "Android", "Cross-platform", "Dart"},
    "SECURITY": {None, "IAM", "Compliance", "Пентест/AppSec", "Криптография",
                 "Системное ПО", "Сетевая безопасность", "SIEM", "Cloud Security"},
    "AI_ML":    {None, "ML/AI", "Deep Learning", "NLP", "Computer Vision",
                 "MLOps", "Data Engineering", "Python", "Reinforcement Learning"},
    "CLOUD":    {None, "AWS", "GCP", "Azure", "Cloud", "Контейнеры", "CI/CD",
                 "Мониторинг", "IaC", "Сети"},
}

conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

for domain in DOMAINS:
    cur.execute("""
        SELECT tech_family, COUNT(*) as n
        FROM skill_canonical
        WHERE domain = %s
        GROUP BY tech_family
        ORDER BY n DESC
    """, (domain,))
    families = cur.fetchall()

    ok = OK_FAMILIES.get(domain, set())
    wrong = [(f, n) for f, n in families if f not in ok]
    right = [(f, n) for f, n in families if f in ok]

    print(f"\n{'═'*70}")
    print(f"  ДОМЕН: {domain}  (всего семейств: {len(families)})")
    print(f"{'═'*70}")
    print(f"  {'Семейство':<28}  {'Кол-во':>7}  {'Статус'}")
    print(f"  {'─'*50}")
    for fam, n in families:
        status = "✅ OK" if fam in ok else "⚠️  WRONG"
        print(f"  {str(fam):<28}  {n:>7}  {status}")

    if wrong:
        print(f"\n  ⚠️  Проблемных семейств: {len(wrong)}, скиллов: {sum(n for _,n in wrong)}")
        for fam, n in wrong:
            cur2 = conn.cursor()
            cur2.execute(
                "SELECT id, name FROM skill_canonical WHERE domain=%s AND tech_family IS NOT DISTINCT FROM %s ORDER BY name",
                (domain, fam)
            )
            skills = cur2.fetchall()
            print(f"\n  [{str(fam)}]  ({n} skills)")
            for sid, name in skills:
                print(f"    {sid:>7}  {name}")
    else:
        print(f"\n  ✅ Все семейства корректны")

conn.close()
