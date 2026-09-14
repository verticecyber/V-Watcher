# AGENTIC_PLAN_STANDARD - VWATCHER-IMPL-20260912

## Derived execution profile

```yaml
standard:
  schema: vertice.agentic_plan_standard.v1
  plan_id: VWATCHER-IMPL-20260912
  authority_order: [AUTH-OWNER]
  scope: G1-test-harness G2-G7-G21-consumption G3-G6-fusion G12-G14-G17-honesty G13-UI-binding G18-G19-device-validation
  non_scope: G4 G5 G8 G9 G10 G11 G15 G16 G20 neural-inference store-console perf-optimization
  plan_specification: IMPLEMENTATION_PLAN.md
  plan_specification_fingerprint: 4551668fd99ff22dfbdcf8771686ad299b978c7b4e1a5eb66228f1450628dc8
  plan_records_manifest: PLAN_RECORDS_MANIFEST.yaml
  manifest_fingerprint: 438bd45d53ad45c24d0007bfdaac80a1c5a3255aa22ff972ff65d2a95650
  canonical_transition_ledger: TRANSITION_LEDGER.jsonl
  canonical_evidence_registry: EVIDENCE_REGISTRY.jsonl
  progress_projection_record: PROGRESS_PROJECTION.json
  progress_projection_id: PROGRESS-VWATCHER-01
  work_projection_record: WORK_PROJECTION.json
  work_projection_id: WORK-VWATCHER-01
  status_mapping_ref: STATUS_MAPPING.yaml
  status_mapping_fingerprint: f97affec90d2bd7cc238115a32866b24d00e77742911e93b0927a688efaa2676
  plan_graph: PLAN_GRAPH.yaml
  graph_fingerprint: badf6e6904d9b6a594831b3fa1928268b641fb1cdab54dc15f329f14
  material_sources_fingerprint: f97affec90d2bd7cc238115a32866b24d00e77742911e93b0927a688efaa2676
  claim_ceiling_source: plan claim_ceiling plus CLAIM C-01 through C-06
```

This guide is derived from the canonical primary plan and introduces no independent permission, obligation, or status authority. Executors may rely on it only where it faithfully restates a requirement adopted by the primary plan or an applicable external authority. A conflict, omission, or unexplained strengthening yields `DECIDE` against that canonical source. Conversation, memory, summaries, and dashboards are not status authority.

## Adopted execution requirements

| ID | Exact restatement | Canonical source ref |
|---|---|---|
| `AA-0` | No production placeholder, fake success, mock replacement, silent exception, empty pass, or weakened assertion. | `IMPLEMENTATION_PLAN.md::AA-0` |
| `AA-1` | Verify target paths, symbols, callers, consumers, schemas, and runtime identity before mutation. | `IMPLEMENTATION_PLAN.md::AA-1` |
| `AA-2` | Inventory governed primitives before introducing a new abstraction. | `IMPLEMENTATION_PLAN.md::AA-2` |
| `AA-3` | Bind material premises to complete sources, locators, fingerprints, freshness, lineage, and limitations. | `IMPLEMENTATION_PLAN.md::AA-3` |
| `AA-4` | Preserve external and authority contracts unless a separately authorized phase changes them. | `IMPLEMENTATION_PLAN.md::AA-4` |
| `AA-5` | Bound effects, define rollback, and append immutable transition and evidence events. | `IMPLEMENTATION_PLAN.md::AA-5` |
| `AA-6` | Test actual behavior, negative and failure cases, and the real boundary required by the claim. | `IMPLEMENTATION_PLAN.md::AA-6` |
| `AA-7` | Ingest canonical decisions and CI/runtime observations, invalidate affected descendants, and expose the next control outcome. | `IMPLEMENTATION_PLAN.md::AA-7` |

## Entry protocol

Before acting, the executor must:

1. Verify that the plan and graph both say `ACTIVE`, then verify progress, work state, canonical decision registry, referenced external authority records, and source fingerprints.
2. Confirm the assigned phase owns the relevant gaps and every dependency has the required state.
3. Re-run idempotent entry probes, require typed PASS `ARTIFACT` evidence for every graph-derived `input_artifacts` item, and reconcile code, tests, runtime, and projections.
4. Confirm allowed targets, forbidden effects, rollback, acceptance oracle, evidence destination, and claim ceiling.
5. Stop with `DECIDE` for a sovereign choice, conflict, ambiguous mapping, or authority ambiguity. Use `WAIT_FOR_UNBLOCK` only for a named external dependency with explicit unblock evidence.

NOTE: current bundle status is DRAFT with empty ledgers. No START, mutation, or record append is authorized until the owner issues an ACTIVE binding (fresh bundle per v1). This guide restates that gate; it does not grant it.

## Phase state law

Documents use `DRAFT`, `APPROVED`, `ACTIVE`, `BLOCKED`, `SUPERSEDED`, and `CLOSED`. Phases use `PENDING`, `IN_PROGRESS`, `BLOCKED`, `PHASE_FAIL`, `PHASE_PASS_WITH_LIMITATIONS`, and `PHASE_PASS`. Preserve the authority-native value beside the canonical value and fingerprint the deterministic mapping; use a declared identity mapping when both vocabularies match. An unmapped, lossy, or ambiguous state yields `DECIDE`; historical and sovereign records are never rewritten.

- `PROCEED`: all conjunctive gates pass; append the transition and continue only to a declared successor.
- `REPAIR`: a bounded, authorized defect has a deterministic repair path; append failure evidence before retry.
- `DECIDE`: a sovereign choice, contract interpretation, or authority grant is required.
- `WAIT_FOR_UNBLOCK`: an external dependency prevents meaningful progress; set phase state `BLOCKED` and name the unblock evidence.
- Missing, stale, ambiguous, skipped, or unauthorized input is never PASS.
- `PHASE_PASS` requires all named local and cumulative gates; it enables only the phase claim ceiling.

## Phase quick reference (restated, non-authoritative)

| Phase | Owns | Mutates | Entry | Exit | Ceiling |
|---|---|---|---|---|---|
| PH-01 harness | G1 | tests only | suite green | new VM tests green | L2 pinning |
| PH-02 honesty | G12 G14 G17 | boundary telemetry engine-mirror | suite green | honesty tests green | L2 truth |
| PH-03 consumption | G2 G7 G21 | guardrails engine | suite green + T-EXIT-02 | gating tests green | L2 gating, no actuation |
| PH-04 fusion | G3 G6 | VM orchestrator channel | suite green + T-EXIT-03 | single-path tests + line budget | L2 integration, no perf |
| PH-05 UI binding | G13 | screens + copy | suite green + T-EXIT-04 | 15-string tests green | L2 binding |
| PH-06 device | G18 G19 | nothing | device probe + T-EXIT-05 | device notes | observations only |
| PH-CLOSE | none | nothing | all six exits | GATE-CLOSE ALL_PASS | candidacy only |

## Evidence and closure law (restated)

Statement kind, verification state, source grade, and lineage stay independent. Mocks certify only the boundary they exercise. Every record binds a graph subject plus a raw artifact with byte hash. START needs PASS inputs; PASS needs produced outputs. Closure needs replayed PASS for every gap closer plus current fingerprints plus projection parity. See primary plan phase evidence schema for the closed keyset.
