# V-WATCHER — Single SOT (unified, dense, complete) — 2026-09-12 16:00 -03:00

> CURRENT AUTHORITY FOR IMPLEMENTATION STATE. Self-contained: an external analyst needs only this
> file + the repo + toolchain below. Supersedes all prior `PETSCAN_*`, `RELEASE_*`, and legacy
> validation reports where they conflict (§16). Frozen baseline — re-verify after any product change.
> Claim ceiling: L2 (Robolectric/JVM) proven; L3–L5 absent; nothing device-side is faked.

---

## 1. Product identity

- App: V-Watcher, foreground-only Android observer (Kotlin/Compose, single activity, 5 tabs).
- `applicationId com.aistudio.vwatcher.hkmv` — PROVISIONAL (human decision required pre-upload).
- `namespace com.example`, `compileSdk 36.1`, `targetSdk 36` (meets Play 2026-08-31 rule), `minSdk 24`.
- AGP 9.1.1, Kotlin 2.2.10, Gradle 9.3.1 (regenerated wrapper), JDK Temurin 21.0.12.1+1.
- `versionCode 1`, `versionName "1.0"`, no tags; git `main`, no commits.
- Evidence: `app/build.gradle.kts:13-24`, AAB manifest dump.

## 2. Build / release state (proven, not declared)

- `assembleDebug`, `bundleRelease`, `testDebugUnitTest` (**30/30, 0 failures**), `lintRelease` (0 errors, 49 info warnings) — all EXIT 0.
- AAB `app/build/outputs/bundle/release/app-release.aab`: 11,016,375 B,
  sha256 `28dd9bedfbd27c1a3ce08bd1986bf8853cdf83a695520e519d93b2000f6af72e`,
  `bundletool validate` 0, test-signed (CN=NOT FOR PLAY, key in /tmp, never in repo).
- AAB manifest: package + versions as §1; permissions exactly
  `ACCESS_NETWORK_STATE, QUERY_ALL_PACKAGES, PACKAGE_USAGE_STATS`
  (+ auto `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`); `INTERNET` removed (zero endpoints);
  `debuggable` absent (=false); backup rules wired; `UPLOAD.RSA` present; dex strings clean
  (no `AIza`/private keys/zero `GEMINI_API_KEY`).
- Toolchain (outside repo, `/media/juan/DATA/.toolchains`): Temurin JDK 21.0.12.1+1,
  cmdline-tools 11076708 (sha256 `2d2d5085…e258`), `platforms;android-36` (+AGP-auto 36.1),
  `build-tools;36.0.0`, platform-tools 37.0.1. Repro: `JAVA_HOME`, `GRADLE_USER_HOME`,
  `ANDROID_HOME` (or gitignored `local.properties:sdk.dir`), signing via
  `KEYSTORE_PATH/STORE_PASSWORD/KEY_PASSWORD` (alias `upload`).
- Levels: BUILD_READY YES; TECHNICALLY_RELEASE_READY YES; PLAY_POLICY_READY YES-technical;
  PLAY_CONSOLE_READY NO; PRODUCTION_READY NO (account, filings, listing, production signing — human).

## 3. Actual architecture (one paragraph of truth)

Foreground-only app, no background/network/persistence. Single telemetry ingress
(`AndroidSentinel.observeNow`, 6 providers, ~158–181 ms) feeding TWO coexisting decision paths:
(A) legacy VM cells (`ImmuneCells.kt`, `VWatcherViewModel.kt:83-152`) and (B) `BiomimeticImmuneSystem`
(`VWatcherViewModel.kt:348`) — they share the `cases` list and system incidents **clobber**
sim/manual cases on refresh (`VWatcherViewModel.kt:389`). Reasoning = router → deterministic
fallback (Nano/Gemma are honest stubs). Actuation = own-cache reclaim + in-memory flags + honest
refusals. Memory = gated RAM store. Homeostasis = computed labels, no actuation.

## 4. Runtime pipeline (L2 traces, rerun 2×, harness kept)

