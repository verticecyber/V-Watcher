# V-WATCHER — Physical-Scope / Out-of-Scope Audit (2026-09-12, read-only)

> Self-contained consolidated report. ZERO product modification in this phase
> (verified: only these two docs were created). Authority order applied throughout:
> runtime > tests > reachable code > docs > plan. Contradictions recorded, not harmonized.

## 1. Executive State

The organism is a foreground-only deterministic observer with real telemetry, an honest
fallback router, label-grade homeostasis, flag-grade actuation, RAM-only memory, and a UI that
is now ~80% bound to backend truth with disclosed residuals. All execution paths that CAN run
on this Linux host are proven (suite 63/63 L2, lint 0 errors, bundle validators green,
PH-01..PH-05 canonically closed). Everything requiring hardware, weights, time, accounts, or
human authority is explicitly NOT proven and itemized below. The single most load-bearing
discovery of this audit: **the guardrail feed (PH-03's flagship) cannot deny anything in the
production deterministic path** — the router consults guardrails only when a neural backend is
READY, which never happens without AICore/weights. The effective production deny is
Regulatory→T-Helper suppression (same-tick, backend-independent). Homeostasis therefore
upgraded only from DISPLAY_AND_LABEL to **CAUSAL_CONTROL (deny-only)** with that exact boundary.

## 2. Current Build

Same validated artifact as release remediation: AAB 11,016,375 B, sha256
`28dd9bed…f72e`, `bundletool validate` 0, test-signed NOT FOR PLAY, 3-permission manifest
(`ACCESS_NETWORK_STATE`, `QUERY_ALL_PACKAGES`, `PACKAGE_USAGE_STATS`), `debuggable` absent,
dex secret scan clean. Toolchain `/media/juan/DATA/.toolchains` (Temurin 21.0.12.1+1,
platform 36+36.1, build-tools 36.0.0). AGP 9.1.1, Kotlin 2.2.10, Gradle 9.3.1, targetSdk 36.
Suite 63/63, lintDebug 0 errors / 49 warnings. `adb devices -l` EMPTY, no AVD directory:
NO_DEVICE. Git: initialized, branch main, zero commits (still).

## 3. Current Architecture

Foreground-only Compose app. ONE decision path post-fusion (legacy DecisionCell entry retired,
`DecisionCell` class + legacy `reason(bundle,battery)` overload + synthetic builder + 7 dead
imports swept). `BiomimeticImmuneSystem.processObservation` canonical; VM maps its snapshot to
UI state, merges cases per-id, feeds guardrail context. No background, no network endpoints
(`INTERNET` removed, AAB-confirmed), no persistence, no accounts. Dual-pipeline debt CLOSED;
threshold sprawl, NK, resolution stall, memory bounds, dead contract remainder OPEN (below).

## 4. Current Runtime Pipeline (L2-observed, trace harness rerun post-fusion)

`observeNow` ~137–181 ms → PRR → bus (2 dispatches) → T-helper gate → dendritic (if escalated)
→ router → deterministic → homeostasis label → UI. Storm probe: 40 msgs / 84–117 ms → rate 40,
storm=true, dropped=0 (detects, never contains). Router probe: GEMINI_NANO → DETERMINISTIC,
FALLBACK/UNAVAILABLE, 3–9 ms, socket rule SUSPICIOUS/88 live. Post-fusion trace matches
pre-fusion baselines structurally (chain shape, states, counts; timings overlap).

## 5. Components (EXISTS / CALLED / CONSUMED / EFFECTIVE / TESTED / INTEGRATED)

- Telemetry 6 providers: EXISTS, CALLED (every refresh), CONSUMED, EFFECTIVE (5 real + 1 static),
  TESTED (L2), INTEGRATED. Caveats: Usage denied + Resource-degraded paths now honestly
  `isReal=false` (PH-02); scoped-visibility subset risk remains (no permission-degraded branch).
- Sentinel: EXISTS/CALLED/CONSUMED/EFFECTIVE/TESTED/INTEGRATED. Fail-closed proven live.
  `startContinuousObserving` DORMANT (zero callers).
- PRR/Regulatory/T-Helper/B/Dendritic/Cytotoxic/Neutrophil-respond/Macrophage-scavenge/Resolution:
  all EXISTS/CALLED/CONSUMED/EFFECTIVE/TESTED/INTEGRATED (adversarial-covered).
- NK: EXISTS, never CALLED, counter always 0, decision unconsumed except display-only homeostasis
  branch that can never fire. TESTED only as unit. Verdict: DELETE (or keep static row knowingly).
- Deterministic backend: fully EFFECTIVE/TESTED. Nano/Gemma: detection EXISTS, inference SKELETON
  (canned NORMAL/92 and NORMAL/90, `isRealOnDeviceModel=true` overclaims), never EXECUTED here.
- Boundary: 1 real action (own-cache reclaim, usually 0/0) + in-memory flags (now thread-safe) +
  honest refusals + settings intent now LAUNCHED (PH-02). KILL_EXTERNAL honest UNAVAILABLE.
- ViewModel 1218 lines: god object retained, now single-path; legacy display cells removed with the
  fusion. TESTED (7 harness tests). No rotation persistence (`remember` only), death wipes all.

## 6. Integration

PROVEN: telemetry→sentinel→PRR→bus→coordination→router→deterministic; cells→boundary;
resolution→memory; VM→guardrail feed (values verified mirrored). BROKEN-BY-DESIGN (documented):
bus→cells has exactly one functional subscriber (evidence ledger); channel flows
(`telemetryEvents`, `reasoningResponses`), `sentinel.events`, `immune.snapshot`,
`memory.cases` remain EMIT_ONLY with zero main collectors; 12/14 bus payloads dead;
`isWarm`, backend `invocationCount`, `getProvenance`, `Regulatory.reset`, `ImmuneBus.clear`,
`memory.cases` reads all unwired.

## 7. Homeostasis

CAUSAL_CONTROL (deny-only) + triage labels — upgraded from DISPLAY_AND_LABEL by the verified
VM→guardrail feed (`VWatcherViewModel.kt:335-343` → `ResourceGuardrails.kt:84-116`, deny reasons
`RESOURCE_GUARD_DENIED`/`RATE_LIMIT_COOLDOWN`, battery/memory keep explanation priority).
BOUNDARIES (adversarially established, §21-tested by reasoning, not by device):
(a) guardrails consulted ONLY when a neural backend is READY (`ReasoningRouter.kt:44,64`);
deterministic-only production never consults → feed is defense-in-depth for neural-enabled
devices, NOT the prod gate; (b) one-cycle lag (feed applied after the observation's own
inference); (c) no hysteresis anywhere (only regulatory 15/30/60/20 s + memory 30 s debounce);
(d) `forceDeterministic` is regulatory-derived, independent of homeostasis (no shared import);
(e) oscillation at thresholds (15%, 92%/250 MB, 200 MB heap, 25 msg/s) damped only by deny-only
direction. The prod-effective deny remains Regulatory→T-Helper same-tick suppression.

## 8. Immune Cells

11 wired + NK dead. Per-cell truth (purpose→effect) re-verified unchanged except: Dendritic throw
still aborts pipeline tail (UNSAFE_ABORT, G11 open); Neutrophil/Macrophage dead siblings
(`attemptContainment`, `consolidateIncidents`) still present; B-cell live hit-rate ~0 (substring
vs PRR strings); Cytotoxic triple-gate effective; Resolution gating→stuck (see §10).

## 9. Bus

Transport LIVE but narrow: 2/14 payload types produced, 1 functional subscriber (ledger, bounded
8, prunes oldest). Storm flag-only (no containment, no shed, no backpressure accounting that ever
triggers — `droppedMessagesCount` structurally ~0). `bus.stats` polled, never collected. Scope
owns the 25-vs-30 tier conflict.

## 10. Resolution

RESOLUTION_GATING → STUCK (proven boolean trace): new incidents are born CONTAINED; the only
resolve path requires clean+normal observations AND the cumulative `totalBytesReclaimed>0` latch;
healthy devices never reclaim (0/0 EXECUTED) so the latch never sets → indefinite CONTAINED →
indefinite ACTIVE_DEFENSE homeostasis. Compounded by: global-current-evidence blocking all old
incidents (ledger is per-observation, incidents lack observation linkage); 40-vs-42°C thermal
gap (detectable-but-unresolvable band); Rule2 forcing CONTAINED even from RECOVERING. No timeout,
no per-incident proof, no duplicate protection beyond map keys.

## 11. Memory

RAM-only proven (zero disk IO in package). NO capacity, NO TTL, NO eviction, NO deletion path;
dedupe by patternCode + 5 write gates + 30 s debounce only. Worst case: +1 incident/+1 case/+1
pattern per escalated observation, unbounded over process lifetime; `MEM-<millis>` codes make
debounce bypassable across incidents; `lastWriteTimes`/`provenances` leak monotonically. Seeds
(3, hardcoded) are bootstrap+test fixtures with synthetic-but-honest provenance. Death wipes all;
`resolveCase` flips status, retains rows; map never removes (RESOLVED re-looped and counted).

## 12. Thresholds (unified; S=single, D=duplicated, C=conflicting, M=magic, T=tested)

- Battery 15% ×3 owners (guardrails:39, homeostasis:127, regulatory:71): D, T. Dead 10% baseline: DEAD.
- Thermal 42.0 ×5 sites (baseline:9, PRR:35, deterministic:53, VM score:151, VM vital:196): D, untested;
  resolution demands ≤40.0 (:60): C (2°C unresolvable band).
- Memory 92% guardrails:66 + homeostasis:128: D, T; vs 88% baseline:11 + deterministic:54: C (4pp gap).
- MB 250 homeostasis:128 + orchestrator:234 + resolution:60: D; vs 200 PRR:49 + regulatory:85: C (50 MB gap).
- Heap self 200MB:129: S, untested. Storm 30/s bus:34 vs 25/s homeostasis:129: C. Cooldowns:
  guardrail 3000 default vs 2500 prod override: C (tests pin the wrong value); regulatory
  15 s/30 s/60 s/20 s tiers: S each, mostly tested; staleness 30 s: S untested; debounce 30 s: S
  untested; model timeout 5 s: S untested; continuous 10 s: DORMANT. Confidence 80 double-gated
  (orchestrator:202 + cytotoxic:58): D consistent, tested; 85 T-helper: S tested; response-cell 75:
  orphan (class dead); assessment literals (75–99): M cluster, display-only. Buffers: bus 128+replay
  20, channel replay1+64 ×3 (triplicated literal), rings takeLast(20) ×2 (one tested? no—write-only),
  ledger 8 (tested bound), usage 15/20 + 24 h window (untested). Score arithmetic 97−15−12−1−6n−25−5
  clamp 55–100 (VM:149-161): M cluster, weights untested. Exam label ≥90: consistent VM↔binding,
  tested; sim ad-hoc 82/91/93/96. UI delays (800×5+700 exam, sim choreography): M behavioral.
- Top conflicts to resolve (no values chosen here): 2500/3000 shadow, 88/92, 200/250, 40/42, 25/30,
  triple-15 without const, triple-30s value collision across distinct semantics.

## 13. Failure Containment (per stage)

EXPECTED_FAIL_CLOSED: per-provider sentinel catch, router all-paths fallback, backend error
results, boundary refusals, regulatory veto, resolution validation-stall. UNSAFE_ABORT (no
try/catch, stale snapshot, tail suppressed, frozen UI, silent): PRR throw, bus dispatch throw,
T-helper throw, **Dendritic exception path** (kills cytotoxic/incident/macrophage/resolution/
homeostasis/snapshot/feed for that tick), resolution-loop throw, memory IO throw, outer
sentinel-emit throw, any ViewModel refresh throw (whole UI update skipped, only next refresh
recovers). No stage has rollback; snapshot is single-slot (`_snapshot.value=` once per cycle).

## 14. UI Truth (FIXED / PARTIAL / UNCHANGED / NEW)

FIXED (bound or reworded with tests): banner gating, sentinel IDLE default, exam-condition label,
live exam categories, isolated/uncontained accounting, quarantine button + sandbox notes,
transport/permissions/system descriptors, decoy headers, latency/efficiency honesty, history
disclosure, device-check copy, local-only design-property wording, confidence "—",
homeostasis pending reason. PARTIALLY_FIXED (residuals): ImmuneSystemScreen null-macro defaults
were fixed BUT **DiagnosticScreen:435 still renders null→HOMEOSTATIC green** with "Equilibrium
maintained…" + "Nominal surveillance" fallbacks (NEW finding this audit — tripwire missed it:
literal differs); efficiency `EfficiencyMetrics()` defaults (`<0.4%/day`, `84 MB`, `0.8% avg`)
still render one pre-refresh frame; decoy/case seed bodies still imply past diversion capability;
MEM-0019 "instantly" wording; `VWatcherModels.protocol="TLS 1.3"` default (production overrides);
"Continuous On-Device Stream" puff; exam producer always-Healthy (91/97); hardcoded
zero-anomaly summaries in `DeviceState`/`UiState` defaults. UNCHANGED (accepted): sim-copy
honesty prefixes. No NEW false claims introduced by phases (tripwire green on 30 literals).

