# PETSCAN — Evidence Index (2026-09-12)

## Methods

- 4 parallel code probes (full-file reads, caller greps): telemetry/sentinel/homeostasis; immune/bus/memory; reasoning/router/boundary; viewmodel/UI/tests.
- Static scans: `tools/validation/petscan/static_scan.sh` → `tools/validation/petscan/static_scan_output.txt` (definitions, TODO scan — 1 hit `BatteryProvider.kt:56` default const, thresholds, confidences, payload construction counts, collectors, `startActivity` sites, timings).
- Live runtime (L2 Robolectric, no device): `app/src/test/java/com/example/petscan/PetScanTraceTest.kt` (kept as harness — 3 tests, all green).

## Trace outputs (verbatim, from test XML `system-out`)

```text
PETSCAN nominal observationId=obs_1789238031091_411807d9 observeMs=181 macroState=DEGRADED confidence=60 busTotal=2 busRate=2.0 storm=false dropped=0 reasoningCalls=0 telemetryDispatched=0
PETSCAN storm dispatched=40 wallMs=99 total=40 rate=40.0 storm=true dropped=0
PETSCAN router selected=GEMINI_NANO actual=DETERMINISTIC mode=deterministic status=FALLBACK fallback=GEMINI_NANO_UNAVAILABLE latencyMs=1 wallMs=9 classification=SUSPICIOUS confidence=88
```

Note: nominal `DEGRADED/60` is a Robolectric shadow artifact (a provider reports ERROR under JVM) — it usefully proves the fail-closed path fires for real. `reasoningCalls=0` proves the escalation gate idles correctly (no evidence → no inference spend).

## Coverage map (feature → test class)

Substrates (sentinel/router/guardrails/channel/deterministic-honesty): `HandoffFoundationTest` (5). Cells + pipeline + adversarial (stale/lowConf/storm/RAM/battery/kill/time): `BiomimeticImmuneSystemTest` (10) + `BiomimeticAdversarialImmuneTest` (9). Wiring gaps (VM scoring/sim/quarantine/exam/memory-gates/UI defaults): **none** — see G1. Total suite after harness: 30/30 green; `lintRelease` 0 errors.

## Key file:line anchors

- Pipeline: `BiomimeticImmuneSystem.kt:79-298` (order), `:100,106` (only dispatches), `:73` (no-op collector), `:191` hardcoded `EXPERIMENTAL_SOCKETS`.
- Router: `ReasoningRouter.kt:38-100` select, `:106-217` execute, `:83-89` reason mapping; canned: `GeminiNanoBackend.kt:122-145`, `GemmaBackend.kt:120-141`.
- Boundary: `AndroidRealityBoundary.kt:40-175`; intent-not-sent `:114-130`; honest refusal `:132-148`.
- Homeostasis: `HomeostasisModel.kt:88-230` (dead `RECOVERING` `:210-214`); consumerlessness: only `VWatcherViewModel.kt:360-377` UI mapping.
- Memory: `LocalImmuneMemoryRepository.kt:32-202` (gates `:111-156`, no TTL); stale Room claims in `docs/evidence/historical/REAL_DEVICE_VALIDATION_REPORT.md:41,99,115,185`, `docs/evidence/validation/CLAIM_PROOF_MATRIX.json:17`, `docs/evidence/validation/MEMORY_VALIDATION.json:2`, `docs/evidence/validation/LIFECYCLE_VALIDATION.json:11`, `docs/evidence/historical/TRUTH_FIRST_VALIDATION_REPORT.md:49-50`.
- VM: `VWatcherViewModel.kt:116-403` (untested core), `:493-749` (sim bypass), `:754-844` (in-app flags), `:880-1217` (seed state).
- Thresholds: guardrails `15%/92%/2.5s`; homeostasis `15%/92%/250MB/25msg/200MB-heap`; PRR `42°/200MB`; baseline/deterministic `42°/88%/50%`; T-helper `85`; cytotoxic `80`; memory gates `80%/30s`; bus storm `30/s`; sentinel staleness `30s`; router timeout `5s`.

## Environment ceiling

L2 only. L3–L5 (emulator/device/instrumentation) unavailable: no device, no AVD. Nano/Gemma/physical-sensor/Doze/battery claims all `NOT_AVAILABLE_IN_ENVIRONMENT`.
