# TAZALARM v2.1.0 validation

Validation completed in the packaging environment on 23 July 2026.

## Passed source/package checks

- **43/43 targeted checks passed.**
- All 20 Android manifest/resource XML files parsed successfully.
- Required Android, workflow, signing and documentation files are present.
- Application ID remains `com.james.mathwakealarm`.
- Version is `2.1.0`, version code `210`.
- TAZALARM app label and universal sneezing-cat source/logo are present.
- Production application source contains no location-specific **Kitchen** wording.
- Barcode alarm wording is present exactly as requested:
  - `Scan Barcode`
  - `Barcode must match your saved code`
  - `Open Scanner`
- Multiple-alarm onboarding and independent alarm/routine storage are present.
- Five main navigation areas are present: Home, Alarms, Routines, Progress and Settings.
- Questions, barcode scanning, live photo capture and 50-question penalty logic are present.
- One-minute night-to-day sunrise and app-window brightness ramp are present.
- Exact alarm → direct foreground service delivery is present.
- Separate ten-second backup alarm is present.
- The primary start cancels the backup.
- Partial wake lock, sticky service and active-alarm recovery are present.
- Alarm volume starts at 5% and rises every ten seconds.
- Reboot, clock, timezone and application-update rescheduling are present.
- Two-minute screen-off testing and reliability logging are present.
- Kotlin source delimiters balance across 17 source/test files.
- The included PKCS12 release keystore opens successfully and contains the `tazalarm` private-key alias.
- No generated build folders, IDE caches or Gradle caches are included.

## Automated tests supplied to GitHub Actions

- Generated maths answers validate correctly across 2,000 generated questions.
- Incorrect numeric answers are rejected.
- Text answers are case/punctuation tolerant.
- Same-day, next-day and skipped-occurrence scheduling cases.
- The default routine has Questions → Barcode → Questions → Photo.
- The default barcode title is generic and contains no Kitchen wording.

## Authoritative Android build boundary

The active packaging environment does not contain the complete Android SDK or cached Android/Compose dependencies. It therefore cannot produce or launch the APK locally.

The included GitHub Actions workflow performs the authoritative operations:

1. Installs Android API 36 and Build Tools 36.0.0.
2. Runs `:app:testReleaseUnitTest`.
3. Builds `:app:assembleRelease`.
4. Verifies the release APK signature with `apksigner`.
5. Uploads `TAZALARM-v2.1.0.apk` as a downloadable artifact.

## Real-device checks still required

- Exact-alarm and notification permission behaviour on the specific handset.
- Full-screen presentation while locked and idle.
- Manufacturer-specific battery restrictions.
- Camera and Google Play Services barcode-scanner availability.
- Photo-match tolerance under the actual room lighting.
- Alarm audio routing and volume on the selected device.
