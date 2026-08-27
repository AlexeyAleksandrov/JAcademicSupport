import csv
import os
import sys

import psycopg2
from dotenv import load_dotenv

load_dotenv()
connection = psycopg2.connect(
    host=os.getenv("DB_HOST", "localhost"),
    port=os.getenv("DB_PORT", "5432"),
    dbname=os.getenv("DB_NAME", "AcademicSupport"),
    user=os.getenv("DB_USER", "postgres"),
    password=os.getenv("DB_PASSWORD", "1111"),
)

with connection, connection.cursor() as cursor:
    cursor.execute("""
        SELECT d.id, d.name, COALESCE(d.total_hours, 0), dc.domain, dc.tech_family,
               dc.canonical_id, COALESCE(dc.hours, 0), sc.domain, sc.tech_family
        FROM discipline d
        LEFT JOIN discipline_coverage dc ON dc.discipline_id = d.id
        LEFT JOIN skill_canonical sc ON sc.id = dc.canonical_id
        ORDER BY d.id, dc.id
    """)
    rows = cursor.fetchall()

violations = []
disciplines = {}
for discipline_id, name, total, domain, family, canonical_id, hours, skill_domain, skill_family in rows:
    item = disciplines.setdefault(discipline_id, {"name": name, "total": total, "domains": {}})
    if domain is None and family is None and canonical_id is None:
        continue
    effective_domain = domain or skill_domain
    if effective_domain is None and family:
        with connection.cursor() as cursor:
            cursor.execute("SELECT domain FROM skill_canonical WHERE tech_family = %s AND domain IS NOT NULL LIMIT 1", (family,))
            found = cursor.fetchone()
            effective_domain = found[0] if found else None
    if not effective_domain:
        continue
    domain_node = item["domains"].setdefault(effective_domain, {"explicit": 0, "families": {}})
    if canonical_id:
        effective_family = family or skill_family or "Прочее"
        family_node = domain_node["families"].setdefault(effective_family, {"explicit": 0, "skills": 0})
        family_node["skills"] += max(0, hours)
    elif family:
        family_node = domain_node["families"].setdefault(family, {"explicit": 0, "skills": 0})
        family_node["explicit"] += max(0, hours)
    else:
        domain_node["explicit"] += max(0, hours)

for discipline_id, item in disciplines.items():
    domain_total = 0
    for domain, domain_node in item["domains"].items():
        children_sum = 0
        for family, family_node in domain_node["families"].items():
            family_total = family_node["explicit"] if family_node["explicit"] else family_node["skills"]
            children_sum += family_total
            if family_node["explicit"] and family_node["skills"] > family_node["explicit"]:
                violations.append([discipline_id, item["name"], "FAMILY", f"{domain} / {family}",
                                   family_node["explicit"], family_node["skills"],
                                   family_node["skills"] - family_node["explicit"]])
        domain_total += domain_node["explicit"] if domain_node["explicit"] else children_sum
        if domain_node["explicit"] and children_sum > domain_node["explicit"]:
            violations.append([discipline_id, item["name"], "DOMAIN", domain,
                               domain_node["explicit"], children_sum, children_sum - domain_node["explicit"]])
    if domain_total > item["total"]:
        violations.append([discipline_id, item["name"], "DISCIPLINE", item["name"],
                           item["total"], domain_total, domain_total - item["total"]])

writer = csv.writer(sys.stdout)
writer.writerow(["discipline_id", "name", "level", "parent", "parent_hours", "children_sum", "excess"])
writer.writerows(violations)
