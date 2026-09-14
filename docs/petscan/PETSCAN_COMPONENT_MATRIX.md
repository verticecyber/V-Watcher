# PETSCAN — Component Matrix (2026-09-12)

Per-component verdicts (probes 1–4 + trace). `EFFECTIVE` = runs in production path with observed effect.

## Cells

| Cell | Class | Evidence |
|---|---|---|
| PRR | RULE_ONLY, live | `PatternRecognitionCell.kt:35,49,63` 3 rules; fans evidence to 6 downstream |
| Dendritic | REAL_RUNTIME_AGENT | only inference spender; uncaught throw aborts pipeline tail (`BiomimeticImmuneSystem.kt:181`) |
| Neutrophil | REAL_RUNTIME_AGENT + UNUSED fn | `respondToAcuteStress` live (throttle flag); `attemptContainment:57` zero callers |
| Macrophage | REAL_RUNTIME_AGENT + UNUSED fn | `scavenge` live (usually 0/0 EXECUTED); `consolidateIncidents:49` zero callers |
| NK | UNUSED | zero callers; `internalCorruptionsDetected` always 0; honesty logic (UNAVAILABLE kill) tested but never runs |
| T-Helper | COORDINATION_ONLY | sole escalation gate (`:172,184`); single-signal ≥85 escalates |
| B-Cell | RULE_ONLY | live hit rate ~0 (PRR strings never contain seed names) |
| Cytotoxic | REAL_RUNTIME_AGENT | 3 gates (fresh/conf≥80/non-empty) then internal-only action |
| Regulatory | RULE_ONLY | veto live; `forceDeterministic` + `imposedCooldownMs` produzido mas ignorado; `reset()` never called; `activeIncidentsCount` dead input |
| Resolution | RULE_ONLY | blocks on current-evidence for old incidents; `RECOVERING` unreachable (see homeostasis) |
| SentinelCell | DATA_ONLY | formats counters; orchestrator dispatches |
| MemoryCell | SKELETON | counter; real write is repo |
| Legacy Receptor/Context/Decision/Response/Sentinel | DATA/RULE, Decision REAL | second reasoning path via VM, independent of dendritic |

## Engine / infra

| Component | Class |
|---|---|
| `BiomimeticImmuneSystem` | COORDINATION_ONLY, EFFECTIVE (sync pipeline; no try/catch around tail) |
| `ImmuneBus` | COORDINATION_ONLY transport; storm DETECTS, never CONTAINS; `dropped` counter structurally ~0 (DROP_OLDEST); `clear()` keeps replay |
| `HomeostasisEngine` | RULE_ONLY, WIRED_BUT_NO_EFFECT; `RECOVERING` dead branch; dims `stability.*=0`, `isAnomalous=false`, `isDeviceSecure=true` hardcoded |
| `BaselineEngine` | RULE_ONLY, EFFECTIVE; `minHealthyBatteryPercent=10` dead; learned package set never compared; re-established every refresh (no learning) |
| Router / Guardrails / Deterministic | EFFECTIVE; `RESOURCE_GUARD_DENIED` + `NONE` dead; timeout relabels everything `MODEL_INITIALIZATION_FAILED`; engine `:109` stale-backend mirror bug |
| Nano / Gemma | SKELETON (detection honest; inference canned; `isRealOnDeviceModel=true` overclaims; prompt string built then dropped) |
| Boundary | EFFECTIVE with 1 gap: `NAVIGATE_APP_SETTINGS` reports EXECUTED without `startActivity` |
| Channel | PARTIALLY (emit-only; both flows collector-less) |
| Memory repo | DATA_ONLY PARTIAL (gates good incl. `[SIM]` reject; no TTL/cap; `cryptographic` wording false; seed `MEM-0035` claims `<35ms` unmeasured) |
| Telemetry | EFFECTIVE (Battery/Resources/Network/Inventory/Usage-real-when-granted; SystemState static-not-sensor; `isReal=true` on Usage-denied + Resource-fallback questionable) |
| Sentinel | EFFECTIVE (fail-closed DEGRADED proven in trace; `captureStart` dead; `COLLECTION_FAILED` enum never emitted; continuous mode dead) |
| ViewModel | EFFECTIVE + UNTESTED god class (1218L; 2 pipelines; sim bypasses repo gates; `whyExplainingAppId`/`activeTab` dead state) |

## Confidence semantics (all HEURISTIC_SCORE, none calibrated)

Fixed ints: deterministic 90/88/82/75/96 (NORMAL highest — match-strength, not posterior); PRR none (presence only); T-helper 85/80 gates; cytotoxic 80 gate; homeostasis 0/60/88/92/94/95/96/98 display; Baseline 85/90/75; sim literals 84/92%. `Elevated`, `HIGH_CONFIDENCE_ANOMALY`, `RESOURCE_GUARD_DENIED`, `NONE` defined-never-emitted. No threshold has calibration basis; treat as ordinal gates.