`app/src/test/java/com/example/petscan/PetScanTraceTest.kt` (3/3 green both runs):

```text
nominal: observeMs=181→158 macroState=DEGRADED/60 busTotal=2 rate=2.0 storm=false dropped=0 reasoningCalls=0
storm:   40 msgs / 99→117 ms → rate=40.0 storm=true dropped=0   (DETECTS, never CONTAINS)
router:  selected=GEMINI_NANO actual=DETERMINISTIC FALLBACK/GEMINI_NANO_UNAVAILABLE, 3–9 ms;
         socket rule → SUSPICIOUS/88 live
```

`DEGRADED/60` nominal = Robolectric shadow artifact that usefully proves fail-closed fires;
`reasoningCalls=0` proves the escalation gate idles (no evidence → no inference spend).

## 5. Component matrix (verdict per component; EFFECTIVE = observed effect in prod path)

- EFFECTIVE: Battery/Resources/Network/Inventory providers (+Usage when granted), Sentinel
  (fail-closed; continuous mode dead), PRR (3 rules), T-Helper (escalation gate), Dendritic (gated
  inference spender; uncaught throw aborts pipeline tail), Cytotoxic (3 gates), Neutrophil-respond,
  Macrophage-scavenge (usually 0/0), Regulatory-veto, Resolution-guard, Router, Guardrails
  (15%/<-charge, 92%/low-mem, 2.5–3 s cooldown, Mutex), Deterministic (5 rules), Baseline
  (dead 10% threshold; learned set unused), Boundary (1 real fs action + honest refusals).
- UNUSED (implemented, zero callers): NK cell, `attemptContainment`, `consolidateIncidents`,
  `DeviceTelemetryProvider.captureSnapshot`, `start/stopContinuousObserving`, `Regulatory.reset`.
- SKELETON: Nano/Gemma execution (detection honest, inference canned), `MemoryCell` (counter),
  channel flows (emit-only, zero collectors), `NAVIGATE_APP_SETTINGS` launch.
- DEAD CONTRACT: 10/12 bus payloads, `RESOURCE_GUARD_DENIED`, `FallbackReason.NONE`,
  `COLLECTION_FAILED`/`PROVIDER_STATE_CHANGED` events, homeostasis `RECOVERING` branch.

## 6. Integration map (transition verdicts)

PROVEN: telemetry→sentinel→PRR→bus→coordination→router→deterministic; cells→boundary;
resolution→memory; sim/manual→UI list. BROKEN: bus→cells (sole collector no-op; cells use direct
args), homeostasis→decisions, sim/exam→organism (bypasses repo gates), action→observation loop.
PARTIAL: boundary→OS (1 real, flags, 1 intent-not-sent). Dead: NK→anything, `forceDeterministic`/
cooldown outputs (produced, ignored), channel flows→anyone.
## 7. AI/model state (5-rung matrix, no inference)

| Backend | Configured | Available | Loaded | Executed | Result observed |
|---|---|---|---|---|---|
| Deterministic | YES | YES (always READY) | n/a | YES | YES (live) |
| Gemini Nano | YES (contract) | NO (no AICore) | NO | NO (canned NORMAL/92) | NO |
| Gemma | YES (contract) | NO (no weights; id pre-Gemma-4) | NO | NO (canned NORMAL/90) | NO |

Caveats: `isRealOnDeviceModel=true` on canned paths overclaims; Nano prompt string built then
dropped (`GeminiNanoBackend.kt:122`); Gemma runtime hook is a comment (`:78`).

## 8. Homeostasis (DISPLAY_AND_TRIAGE_LABEL — refined adversarially, NOT zero-effect, NOT control)

8 dims computed (stability counts hardcoded 0; `isAnomalous=false`; `isDeviceSecure=true`;
no hysteresis; `RECOVERING` unreachable since resolving⊆active). Consumed ONLY by
`VWatcherViewModel.kt:361-374` → DeviceCondition/doctor-note/barrier label. Zero gating of
guardrails, cells, router, bus. Guardrails/cells re-derive their own thresholds.

