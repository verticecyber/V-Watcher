# V-WATCHER PRO — CANONICAL EVIDENCE MANIFEST
**Classification**: Forensic Architectural Truth Ledger & Evidence Provenance SOT  
**Document Authority**: `docs/V_WATCHER_EVIDENCE_MANIFEST.md`  
**Execution Environment**: Linux Container (Headless JVM / Android SDK Tools / Streaming Emulator Decoupled)  
**Verification Framework**: 5-Tier Epistemic Provenance Hierarchy  

---

## 1. EPISTEMIC PROVENANCE TIERS

To eliminate ambiguity, self-attestation, and methodology inflation, all architectural claims in V-Watcher Pro are classified under five non-fungible provenance tiers:

| Tier | Category | Definition & Verification Boundary | Current Environment Status |
|:---|:---|:---|:---:|
| **L1** | **Static Source Code** | Syntactic structure, type signatures, class declarations, and compiled bytecode. | **PROVEN** |
| **L2** | **Deterministic In-Process (Robolectric/JVM)** | Execution of real Android runtime components against Robolectric shadows and local JVM threads. Proves API consumption, type conversions, state-machine transitions, and boundary denials. | **PROVEN** (103/103 Tests Passing) |
| **L3** | **Streaming Emulator / Container Host** | Real-time observation of an executing Android OS instance running inside the agent host environment via local ADB socket or instrumentation runner. | **UNAVAILABLE** (No ADB target / virtualization in container) |
| **L4** | **Bare-Metal Physical Device** | Execution on physical Android silicon (Qualcomm, Tensor, MediaTek) validating hardware battery chemistry, thermal sensor registers, and kernel Doze states. | **UNAVAILABLE** (Requires physical hardware) |
| **L5** | **App Store / Production Fleet** | Real-world telemetry across diverse end-user devices, Google Play integrity attestation, and carrier network conditions. | **EXTERNAL** (Requires human operator & Play Console submission) |

> **CORE POSTURE**: Any claim regarding V-Watcher execution is strictly verified at **L1 and L2**. Statements claiming "LIVE 100% Host OS coupling" are categorically rejected as methodological overreach because the container lacks an ADB connection to the decoupled browser Streaming Emulator.

---

## 2. THE 12-STEP AUTONOMOUS REFLEX RECONCILIATION

The product literature and UI describe a **12-step autonomous immune reflex**. A forensic audit of the implementation reveals:

### Clarification of Terms: Pipeline Stage vs. Runtime Agent
1. **12 Pipeline Processing Stages**: A single call to `BiomimeticImmuneSystem.processObservation(CanonicalObservation)` deterministically executes exactly **12 sequential processing stages**.
2. **11 Injected Cooperating Agents**: The multi-agent orchestrator coordinates **11 discrete biomimetic agent classes** (plus the model-independent `ImmuneBus`).
3. **Reconciliation**: The "12-step reflex" refers strictly to the **12 processing stages of the pipeline**, *not* a 1:1 mapping where each agent runs exactly once in isolation.

### The 12 Pipeline Stages in `processObservation()`

| Stage | Name | Implementing Unit | Execution Semantics & Boundary | Provenance |
|:---:|:---|:---|:---|:---:|
| **1** | **Sentinel Dispatch** | `SentinelCell` + `ImmuneBus` | Translates `CanonicalObservation` into `ImmunePayload.TelemetryArrival` and dispatches to bus. | L1, L2 |
| **2** | **PRR Pattern Recognition** | `PatternRecognitionCell` | Evaluates multi-source telemetry against heuristic danger patterns (thermal >42°C, low memory, abnormal foreground package). Emits `PatternDetected`. | L1, L2 |
| **3** | **Regulatory Evaluation** | `RegulatoryTCell` | Evaluates battery discharge, memory pressure, and bus storm rate (>30 msgs/sec). Enforces deterministic fallback and cooldowns if resources are constrained. | L1, L2 |
| **4** | **Acute Rapid Response** | `NeutrophilCell` | Executes immediate, non-destructive tactical stabilization if acute stress is detected. | L1, L2 |
| **5** | **Antibody Matching** | `BCell` | Matches detected signal signatures against known patterns in `LocalImmuneMemoryRepository`. | L1, L2 |
| **6** | **T-Helper Coordination** | `THelperCoordinatorCell` | Arbitrates whether escalation is justified based on antibody match and regulatory veto status. | L1, L2 |
| **7** | **Antigen Presentation** | `DendriticCell` | Synthesizes observation context into structured antigen presentation and routes to on-device reasoning substrate. | L1, L2 |
| **8** | **Cytotoxic Effector Defense** | `CytotoxicEffectorCell` | Evaluates 3 strict gates (freshness, confidence ≥80%, non-empty evidence). If satisfied, invokes `AndroidRealityBoundary`. | L1, L2 |
| **9** | **Macrophage Scavenging** | `MacrophageCell` | Triggers cache reclamation (`RECLAIM_INTERNAL_CACHE`) and heap garbage collection if available memory is low. | L1, L2 |
| **10** | **Resolution Verification** | `ResolutionCell` | Verifies physiological baseline return against the `evidenceLedger`. Prevents premature resolution while anomalies persist or host resources remain strained. | L1, L2 |
| **11** | **Memory Commitment** | `MemoryCell` | Commits clinically verified incident patterns into local memory with provenance tracking. | L1, L2 |
| **12** | **Homeostasis Synthesis** | `HomeostasisEngine` | Computes holistic macro-state (`HOMEOSTATIC`, `WATCH`, `STRESSED`, `ACTIVE_DEFENSE`, `RECOVERING`, `DEGRADED`) and updates system snapshot. | L1, L2 |

