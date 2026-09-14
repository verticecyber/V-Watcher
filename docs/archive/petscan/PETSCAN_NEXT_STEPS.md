# PETSCAN — Next Steps (2026-09-12)

## MUST BUILD

- G1: integration tests for `refreshRealTelemetry` (score bands, fail-closed, homeostasis override), sim/exam state machines, isolate/release/resolve semantics. Reuse `petscan/PetScanTraceTest` harness pattern.
- G10: memory cap + TTL + user delete path.

## MUST INTEGRATE

- G3: single reasoning entry (Dendritic XOR DecisionCell, not both); single pipeline (retire or merge legacy cells).
- G2/G7: feed `HomeostasisEvaluation` + `forceDeterministic` into guardrails/router; or explicitly demote homeostasis to diagnostics.
- G4: wire NK integrity probes (or delete cell + corruption dimension).
- G6: real bus subscribers (at least regulatory + resolution events) or shrink `ImmunePayload` to the 2 used variants.

## MUST VALIDATE

- G13: UI-claim checklist — bind each of the 15 flagged strings to a backend field or reword (esp. `REAL DEVICE DATA` banner gating, `HOMEOSTATIC`/`ACTIVE` null-defaults, hardcoded Healthy/exam/0-threats, TLS/certified/signatures, decoy pipeline, `<40ms`/efficiency).
- G18/G19: device lab (Nano AICore states, Gemma weights, perf/battery, Doze, permission UX) + pre-launch report.
- G12/G14/G17/G20: one-line honesty fixes with tests (relabel `PREPARED`, split `isReal`, fix stale mirror, rename flags).

## SHOULD HARDEN

- G5 (per-incident cleanup proof), G11 (per-stage try/catch + guaranteed snapshot), G8 (threshold table), G9/G16 (dead-code sweep), threshold hysteresis (storm 30 vs 25, thermal flapping).

## OPTIONAL

- OSS license screen, CI workflow, tablet/foldable layouts, `rememberSaveable` pass, continuous-observe mode (foreground service + disclosure — product decision).

## EXTERNAL

- Play Console filings/listing/signing/12×14 (see `PLAY_CONSOLE_ACTIONS.md`); physical-device-only proofs; Gemma weight licensing.
