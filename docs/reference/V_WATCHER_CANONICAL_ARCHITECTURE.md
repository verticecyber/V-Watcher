# V-WATCHER PRO — CANONICAL ARCHITECTURE SPECIFICATION
**Document Authority**: `docs/reference/V_WATCHER_CANONICAL_ARCHITECTURE.md`  
**Classification**: Architecture Freeze & Conceptual Grounding  
**Version**: 1.0 (Frozen)  

---

## 1. ARCHITECTURAL ONTOLOGY

V-Watcher Pro distinguishes three distinct architectural dimensions:
1. **Conceptual Cells (6 Archetypes)**: The foundational biologically inspired design contract.
2. **Runtime Agents (11 Discrete Classes + Bus)**: The concrete object model executing in the Android process.
3. **Pipeline Stages (12 Execution Phases)**: The deterministic processing loop traversed upon receiving telemetry.

```
                    ┌────────────────────────────────────────┐
                    │     Android Platform Substrates        │
                    │ (Battery, AM, PM, CM, AppOps, Build)   │
                    └──────────────────┬─────────────────────┘
                                       │
                                       ▼
                       [ AndroidSentinel.observeNow() ]
                                       │
                                       ▼
                             CanonicalObservation
                                       │
             ┌─────────────────────────┴─────────────────────────┐
             │                                                   │
             ▼                                                   ▼
 ┌───────────────────────┐                           ┌───────────────────────┐
 │   Stage 1: Sentinel   │                           │  ImmuneBus (SharedFlow)│
 └───────────┬───────────┘                           └───────────▲───────────┘
             │                                                   │
             ▼                                                   │
 ┌───────────────────────┐                                       │
 │     Stage 2: PRR      │ ──[ Dispatches PatternDetected ]──────┘
 └───────────┬───────────┘
             │
             ▼
 ┌───────────────────────┐
 │ Stage 3: Regulatory   │ ──[ Evaluates Storm, Battery, Memory Guardrails ]
 └───────────┬───────────┘
             │
             ▼
 ┌───────────────────────┐
 │  Stage 4: Neutrophil  │ ──[ Rapid Non-Destructive Triage ]
 └───────────┬───────────┘
             │
             ▼
 ┌───────────────────────┐
 │     Stage 5: B-Cell   │ ──[ Matches Antibody in Memory Repository ]
 └───────────┬───────────┘
             │
             ▼
 ┌───────────────────────┐
 │  Stage 6: T-Helper    │ ──[ Coordinates Escalation Arbitration ]
 └───────────┬───────────┘
             │
             ▼
 ┌───────────────────────┐
 │   Stage 7: Dendritic  │ ──[ Synthesizes Antigen -> Reasoning Router ]
 └───────────┬───────────┘
             │
             ▼
 ┌───────────────────────┐
 │  Stage 8: Cytotoxic   │ ──[ Evaluates Gates -> AndroidRealityBoundary ]
 └───────────┬───────────┘
             │
             ▼
 ┌───────────────────────┐
 │  Stage 9: Macrophage  │ ──[ Scavenges Cache & Heap on Memory Pressure ]
 └───────────┬───────────┘
             │
             ▼
 ┌───────────────────────┐
 │  Stage 10: Resolution │ ──[ Verifies Baseline Return via Evidence Ledger ]
 └───────────┬───────────┘
             │
             ▼
 ┌───────────────────────┐
 │   Stage 11: Memory    │ ──[ Commits Signature to Local Memory Store ]
 └───────────┬───────────┘
             │
             ▼
 ┌───────────────────────┐
 │ Stage 12: Homeostasis │ ──[ Computes Macro-State: Equilibrium Synthesis ]
 └───────────────────────┘
```

---

## 2. THE 11 COOPERATING AGENTS & ROLES