## 15. Gemini Nano (CODE_PATH_EXISTS only)

Package-presence probe (`PackageManager com.google.android.aicore`) + enabled check; canned
NORMAL/92 on READY; zero ML Kit imports; no `checkStatus/download/warmup/generateContent/quota`
integration. Missing, in order: ① device with AICore, ② `com.google.mlkit:genai-prompt`
dependency, ③ `Generation.getClient().checkStatus()` gate replacing the package probe,
④ download-flow UI, ⑤ `warmup()`, ⑥ `generateContent` wiring with token/quota handling,
⑦ per-app quota observability. No bootloader-unlocked-device handling. NOT_AVAILABLE_IN_ENVIRONMENT.

## 16. Gemma (MODEL_REFERENCE only)

ID `gemma-2b-it-cpu.bin` (pre-Gemma-4, two generations stale) probed in `filesDir[/models]`;
no weights in repo; no LiteRT/MediaPipe/TFLite dependency; init is a flag flip with an honest
please-wire comment; canned NORMAL/90. Missing: ① Gemma 4 mobile artifact decision
(E2B vs E4B per device RAM), ② `.litertlm`/`mobile-ct` weights + license acceptance,
③ `litertlm-android` runtime + `Engine.initialize()` lifecycle, ④ memory/concurrency budget
proof, ⑤ tool-calling policy. NOT_READY. Direction stands (skill docs current).

