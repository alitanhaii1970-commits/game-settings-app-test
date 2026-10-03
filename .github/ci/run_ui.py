#!/usr/bin/env python3
"""برنامه را روی شبیه‌ساز اجرا می‌کند: ورود اولیه، رفتن به صفحه‌ها در تم روشن/تیره/خودکار و زبان انگلیسی،
اسکرین‌شات، تست تصادفی (monkey) و جمع‌کردن لاگ خطاها. هر مرحله مستقل است؛ خطا در یکی بقیه را متوقف نمی‌کند."""
import os
import re
import subprocess
import time
import traceback
import xml.etree.ElementTree as ET

PKG = 'com.gamesettings.app.test'
OUT = 'ci-shots'
os.makedirs(OUT, exist_ok=True)
LOG = open(f'{OUT}/steps.txt', 'w', encoding='utf-8')


def log(msg):
    print(msg, flush=True)
    LOG.write(msg + '\n')
    LOG.flush()


def run(cmd, timeout=90):
    return subprocess.run(cmd, capture_output=True, timeout=timeout)


def adb(*args, timeout=90):
    return run(['adb', *args], timeout).stdout.decode('utf-8', 'replace')


def shot(name, wait=1.3):
    time.sleep(wait)
    data = run(['adb', 'exec-out', 'screencap', '-p']).stdout
    open(f'{OUT}/{name}.png', 'wb').write(data)
    log(f'SHOT {name} ({len(data) // 1024} KB)')


def dump():
    for _ in range(3):
        adb('shell', 'uiautomator', 'dump', '/sdcard/ui.xml')
        xml = adb('exec-out', 'cat', '/sdcard/ui.xml')
        try:
            return ET.fromstring(xml[xml.index('<'):])
        except Exception:
            time.sleep(1)
    return None


def nodes(root, rid=None):
    if root is None:
        return []
    return [n for n in root.iter('node') if not rid or n.get('resource-id') == f'{PKG}:id/{rid}']


def center(n):
    x1, y1, x2, y2 = map(int, re.findall(r'-?\d+', n.get('bounds')))
    return (x1 + x2) // 2, (y1 + y2) // 2


def tap_xy(x, y):
    adb('shell', 'input', 'tap', str(x), str(y))


def wait_nodes(rid, timeout=20):
    t0 = time.time()
    while time.time() - t0 < timeout:
        ns = nodes(dump(), rid)
        if ns:
            return ns
        time.sleep(1)
    return []


def tap_id(rid, timeout=15, after=1.0):
    ns = wait_nodes(rid, timeout)
    if not ns:
        log(f'WARN not found: {rid}')
        return False
    x, y = center(ns[0])
    tap_xy(x, y)
    time.sleep(after)
    log(f'TAP {rid} ({x},{y})')
    return True


def back():
    adb('shell', 'input', 'keyevent', '4')
    time.sleep(1.0)


def size():
    m = re.search(r'(\d+)x(\d+)', adb('shell', 'wm', 'size'))
    return (int(m.group(1)), int(m.group(2))) if m else (1080, 2400)


W, H = size()


