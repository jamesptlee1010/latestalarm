# TAZALARM v2.1.0

TAZALARM is a personal Android sunrise alarm that keeps sounding until its configured wake-up routine has been completed.

This package is arranged for a **GitHub Actions build**, so Android Studio is not required. The existing Android identity is retained:

```text
applicationId: com.james.mathwakealarm
version: 2.1.0 (210)
minimum Android: 8.0 / API 26
```

## What is included

### New TAZALARM design

- Universal minimalist monoline sneezing-cat identity.
- TAZALARM name throughout the app and launcher icon.
- Premium light and dark main screens based on the selected mockup.
- Home, Alarms, Routines, Progress and Settings navigation.
- Next-alarm card, quick actions, upcoming alarms and morning completion summary.

### First-time setup

- Name and personalised morning/afternoon/evening greeting.
- Create one or several alarms during setup.
- Alarm name, time and recurring weekdays.
- Guided barcode registration.
- Guided live-camera reference photos.
- Final review before alarms are enabled.

### Multiple alarms and routines

- Independent alarm time, weekdays, enabled state, sunrise duration and vibration.
- Ordered Questions, Barcode and Photo steps.
- Add, edit, move, duplicate and delete routine steps.
- Presets: Quick Start, Normal Workday, Must Get Up and Weekend.
- Independent routine per alarm.

### Live alarm

- Full-screen display over the lock screen.
- One-minute animated horizon: night navy → purple → red → orange → warm daylight.
- App-window brightness rises with the sunrise.
- Alarm begins at 5% volume and increases every 10 seconds.
- Five seconds of audio relief after each correct answer.
- Clear `Step X of Y` progress.
- Generic barcode wording exactly as requested:
  - **Scan Barcode**
  - **Barcode must match your saved code**
  - **Open Scanner**
- Live-camera photo verification against saved reference photos.
- Deliberate 50-correct-question penalty route with no return to the normal routine.
- Back navigation cannot dismiss the active alarm.

### Questions and progress

- Generated arithmetic plus built-in questions across World War II, Carl Jung, twentieth-century history, geography, science, sport, general knowledge and logic.
- Unlimited question skipping, but skipped questions give no progress.
- Per-topic correct/attempted data.
- Completion times, step results, penalty usage and recent mornings.
- Persistent reliability event timeline.

### Reliability retained from v2.0.1

- Exact alarms delivered directly to a foreground service.
- A separate backup trigger ten seconds later.
- Partial wake lock, `START_STICKY` recovery and active-alarm restoration.
- High-importance full-screen alarm notification.
- Rescheduling after reboot, clock changes, timezone changes and app update.
- Two-minute screen-off reliability test.
- Battery optimisation, exact-alarm and notification setup links.

## Build without Android Studio

1. Keep the GitHub repository **Private** because this personal package contains its signing keystore.
2. Extract the ZIP.
3. Upload everything inside the extracted folder to the top level of the GitHub repository. Include the hidden `.github` folder.
4. Commit to `main`.
5. Open **Actions → Build TAZALARM APK**.
6. Run the workflow or open the automatically started run.
7. After the green tick, download **TAZALARM-v2.1.0-installable-APK** from Artifacts.
8. Unzip the artifact and install `TAZALARM-v2.1.0.apk` on the phone.

## Critical signing note

The v2.0.1 private signing file was not available in this chat, so this package contains a newly generated TAZALARM personal key. The project keeps the old application ID, but Android requires both the application ID **and the exact same signing certificate** to update an installed app.

- With the included key, this is a signed and installable **fresh installation**.
- To install over MathWake Alarm v2.0.1 without uninstalling it, replace `personal-release.keystore` with the exact keystore from the v2.0.1 repository and update the four signing values in `app/build.gradle.kts` to match that key.

See `SIGNING_COMPATIBILITY.md` before replacing an existing installation.

## First real-device checks

Before relying on it as the only alarm:

1. Allow notifications.
2. Allow exact alarms / Alarms & reminders.
3. Allow full-screen alarm behaviour if Android exposes that setting.
4. Exclude TAZALARM from aggressive battery optimisation.
5. Use Settings → **Schedule Screen-Off Test**, lock the phone and confirm the alarm opens after two minutes.
6. Test once after restarting the phone.
7. Keep a backup alarm until it has succeeded on several mornings on the exact handset.

## Build validation boundary

The packaging environment can validate source structure, XML, signing material and reliability implementation, but does not contain a complete Android SDK. The supplied GitHub Actions workflow performs the authoritative unit tests, Android release compilation, APK signing and signature verification.
