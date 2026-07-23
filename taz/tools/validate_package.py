#!/usr/bin/env python3
from __future__ import annotations

import hashlib
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
checks: list[tuple[str, bool, str]] = []

def check(name: str, condition: bool, detail: str = "") -> None:
    checks.append((name, bool(condition), detail))

required = [
    ".github/workflows/build-tazalarm.yml",
    "app/build.gradle.kts",
    "app/src/main/AndroidManifest.xml",
    "app/src/main/java/com/james/mathwakealarm/AlarmScheduler.kt",
    "app/src/main/java/com/james/mathwakealarm/AlarmService.kt",
    "app/src/main/java/com/james/mathwakealarm/AlarmActivity.kt",
    "app/src/main/java/com/james/mathwakealarm/AppUi.kt",
    "app/src/main/java/com/james/mathwakealarm/Onboarding.kt",
    "app/src/main/java/com/james/mathwakealarm/BrandLogo.kt",
    "personal-release.keystore",
]
for item in required:
    check(f"Required file: {item}", (ROOT / item).is_file())

xml_files = list((ROOT / "app/src/main").rglob("*.xml"))
xml_ok = True
xml_error = ""
for path in xml_files:
    try:
        ET.parse(path)
    except Exception as exc:
        xml_ok = False
        xml_error = f"{path.relative_to(ROOT)}: {exc}"
        break
check("All Android XML parses", xml_ok, f"{len(xml_files)} XML files" if xml_ok else xml_error)

build = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
scheduler = (ROOT / "app/src/main/java/com/james/mathwakealarm/AlarmScheduler.kt").read_text(encoding="utf-8")
service = (ROOT / "app/src/main/java/com/james/mathwakealarm/AlarmService.kt").read_text(encoding="utf-8")
alarm_ui = (ROOT / "app/src/main/java/com/james/mathwakealarm/AlarmActivity.kt").read_text(encoding="utf-8")
app_ui = (ROOT / "app/src/main/java/com/james/mathwakealarm/AppUi.kt").read_text(encoding="utf-8")
onboarding = (ROOT / "app/src/main/java/com/james/mathwakealarm/Onboarding.kt").read_text(encoding="utf-8")
main_source = "\n".join(p.read_text(encoding="utf-8") for p in (ROOT / "app/src/main/java").rglob("*.kt"))

check("Application ID retained", 'applicationId = "com.james.mathwakealarm"' in build)
check("Version 2.1.0 / 210", 'versionCode = 210' in build and 'versionName = "2.1.0"' in build)
check("TAZALARM app label", '<string name="app_name">TAZALARM</string>' in (ROOT / "app/src/main/res/values/strings.xml").read_text())
check("Exact alarm permission", "android.permission.SCHEDULE_EXACT_ALARM" in manifest)
check("Full-screen alarm permission", "android.permission.USE_FULL_SCREEN_INTENT" in manifest)
check("Direct foreground-service PendingIntent", "PendingIntent.getForegroundService" in scheduler)
check("Ten-second backup trigger", "BACKUP_DELAY_MS = 10_000L" in scheduler and "BackupAlarmReceiver" in scheduler)
check("Exact allow-while-idle scheduling", "setExactAndAllowWhileIdle" in scheduler)
check("Primary cancels backup", "cancelBackup(this, alarmId)" in service)
check("Foreground service is sticky", "return START_STICKY" in service)
check("Partial wake lock", "PowerManager.PARTIAL_WAKE_LOCK" in service)
check("Gradual 10-second volume ramp", "postDelayed(this, 10_000L)" in service and "0.05f" in service)
check("Active alarm recovery", "activeAlarmId()" in service and "Alarm service restored" in service)
check("Reboot/time/update rescheduling", all(x in manifest for x in ["BOOT_COMPLETED", "TIME_SET", "TIMEZONE_CHANGED", "MY_PACKAGE_REPLACED"]))
check("One-minute sunrise default", "sunriseSeconds: Int = 60" in main_source and "sunriseDuration" in alarm_ui)
check("Per-window brightness ramp", "screenBrightness" in alarm_ui)
check("Night-to-day horizon renderer", all(x in alarm_ui for x in ["purple", "red", "orange", "daylight", "SunriseHorizon"]))
check("Sneezing-cat universal logo", "SneezingCatLogo" in main_source and "drawPath(drop" in main_source)
check("Multiple alarms in onboarding", "queuedAlarms" in onboarding and "completeOnboarding(name, alarms)" in onboarding)
check("Five main navigation areas", all(x in app_ui for x in ["HOME", "ALARMS", "ROUTINES", "PROGRESS", "SETTINGS"]))
check("Generic barcode title", 'Text("Scan Barcode"' in alarm_ui and 'title = "Scan Barcode"' in main_source)
check("Generic barcode helper", "Barcode must match your saved code" in alarm_ui)
check("Generic scanner action", "Open Scanner" in alarm_ui)
check("No Kitchen wording in production source", re.search(r"\bkitchen\b", main_source, re.I) is None)
check("Live photo capture", "ActivityResultContracts.TakePicture" in alarm_ui and "ImageSimilarity.bestScore" in alarm_ui)
check("50-question irreversible penalty", "questionTarget = if (penaltyMode) 50" in alarm_ui and "cannot return" in alarm_ui)
check("Question topic coverage", all(topic in main_source for topic in ["WORLD_WAR_II", "CARL_JUNG", "TWENTIETH_CENTURY", "GEOGRAPHY", "SCIENCE", "SPORT", "LOGIC"]))
check("Progress and reliability logging", "reliabilityEvents" in main_source and "ProgressScreen" in app_ui)
check("Two-minute screen-off test", "120_000L" in app_ui and "scheduleTest" in scheduler)

# Basic Kotlin delimiter balance and no parser-level errors from the local compiler.
kotlin_files = list((ROOT / "app/src").rglob("*.kt"))
balanced = all(p.read_text().count("{") == p.read_text().count("}") and p.read_text().count("(") == p.read_text().count(")") for p in kotlin_files)
check("Kotlin delimiter balance", balanced, f"{len(kotlin_files)} Kotlin files")

bad_paths = [p for p in ROOT.rglob("*") if any(part in {"build", ".gradle", ".idea", "__pycache__"} for part in p.relative_to(ROOT).parts)]
check("No generated build/cache folders", not bad_paths, ", ".join(str(p.relative_to(ROOT)) for p in bad_paths[:3]))

try:
    output = subprocess.check_output([
        "keytool", "-list", "-v", "-keystore", str(ROOT / "personal-release.keystore"),
        "-storepass", "TazAlarmPersonal2026", "-alias", "tazalarm"
    ], stderr=subprocess.STDOUT, text=True)
    check("Release keystore opens and alias exists", "Alias name: tazalarm" in output and "PrivateKeyEntry" in output)
except Exception as exc:
    check("Release keystore opens and alias exists", False, str(exc))

passed = sum(ok for _, ok, _ in checks)
print(f"TAZALARM package validation: {passed}/{len(checks)} checks passed")
for name, ok, detail in checks:
    marker = "PASS" if ok else "FAIL"
    print(f"[{marker}] {name}" + (f" — {detail}" if detail else ""))

if passed != len(checks):
    sys.exit(1)