## 17. Physical Device Readiness (per-item classification)

DEVICE_REQUIRED: Nano/AICore states, Gemma load/inference/tool-calling, thermal/Doze/battery
behavior, permission UX (usage-stats grant flow, QUERY_ALL_PACKAGES declaration UX),
real inventory scale perf, process death/restore, memory-pressure + low-RAM paths, inference
latency/throughput, quota exhaustion, cold/warm start, AAB install parity, pre-launch report.
EMULATOR_SUFFICIENT: lifecycle rotation/background/foreground, backup/restore rules, dark
mode/locales/foldables smoke, `adb`-driven incident scripts. L2_SUFFICIENT (done): routing,
fallback bookkeeping, guardrail matrix, merge semantics, binding dispositions, storm flagging.
Result: G18/G19 fully open; nothing above is device-proven.

## 18. Security/Privacy (recheck: ZERO regression)

Manifest still 3 permissions with INTERNET-removal comment; single exported launcher activity;
no services/receivers/providers; deps still stripped (compose/lifecycle/coroutines/test only,
grep-confirmed zero okhttp/retrofit/firebase/prefs/datastore/room/webview/Log); network
grep hits only simulation copy + deterministic keyword match + icon name; boundary touches only
own cache + GC + settings intent; backup excludes explicit; memory RAM-only. No secrets, no
persistence, no third-party SDK reintroduced by any phase. Privacy posture unchanged and still
the strongest part of the system.

