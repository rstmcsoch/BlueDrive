#!/usr/bin/env python3
"""Offline sanity checks, not a substitute for AAPT, lint or device tests."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
res = root / 'app/src/main/res'
names = {}
for path in res.rglob('*.xml'):
    tree = ET.parse(path)
    for item in tree.getroot():
        if 'name' in item.attrib:
            names.setdefault(item.tag, set()).add(item.attrib['name'])
errors = []
for path in (root / 'app/src/main/java').rglob('*.java'):
    for kind, name in re.findall(r'(?<!android\.)R\.(string|color|layout|drawable)\.(\w+)', path.read_text()):
        valid = name in names.get(kind, set()) if kind in ('string', 'color') else any(res.glob(f'{kind}*/{name}.*'))
        if not valid:
            errors.append(f'{path.name}: missing {kind}/{name}')
for qualifier in ('values-fr', 'values-ar'):
    expected = {item.attrib['name'] for item in ET.parse(res/'values/blue_strings.xml').getroot()}
    actual = {item.attrib['name'] for item in ET.parse(res/qualifier/'blue_strings.xml').getroot()}
    if expected != actual:
        errors.append(f'{qualifier}: untranslated redesign strings')
assert not errors, '\n'.join(errors)
print('PASS: XML parses, Java resource references resolve, new translation keys match.')