## 9. Actuation (per-action truth)

- `RECLAIM_INTERNAL_CACHE` EXECUTED, real (own `vwatcher_temp_*` + `System.gc()`), usually 0/0.
- `THROTTLE_INTERNAL_INFERENCE` / `ISOLATE_INTERNAL_SUBSYSTEM` / `RESET_SUBSYSTEM_ISOLATION`:
  in-memory flag/set only — and **write-only (G21)**: zero readers in main, so currently signals nothing.
- `NAVIGATE_APP_SETTINGS` reports EXECUTED but never calls `startActivity` (G12; UI screens do launch settings intents elsewhere).
- `KILL_EXTERNAL_PROCESS` UNAVAILABLE, honest sandbox refusal + alternative. Unknown verbs DENIED.

## 10. Memory (RAM-only, proven)

StateFlows + ConcurrentHashMaps; 3 compiled seeds; gates reject synthetic/`[SIM]`/conf<80%/
degraded-source/30 s debounce; no TTL/cap/delete; dies with process; `cryptographic` wording false.
Disk-IO grep empty; zero Room refs in main. Legacy reports claiming Room/SQLite (5 files, §16) are stale.

## 11. Telemetry taxonomy (per provider)

REAL_DEVICE: Battery (sticky intent; null→synthetic 100%/UNAVAILABLE), Resources system part
(ActivityManager; AM-null→JVM-heap fallback marked DEGRADED **but `isReal=true` — G14**),
Network state (offline correctly AVAILABLE/Disconnected; DPI honestly gated), Inventory
(full enumeration per observe; scoped-visibility subset risk: no permission-degraded branch),
Usage (24 h events + aggregates; denied→PERMISSION_REQUIRED **but `isReal=true` — G14**).
ANDROID_RUNTIME (static, not sensor): SystemState (`Build`/locale/TZ). SOFTWARE_SYNTHETIC:
Sentinel zero-fallbacks, legacy `reason(bundle,battery)` hardcoded observation, homeostasis
hardcoded dims. INTERNAL_STATE: bus rates, cell counts, incident map, heap bytes.

## 12. UI truth state (15 overclaims mapped; bind-or-reword, none fixed yet)

Proven honest: AICORE/DETERMINISTIC badge, Selected-vs-Actual fallback disclosure, provider
diagnostics, usage-access gating + real `startActivity` settings intents, `[SIMULATION]` prefixes,
sandbox notes (`VWatcherViewModel.kt:772,786`).
UNPROVEN/FALSE/STALE: (1) `REAL DEVICE DATA` banner ungated (shows on seed/degraded);
(2) null-default `HOMEOSTATIC`; (3) null-default ACTIVE sentinel; (4) hardcoded `Condition: Healthy`
exam dialog; (5) static exam categories Healthy; (6) `0 uncontained threats` literal;
(7) `ISOLATED||RESOLVED` counted as isolated; (8) per-app TLS/`Secured` (transport-level only);
(9) `Certified/Verified` apps (isSystemApp only); (10) `Signatures verified`/`Integrity intact`
(no signature check); (11) decoy pipeline (always Standby/0); (12) `MEM <35–40ms` literals;
(13) efficiency battery/CPU literals (only RAM real); (14) static 7-day history; (15) `RUN DEVICE
CHECK` copy (3 real numbers) + `Real device examination complete` overstatement.
## 13. Security / privacy (code-proven)

Minimal surface: 1 exported activity (launcher), no IPC/providers/deep-links/WebView/native/
network/logs/secrets; backup excludes explicit; local-only by construction (zero sinks,
AAB-confirmed); signing test-only. Exposure = `QUERY_ALL_PACKAGES` + usage-stats, both local-only.
Filings/policy/URLs/app-signing: external (human). Evidence: `docs/release/SECURITY_BASELINE.md`,
dex scan, `docs/release/PLAY_CONSOLE_ACTIONS.md`.

