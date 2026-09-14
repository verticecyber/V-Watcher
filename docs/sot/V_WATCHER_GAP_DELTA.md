# Gap Delta — PETSCAN original vs SOT adversarial (2026-09-12)

| Gap | Delta | Reason |
|---|---|---|
| G1 ViewModel untested | CONFIRMED | untouched by review; still 0 tests |
| G2 Homeostasis display-only | CONFIRMED + refined | DISPLAY_AND_TRIAGE_LABEL (A2); still no actuation |
| G3 Dual pipelines | CONFIRMED + sharpened | clobber semantics VM:389 proven (B5) |
| G4 NK unwired | CONFIRMED | A1 recount |
| G5 Resolution stall | CONFIRMED | logic unchanged |
| G6 Bus subscriber-less | CONFIRMED | A3/A4 recounts |
| G7 forceDeterministic ignored | CONFIRMED | outputs still dropped |
| G8 Threshold sprawl | CONFIRMED | full matrix built, no values chosen |
| G9 Dead code | CONFIRMED | B1/B2 re-proven |
| G10 Memory unbounded | CONFIRMED | A6 |
| G11 Dendritic abort | CONFIRMED | code unchanged |
| G12 Intent-not-sent | CONFIRMED | A5 (UI does startActivity; boundary doesn't) |
| G13 UI overclaims | CONFIRMED (15, itemized, not fixed per §26) | — |
| G14 isReal semantics | CONFIRMED + 2nd site | `DeviceResourceProvider.kt:51` |
| G15 Stale docs | CONFIRMED → resolving | banners + SOT §16 supersede |
| G16 Channel flows | CONFIRMED | A3 |
| G17 Stale mirror | CONFIRMED | B3 |
| G18 L3–L5 absent | CONFIRMED | still no device |
| G19 Neural execution | CONFIRMED | stubs intact |
| G20 Flags unenforced | CONFIRMED + strengthened | B7: not just unenforced — unread (see G21) |
| **G21 Write-only boundary flags** | NEW | `isInferenceThrottled`/`isSubsystemIsolated`: zero readers in main |

REMOVED: 0. DOWNGRADED: 0 (one refinement is precision, not severity). UNKNOWN: 0 — every checked claim resolved to CODE/RUNTIME/DOCUMENTATION verdict.