## 19. Test Coverage (by feature, not counts)

L2 integration: sentinel/router/guardrails/channel/deterministic (Handoff), guardrail matrix +
feed (Consumption), fusion/merge/ledger/feed (Fusion), VM scoring/sim/quarantine/exam/resolve
(Harness), UI bindings + tripwire (UiBinding, incl. 1 Compose semantics test), cells + pipeline
+ adversarial battery/memory/storm/stale/kill (Immune ×2), honesty gates (Honesty), trace
stdout (PetScan, probe-not-assertion). Unit-only: threshold spot checks. UNTESTED (ranked):
① homeostasis branch matrix + exact boundaries + oscillation; ② DiagnosticScreen null-fallback
just found; ③ initial-frame seed window vs post-refresh truth; ④ actuator+resolution+memory
commit via VM refresh under failing providers; ⑤ backup/manifest/exported invariants;
⑥ timeout path (5 s never forced); ⑦ staleness forcing (30 s); ⑧ memory-gate <80% rejection.
L3/L4/L5: nothing.

## 20. Dead/Orphan Code (post-sweep remainder)

DEAD (zero callers, grep-proven): ReceptorCell, ContextCell, ResponseCell (+75 gate),
SentinelCell snapshot overload, Neutrophil.attemptContainment, Macrophage.consolidateIncidents,
NK.inspect (DORMANT/wired-but-unreachable: homeostasis branch exists), ImmuneBus.clear,
Regulatory.reset, 12/14 bus payloads, DeviceTelemetryProvider facade, continuous-observing API,
sentinel/channel/immune-snapshot/memory-cases flows (EMIT_ONLY), channel rings (write-only),
isWarm, backend invocationCount, getProvenance, minHealthyBatteryPercent, establishedPackages
(write-only), lastPresentation/lastResult/counters (write-only), VM sim legacy name branches,
SKIPPED/SUPERSEDED/DEGRADED-enum/RESOURCE_DENIED productions, COLLECTION_FAILED events.
DORMANT (legitimate future): NK logic, continuous API, warmup/release/reason overloads (lifecycle
unwired to Activity), provider payloads, storm tiers. Removed already: DecisionCell, legacy
reason() overload, synthetic builder, legacy VM cells, 7 dead imports.

