#!/usr/bin/env python3
"""گزارش Android Lint را به متن ساده‌ی مرتب‌شده (بر اساس شدت) تبدیل می‌کند."""
import os
import sys
import xml.etree.ElementTree as ET

SRC = 'app/build/reports/lint-results-debug.xml'
os.makedirs('ci-shots', exist_ok=True)
out = open('ci-shots/lint.txt', 'w', encoding='utf-8')
if not os.path.exists(SRC):
    out.write('گزارش lint ساخته نشد\n')
    sys.exit(0)
issues = ET.parse(SRC).getroot().findall('issue')
order = {'Fatal': 0, 'Error': 1, 'Warning': 2, 'Informational': 3, 'Ignore': 4}
issues.sort(key=lambda i: (order.get(i.get('severity'), 9), i.get('id') or ''))
out.write(f'جمع مشکلات: {len(issues)}\n')
for i in issues:
    loc = i.find('location')
    where = ''
    if loc is not None:
        where = (loc.get('file', '').replace(os.getcwd() + '/', '') + ':' + loc.get('line', ''))
    out.write(f"[{i.get('severity')}] {i.get('id')}: {i.get('message')}  @ {where}\n")
