# Signing compatibility

Android only accepts an APK as an update when these both match the installed app:

1. Application ID / package name.
2. Signing certificate.

TAZALARM v2.1.0 preserves the previous package name:

```text
com.james.mathwakealarm
```

However, the old MathWake Alarm v2.0.1 keystore was not available in the active build environment. A new personal TAZALARM key was therefore generated so GitHub can produce a signed APK.

## Using the included key

The generated APK is suitable for a fresh installation. Keep `personal-release.keystore` permanently; every later TAZALARM build must use it to update this installation.

Included signing values:

```text
store file: personal-release.keystore
alias: tazalarm
store password: TazAlarmPersonal2026
key password: TazAlarmPersonal2026
```

The repository must remain Private while it contains this file.

## Updating the already-installed v2.0.1 app

Retrieve `personal-release.keystore` from the private GitHub repository or extracted v2.0.1 source package. Replace the new keystore in this project with that original file. Then open `app/build.gradle.kts` and change:

```kotlin
storePassword = "..."
keyAlias = "..."
keyPassword = "..."
```

to the original v2.0.1 values.

Do not uninstall the existing app until deciding which signing lineage to retain. Uninstalling removes the app's local alarm settings unless Android restores a backup.
