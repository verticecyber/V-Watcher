# V-WATCHER SOT — Single Source of Truth, implementation state (2026-09-12 15:40 -03:00)

> CURRENT AUTHORITY FOR IMPLEMENTATION STATE. Older docs that contradict this file are
> HISTORICAL (§16). No "vamos fazer" below — only what exists now (`Claim ceiling`, §19).

## 1. Product identity

`applicationId com.aistudio.vwatcher.hkmv` (PROVISIONAL, human decision required); `namespace com.example`;
`vCode 1 / vName 1.0`; no tags; git `main`, no commits. Evidence: `app/build.gradle.kts:13-24`, AAB dump.

## 2. Current build state

`assembleDebug` / `bundleRelease` / `testDebugUnitTest` (30/30) / `lintRelease` (0 errors) EXIT 0.
AAB 11,016,375 B sha256 `28dd9bed…f72e`, `bundletool validate` 0, test-signed (NOT FOR PLAY).
Toolchain `/media/juan/DATA/.toolchains` (Temurin 21.0.12.1+1, platform 36+36.1, build-tools 36.0.0).
Evidence: `docs/release/RELEASE_PACKAGE.md`, test XML, lint XML.

## 3. Runtime environment

L2 verified (Robolectric). L3–L5 absent (no device/AVD). Evidence: trace reruns (2×, §18).

## 4. Actual architecture

Foreground-only Compose app. Two coexisting decision paths (G3): legacy VM cells
(`ImmuneCells.kt`, VM:83-152) and `BiomimeticImmuneSystem.processObservation` (VM:348).
Single telemetry ingress `sentinel.observeNow()`. No background, no network, no persistence.
Evidence: `PETSCAN_RECONCILIATION.md` (clobber semantics VM:389).

## 5. Actual runtime pipeline

`observeNow` (~158–181 ms) → PRR → bus (2 dispatches) → T-helper gate → (dendritic→router only if
evidence+escalation) → deterministic fallback → homeostasis label → UI. Idle observations spend zero
inference (`reasoningCalls=0`). Evidence: trace runs 1+2.

## 6. Component matrix

EFFECTIVE: 5 telemetry providers, Sentinel, PRR, T-helper, Dendritic (gated), Cytotoxic (gated),
Neutrophil-respond, Macrophage-scavenge, Regulatory-veto, Resolution-guard, Router, Guardrails,
Deterministic, Boundary (with G12/G21 caveats), Baseline (with dead threshold).
UNUSED: NK, `attemptContainment`, `consolidateIncidents`, `DeviceTelemetryProvider`,
`start/stopContinuousObserving`. SKELETON: Nano/Gemma execution, `MemoryCell`, channel flows.
Evidence: `PETSCAN_COMPONENT_MATRIX.md`, A1–A8/B1–B7 greps.

## 7. Integration map

Proven links: telemetry→sentinel→PRR→bus→coordination→router→deterministic; cells→boundary;
resolution→memory. Broken: bus→cells (no subscriber), homeostasis→decisions, sim→organism,
action→observation loop. Storm DETECTS (40/99–117 ms, flag) never CONTAINS.
Evidence: `PETSCAN_INTEGRATION_MAP.md`, traces.

## 8. AI/model state

| Backend | Configured | Available | Loaded | Executed | Result observed |
|---|---|---|---|---|---|
| Deterministic | YES | YES (always READY) | n/a (rules) | YES | YES (SUSPICIOUS/88 live) |
| Gemini Nano | YES (contract) | NO (no AICore here) | NO | NO (canned Assessment) | NO (hardcoded NORMAL/92) |
| Gemma | YES (contract) | NO (no weights; id pre-Gemma-4) | NO | NO (canned) | NO (hardcoded NORMAL/90) |

`isRealOnDeviceModel=true` on canned paths overclaims (G14-adjacent). Evidence: backend files, trace router line.

## 9. Homeostasis state

Computes 8 dims (2 hardcoded-zero: stability counts; 2 hardcoded-false/true: `isAnomalous=false`,
`isDeviceSecure=true`); `RECOVERING` unreachable; no hysteresis. Status: **DISPLAY_AND_TRIAGE_LABEL**
(condition/note/barrier label via VM:361-374) — NOT causal control. Evidence: A2, B2.