def swipe_up():
    adb('shell', 'input', 'swipe', str(W // 2), str(int(H * 0.75)), str(W // 2), str(int(H * 0.25)), '450')
    time.sleep(0.8)


def swipe_down():
    adb('shell', 'input', 'swipe', str(W // 2), str(int(H * 0.25)), str(W // 2), str(int(H * 0.75)), '450')
    time.sleep(0.8)


def step(name, fn):
    log(f'=== {name}')
    try:
        fn()
    except Exception:
        log('STEP FAILED: ' + traceback.format_exc())
        shot(f'zz-failed-{re.sub("[^a-z0-9]+", "-", name.lower())}', 0.3)


def open_first_game():
    names = wait_nodes('game_name', 40)
    log(f'visible game cards: {len(names)}')
    if names:
        x, y = center(names[0])
        tap_xy(x, y)
        time.sleep(1.5)


def onboarding():
    adb('shell', 'pm', 'clear', PKG)
    adb('shell', 'am', 'start', '-n', f'{PKG}/com.gamesettings.app.MainActivity')
    wait_nodes('onboard_button', 40)
    shot('01-welcome', 1.8)
    tap_id('onboard_button')
    shot('02-language')
    tap_id('onboard_button')
    shot('03-theme')
    tap_id('tile_theme_light', after=0.45)
    shot('04-reveal-mid', 0.0)
    time.sleep(1.5)
    shot('05-theme-light-after', 0.3)
    still = bool(nodes(dump(), 'tile_theme_light'))
    log(f'after theme change we are still on the theme page: {still}')
    tap_id('onboard_button')
    shot('06-tier')
    tap_id('option_tier_strong')
    tap_id('onboard_button', after=1.0)


def list_and_detail_light():
    wait_nodes('game_name', 45)
    shot('07-list-light', 2.0)
    open_first_game()
    shot('08-detail-light', 2.0)
    swipe_up()
    shot('09-detail-light-scrolled', 0.5)
    back()
    shot('10-list-after-back', 1.0)


def settings_and_themes():
    tap_id('settings_button')
    shot('11-settings-light', 1.5)
    swipe_up()
    shot('12-settings-light-scrolled', 0.5)
    swipe_down()
    tap_id('tile_theme_dark', after=0.5)
    shot('13-dark-reveal-mid', 0.0)
    time.sleep(1.5)
    shot('14-settings-dark', 0.3)
    back()
    shot('15-list-dark', 1.5)
    open_first_game()
    shot('16-detail-dark', 2.0)
    back()


def system_and_english():
    tap_id('settings_button')
    tap_id('tile_theme_system', after=1.8)
    shot('17-settings-system-theme', 0.3)
    swipe_up()
    tap_id('lang_en', after=2.0)
    shot('18-settings-english', 0.5)
    swipe_down()
    back()
    shot('19-list-english', 1.5)


def search_and_refresh():
    tap_id('search_box', after=0.6)
    adb('shell', 'input', 'text', 'a')
    shot('20-search-a', 0.8)
    adb('shell', 'input', 'keyevent', '4')
    time.sleep(0.5)
    tap_id('refresh_button', after=0.0)
    shot('21-refresh-spinning', 0.25)
    shot('22-after-refresh', 3.0)


def monkey():
    out = adb('shell', 'monkey', '-p', PKG, '-s', '7', '--throttle', '150', '--pct-syskeys', '0',
              '--pct-appswitch', '0', '-v', '700', timeout=480)
    open(f'{OUT}/monkey.txt', 'w', encoding='utf-8').write(out[-6000:])
    log('monkey crashed: ' + str('CRASH' in out or 'ANR' in out))
    shot('23-after-monkey', 1.0)


def collect_logs():
    full = adb('logcat', '-d', '-v', 'threadtime', timeout=180)
    pid = adb('shell', 'pidof', PKG).strip()
    keep = [l for l in full.splitlines()
            if PKG in l or (pid and f' {pid} ' in l)
            or re.search(r'FATAL EXCEPTION|AndroidRuntime|ANR in|StrictMode', l)]
    open(f'{OUT}/logcat-app.txt', 'w', encoding='utf-8').write('\n'.join(keep[-900:]))
    errs = [l for l in keep if re.search(r' [EF] |FATAL|Exception|ANR', l)]
    open(f'{OUT}/logcat-errors.txt', 'w', encoding='utf-8').write('\n'.join(errs[-300:]))
    log(f'logcat: {len(keep)} app lines, {len(errs)} error-ish lines')


for title, fn in [
    ('onboarding + theme picker', onboarding),
    ('list and detail (light)', list_and_detail_light),
    ('settings + switch to dark', settings_and_themes),
    ('system theme + english', system_and_english),
    ('search + refresh', search_and_refresh),
    ('monkey', monkey),
    ('logs', collect_logs),
]:
    step(title, fn)
log('DONE')
