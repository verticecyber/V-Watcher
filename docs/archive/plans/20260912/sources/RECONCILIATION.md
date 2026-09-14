# PETSCAN Reconciliation (2026-09-12 — adversarial re-verification)

Authority order applied: L1 runtime > L2 tests/harness > L3 reachable code > L4 unreachable code > L5 docs > L6 intent.
Method: independent grep battery (A1–A8, B1–B7) + trace rerun. Prior PETSCAN docs were the hypothesis; below is the verdict.

## Conflict table

| Claim | Source A | Source B | Code evidence | Runtime evidence | Resolution |
|---|---|---|---|---|---|
| NK integrated | `SYSTEM_MAP` (UNUSED) vs cell file (full logic) | — | A1: zero callers; only `internalCorruptionsDetected` read + `toModel` | never runs | CODE_TRUTH: UNUSED (claim CONFIRMED) |
| Homeostasis zero-effect | `EXEC_SUMMARY` (display-only) vs `VM:361-374` (condition mapping) | — | A2: macroState → DeviceCondition/doctorNote/barrier label | trace DEGRADED/60 flows to UI | RUNTIME_TRUTH: **DISPLAY_AND_TRIAGE_LABEL** (refines DISPLAY_ONLY; still no actuation/gating) |
| 10/12 bus payloads dead | probe 2 vs contract file (12 variants) | — | A4: only TelemetryArrival/PatternDetected constructed outside definition | busTotal=2 in trace | CODE_TRUTH CONFIRMED (recount method fixed: definition file counts as 1) |
| Channel flows used | interface (2 flows) vs probe (no collectors) | — | A3: decl + emit only | — | CODE_TRUTH: emit-only CONFIRMED |
| Memory Room/SQLite | old JSON/MD reports vs `memory/*.kt` | — | A6/A7: zero disk IO, zero Room refs in main | reseed each launch | DOCUMENTATION_ERROR (code wins); 5 stale files listed in SOT §16 |
| `isReal=true` on degraded | probe G14 vs provider files | — | B4 + `DeviceResourceProvider.kt:33-52` (DEGRADED+isReal), `AppUsageProvider.kt:44` (PERMISSION_REQUIRED+isReal) | — | CODE_TRUTH CONFIRMED (2 sites) |
| Engine backend mirror | probe G17 vs `OnDeviceReasoningEngine.kt:109` | — | B3: `it.selectedBackend` (stale) | — | CODE_TRUTH: bug CONFIRMED |
| RECOVERING reachable | `HomeostasisModel` branch vs probe | — | B2: resolving ⊆ active, prior branch wins | — | CODE_TRUTH: dead branch CONFIRMED |
| Dual pipelines compete | probe G3 vs VM | — | B5: VM:389 system list **clobbers** sim/manual cases when non-empty; VM:628/671/730/783 separate writers | — | CODE_TRUTH CONFIRMED + sharpened (clobber semantics) |
| Throttle/isolate enforced | boundary `EXECUTED` vs probe G20 | — | B7: **zero readers** of `isInferenceThrottled`/`isSubsystemIsolated` in main | — | CODE_TRUTH: **write-only flags (NEW G21)** — weaker than previously stated |
| Bus metadata (senderState/isStale) | contract vs `ImmuneBus.kt` | — | B6: never read in bus | — | CODE_TRUTH: metadata-only CONFIRMED |
| `RESOURCE_GUARD_DENIED`/`NONE`/`COLLECTION_FAILED` live | enums vs probe | — | B1: definition-only hits | — | CODE_TRUTH: dead CONFIRMED |
| static_scan §1 as orphan evidence | script vs targeted greps | — | counts definitions, not references | — | METHODOLOGY WEAK — superseded by A/B greps; script kept for thresholds/payloads/collectors |

## Counts (§27 inputs)

- Document contradictions: 1 refined (homeostasis effect scope) + 0 overturned of 12 checked.
- Code↔doc contradictions: 5 stale files (Room claims) + 15 UI overclaims (unchanged, re-listed in SOT §12).
- Runtime↔doc: 0 (trace reproduced; numbers differ run-to-run as expected: observeMs, IDs).
- New gaps: G21 (write-only boundary flags). Sharpened: G3 (clobber), G14 (2nd site), G20 (stronger).
- Removed gaps: 0.