## 10. Actuation state

Real: own-cache reclaim (usually 0/0), in-memory flags/sets. Refused honestly: external kill
(UNAVAILABLE). Gap: settings Intent built-not-sent (reports EXECUTED — G12). Flags write-only,
zero readers (G21). Unknown verbs DENIED. Evidence: boundary file, A5/B7.

## 11. Memory state

RAM-only StateFlows + maps; 3 seeds; gates (synthetic/<80%/degraded-source/30 s debounce);
no TTL/cap/delete; dies with process; `cryptographic` wording false. Evidence: A6, memory file.

## 12. UI truth state

Backed: Nano/Deterministic badge, fallback disclosure, provider diagnostics, usage-access gating,
`[SIMULATION]` prefixes, sandbox notes (VM:772/786). Unproven/overclaimed (15, bind-or-reword):
`REAL DEVICE DATA` ungated banner, null-default HOMEOSTATIC/ACTIVE/Healthy, hardcoded
exam/0-threats/history, per-app TLS/certified/signatures, decoy pipeline, `<40ms`/efficiency literals.
Evidence: probe 4 §2, SOT does not re-litigate strings (see GAP_DELTA).

## 13. Security/privacy state

Minimal surface (1 exported activity), no IPC/net/native/WebView/logs/secrets; backup excludes;
local-only by construction (zero sinks, AAB-confirmed). Filings/policy/URLs external.
Evidence: `SECURITY_BASELINE.md`, dex scan, `PLAY_CONSOLE_ACTIONS.md`.

## 14. Test coverage

30/30 (Handoff 5 substrates, Immune 10, Adversarial 9, Trace 3, misc 3). Untested: entire ViewModel
(G1), memory gates direct, UI bindings, guardrail-through-VM. No mocks; synthetic-telemetry-by-copy
in tests; all L2. Evidence: `PETSCAN_EVIDENCE_INDEX.md` (prior), B-plumbing above.

## 15. Known limitations

L3–L5 absent; perf/battery unmeasured; no CI/tags; thresholds uncalibrated (full table
`PETSCAN_THRESHOLD_MATRIX.md`); confidences heuristic; rotation resets UI-local state; death wipes all.

## 16. Known contradictions resolved

Stale Room/SQLite claims in `REAL_DEVICE_VALIDATION_REPORT.md`, `TRUTH_FIRST_VALIDATION_REPORT.md`,
`CLAIM_PROOF_MATRIX.json`, `MEMORY_VALIDATION.json`, `LIFECYCLE_VALIDATION.json` → HISTORICAL,
superseded by this SOT (code: zero Room refs/disk IO). Banner added to the two MD files; JSONs
immutable-in-place, listed here instead. Prior PETSCAN refinement: homeostasis DISPLAY_ONLY →
DISPLAY_AND_TRIAGE_LABEL; static_scan §1 retired as orphan evidence.

## 17. Gaps

G1–G20 (`PETSCAN_GAPS.md`) + **G21 write-only boundary flags** (B7: `isInferenceThrottled`,
`isSubsystemIsolated` produced, never read — throttle/isolate currently signal nothing downstream).

## 18. Evidence index (pointer)

Full per-claim index: `docs/sot/V_WATCHER_EVIDENCE_INDEX.md`. Trace runs: 2026-09-12 ~15:35 (181 ms)
and ~15:50 (158 ms), 3/3 green both. Static: `tools/petscan/`. Adversarial batteries: A1–A8, B1–B7
in `docs/sot/PETSCAN_RECONCILIATION.md`.

## 19. Claim ceiling

L2-verified claims: pipeline, fallback, gates, refusals, RAM-only, zero-sink. L5-only: Nano/Gemma
execution, 24/7, autonomy, perf. UI adjectives without backend fields: UNPROVEN until bound.

## 20. Last verified

2026-09-12 15:50 -03:00. Next re-verification required after any product change (SOT frozen as baseline).
