# Phase Evaluation Brief — VWATCHER-IMPL-20260912 (for external review)

> Non-authoritative reading aid. Authority: `IMPLEMENTATION_PLAN.md` + `PLAN_GRAPH.yaml` (both validators green).

## Canonical order (linear)

PH-01 → PH-02 → PH-03 → PH-04 → PH-05 → PH-06 → PH-CLOSE. No phase depends on a later mutation. D-FLAGS adds the wire-or-rename decision to PH-03.

## Bundle status

DRAFT, pre-genesis, all 7 phases PENDING, ledgers empty, next outcome DECIDE. Graph 70 nodes / 159 edges, 0 violations. No execution authorized.

## Phase table (evaluate each row independently)

| Phase | Owns | Mutates | Entry | Exit gate (must PASS) | Falsifier / top risk | Ceiling |
|---|---|---|---|---|---|---|
| PH-01 harness | G1 | tests only (`app/src/test/.../viewmodel/`) | suite 30/30 | new scoring/fail-closed/sim-gating/quarantine/clobber tests green + suite green | tests that pass without touching VM logic (must assert production paths) | L2 pinning |
| PH-02 honesty | G12 G14 G17 | boundary, 2 telemetry providers, engine mirror | suite green | intent-sent-or-PREPARED, isReal split, mirror tests green | shadow-only intent proof overstated as device proof | L2 truth |
| PH-03 consumption | G2 G7 G21 | guardrails, engine wiring | suite green | macroState veto, forceDeterministic, flag-reader tests green | any actuator/OS-capability change (forbidden) | L2 gating, no actuation |
| PH-04 fusion | G3 G6 | VM, orchestrator, channel | suite + T-EXIT-01 | single case authority + single reasoning entry + fallback intact + VM ≤1218 lines | trace-sequence divergence vs pre-fusion baseline | L2 integration, no perf |
| PH-05 UI binding | G12→G13 (G13) | screens + VM strings only | suite + T-EXIT-01 | 15/15 string dispositions tested | screenshot or copy judgment without semantics test | L2 binding |
| PH-06 device | G18 G19 | NOTHING (validate only) | device probe + T-EXIT-05 | device notes recorded (Nano states, latency, quota, perf, Doze) | empty `adb devices` → must BLOCK/WAIT_FOR_UNBLOCK, never PASS-vazio | observations only |
| PH-CLOSE | — | nothing | all six exits | GATE-CLOSE ALL_PASS + SOT re-verification | stale fingerprint or skipped gate | candidacy only |

## Canonical decisions to audit

- D-FUSION: BiomimeticImmuneSystem canonical; legacy DecisionCell entry retired after parity. Challenge: is retiring (vs keeping both with authority flag) justified by clobber evidence (VM:389)?
- D-HOMEO: deny-only coupling, no actuator wiring. Challenge: does any SOT evidence support actuator coupling today? (No — flags write-only, G21.)

## Contracts every phase must hold

CTR-NO-MOCK (real collaborators + negative cases), CTR-FAIL-CLOSED (selected-vs-actual + fallback intact), CTR-CEILING-L2 (no hardware/store claims), CTR-BUDGET (VM ≤1218, new files ≤500).

## Out of scope (must NOT appear in execution)

G4 G5 G8 G9 G10 G11 G15 G16 G20, neural inference, store/console filings, perf optimization.

## What a reviewer should verify

1. Each gap has exactly one owner and a closure oracle (graph metrics confirm, recheck after edits).
2. Phase blocks match graph values (fingerprints in headers).
3. No phase claims beyond its ceiling; PH-06 cannot PASS without device evidence.
4. Evidence schema closed keyset honored at execution; producer = canonical appender (human-operator).

## Execution status (2026-09-12)

PH-01 PHASE_PASS (canonical replay: 5 evidence, 2 transitions, bundle RECORDS valid).
PH-02..PH-06 PENDING. Next authorized: PH-02 START after T-ENTRY-SUITE re-run.

## Adversarial verdict on PH-01 (2026-09-12, read-only + green rerun)

