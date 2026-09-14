# Evidence Index — single reference (2026-09-12, SOT companion)

| Claim | Evidence | Source | Confidence | Layer | Last verified |
|---|---|---|---|---|---|
| observeNow ~158–181 ms, 6 providers + provenance | 2 trace runs (181/158 ms, distinct IDs) | `PetScanTraceTest.traceNominalChain` | HIGH | L2 | 2026-09-12 15:50 |
| Fail-closed DEGRADED fires | trace `macroState=DEGRADED confidence=60` | same + `Sentinel.kt:212-254` | HIGH | L2 | 2026-09-12 |
| Router Nano→Deterministic fallback | trace `FALLBACK/GEMINI_NANO_UNAVAILABLE`, 3–9 ms | `traceRouterDirect` + `ReasoningRouter.kt:106-217` | HIGH | L2 | 2026-09-12 |
| Deterministic socket rule live | trace `SUSPICIOUS/88` on "socket burst" | same + `DeterministicBackend.kt:73-86` | HIGH | L2 | 2026-09-12 |
| Storm detected, not contained | 40 msgs → rate 40.0 storm=true dropped=0 (2 runs) | `traceStormLoad` + `ImmuneBus.kt:53-76` | HIGH | L2 | 2026-09-12 |
| NK unwired; dead fns/enums/branches | A1/A8/B1/B2 greps (zero-caller proofs) | code refs in `PETSCAN_RECONCILIATION.md` | HIGH | L3-code | 2026-09-12 |
| Homeostasis label-only | A2 consumer list (VM:361-374 UI only) | `HomeostasisModel.kt`, VM | HIGH | L3-code | 2026-09-12 |
| Nano/Gemma canned | prompt built+dropped; hardcoded assessments | backend files `:122-145`/`:120-141` | HIGH | L3-code | 2026-09-12 |
| RAM-only memory; Room claims stale | A6/A7 zero-IO proofs; 5 stale files | memory pkg + reports | HIGH | L3-code | 2026-09-12 |
| Zero network sinks | grep + AAB manifest dump (3 perms) | `NETWORK_ENDPOINT_INVENTORY.md`, AAB | HIGH | L2-artifact | 2026-09-12 |
| 30/30 tests; VM uncovered | suite XML; 0 VM-touching tests | `testDebugUnitTest`, probe 4 §3 | HIGH | L2 | 2026-09-12 |
| Boundary: 1 real action, refusals honest, 2 gaps | A5/B7 + file read | `AndroidRealityBoundary.kt` | HIGH | L3-code | 2026-09-12 |
| Threshold inventory (no endorsement) | `PETSCAN_THRESHOLD_MATRIX.md` (30+ rows) | static scan + reads | MED (positions) | L3-code | 2026-09-12 |
| UI 15 overclaims | probe 4 §2 strings + sources | screens + VM | MED (wording) | L3-code | 2026-09-12 |
| Release artifact + signing state | AAB sha, validate 0, dex scan | `RELEASE_PACKAGE.md` | HIGH | L2-artifact | 2026-09-12 |
| L3–L5/device/Nano-hardware | unavailable (no device/AVD) | `adb devices` empty | n/a (absence) | — | 2026-09-12 |

Supersedes prior `PETSCAN_EVIDENCE_INDEX.md` as the single per-claim table (that file remains as method log).
