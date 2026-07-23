# Upload TAZALARM v2.1.0 to GitHub

Upload the **contents** of the extracted package, not an extra outer folder.

The top of the GitHub repository should show:

```text
.github
app
design-reference
gradle
.gitignore
build.gradle.kts
gradle.properties
personal-release.keystore
settings.gradle.kts
README.md
SIGNING_COMPATIBILITY.md
VALIDATION.md
```

## Steps

1. Open the GitHub repository.
2. Change it to **Private** before uploading the signing key.
3. Delete the old nested `MathWakeAlarm_online_build` folder if it remains.
4. Choose **Add file → Upload files**.
5. Open the extracted TAZALARM folder on the computer.
6. Select everything inside it, including `.github`, and drag it onto GitHub.
7. Commit directly to `main`.
8. Open **Actions**.
9. Choose **Build TAZALARM APK**.
10. Open the completed green run and download the APK artifact.

In Windows File Explorer, use **View → Show → Hidden items** if `.github` is not visible.