---

## 3. LINEAGE MAPPING: 6 ORIGINAL CELLS TO CURRENT MULTI-AGENT RUNTIME

The original V-Watcher architectural contract established 6 conceptual cell archetypes. The production runtime expanded these into 11 concrete biomimetic agents to separate detection, arbitration, effector action, and safety regulation.

| Original Conceptual Cell | Current Runtime Agent(s) | Architectural Evolution & Justification | Current Implementation Status |
|:---|:---|:---|:---:|
| **Sentinel Cell** | `SentinelCell` + Telemetry Providers | Unbundled telemetry acquisition into 6 specialized Android providers (`BatteryProvider`, `DeviceResourceProvider`, `NetworkTelemetryProvider`, `AppInventoryProvider`, `AppUsageProvider`, `SystemStateProvider`) coordinated by `AndroidSentinel`. | **EFFECTIVE** (Production Ingress) |
| **Receptor Cell** | `PatternRecognitionCell` (PRR) | Transformed from passive signal receiver into active Pattern Recognition Receptors scanning for biochemical danger signals. | **EFFECTIVE** (Heuristic Rules) |
| **Context Cell** | `BCell` + `DendriticCell` | Divided into antibody recognition (`BCell`) and structured antigen presentation with context synthesis (`DendriticCell`). | **EFFECTIVE** (Context Synthesis) |
| **Decision Cell** | `ReasoningRouter` + `THelperCoordinatorCell` | Divided into architectural arbitration (`THelperCoordinatorCell`) and backend inference routing (`ReasoningRouter` between Nano, Gemma, and Deterministic engines). | **EFFECTIVE** (Deterministic Fallback Live) |
| **Response Cell** | `NeutrophilCell`, `MacrophageCell`, `CytotoxicEffectorCell` | Subdivided by biological kinetic response: rapid acute triage (`Neutrophil`), debris and cache recycling (`Macrophage`), and authorized effector containment (`CytotoxicEffector`). | **EFFECTIVE** (Gated Actuation) |
| **Memory Cell** | `MemoryCell` + `LocalImmuneMemoryRepository` | Backed by an in-memory repository with debounce, confidence, and provenance validation filters. | **EFFECTIVE** (RAM Repository) |

*Additional Specialized Runtime Guard:*  
- `RegulatoryTCell`: Introduced to prevent "autoimmune" storms, throttling execution when battery is <15% discharging or bus dispatches exceed 30 msgs/sec.

---

## 4. ANDROID CONTRACT VERIFICATION & PROVENANCE

### Android Telemetry Providers Contract (`com.example.contract.AndroidProviderContractTest`)
Verified on local JVM via Robolectric Shadow Substrates:

1. **Battery Telemetry**:
   - Captures `ACTION_BATTERY_CHANGED` sticky broadcast.
   - Accurately converts level percentage, charging state, health status, and raw tenths-of-degree Celsius to float degrees (e.g., 295 -> 29.5°C; 465 -> 46.5°C overheat threshold).
   - Provenance: **L2 PROVEN**.
2. **Device Resources Telemetry**:
   - Interrogates `ActivityManager.getMemoryInfo()`.
   - Accurately captures available memory, total memory, and low-memory boolean flag.
   - Provenance: **L2 PROVEN**.
3. **Network State Telemetry**:
   - Queries `ConnectivityManager.getNetworkCapabilities()`.
   - Validates transport type without opening external network sockets.
   - Provenance: **L2 PROVEN**.
4. **App Inventory Telemetry**:
   - Enumerates installed packages via `PackageManager.getInstalledPackages()`.
   - Distinguishes system apps from user-installed apps via `ApplicationInfo.FLAG_SYSTEM`.
   - Filters and surfaces requested permissions per package.
   - Provenance: **L2 PROVEN**.
