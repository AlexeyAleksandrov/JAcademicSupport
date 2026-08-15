"""Быстрый тест промпта на заведомо проблемных навыках."""
import os, sys
sys.path.insert(0, os.path.dirname(__file__))
from assign_families_llm import call_gigachat, parse_llm_response, SYSTEM_PROMPT

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

TEST_SKILLS = [
    (1, "1С", "Navision"),
    (2, "1С", "Payroll"),
    (3, "1С", "СКД"),
    (4, "1С", "BSP"),
    (5, "AI_ML", "4-bit QLoRA"),
    (6, "AI_ML", "ADK"),
    (7, "AI_ML", "LangChain"),
    (8, "BACKEND", "back-end"),
    (9, "BACKEND", "backend разработка"),
    (10, "BACKEND", "DELETE"),
    (11, "BACKEND", "GET"),
    (12, "BACKEND", "CRUD"),
    (13, "BACKEND", "FastAPI"),
    (14, "BACKEND", "Spring Boot"),
    (15, "BACKEND", "high-load сервисы"),
]

user_msg = "\n".join(f"{sid}|{dom}|{name}" for sid, dom, name in TEST_SKILLS)
print("=== Входные данные ===")
print(user_msg)
print()

raw = call_gigachat(SYSTEM_PROMPT, user_msg)
print("=== Ответ GigaChat ===")
print(raw)
print()

ids = [s[0] for s in TEST_SKILLS]
parsed = parse_llm_response(raw, ids)

print("=== Итог ===")
for sid, dom, name in TEST_SKILLS:
    fam = parsed.get(sid, "???")
    status = "✓" if fam else "—"
    print(f"  {status} [{dom:12}] {str(fam or '—'):30} <- {name}")
