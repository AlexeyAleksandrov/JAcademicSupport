"""
Fix wrong tech_family assignments in SYSTEMS, TESTING, MOBILE, SECURITY, AI_ML, CLOUD.
Run with --save to apply. Run with --domain X to preview only that domain.
"""
import os, sys
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
from dotenv import load_dotenv; load_dotenv()
import psycopg2

SAVE = "--save" in sys.argv
FILTER_DOMAIN = None
for arg in sys.argv[1:]:
    if arg.startswith("--domain="):
        FILTER_DOMAIN = arg.split("=", 1)[1]

conn = psycopg2.connect(os.getenv("DB_URL"))
cur = conn.cursor()

# ─────────────────────────────────────────────────────────────────────────────
# Rule sets: each entry = (domain, name_pattern, match_type, new_domain, new_family)
# match_type: 'id' | 'name' | 'domain+family'
# ─────────────────────────────────────────────────────────────────────────────

# ══ SYSTEMS ══════════════════════════════════════════════════════════════════
# Vulkan — low-level GPU API, stays SYSTEMS
# Altera/AMD FPGA → IOT/Микроконтроллеры
# VMware/Proxmox/oVirt → виртуализация, create new family "Виртуализация"
# AMQP, ZeroMQ → BACKEND messaging
# Concurrency → SYSTEMS/Системное ПО
# 3PAR StoreServ → hardware storage, stays SYSTEMS/NULL

SYSTEMS_FIXES = [
    # FPGA chips → IOT
    (36015, "SYSTEMS", "IOT",     "Микроконтроллеры"),  # Altera Max 10
    (36014, "SYSTEMS", "IOT",     "Микроконтроллеры"),  # Altera Max 5
    (34833, "SYSTEMS", "IOT",     "Микроконтроллеры"),  # AMD/Xilinx Kintex 7
    # Virtualisation stays SYSTEMS but gets proper family
    (50120, "SYSTEMS", "SYSTEMS", "Виртуализация"),     # oVirt
    (40252, "SYSTEMS", "SYSTEMS", "Виртуализация"),     # Proxmox
    (40253, "SYSTEMS", "SYSTEMS", "Виртуализация"),     # VMware
    (34402, "SYSTEMS", "SYSTEMS", "Виртуализация"),     # VMware vSphere
    # Messaging protocols → BACKEND
    (37711, "SYSTEMS", "BACKEND", "Message Brokers"),   # AMQP
    (55264, "SYSTEMS", "BACKEND", "Message Brokers"),   # ZeroMQ
    # Concurrency concept → correct family
    (40878, "SYSTEMS", "SYSTEMS", "Системное ПО"),      # Concurrency
    # Vulkan → low-level GPU API, correct family
    (43770, "SYSTEMS", "SYSTEMS", "Системное ПО"),      # Vulkan
    # 3PAR storage hardware → clear wrong family name
    (35138, "SYSTEMS", "SYSTEMS", None),                # 3PAR StoreServ 8400
]

# ══ TESTING ══════════════════════════════════════════════════════════════════
# Language-family testing tools → "Автотестирование"
# gRPC/Протоколы in TESTING → "API Testing"
# "Нагрузочное" typo → "Нагрузочное тестирование"
# X-Ray (AWS) in TESTING → "Тест-менеджмент"

def get_ids_by_domain_family(domain, family):
    cur.execute(
        "SELECT id FROM skill_canonical WHERE domain=%s AND tech_family IS NOT DISTINCT FROM %s",
        (domain, family)
    )
    return [r[0] for r in cur.fetchall()]

# We'll build TESTING_FIXES dynamically below after fetching IDs