## 14. Tests (30/30; wiring gap declared)

Handoff 5 (substrates), Immune 10 (cells+pipeline), Adversarial 9 (stale/lowConf/storm/RAM/batt/
kill/time), Trace 3 (kept harness), misc 3. No mocks; synthetic-telemetry-by-copy; all L2.
UNTESTED: entire ViewModel (G1 — scoring/sim/quarantine/exam/memory-gates/UI defaults), UI bindings,
guardrail-through-VM. Greeting/Instrumented are smoke only.

## 15. Thresholds (inventory excerpt; full table `docs/sot/PETSCAN_THRESHOLD_MATRIX.md`; none endorsed)

42 °C thermal (PRR/Baseline/Deterministic/VM-score; Resolution uses ≤40 — conflict); 88% assess vs
92% deny memory; 200 vs 250 MB free; 200 MB own-heap; 15% battery; powersave+>50%; 25 vs 30 msg/s
storm; conf gates 85/80/80/80 (heuristic, uncalibrated); memory commit <80 reject + 30 s debounce;
cooldowns 15 s / 2.5–3 s; staleness 30 s; router timeout 5 s; score `97−15−12−1−6n−25−5`, clamp 55–100;
usage caps 20/15 over 24 h; bus replay 20 / buffer 128.

## 16. Contradictions resolved (stale docs neutralized)

 Superseded → HISTORICAL: `docs/evidence/historical/REAL_DEVICE_VALIDATION_REPORT.md`, `docs/evidence/historical/TRUTH_FIRST_VALIDATION_REPORT.md`
(banners added), `docs/evidence/validation/CLAIM_PROOF_MATRIX.json`, `docs/evidence/validation/MEMORY_VALIDATION.json`, `docs/evidence/validation/LIFECYCLE_VALIDATION.json`
(JSONs immutable-in-place, listed here). Refined: homeostasis DISPLAY_ONLY → DISPLAY_AND_TRIAGE_LABEL.
Retired method: `static_scan.sh` §1 (definition counts) superseded by targeted A/B greps.

## 17. Gaps (G1–G21; full register `docs/sot/V_WATCHER_GAP_DELTA.md`)

P0: G1 ViewModel untested; G2 homeostasis unconsumed; G3 dual pipelines + clobber.
P1: G4 NK; G5 resolution stall; G6 bus subscribers; G7 deterministic plumbing; G8 threshold sprawl;
G10 memory cap/TTL; G11 per-stage try/catch; G12 intent launch-or-relabel; G13 UI-15; G18 device lab;
G19 neural execution (external). P2: G9 dead code; G14 `isReal` split; G15 stale docs (done);
G16 channel flows; G17 stale mirror; G20/G21 flag semantics (rename or enforce).
External: app ID, policy URL + legal, filings, rating/audience, listing, upload key + App Signing,
12×14 if gated, pre-launch (`docs/release/PLAY_CONSOLE_ACTIONS.md`).

## 18. Verification (how to re-prove; ceiling restated)

```bash
export JAVA_HOME=/media/juan/DATA/.toolchains/jdk-21.0.12.1+1
export GRADLE_USER_HOME=/media/juan/DATA/.toolchains/gradle-home
export ANDROID_HOME=/media/juan/DATA/.toolchains/android-sdk
./gradlew testDebugUnitTest   # 30/30 expected (incl. 3 PETSCAN traces)
./gradlew lintRelease         # 0 errors expected
./tools/validation/petscan/static_scan.sh
```

Per-claim table: `docs/sot/V_WATCHER_EVIDENCE_INDEX.md`. Ceiling: L2 only; L3–L5, Nano/Gemma on
hardware, battery/Doze/perf: `NOT_AVAILABLE_IN_ENVIRONMENT` — never faked. No known blocker hidden;
externals labeled `REQUIRES_*`. Baseline frozen 2026-09-12 16:00 -03:00.

*END OF UNIFIED SOT — 18 sections, single file, complete.*