5. **App Usage Telemetry**:
   - Interrogates `AppOpsManager` mode for `OPSTR_GET_USAGE_STATS`.
   - Honestly degrades to `PERMISSION_REQUIRED` when access is not granted, rejecting false "zero-activity" claims.
   - Provenance: **L2 PROVEN**.

### End-to-End Causal Pipeline Contract (`com.example.contract.InProcessEndToEndContractTest`)
1. **Resource Degradation Flow**: Shadow low-memory condition (110MB free) propagates through `DeviceResourceProvider` -> `AndroidSentinel` -> `CanonicalObservation` -> `BiomimeticImmuneSystem` -> triggers `MacrophageCell` cache reclamation (`RECLAIM_INTERNAL_CACHE`) and drives `HomeostasisEngine` to non-equilibrium macro-state.
2. **Permission Honesty Flow**: Denied usage access sets `isReal = false` and `availability = PERMISSION_REQUIRED`.
3. **Change != Threat Principle**: Addition of standard utility package (`com.utility.calculator` with standard permissions) is recognized as benign and does *not* trigger false anomaly candidates or active incidents.

---

## 5. ACTUATION & REALITY BOUNDARY AUDIT

`AndroidRealityBoundary` strictly enforces the boundaries of Android application sandboxing:

| Action Verb | Execution Classification | Real Platform Behavior | Boundary Safeguard |
|:---|:---:|:---|:---|
| `RECLAIM_INTERNAL_CACHE` | **EXECUTED** | Cleans internal app cache directories (`vwatcher_temp_*`) and invokes `System.gc()`. | Scoped strictly to own process storage. |
| `THROTTLE_INTERNAL_INFERENCE` | **EXECUTED** (In-Memory) | Sets internal concurrency throttle flag. | Prevents local CPU/GPU exhaustion. |
| `ISOLATE_INTERNAL_SUBSYSTEM` | **EXECUTED** (In-Memory) | Marks target internal subsystem as isolated in tracking set. | Restricts internal software components. |
| `RESET_SUBSYSTEM_ISOLATION` | **EXECUTED** (In-Memory) | Clears target subsystem isolation. | Restores nominal execution state. |
| `NAVIGATE_APP_SETTINGS` | **EXECUTED** (UI Bound) | Emits target action parameters for UI intent dispatch. | UI invokes `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`. |
| `KILL_EXTERNAL_PROCESS` | **UNAVAILABLE** (Honest Refusal) | Strictly refuses to execute. | Returns platform reason citing Android sandbox restrictions; directs user to system settings. |
| *Unknown / Arbitrary Verb* | **DENIED** | Strictly rejected. | Unknown actions fail closed. |

---

## 6. MULTI-MODEL REASONING STATUS

| Substrate Leg | Declared Capability | Real Operational Substrate | Status |
|:---|:---|:---|:---:|
| **Gemini Nano** | On-device neural inference via AICore | Probes `com.google.android.aicore`. If missing, gracefully falls back. | **HONEST FALLBACK** |
| **Gemma** | Local on-device LLM inference via LiteRT / MediaPipe | Checks for weights file `filesDir/models/gemma-2b-it-cpu.bin`. If absent, gracefully falls back. | **HONEST FALLBACK** |
| **Deterministic Engine** | Clinical rule-based assessment | Always READY. 5 evaluated rule archetypes (Thermal, Low Memory, Rapid Battery Drain, Hostile Sockets, Baseline Equilibrium). | **EFFECTIVE (ACTIVE)** |

---

## 7. FULL VERIFICATION METRICS

- **Test Suite Results**: `103 tests, 0 failures, 0 errors, 0 skipped` across 17 test suites.
- **Contract Test Coverage**:
  - `AndroidProviderContractTest`: 8 tests (Battery, Memory, Network, Inventory, AppOps).
  - `InProcessEndToEndContractTest`: 3 tests (Degradation Causal Flow, AppOps Guard, Benign Package).
  - `ImmuneBusContractTest`: 3 tests (Ordering, Role Counts, Storm Velocity Detection).
  - `BiomimeticAdversarialImmuneTest`: 9 tests (Adversarial injections, stale rejection, empty evidence denial).
  - `BiomimeticImmuneSystemTest`: 10 tests (12-stage pipeline and multi-agent coordination).
  - `HonestyTest`: 7 tests (Boundary enforcement, permission honesty).
- **Network Posture**: Clean. Manifest removes `android.permission.INTERNET` (`tools:node="remove"`). Zero external socket egress.
- **Local Persistence Posture**: In-memory repository with bounded ring buffers. No unencrypted disk leakage.