1. **`SentinelCell` (`AgentRole.SENTINEL`)**: Ingests `CanonicalObservation` from the 6 Android telemetry providers and translates raw hardware metrics into typed bus events.
2. **`PatternRecognitionCell` (`AgentRole.PRR`)**: Analyzes multi-vector data for cellular danger signals (Thermal Elevation, Resource Depletion, Application Anomalies).
3. **`RegulatoryTCell` (`AgentRole.REGULATORY`)**: Evaluates device constraints (discharging battery <15%, low memory, bus storm >30 msgs/s) to veto heavy inference and enforce deterministic mode.
4. **`NeutrophilCell` (`AgentRole.NEUTROPHIL`)**: Rapid acute responder executing immediate, non-destructive tactical containment.
5. **`BCell` (`AgentRole.B_CELL`)**: Matches antigens against local memory signatures to identify known benign vs. novel anomalies.
6. **`THelperCoordinatorCell` (`AgentRole.T_HELPER`)**: Orchestrates the immune escalation cascade, determining whether dendritic presentation is warranted.
7. **`DendriticCell` (`AgentRole.DENDRITIC`)**: Synthesizes observation context into structured antigen presentations and routes them to the on-device reasoning substrate.
8. **`CytotoxicEffectorCell` (`AgentRole.CYTOTOXIC`)**: Enforces multi-point gating (evidence freshness, confidence ≥80%, non-empty evidence) before executing containment actions via `AndroidRealityBoundary`.
9. **`MacrophageCell` (`AgentRole.MACROPHAGE`)**: Housekeeping and phagocytosis agent that reclaims internal temporary cache directories (`RECLAIM_INTERNAL_CACHE`) during memory strain.
10. **`NaturalKillerCell` (`AgentRole.NATURAL_KILLER`)**: Internal integrity agent auditing software state consistency and flagging internal corruptions.
11. **`ResolutionCell` (`AgentRole.RESOLUTION`)**: Enforces clinical return-to-baseline criteria, preventing premature resolution while active anomalies or unnormalized resources persist.
12. **`MemoryCell` (`AgentRole.MEMORY`)**: Indexes verified incident signatures into `LocalImmuneMemoryRepository` with provenance metadata.

---

## 3. REALITY BOUNDARY CONTRACT

`AndroidRealityBoundary` is the sole actuation gateway for all effector agents. It guarantees:
- **Sandbox Honesty**: It refuses calls to kill third-party processes (`KILL_EXTERNAL_PROCESS`), returning `ActionExecutionStatus.UNAVAILABLE` with clear platform rationale and navigation guidance to system settings.
- **Local Isolation Only**: Remediation is restricted to internal cache reclamation (`vwatcher_temp_*`), garbage collection requests, and in-memory execution throttling.
- **Fail-Closed Behavior**: Any unrecognized action verb returns `ActionExecutionStatus.DENIED`.

---

## 4. MULTI-MODEL REASONING SUBSYSTEM

The reasoning subsystem incorporates three tiers coordinated by `ReasoningRouter`:
1. **Tier 1 (Gemini Nano)**: Probes AICore system service. If absent or disabled, falls back cleanly without simulated or canned outputs masquerading as real inference.
2. **Tier 2 (Gemma)**: Local on-device LLM stub checking for local weight binaries in `filesDir/models/`. Falls back cleanly if absent.
3. **Tier 3 (Deterministic Engine)**: Evaluates 5 explicit rule patterns covering thermal elevation, memory pressure, rapid battery drain, suspicious network sockets, and baseline equilibrium. Always available, zero network dependencies.

---

## 5. UI BINDING & CLAIM INTEGRITY

All user interface surfaces are bound to real system state via `UiClaimBindings.kt`:
- `REAL DEVICE DATA` banner renders conditionally based on the existence of a real observation.
- `AICORE READY` / `DETERMINISTIC MODE` badges accurately reflect the active backend.
- Homeostasis status badges display evaluated macro-states (`HOMEOSTATIC`, `WATCH`, `STRESSED`, `ACTIVE_DEFENSE`, `RECOVERING`, `DEGRADED`) or `EVALUATING`, never hardcoded defaults.
- Incident case statistics accurately reflect uncontained and isolated cases without inflation.