# ══ MOBILE ═══════════════════════════════════════════════════════════════════
MOBILE_FIXES = [
    # Unity/Godot game engines → new "Game Development" family
    (50487, "MOBILE", "MOBILE", "Game Development"),   # Godot
    (41382, "MOBILE", "MOBILE", "Game Development"),   # Unity
    (38043, "MOBILE", "MOBILE", "Game Development"),   # Unity 3D
    (36960, "MOBILE", "MOBILE", "Game Development"),   # Unity Ads
    (43757, "MOBILE", "MOBILE", "Game Development"),   # Unity GameObject-UI
    (40640, "MOBILE", "MOBILE", "Game Development"),   # Unity WebGL
    (42018, "MOBILE", "MOBILE", "Game Development"),   # Unity3D
    # Swift-tagged iOS tools → "iOS"
    (42672, "MOBILE", "MOBILE", "iOS"),  # Clean Swift
    (44553, "MOBILE", "MOBILE", "iOS"),  # R.swift
    (41902, "MOBILE", "MOBILE", "iOS"),  # RxSwift
    (52178, "MOBILE", "MOBILE", "iOS"),  # Xcode Instruments
    (56698, "MOBILE", "MOBILE", "iOS"),  # Xcode logs
    (35911, "MOBILE", "MOBILE", "iOS"),  # Xcode Simulator
    # React Native family → "Cross-platform"
    (33651, "MOBILE", "MOBILE", "Cross-platform"),  # React Native
    (36717, "MOBILE", "MOBILE", "Cross-platform"),  # React Native FS
    (42651, "MOBILE", "MOBILE", "Cross-platform"),  # React Native Reanimated
    (47651, "MOBILE", "MOBILE", "Cross-platform"),  # React Native Skia
    (46810, "MOBILE", "MOBILE", "Cross-platform"),  # React Native Testing Library
    # Android API levels → "Android"
    (44234, "MOBILE", "MOBILE", "Android"),  # API level 19
    (44235, "MOBILE", "MOBILE", "Android"),  # API level 21
    (44236, "MOBILE", "MOBILE", "Android"),  # API level 23
    (44237, "MOBILE", "MOBILE", "Android"),  # API level 26
    (56147, "MOBILE", "MOBILE", "Android"),  # minSdk 26
    (53637, "MOBILE", "MOBILE", "Android"),  # Safe Android Development Concepts
    # VIPER = iOS architecture pattern (was Go family)
    (49737, "MOBILE", "MOBILE", "iOS"),     # VIPER
    # .NET MAUI → Cross-platform
    (60374, "MOBILE", "MOBILE", "Cross-platform"),  # .NET MAUI
    # Mobile First → FRONTEND domain
    (35659, "FRONTEND", "FRONTEND", "HTML/CSS"),  # Mobile First
    # Cloud Firestore → DATABASE/NoSQL
    (40541, "DATABASE", "DATABASE", "NoSQL"),  # Cloud Firestore
]

# ══ SECURITY ═════════════════════════════════════════════════════════════════
SECURITY_FIXES = [
    # Пентестирование → rename to Пентест/AppSec
    # (all 15 skills via domain+family bulk update below)
    # 3D-named security items
    (39181, "SECURITY", "SECURITY", "Compliance"),        # 3D Secure
    (40183, "SECURITY", "SECURITY", "Криптография"),      # 3DES
    # OWASP/CWE items
    (34588, "SECURITY", "SECURITY", "Пентест/AppSec"),    # CWE Top 25
    (56689, "SECURITY", "SECURITY", "Пентест/AppSec"),    # OWASP Mobile Top 10
    (62549, "SECURITY", "SECURITY", "Пентест/AppSec"),    # Уязвимости OWASP Top 10 CWE Top 25
    # ISO/IEC 27001 → Compliance
    (59151, "SECURITY", "SECURITY", "Compliance"),        # ISO/IEC 27001
]

# ══ AI_ML ════════════════════════════════════════════════════════════════════
AI_ML_FIXES = [
    # Cloud ML services → MLOps
    (45024, "AI_ML", "AI_ML", "MLOps"),   # AutoML
    (39580, "AI_ML", "AI_ML", "MLOps"),   # Vertex AI
    (54233, "AI_ML", "AI_ML", "MLOps"),   # SageMaker
    (45893, "AI_ML", "AI_ML", "MLOps"),   # Cloud ML Platform
    # CoreML → ML/AI
    (51360, "AI_ML", "AI_ML", "ML/AI"),   # CoreML
    # BullMQ = Node.js task queue → BACKEND
    (43208, "BACKEND", "BACKEND", "Message Brokers"),  # BullMQ
]

# ══ CLOUD ════════════════════════════════════════════════════════════════════
CLOUD_FIXES = [
    # Google Pub/Sub → GCP family
    (60776, "CLOUD", "CLOUD", "GCP"),    # Google Pub/Sub
    # REST S3 → AWS
    (37816, "CLOUD", "CLOUD", "AWS"),    # REST S3
    # IAM in CLOUD → stays, but change to Cloud family (it's generic IAM)
    (38996, "CLOUD", "CLOUD", "Cloud"),  # IAM
]

