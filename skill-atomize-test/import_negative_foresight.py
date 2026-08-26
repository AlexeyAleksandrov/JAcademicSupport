#!/usr/bin/env python3
"""
import_negative_foresight.py
Imports negative foresight signals from negative_foresight_data.csv
into the AcademicSupport foresight table.

Usage:
    python import_negative_foresight.py --dry-run   # preview
    python import_negative_foresight.py --save      # insert
"""

import sys
import io
import csv
import psycopg2
from pathlib import Path
from datetime import datetime

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

DB_PARAMS = dict(host="localhost", port=5432, dbname="AcademicSupport",
                 user="postgres", password="1111")

DRY_RUN = "--dry-run" in sys.argv
SAVE = "--save" in sys.argv

if not DRY_RUN and not SAVE:
    DRY_RUN = True

CSV_PATH = Path(__file__).parent / "negative_foresight_data.csv"


def connect():
    return psycopg2.connect(**DB_PARAMS)


def lookup_canonical(cur, name):
    cur.execute(
        """SELECT id, domain, tech_family FROM skill_canonical
           WHERE LOWER(name) = LOWER(%s)
              OR LOWER(normalized_name) = LOWER(%s)
           ORDER BY CASE WHEN LOWER(name) = LOWER(%s) THEN 0 ELSE 1 END
           LIMIT 1""",
        (name, name, name)
    )
    return cur.fetchone()


def exists(cur, canonical_id, source_url, domain, tech_family):
    cur.execute(
        """SELECT 1 FROM foresight
           WHERE canonical_id IS NOT DISTINCT FROM %s
             AND source_url = %s
             AND domain IS NOT DISTINCT FROM %s
             AND tech_family IS NOT DISTINCT FROM %s""",
        (canonical_id, source_url, domain, tech_family)
    )
    return cur.fetchone() is not None


def parse_date(s):
    if not s:
        return None
    try:
        return datetime.strptime(s, "%Y-%m-%d").date()
    except ValueError:
        return None


def main():
    if not CSV_PATH.exists():
        print(f"ERROR: CSV not found: {CSV_PATH}")
        return

    conn = connect()
    cur = conn.cursor()
    inserted = 0
    skipped_dup = 0
    not_found = 0
    overrides = 0

    print("=== DRY RUN ===" if DRY_RUN else "=== SAVE MODE ===")

    with CSV_PATH.open(encoding='utf-8', newline='') as f:
        reader = csv.DictReader(f)
        for row in reader:
            tech = (row.get('tech') or '').strip()
            source_name = (row.get('source_name') or '').strip()
            source_url = (row.get('source_url') or '').strip()
            confidence_str = (row.get('confidence') or '0.5').strip()
            domain_csv = (row.get('domain') or '').strip() or None
            tech_family_csv = (row.get('tech_family') or '').strip() or None
            date_str = (row.get('forecast_date') or '').strip()

            try:
                confidence = float(confidence_str)
            except ValueError:
                confidence = 0.5

            canonical = lookup_canonical(cur, tech) if tech else None
            if canonical:
                canonical_id, db_domain, db_family = canonical
                domain = domain_csv if domain_csv else db_domain
                tech_family = tech_family_csv if tech_family_csv else db_family
                if domain_csv or tech_family_csv:
                    overrides += 1
            else:
                canonical_id = None
                domain = domain_csv
                tech_family = tech_family_csv
                if not domain or not tech_family:
                    not_found += 1
                    print(f"  [SKIP] no canonical and no fallback for {tech!r}")
                    continue

            if exists(cur, canonical_id, source_url, domain, tech_family):
                skipped_dup += 1
                continue

            if DRY_RUN:
                print(f"  [DRY] {tech!r} -> canonical_id={canonical_id}, "
                      f"domain={domain}, family={tech_family}, "
                      f"conf={confidence:.3f}, src={source_name}")
                inserted += 1
                continue

            cur.execute(
                """INSERT INTO foresight
                     (source_name, source_url, canonical_id, confidence, direction,
                      profession_code, domain, tech_family, forecast_date)
                   VALUES (%s, %s, %s, %s, 'NEGATIVE', %s, %s, %s, %s)""",
                (source_name, source_url, canonical_id, confidence,
                 None, domain, tech_family, parse_date(date_str))
            )
            inserted += 1

    if SAVE:
        conn.commit()

    print(f"\nTotal rows: {inserted}")
    print(f"Duplicates skipped: {skipped_dup}")
    print(f"Not found (no canonical, no fallback): {not_found}")
    print(f"Domain/tech_family overrides: {overrides}")

    conn.close()


if __name__ == "__main__":
    main()
