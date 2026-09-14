# Release Remediation Report — V-Watcher (2026-09-12)

## Per-blocker ledger

| Original blocker | Action taken | Files changed | Tests run | Evidence | New status | Remaining limitation |
|---|---|---|---|---|---|---|
| No `gradlew`/SDK/JDK (P0) | Installed Temurin JDK 21.0.12.1+1, cmdline-tools 11076708, `platforms;android-36`(+36.1 auto), `build-tools;36.0.0`, platform-tools 37.0.1 under `/media/juan/DATA/.toolchains`; regenerated wrapper 9.3.1 via official dist | `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `local.properties` (gitignored) | — | `gradle --version`, `sdkmanager` list, `assembleDebug` EXIT 0 | RESOLVED (local) | Toolchain lives outside repo; CI must reproduce via documented recipe (`RELEASE_PACKAGE.md`) |
| No debug keystore | Generated standard debug keystore via `keytool` (gitignored, never committed) | `debug.keystore` (ignored) | — | `ls`, `.gitignore`, `git status` clean of it | RESOLVED | Dev-only identity, correct as such |
| No release artifact (P0) | `bundleRelease` with env-driven test key (alias `upload`, CN marked NOT FOR PLAY, key in /tmp) | none committed (env only) | — | AAB 11 MB, sha `28dd9bed…`, `bundletool validate` 0, manifest dump, dex secret scan | PIPELINE PROVEN | `REQUIRES_HUMAN_SIGNING` — Play upload needs real upload key + App Signing |
| Dead deps Room/Retrofit/Moshi/OkHttp/Firebase + dead plugins (P1) | Proved zero imports in main+test+androidTest; removed deps + ksp/secrets/google-services plugins + config blocks | `app/build.gradle.kts` | `assembleDebug` 0, `testDebugUnitTest` 27/0, `bundleRelease` 0, `lintRelease` 0 errors | grep evidence + build outputs | RESOLVED | Unused catalog entries remain in `libs.versions.toml` (inert, kept for deliberate re-add) |
| `INTERNET` without endpoint (P1) | Removed; documented in manifest comment + network inventory | `AndroidManifest.xml` | same build+tests | AAB manifest dump shows only 3 permissions | RESOLVED | Re-add only with a real endpoint (gate documented) |
| Empty backup rules (P2) | Explicit excludes (sharedpref/database/file/external) on both API paths, stateless posture documented | `backup_rules.xml`, `data_extraction_rules.xml` | manifest merge in builds above | AAB manifest references both rules | RESOLVED | None (revisit if persistence ever lands) |
| Secrets hygiene (P0) | Full-repo scan: only env-var refs + placeholder; dex scan clean | none | — | grep + dex strings | PASS | Git history starts now (`git init` 2026-09-12) → no historical exposure possible |
| No VCS (P1) | `git init`, branch `main`, verified ignores; no commit (no authorship convention) | `.git/` | — | `git status` | LOCAL_GIT_READY | `REMOTE_REQUIRES_OPERATOR`; no tags yet (tag at first release commit) |
| Provisional app ID (P1) | Full inventory: `applicationId` (gradle) + `namespace com.example` (50+ Kotlin files) + stale third ID in old JSON reports (not code); no providers/authorities/deep-links/Room/prefs to break | none | — | grep inventory | REQUIRES_HUMAN_DECISION | Rename is mechanical but identity is a product decision; must precede first upload |
| Privacy policy / Data Safety filing (P0/P1) | Technical draft from code behavior + Console action checklist; inventory updated post-strip | `PRIVACY_POLICY_DRAFT.md`, `PLAY_CONSOLE_ACTIONS.md`, `V_WATCHER_DATA_INVENTORY.md` | — | docs | DATA_SAFETY_TECHNICAL_BASELINE_READY | `HUMAN_LEGAL_REVIEW_REQUIRED`, `PUBLIC_URL_REQUIRED`, filings Console-side |
| Deceptive copy (P1) | Extended audit fix to all user-visible isolation strings (in-app-flag truth) | `VWatcherViewModel.kt` (strings only) | unit tests 27/0 | diff + tests | RESOLVED (code) | Mirror wording in future store copy |
| QUALITY/perf/a11y/device (P2) | `lintRelease` gate; device lab still absent | none | lint 0 errors / 49 info warnings | lint XML | PARTIAL | `PHYSICAL_RUNTIME_NOT_AVAILABLE`; pre-launch report pending AAB upload |

## Regression gate (§27)

`AUDIT → FIX → BUILD → TEST → STATIC AUDIT (lint+bundletool+dex) → RE-AUDIT`: all green on the same tree.
Behavior delta vs audit baseline: UI strings (no logic), one permission removed (unused), dead deps removed
(unused), backup XML content, new wrapper/keystore/property files. No product behavior changed — deterministic
engine, router, guardrails, boundary untouched.

## Scope discipline (§26)

No new cells, AI, UI, telemetry, cloud, analytics, or accounts added. One deliberate non-change: catalog leftovers
in `libs.versions.toml` (inert) to keep the diff reviewable.
