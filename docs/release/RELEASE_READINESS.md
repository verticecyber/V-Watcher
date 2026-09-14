# Release Readiness — V-Watcher (2026-09-12)

Scope: code + artifact + config ready for **submission** (not store listing, not Console process — see `PLAY_STORE_READINESS.md`).

## API level (§3–§4): NO MIGRATION NEEDED

- Current: `compileSdk 36 (minor 1)`, `targetSdk 36`, `minSdk 24` (`app/build.gradle.kts:12-24`). Google Play from 2026-08-31 requires **target API 36+** for new apps/updates (`support.google.com/googleplay/android-developer/answer/11926878`). `CURRENT == GOOGLE_REQUIRED` → GAP none. Prior API-34 generation already migrated; behavior-change audit for 34→36 deltas (edge-to-edge, background restrictions, photo picker, etc.) still owed on a real device (P2).

## Quality / stability / performance / accessibility / adaptive (§21–§25)

- Architecture is foreground-only Compose (single activity, `enableEdgeToEdge`, lifecycle-aware collectors) — no ANR-prone background work exists; no StrictMode harness, no startup/frame metrics, no pre-launch report → all NOT_VERIFIED (P2, device-gated).
- Accessibility partial: nav icons carry `contentDescription` (`MainActivity.kt:96-179`); touch targets/contrast/font-scaling/screen-reader pass NOT_VERIFIED.
- Adaptive: phone-first, no tablet/foldable resources, no `resizeableActivity` flags (platform default applies) → NOT_TARGETED/UNKNOWN; portrait/landscape smoke owed.
- Stability scenarios (rotation, process death, Doze, low memory, network loss): unrun — process death wipes all state by design (no persistence), which is honest but must be UX-accepted.
- Doze/standby (§26): nothing to exempt — no services/WorkManager/alarms; "continuous watcher" narrative is **PLATFORM_CONSTRAINT**: continuous background monitoring is not implemented and Android would not grant it silently. Store copy must not promise it.

## Background disclosure (§15)

No background execution, no background collection/transmission exists. Transparency duty = saying so (foreground-only observer) + keeping the two sensitive-permission disclosures prominent. No hidden monitoring to disclose.

## Backup / deletion (§27–§28)

No accounts, no persisted user data → account-deletion N/A. `allowBackup=true` + empty rules = P2 (define excludes even for a near-empty dataset). Deletion = clear data / uninstall; document in policy.

## Ratings / access (§29, §31)

No login → reviewer path trivial (open app). Content-rating questionnaire + target-audience + ads-declaration are Console-side (see store doc).

## Fixes applied this audit (safe/reversible only)

1. `VWatcherViewModel.isolateApp/releaseApp` copy → in-app-only truthful wording (removed "background activity suspended / conduit suspended / returned to normal operation" overclaims) — P1 deception-risk closed at code level.
2. `.gitignore` extended (`*.apk/*.aab/*.jks/*.keystore`, `google-services.json`, `keystore.properties`) — hygiene, no product impact.
3. Everything structural (dep stripping, appId, backup rules, minify) → planned, not forced (see matrix; no toolchain to verify builds).
