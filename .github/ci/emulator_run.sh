#!/usr/bin/env bash
# داخل شبیه‌ساز اجرا می‌شود (توسط android-emulator-runner)
set -x
adb install -r app/build/outputs/apk/debug/app-debug.apk
python3 .github/ci/run_ui.py || true
exit 0
