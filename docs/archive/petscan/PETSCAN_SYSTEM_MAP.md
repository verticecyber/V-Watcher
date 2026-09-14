# PETSCAN — System Map (2026-09-12)

| Subsystem | Files | Public API | Callers | Tests | Runtime path | Status |
|---|---|---|---|---|---|---|
| Telemetry providers (6) | `telemetry/*Provider.kt`, `TelemetryModels.kt` | `collect(): TelemetryResult` | `AndroidSentinel.observeNow` | Handoff/Immune (Robolectric) | L2 live (181 ms obs) | EFFECTIVE (real APIs; 2 honest degradations, 1 false-confidence: scoped visibility) |
| `DeviceTelemetryProvider` | `telemetry/DeviceTelemetryProvider.kt:17` | `captureSnapshot()` | **none** | none | dead | ORPHANED |
| `AndroidSentinel` | `sentinel/Sentinel.kt`, `CanonicalObservation.kt` | `observeNow()`, `start/stopContinuousObserving` | VM `refresh/runDeviceCheck` | Handoff + Immune | L2 live | EFFECTIVE (continuous mode ORPHANED) |
| Provenance/Freshness | `TelemetryModels.kt`, `CanonicalObservation.kt` | structs + `isStale` (30 s gap) | Homeostasis gate, Cytotoxic gate | Adversarial (stale) | L2 live | EFFECTIVE (gap-semantics, documented) |
| `ImmuneBus` | `immune/ImmuneBus.kt`, `ImmuneEventContract.kt` | `dispatch()`, `stats` | 2 dispatch sites; collectors: 1 no-op | Adversarial storm | L2 live (40msg/99ms, storm=true) | PARTIALLY_IMPLEMENTED (transport ok; storm flag-only; 10/12 payloads dead) |
| `BiomimeticImmuneSystem` | `immune/BiomimeticImmuneSystem.kt` | `processObservation()` | VM:348 | Immune pipeline test | L2 live | EFFECTIVE (linear-sync; exception aborts tail) |
| PRR / Regulatory / T-Helper / B / Resolution | cells (5) | `scan/evaluate/coordinate/bind/verify` | orchestrator | Immune unit | L2 live | EFFECTIVE (RULE/COORDINATION; T-helper gate real) |
| Neutrophil / Macrophage / Dendritic / Cytotoxic | cells (4) | `respond/scavenge/present/execute` | orchestrator (gated) | Immune unit | L2 live | EFFECTIVE (gated; dendritic spends inference budget) |
| NK cell | `immune/NaturalKillerCell.kt` | `inspectComponentIntegrity` | **none** | unit (direct) | never | UNUSED (wired logic, zero callers) |
| `MemoryCell` | `ImmuneCells.kt:209` | `remember()` | orchestrator:251 | none direct | counter only | SKELETON (real store is repo) |
| Legacy cells (Sentinel/Receptor/Context/Decision/Response) | `ImmuneCells.kt` | `observe/receive/correlate/decide/safeResponse` | VM only (2nd pipeline) | none | live parallel path | EFFECTIVE but DUPLICATE architecture |
| `LocalImmuneMemoryRepository` | `memory/*.kt` | `addPattern/addCase/resolveCase` | orchestrator | none direct | RAM-only, unbounded | PARTIALLY_IMPLEMENTED (gates good; no expiry/persist) |
| `HomeostasisEngine` | `immune/HomeostasisModel.kt:88` | `evaluate()` (pure) | orchestrator (2 sites) | pipeline test | L2 live | WIRED_BUT_NO_EFFECT (display-only) |
| `BaselineEngine` | `immune/BaselineEngine.kt` | `establish/evaluateSnapshot` | VM:129,134 | none direct | live, drives score | EFFECTIVE (dead threshold 10%, learned set unused) |
| `ReasoningRouter` | `reasoning/ReasoningRouter.kt` | `selectBackend/routeAndExecute` | channel; engine | Handoff | L2 live (9 ms) | EFFECTIVE (2 dead enum variants) |
| Nano / Gemma backends | `reasoning/GeminiNano*.kt`, `Gemma*.kt` | contract impl | router | Handoff (unavail path) | stub (canned) | SKELETON (detection real, inference canned) |
| `DeterministicBackend` | `reasoning/DeterministicBackend.kt` | `execute()` 5 rules | router, DecisionCell path | Handoff honesty | L2 live (SUSPICIOUS/88) | EFFECTIVE (heuristic confidences) |
| `ResourceGuardrails` | `reasoning/ResourceGuardrails.kt` | `evaluate()` + Mutex | router | Handoff | L2 live | EFFECTIVE (15%/92%/2.5–3 s) |
| `AndroidRealityBoundary` | `immune/AndroidRealityBoundary.kt` | `executeRealAction()` 6 verbs | 4 cells | NK honesty | L2 live | EFFECTIVE (1 real fs action; 1 intent-not-sent; refusals honest) |
| `CommunicationChannel` | `communication/*.kt` | `dispatchTelemetry/sendReasoningRequest` | VM, dendritic, engine | Handoff | emit-only (no collectors) | PARTIALLY_IMPLEMENTED |
| ViewModel | `viewmodel/VWatcherViewModel.kt` (1218L) | 14 public fns | UI | **none** | live, untested | EFFECTIVE but UNTESTED (god class) |
| UI (7 screens) | `ui/screens/*`, `MainActivity.kt` | composables | nav | Greeting only | renders | EFFECTIVE (~15 overclaims, see claim doc) |
| Persistence / Room | — | — | — | — | absent | ABSENT (docs stale claiming Room) |
| Background/WorkManager | — | — | — | — | absent | ABSENT (foreground-only) |
| Lifecycle survival | VM retained; `remember` (no saveable); no SavedState/persist | — | — | none | rotation partial; death total loss | PARTIALLY_IMPLEMENTED |