## 21. Contradictions Found (refused harmonization)

- C1: feed CAUSAL (unit-proven) vs DORMANT in deterministic prod (router never consults) — both
  true at different layers; the SOT claim needs the layer qualifier (recording here).
- C2: UI "fused/bound" vs DiagnosticScreen:435 + defaults + seeds still green — tripwire covers
  literals, not null-fallback branches; branch coverage of bindings incomplete.
- C3: memory "gated" vs unbounded — gates bound rate/shape, nothing bounds size; both true.
- C4: resolution "verified" vs "stuck" — gate logic verified, liveness stuck on healthy devices.
- C5: storm "handled" — flagged yes, contained no, recovered untested. Three distinct claims.
- C6: thresholds "tested" — spot-tested branches, never the boundary values or conflicts.
- C7: suite green vs VM scoring weights untested — formulas run, constants unvalidated.

## 22. Claims Downgraded (vs prior docs)

- Homeostasis DISPLAY_AND_LABEL → CAUSAL_CONTROL (deny-only, layered) — upgrade with boundary.
- "Guardrail feed gates production" → gates neural path only; T-Helper is the prod gate.
- "UI claims fixed (15)" → 12 fixed, 3 partial + 1 new null-fallback violation + seed-frame residuals.
- "Storm handled" → detects only. "Memory safe" → gated input, unbounded store.
- "Tests prove behavior" → prove L2 substrates + named gaps (§19); 63/63 retained as regression net.

## 23. Gaps (consolidated, severity re-confirmed)

P0: G5 resolution liveness (stuck-on-healthy is the worst silent failure); G1-remainder (homeostasis
matrix, oscillation, initial-frame, actuator-via-VM, timeout/staleness/memory-gate tests).
P1: G4 NK verdict pending (DELETE recommended); G8 single-threshold-table + conflict adjudication
(values listed §12, choice NOT made here); G10 bounds/TTL/delete; G11 per-stage containment
(Dendritic abort first); G18/G19 device program; G13-remainder (DiagnosticScreen:435, defaults,
seed bodies); G6 payload shrink (12 dead variants) vs subscriber #2.
P2: G16 flow cleanup (7 EMIT_ONLY flows/rings: collect, bind, or delete); G20/G21 remainder
(isolated-set reader, isWarm binding-or-delete, counters); G9 remainder list (§20);
battery/memory threshold tests at exact boundaries; sim-cell-name dead branches.

