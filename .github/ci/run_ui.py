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


def current_top():
    out = adb('shell', 'dumpsys', 'activity', 'activities')
    m = re.findall(r'(?:mResumedActivity|ResumedActivity)[^\n]*', out)
    return m[0].strip() if m else '?'


def launch_like_launcher():
    adb('shell', 'monkey', '-p', PKG, '-c', 'android.intent.category.LAUNCHER', '1')
    time.sleep(2.5)


def tap_next(after=1.3):
    ns = wait_nodes('onboard_button', 5)
    x, y = center(ns[0]) if ns else (W // 2, int(H * 0.915))
    tap_xy(x, y)
    time.sleep(after)
    log(f'TAP next ({x},{y}) found_node={bool(ns)}')


def open_settings_and_press_check():
    tap_id('settings_button')
    swipe_up()
    return tap_id('check_update_button', after=1.0)


def wait_installer(label, timeout=170):
    """منتظر می‌مانیم تا پس از پایان دانلود، صفحه‌ی نصب (یا تنظیمات «نصب از منبع ناشناس») جلوی برنامه بیاید."""
    t0 = time.time()
    while time.time() - t0 < timeout:
        top = current_top()
        if PKG not in top:
            log(f'{label}: PROMPT APPEARED after {int(time.time() - t0)}s -> {top}')
            return True
        time.sleep(5)
    dl = adb('shell', 'dumpsys', 'downloads')
    open(f'{OUT}/downloads-{label[7:8]}.txt', 'w', encoding='utf-8').write(dl[-5000:])
    files = adb('shell', 'ls', '-la', f'/sdcard/Android/data/{PKG}/files/Download/')
    log(f'{label}: NO PROMPT after {timeout}s; top={current_top()}; download dir: {files.strip()[:200]}')
    return False


def cleanup_installer():
    """صفحه‌ی نصبِ بازمانده نباید در مرحله‌های بعدی تداخل ایجاد کند."""
    adb('shell', 'am', 'force-stop', 'com.google.android.packageinstaller')
    adb('shell', 'am', 'force-stop', 'com.android.packageinstaller')
    adb('shell', 'input', 'keyevent', '3')
    time.sleep(1.5)


def press_check_and_leave():
    """دکمه‌ی بررسی آپدیت را می‌زند و بلافاصله از تنظیمات بیرون می‌رود (کاربری که صبر نمی‌کند)."""
    tap_id('settings_button')
    swipe_up()
    ns = wait_nodes('check_update_button', 15)
    if not ns:
        log('WARN check_update_button not found')
        return
    x, y = center(ns[0])
    tap_xy(x, y)
    adb('shell', 'input', 'keyevent', '4')
    time.sleep(0.8)


def update_flow():
    # A) در همان صفحه‌ی تنظیمات می‌مانیم تا دانلود تمام شود
    cleanup_installer()
    launch_like_launcher()
    log('A start top: ' + current_top())
    open_settings_and_press_check()
    shot('30-update-clicked', 1.0)
    ok_a = wait_installer('UPDATE A (stayed in settings)')
    shot('31-update-A-stay', 0.5)
    cleanup_installer()
    launch_like_launcher()
    # B) بلافاصله بعد از زدن دکمه از تنظیمات بیرون می‌رویم (قبل از پایان دانلود)
    for _ in range(3):
        if nodes(dump(), 'settings_button'):
            break
        back()
    log('B start top: ' + current_top())
    press_check_and_leave()
    log('B after leaving settings, top: ' + current_top())
    ok_b = wait_installer('UPDATE B (left settings right after tapping)')
    shot('32-update-B-left-settings', 0.5)
    log(f'UPDATE RESULT: stay={ok_a} leave={ok_b}')
    cleanup_installer()
    launch_like_launcher()


def offline_first_run():
    adb('shell', 'svc', 'wifi', 'disable')
    adb('shell', 'svc', 'data', 'disable')
    time.sleep(4)
    adb('shell', 'pm', 'clear', PKG)
    adb('shell', 'am', 'start', '-n', f'{PKG}/com.gamesettings.app.MainActivity')
    wait_nodes('onboard_button', 40)
    for _ in range(4):
        tap_next(1.4)
    time.sleep(6)
    shot('40-offline-first-run', 0.5)
    adb('shell', 'svc', 'wifi', 'enable')
    adb('shell', 'svc', 'data', 'enable')
    time.sleep(6)
    adb('shell', 'am', 'force-stop', PKG)
    launch_like_launcher()
    wait_nodes('game_name', 40)
    shot('41-network-back-list', 2.0)


def dont_keep_activities():
    adb('shell', 'settings', 'put', 'global', 'always_finish_activities', '1')
    try:
        launch_like_launcher()
        wait_nodes('game_name', 30)
        open_first_game()
        shot('50-dk-detail', 1.5)
        adb('shell', 'input', 'keyevent', '3')
        time.sleep(2.5)
        launch_like_launcher()
        shot('51-dk-returned', 1.0)
        log('DK top activity after return: ' + current_top())
    finally:
        adb('shell', 'settings', 'put', 'global', 'always_finish_activities', '0')


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
    dl_lines = [l for l in full.splitlines() if re.search(r'DownloadManager|DownloadProvider|DownloadJobService', l)]
    open(f'{OUT}/logcat-download.txt', 'w', encoding='utf-8').write('\n'.join(dl_lines[-120:]))
    leaks = [l for l in full.splitlines() if 'IntentReceiverLeaked' in l or ('leaked' in l.lower() and 'gamesettings' in l)]
    open(f'{OUT}/logcat-leaks.txt', 'w', encoding='utf-8').write('\n'.join(leaks[-60:]))
    log(f'logcat: {len(keep)} app lines, {len(errs)} error-ish lines')
    for pat in ('FATAL EXCEPTION', 'IntentReceiverLeaked', 'Leaked', 'StrictMode'):
        log(f'COUNT {pat}: ' + str(sum(1 for l in full.splitlines() if pat in l and (PKG in l or pat in ('FATAL EXCEPTION', 'IntentReceiverLeaked')))))


for title, fn in [
    ('onboarding + theme picker', onboarding),
    ('list and detail (light)', list_and_detail_light),
    ('settings + switch to dark', settings_and_themes),
    ('system theme + english', system_and_english),
    ('search + refresh', search_and_refresh),
    ('update flow (stay vs leave settings)', update_flow),
    ('offline first run', offline_first_run),
    ("don't keep activities", dont_keep_activities),
    ('monkey', monkey),
    ('logs', collect_logs),
]:
    step(title, fn)
log('DONE')
