import sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
from build_full_doc import unicode_to_latex, make_omml_element, is_formula_quote, MATH_MARKERS
from pathlib import Path
import re

MD_FILE = Path(__file__).parent / "DST_ALGORITHM_FULL.md"
lines = MD_FILE.read_text(encoding='utf-8').splitlines()

err = []
for i, line in enumerate(lines):
    # LaTeX block
    if line.strip() == '$$':
        j = i + 1
        buf = []
        while j < len(lines) and lines[j].strip() != '$$':
            buf.append(lines[j].strip())
            j += 1
        src = ' '.join(buf)
        el = make_omml_element(src)
        if el is None:
            err.append(('LaTeX block', i+1, src[:100]))
        continue
    # Quote formula
    if is_formula_quote(line):
        content = re.sub(r'^>\s*', '', line).strip()
        latex_src = unicode_to_latex(content)
        el = make_omml_element(latex_src)
        if el is None:
            err.append(('inline', i+1, content[:100]))

print(f'Errors: {len(err)}')
for kind, ln, src in err:
    print(f'  [{kind}] line {ln}: {src}')
    latex = unicode_to_latex(src) if kind == 'inline' else src
    print(f'    -> latex: {latex[:100]}')
    import latex2mathml.converter as lc
    import mathml2omml
    try:
        mml = lc.convert(latex)
        print(f'    MathML ok, len={len(mml)}')
        try:
            omml = mathml2omml.convert(mml)
            print(f'    OMML ok, len={len(omml)}')
        except Exception as e2:
            print(f'    OMML ERROR: {e2}')
    except Exception as e1:
        print(f'    MathML ERROR: {e1}')