## 24. Dependencies (for next plan)

Internal: G5 needs G10 decision (what counts as cleanup proof) + G8 thermal band decision;
G4 verdict unblocks homeostasis-corruption branch truth; G10 needs product decision (persist or
stay RAM-only — backup rules assume stateless); device program needs account/hardware owner.
External: physical device (AICore-capable for Nano; ≥4 GB free for Gemma E2B probe), Play
Console filings (unchanged), upload key ceremony (unchanged). No new dependency invented.

## 25. Evidence Index (this audit)

Methods: 3 parallel read-only probes (full-file reads + caller greps), own baseline-diff vs
phase snapshots, `adb devices -l` (empty) + AVD absence, backend import/code-path greps,
spot-reads (DiagnosticScreen:435, decoy seeds, efficiency defaults), threshold table cross-check
(30+ values with file:line). No tests run (deliberately — state observation, not re-proof);
prior L2 evidence stands unexpired (source bytes of evidenced files unchanged since recording —
revalidation recommended at next bundle touch, not asserted here). Layers: all findings L3-code
(reachable-code analysis) unless marked L2.

## 26. Claim Ceiling

Proven L2 (prior, unexpired). This document: L3-code analysis (read + grep, no execution).
Device/API/store claims: none. Numbers herein are code literals, not measurements.

## 27. Recommended Next Decisions (no plan, no implementation)

- D-NK: DELETE vs KEEP_AS_DIAGNOSTIC (recommendation: DELETE + remove unreachable homeostasis
  branch, or keep with explicit "aspirational" label).
- D-RESOLVE: redefine cleanup proof (per-incident bytes? time-bounded recovery? memory-pressure
  equivalence?) — G5 root.
- D-THRESH: adjudicate the 6 conflicts with ONE table (no values proposed here).
- D-MEMORY: persist (Room/Store + migration story) vs RAM-only forever (then add cap/TTL/delete
  as pure hygiene).
- D-CONTAIN: per-stage try/catch + guaranteed snapshot vs fail-fast philosophy (document it).
- D-FLOWS: collect/bind/delete verdict per EMIT_ONLY flow.
- D-UI-RESIDUAL: fix DiagnosticScreen:435 + defaults + seeds, or formally accept with rationale.
- D-DEVICE: run the hardware program (matrix rows in §17) or formally defer Nano/Gemma.

```text
AUDIT_COMPLETE: YES
PRODUCT_MODIFIED: NO
SOT_CREATED: YES (companion file)
CLAIMS_EVALUATED: 60+
PROVEN_L2: routing/fallback/guardrail-matrix/merge/single-entry/ledger-bound/feed-mirror/telemetry-fail-closed/storm-flag/ui-bindings(12)/intent-launch/isReal-split/no-network/no-secrets/no-persistence
PROVEN_L3: NK-dead/resolution-stuck/memory-unbounded/threshold-conflicts/flow-orphans/flag-layering/decoy-seeds
NOT_PROVEN: neural execution/device behavior/perf-battery/timing claims/oscillation/persistence-across-death/production-feed-denial-on-hardware
DEVICE_REQUIRED: nano/gemma/thermal/doze/battery/permission-UX/inventory-scale/death/memory-pressure/latency/quota/startup/prelaunch
UNKNOWN: exact shadow-vs-hardware telemetry deltas; Play review outcomes; real-user battery impact
CONTRADICTIONS: 7 (C1-C7, recorded above)
P0: G5-liveness, G1-remainder(homeostasis-matrix/oscillation/initial-frame/actuator-via-VM/timeout/staleness/memory-gate)
P1: G4-verdict, G8-table, G10-bounds, G11-containment, G18/G19-device, G13-remainder, G6-shrink
P2: G16-cleanup, G20/G21-remainder, G9-remainder, boundary-value tests, sim-name branches
FINAL_STATUS: AUDIT_COMPLETE (awaiting decisions, then plan)
```