Harness green (6/6), lintDebug 0 errors with zero findings on harness and ViewModel files.
Falsification attempts that FAILED (harness holds): suite determinism across reruns, real collaborators
(no doubles), quarantine text binding, exam live-number interpolation, fail-closed mapping under
Robolectric DEGRADED, preservation branch taken deterministically in nominal runs.
Findings (do NOT invalidate closure; all executing assertions are true):
- F1 vacuous `assertNotNull(s.condition)` — field is non-nullable; asserts nothing. Replace with
  score-band/condition consistency assertion.
- F2 clobber test has a conditional branch that skips the preservation assert when system takes over;
  deterministic-today (branch taken) but silently weakens on env change. Split into two deterministic tests.
- F3 `else -> Unit` passes vacuously when macroState is HOMEOSTATIC/WATCH. Assert the mapping table
  exhaustively instead.
- F4 import ordering (2) plus `org.junit.Assert.*` wildcard vs Kotlin/Google style; Truth library absent
  (repo-wide convention gap, not a failure).
- F5 timeouts magic; resolveCase/selectors uncovered; `@Config sdk 34` vs target 36 (repo convention).
Modularity: harness cohesive (154 lines, mirrored package); ViewModel remains a god object (1218 lines,
telemetry+scoring+sim+exam+quarantine+seed) — extraction already scoped to PH-04 split boundary.
Recommendation: harden F1-F3 via amendment re-execution of PH-01 (fresh bundle: A-01 bytes change),
or fold as PH-02 entry precondition. No production file implicated.

## Re-execution (bundle 04, 2026-09-12)

F1-F5 hardened (non-vacuous mapping asserts, split clobber, exhaustive table, ordered imports,
resolveCase test — 7 tests). Fresh bundle MANIFEST-VWATCHER-04 (plan r4/graph r4); prior chain frozen
in superseded/MANIFEST-03. Suite 37/37 green; PH-01 PHASE_PASS re-validated (5 evidence, 2 transitions).

## Execution status (2026-09-12, bundle 04)

PH-01 PHASE_PASS (5 evidence, revalidated). PH-02 PHASE_PASS (4 evidence: entry 42/42, exit 5/5 honesty, output bytes; G12 launch verified by shadow, G14 split, G17 mirror; READY-branch device-only).
PH-03..PH-06 PENDING. Next authorized: PH-03 START after T-ENTRY-SUITE re-run.

## Execution status (2026-09-12, bundle 04)

PH-01 PHASE_PASS (5 evidence). PH-02 PHASE_PASS (4 evidence). PH-03 PHASE_PASS (5 evidence:
entry 42/42, entryb 5/5 honesty re-verified, exit 8/8 consumption, output bytes; G12 launch verified
by shadow, G14 split, G17 mirror; READY-router consultation device-only; production feed residual
to PH-04 recorded). PH-04..PH-06 PENDING. Next authorized: PH-04 START after T-ENTRY-SUITE
plus T-EXIT-03 re-runs.

## Adversarial validation of PH-03 implementation (2026-09-12, temp probes, file removed after)

4/4 probes green, then deleted (bundle bytes untouched; suite 50/50 after removal):
- release() resets context to neutral (reset path proven, was untested).
- Context reversible neutral->deny->neutral (no latch).
- Router path with stressed context still falls back on model-unavailability, NOT guard denial —
  proves guard consult only happens with READY backends; production feed is genuinely PH-04 work.
- 500 concurrent updateContext/evaluate ops: no throw, always decided (Volatile + Synchronized hold).
Integration finding for PH-04: feed path exists without casts — interface exposes
`guardrails: ResourceGuardrails`, so the orchestrator can call
`engine.guardrails.updateContext(...)` directly (D-FLAGS wire-up unblocked).
No production mutation from this exercise.

## Execution status (2026-09-12, bundle 04)

