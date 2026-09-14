# Release Build — V-Watcher (remediated 2026-09-12)

## Current state: PIPELINE PROVEN (local test signing; Play signing remains operator-side)

- `./gradlew bundleRelease` → **BUILD SUCCESSFUL**, AAB `app/build/outputs/bundle/release/app-release.aab`
  (11,016,375 B, sha256 `28dd9bed…f72e`), `bundletool validate` EXIT 0. Full evidence: `docs/release/RELEASE_PACKAGE.md`.
- Toolchain (all in `/media/juan/DATA/.toolchains`, outside repo): Temurin JDK 21.0.12.1+1,
  cmdline-tools 11076708, `platforms;android-36` (+ AGP-auto 36.1), `build-tools;36.0.0`,
  platform-tools 37.0.1, Gradle 9.3.1 via regenerated wrapper. Env: `JAVA_HOME`, `GRADLE_USER_HOME`,
  `ANDROID_HOME` (or `local.properties:sdk.dir`, gitignored).
- `./gradlew testDebugUnitTest` → 27/27 pass. `./gradlew lintRelease` → 0 errors (49 info warnings).
- Release signing: env-driven (`KEYSTORE_PATH/STORE_PASSWORD/KEY_PASSWORD`, alias `upload`); local validation
  used a clearly-labeled NOT-FOR-PLAY test key in /tmp. Real upload key + Play App Signing = operator ceremony.

## Build config audit (`app/build.gradle.kts`)

- `applicationId = "com.aistudio.vwatcher.hkmv"` → **PROVISIONAL_IDENTITY_REQUIRES_REVIEW** (AI-Studio-style namespace; stable-before-first-upload decision needed; changing later breaks Play identity/persistence).
- `versionCode = 1`, `versionName = "1.0"` — no git history (not a git repo) → monotonicity unverifiable; adopt tags before first upload.
- Release type: `isMinifyEnabled = false`, `isCrunchPngs = false`, default ProGuard file + empty `proguard-rules.pro` → **P2**: enable R8/shrinking + verify (dead-dep stripping shrinks this further).
- `debug` type signs with `debugConfig` → file `${rootDir}/debug.keystore` **absent** → even debug build currently fails. Fix: `generate` via `keytool` locally (debug only, gitignored) or let Android Studio create it.
- `release` type: env-driven upload key (`KEYSTORE_PATH/STORE_PASSWORD/KEY_PASSWORD`, alias `upload`) — no secrets in repo (good) → status `RELEASE_REQUIRES_OPERATOR_SIGNING` until Play App Signing + upload key are provisioned in Console.
- `googleServices.missing.passthrough = WARN` + absent `google-services.json` → build tolerates missing Firebase today; decide: wire Firebase (add file, secrethandling) or drop the plugins/deps (recommended until cloud AI is real).
- Secrets plugin maps `.env` → BuildConfig; only `GEMINI_API_KEY` placeholder exists and stays commented out (not packaged) — verify on artifact with `apkanalyzer`/strings before upload.
- `dependenciesInfo.includeInApk = false / includeInBundle = true` — correct for Play.

## Release recipe (when toolchain exists)

1. Provision SDK (`build-tools`, `platforms;android-36`), restore `gradlew` wrapper, `debug.keystore` (dev only).
2. Decide Firebase: remove dead cloud deps OR add `google-services.json` (gitignored) + Data Safety rows.
3. `./gradlew bundleRelease`, verify `debuggable=false`, no secrets in bundle, version stamps, upload to Play Console internal track → pre-launch report.