# ─────────────────────────────────────────────────────────────────────────────
# Build final list
# ─────────────────────────────────────────────────────────────────────────────
ALL_FIXES = SYSTEMS_FIXES + MOBILE_FIXES + SECURITY_FIXES + AI_ML_FIXES + CLOUD_FIXES

# Fetch IDs for bulk TESTING fixes
def build_testing_fixes():
    fixes = []
    # Language families → Автотестирование
    for fam in ["JavaScript", ".NET", "Java", "Ruby", "PHP", "iOS", "Android"]:
        ids = get_ids_by_domain_family("TESTING", fam)
        for sid in ids:
            fixes.append((sid, "TESTING", "TESTING", "Автотестирование"))
    # gRPC/Протоколы → API Testing
    for sid in get_ids_by_domain_family("TESTING", "gRPC/Протоколы"):
        fixes.append((sid, "TESTING", "TESTING", "API Testing"))
    # "Нагрузочное" typo → "Нагрузочное тестирование"
    for sid in get_ids_by_domain_family("TESTING", "Нагрузочное"):
        fixes.append((sid, "TESTING", "TESTING", "Нагрузочное тестирование"))
    # Cloud test → NULL
    for sid in get_ids_by_domain_family("TESTING", "Cloud"):
        fixes.append((sid, "TESTING", "TESTING", None))
    # ML/AI → NULL (A/B Testing is fine without family)
    for sid in get_ids_by_domain_family("TESTING", "ML/AI"):
        fixes.append((sid, "TESTING", "TESTING", None))
    # AWS → Тест-менеджмент (X-Ray Jira plugin)
    for sid in get_ids_by_domain_family("TESTING", "AWS"):
        fixes.append((sid, "TESTING", "TESTING", "Тест-менеджмент"))
    return fixes

TESTING_FIXES = build_testing_fixes()
ALL_FIXES = SYSTEMS_FIXES + TESTING_FIXES + MOBILE_FIXES + SECURITY_FIXES + AI_ML_FIXES + CLOUD_FIXES

# ─────────────────────────────────────────────────────────────────────────────
# Preview + Apply
# ─────────────────────────────────────────────────────────────────────────────
print(f"\n{'ID':>7}  {'Имя':<30}  {'Домен':<22}  {'Семейство'}")
print("─" * 105)

applied = 0
for sid, old_domain, new_domain, new_family in ALL_FIXES:
    if FILTER_DOMAIN and old_domain != FILTER_DOMAIN and new_domain != FILTER_DOMAIN:
        continue
    cur.execute("SELECT name, domain, tech_family FROM skill_canonical WHERE id=%s", (sid,))
    row = cur.fetchone()
    if not row:
        continue
    name, curr_domain, curr_family = row
    # Skip if already correct
    if curr_domain == new_domain and curr_family == new_family:
        continue
    dom_str = f"{curr_domain} → {new_domain}" if curr_domain != new_domain else curr_domain
    fam_str = f"{str(curr_family)} → {str(new_family)}" if curr_family != new_family else str(new_family)
    print(f"{sid:>7}  {name:<30}  {dom_str:<22}  {fam_str}")
    if SAVE:
        cur.execute(
            "UPDATE skill_canonical SET domain=%s, tech_family=%s WHERE id=%s",
            (new_domain, new_family, sid)
        )
    applied += 1

print(f"\nTotal: {applied}")

# Bulk: SECURITY Пентестирование → Пентест/AppSec
if not FILTER_DOMAIN or FILTER_DOMAIN == "SECURITY":
    cur.execute("""
        SELECT id, name FROM skill_canonical
        WHERE domain='SECURITY' AND tech_family='Пентестирование'
    """)
    rows = cur.fetchall()
    print(f"\nSECURITY Пентестирование → Пентест/AppSec: {len(rows)} skills")
    for sid, name in rows:
        print(f"  {sid:>7}  {name}")
    if SAVE and rows:
        cur.execute("""
            UPDATE skill_canonical SET tech_family='Пентест/AppSec'
            WHERE domain='SECURITY' AND tech_family='Пентестирование'
        """)

if SAVE:
    conn.commit()
    print("\n✅ Saved")
else:
    print("\nℹ️  Run with --save to apply")

conn.close()
