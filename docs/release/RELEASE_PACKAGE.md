# Release Package — V-Watcher (built 2026-09-12, local remediation)

```text
applicationId:      com.aistudio.vwatcher.hkmv  (PROVISIONAL — human decision required before first upload)
versionName:        1.0
versionCode:        1
targetSdk:          36  (meets Play 2026-08-31 requirement)
minSdk:             24
compileSdk:         36 minor 1 (platform android-36.1, auto-provisioned by AGP)
AAB path:           app/build/outputs/bundle/release/app-release.aab
AAB size:           11,016,375 bytes
AAB SHA-256:        28dd9bedfbd27c1a3ce08bd1986bf8853cdf83a695520e519d93b2000f6af72e
build timestamp:    2026-09-12 15:23 -03:00
release signing:    LOCAL TEST KEY ONLY (alias `upload`, CN=V-Watcher LOCAL TEST ONLY NOT FOR PLAY,
                    key in /tmp/opencode, never in repo) → REQUIRES_HUMAN_SIGNING for Play App Signing
privacy policy:     DRAFT only (docs/release/PRIVACY_POLICY_DRAFT.md) → PUBLIC_URL_REQUIRED
data safety:        TECHNICAL BASELINE READY (docs/release/V_WATCHER_DATA_INVENTORY.md) → submission REQUIRED
permissions decl:   REQUIRED, unfiled (QUERY_ALL_PACKAGES + usage-stats disclosure)
content rating:     NOT FILED (Console)
testing req:        UNKNOWN until account type determined (12×14 iff new personal account)
Play listing:       NOT STARTED (icon exists; descriptions/graphics/screenshots/contact missing)
known limitations:  provisional app ID; no git history/tags; behavior audit on Android 16 owed;
                    quality/perf/a11y metrics unmeasured; pre-launch report pending upload
```

## Artifact verification (evidence, bundletool 1.18.1)

- `bundletool validate` → EXIT 0.
- Manifest dump: package + versions as above; permissions in artifact exactly
  `ACCESS_NETWORK_STATE, QUERY_ALL_PACKAGES, PACKAGE_USAGE_STATS` (+ auto `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`).
  INTERNET removal confirmed in artifact.
- `android:debuggable` absent on `<application>` (= false). Backup rules wired
  (`dataExtractionRules` + `fullBackupContent`). Signature present (`META-INF/UPLOAD.RSA`).
- Dex strings scan: no `AIza*`, no `PRIVATE KEY`, zero `GEMINI_API_KEY` references.
- Quality gates on same tree: `testDebugUnitTest` 27/27 pass; `lintRelease` 0 errors (49 info warnings:
  newer-dep notices, 7 unused resources, icon density, locale — none blocking).

## Reproducible toolchain (all outside repo, `/media/juan/DATA/.toolchains`)

- JDK Temurin 21.0.12.1+1 (adoptium GitHub release asset).
- cmdline-tools 11076708 (sha256 `2d2d5085…e258`), packages: `platforms;android-36` (r02, + AGP-auto 36.1),
  `build-tools;36.0.0`, `platform-tools` 37.0.1 (AGP-auto).
- Gradle 9.3.1 via regenerated `gradlew` (pinned `distributionUrl`, `validateDistributionUrl=true`).
- Env: `JAVA_HOME=<jdk>`, `GRADLE_USER_HOME=<dir>/gradle-home` (keeps 2 GB+ off rootfs),
  `ANDROID_HOME` or `local.properties:sdk.dir`, plus `KEYSTORE_PATH/STORE_PASSWORD/KEY_PASSWORD` for signing.
