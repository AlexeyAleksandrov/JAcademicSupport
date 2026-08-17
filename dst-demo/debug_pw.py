import sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
import latex2mathml.converter as lc

tex = r'pw_i = raw_w_{i} / \sum_{j} raw_w_{j},   \sum_{i} pw_i = 1'
print('Input:', repr(tex))
try:
    mml = lc.convert(tex)
    print('MathML OK, len=', len(mml))
    print(mml[:300])
except Exception as e:
    print('ERROR:', type(e).__name__, str(e))

# Try simpler variant
tex2 = r'pw_i = \frac{\mathrm{raw\_w}_{i}}{\sum_{j} \mathrm{raw\_w}_{j}},\quad \sum_{i} pw_i = 1'
try:
    mml2 = lc.convert(tex2)
    print('tex2 OK')
except Exception as e2:
    print('tex2 ERROR:', e2)

# Even simpler
tex3 = r'pw_i = w_{i} / \sum_{j} w_{j}'
try:
    mml3 = lc.convert(tex3)
    print('tex3 OK')
except Exception as e3:
    print('tex3 ERROR:', e3)
