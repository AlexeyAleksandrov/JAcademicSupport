import sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
import latex2mathml.converter as lc
import mathml2omml
from lxml import etree

OMML_NS = 'http://schemas.openxmlformats.org/officeDocument/2006/math'

def fix_omml_ns(omml_str):
    ns_attr = 'xmlns:m="{}"'.format(OMML_NS)
    return omml_str.replace('<m:oMath>', '<m:oMath {}>'.format(ns_attr), 1)

# Test 1: simple fraction
tex = r'\frac{|A|}{|B|}'
mml = lc.convert(tex)
omml_str = mathml2omml.convert(mml)
fixed = fix_omml_ns(omml_str)
el = etree.fromstring(fixed.encode('utf-8'))
print('Test1 OK tag:', el.tag[:60])

# Test 2: full formula
tex2 = r'\omega(P, C) = \frac{|\{v \in \mathcal{V}_P : \mathcal{S}(v) \cap C \neq \emptyset\}|}{|\mathcal{V}_P|}'
try:
    mml2 = lc.convert(tex2)
    omml2 = mathml2omml.convert(mml2)
    fixed2 = fix_omml_ns(omml2)
    el2 = etree.fromstring(fixed2.encode('utf-8'))
    print('Test2 omega: OK')
except Exception as e:
    print('Test2 ERROR:', e)

# Test 3: inline unicode formula
inline = 's_{\\text{raw}}(v, C) = \\sum_{s \\in \\mathcal{S}_C} \\mu(s, v) \\cdot w_{\\text{loc}}(s, v)'
try:
    mml3 = lc.convert(inline)
    omml3 = mathml2omml.convert(mml3)
    fixed3 = fix_omml_ns(omml3)
    el3 = etree.fromstring(fixed3.encode('utf-8'))
    print('Test3 s_raw: OK')
except Exception as e:
    print('Test3 ERROR:', e)

# Test 4: kappa formula
tex4 = r'\kappa = 1 - \exp(-\lambda \cdot n / N)'
try:
    mml4 = lc.convert(tex4)
    omml4 = mathml2omml.convert(mml4)
    fixed4 = fix_omml_ns(omml4)
    el4 = etree.fromstring(fixed4.encode('utf-8'))
    print('Test4 kappa: OK')
except Exception as e:
    print('Test4 ERROR:', e)