PH-01 PHASE_PASS (5). PH-02 PHASE_PASS (4). PH-03 PHASE_PASS (5). PH-04 PHASE_PASS
(5 evidence: entry 54/54, entryb 8/8 consumption re-verified, exit 4/4 fusion, output bytes;
merge preserves manual cases, single entry spends once, ledger bounded at 8, feed mirrors pipeline;
post-fusion trace matches pre-fusion baselines; VM exactly 1218 lines; lint 0 errors).
PH-05..PH-06 PENDING. Next authorized: PH-05 START after T-ENTRY-SUITE plus T-EXIT-04 re-runs.

## Adversarial validation of PH-04 implementation (2026-09-12, temp probes, file removed after)

3/3 probes green, then deleted (bundle bytes untouched; suite 54/54 after removal):
- Double refresh idempotent: no duplicate case ids across cycles.
- Forced stress incident surfaces in UI and is not silently resolved by clean refreshes.
- One refresh spends deterministic-only: reasoningCallsCount flat, deterministicCallsCount advances.
Static kills confirmed: DecisionCell class unreferenced in production; legacy `reason(bundle,battery)`
overload caller-less (both flagged for G9 sweep, not this phase). `latestAssessment` field confirmed
UI-unread, so the null-nominal delta has zero render effect.
Residual observations (no action): boundary throttle flag is a plain var read cross-dispatcher
(consider @Volatile in a hardening pass); metric semantic shift documented — fused path increments
the deterministic-skip counter even with candidates present, which matches "inference avoided" truth.
No production mutation from this exercise.

## Execution status (2026-09-12, bundle 04)

PH-01..PH-04 PHASE_PASS (see above). PH-05 PHASE_PASS (5 evidence: entry 54/54, entryb
fusion re-verified, exit 9/9 binding incl. Compose semantics render test, output bytes; all 15
claims bound/conditional/reworded; tripwire caught one missed default mid-phase; VM held at 1218;
lint 0 errors on touched files; MEM-0035 repo copy recorded residual). PH-06 PENDING.
Next authorized: PH-06 START after T-ENTRY-DEVICE probe (expected BLOCKED without hardware).

## Adversarial validation of PH-01..PH-05 implementation (2026-09-12, brutal pass)

Method: diff-vs-snapshot review, sensitivity swap (old VM + new assertion), temp probes (deleted
after), hash re-verification of evidenced bytes.
- Sensitivity PROVEN: merge assertion FAILS on pre-fusion VM ("manual preserved alongside system"),
  PASSES post-fusion. Tests detect the bug they claim, not just green noise.
- Evidenced bytes intact: A-04 hash matches EV-OUT-04 record after all operations.
- VM restored byte-identical after swap experiment (sha 37672f2f…); temp probes removed; suite 63/63.
- Meta-limits admitted: L2-only (shadow veto fires nominally — feed fidelity test covers it, not neutrality);
  channel ring-buffer cap verified by code read, not runtime probe; UI render covered by 1 compose test;
  perf/battery/device absent by ceiling; regulatory veto path in production feed untriggered under L2.
- Residuals routed, not hidden: DecisionCell class + legacy reason() overload → G9 sweep; boundary flag
  plain-var race → hardening backlog; MEM-0035 repo copy → noted in A-05 reflection.
No production mutation from this exercise. No gaps found that invalidate any closed phase.

## Post-closure hardening pass (2026-09-12, owner-ordered, no theater)

Fixed for real, verified by suite 63/63 + lint 0 errors:
- MEM-0035 "<35ms" in LocalImmuneMemoryRepository → latency-unmeasured wording (last UI-adjacent overclaim).
- Boundary flags thread-safe: ConcurrentHashMap set + @Volatile throttle boolean.
- G9 sweep executed: DecisionCell class deleted, legacy reason(bundle,battery) overload + synthetic
  observation builder deleted, 7 dead imports removed. Zero callers in main and tests (grep-proven).
Governance note: bundle evidence attaches test/source files, none touched by this pass, so prior
chains revalidate unchanged (RECORDS valid re-confirmed). Residual: ObservationBundle/ReasoningResult
types may now be unreferenced (G9 remainder); legacy display cells in VM retired in PH-04 already.
