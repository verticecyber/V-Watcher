# Execution Plan - V-Watcher post-SOT implementation (G1-G21 core)

```yaml
document:
  schema: vertice.plan_specification.v1
  id: VWATCHER-IMPL-20260912
  version: 1.0
  revision: 4
  status: ACTIVE
  repository: /media/juan/DATA/V-watcher
  created_at: "2026-09-12"
  scope: [G1-test-harness, G2-G7-G21-consumption, G3-G6-fusion, G12-G14-G17-honesty, G13-UI-binding, G18-G19-device-validation]
  non_scope: [G4-NK-wiring, G5-resolution-redesign, G8-threshold-values, G9-dead-code-sweep, G10-memory-TTL, G11-pipeline-hardening, G15-stale-docs-done, G16-channel-flows, G20-flag-semantics-beyond-G21, neural-inference, store-console-filings, perf-optimization]
  claim_ceiling: active execution; phase claims per exit gates only, no closure claim
authority_order: [AUTH-OWNER]
status_contract:
  document_lifecycle_states: [DRAFT, APPROVED, ACTIVE, BLOCKED, SUPERSEDED, CLOSED]
  canonical_phase_states: [PENDING, IN_PROGRESS, BLOCKED, PHASE_FAIL, PHASE_PASS_WITH_LIMITATIONS, PHASE_PASS]
  authority_native_vocabulary: [BLOCKED, IN_PROGRESS, PENDING, PHASE_FAIL, PHASE_PASS, PHASE_PASS_WITH_LIMITATIONS]
  status_mapping_ref: STATUS_MAPPING.yaml
  status_mapping_fingerprint: c4c0c029649c642710a99ad2f5b26ef1ac871613f1e1158468b9153122dcb434
  status_mapping_authority: referenced_file_only
  status_mapping_parity_gate: python3 -B /home/juan/.config/opencode/skills/vertice-planning/scripts/validate_plan_records.py docs/archive/plans/20260912/PLAN_RECORDS_MANIFEST.yaml
  unmapped_or_ambiguous_state: DECIDE
plan_records:
  manifest: PLAN_RECORDS_MANIFEST.yaml
  canonical_designation_source: manifest
  conflict_control_outcome: DECIDE
traceability_graph:
  required: true
  plan_graph: PLAN_GRAPH.yaml
  graph_fingerprint: f88be9b10b0635a0fb7283fd5ca5bae5ed0d85d93dc61e596ee82b733091b036
  schema: vertice.plan_graph.v1
  validator: python3 -B /home/juan/.config/opencode/skills/vertice-planning/scripts/validate_plan_graph.py docs/archive/plans/20260912/PLAN_GRAPH.yaml
precision_contract:
  canonical_node_registry: PLAN_GRAPH.yaml::nodes
  canonical_edge_registry: PLAN_GRAPH.yaml::edges
  registry_projection:
    SOURCE: [SRC-AAB, SRC-RECON, SRC-SOT, SRC-THRESH, SRC-TRACE]
    FACT: [FACT-AAB, FACT-SUITE30, FACT-TRACE2X, FACT-VM1218]
    GAP: [GAP-G1, GAP-G12, GAP-G13, GAP-G14, GAP-G17, GAP-G18, GAP-G19, GAP-G2, GAP-G21, GAP-G3, GAP-G6, GAP-G7]
    DECISION: [DEC-FLAGS, DEC-FUSION, DEC-HOMEO]
    AUTHORITY: [AUTH-OWNER]
    CONTRACT: [CTR-BUDGET, CTR-CEILING-L2, CTR-FAIL-CLOSED, CTR-NO-MOCK]
    PHASE: [PH-01, PH-02, PH-03, PH-04, PH-05, PH-06, PH-CLOSE]
    TARGET: [T-BOUND12, T-CHAN04, T-ENG03, T-ENG17, T-GUARD03, T-ORCH04, T-TELE14, T-TESTS, T-UI05, T-VM04, T-VM05]
    TEST: [T-ENTRY-DEVICE, T-ENTRY-SUITE, T-EXIT-01, T-EXIT-02, T-EXIT-03, T-EXIT-04, T-EXIT-05, T-EXIT-06]
    GATE: [GATE-CLOSE]
    ARTIFACT: [A-01, A-02, A-03, A-04, A-05, A-06, P-SOT, P-TRACE]
    EXTERNAL_DEPENDENCY: [EXT-DEVICE]
    CLAIM: [C-01, C-02, C-03, C-04, C-05, C-06]
    RISK: []
  narrative_is_authoritative: false
  file_budgets:
    production_source_at_most_lines: 500
    governed_document_target_below_lines: 1500
    governed_document_hard_below_lines: 3000
specification_contract:
  schema: vertice.plan_specification_contract.v1
  required_sections:
    - source_registry
    - fact_ledger
    - contract_registry
    - gap_traceability
    - authority_scope
    - decision_registry
    - execution_requirements
    - status_mapping
    - execution_topology
    - acceptance_matrix
    - threat_register
    - refutation_register
    - product_graph
    - operational_runbook
    - phase_evidence_schema
    - file_budgets
  product_graph:
    applicability: NOT_APPLICABLE
    evaluated_trigger: plan mutates no persisted data knowledge reasoning process causal or hypergraph artifact; immune event flow is ephemeral transport without graph semantics
    authority_ref: AUTH-OWNER
    evidence_refs: [SRC-SOT]
    limitations: [L2-only assessment]
```

## Source registry

| ID | Locator | Revision/hash | Observed at | Lineage | Grade | Freshness/invalidation | Limitations | Permitted use |
|---|---|---|---|---|---|---|---|---|
| SRC-SOT | sources/SOT.md | baseline frozen 2026-09-12 16:00 -03:00 | sha256:4b2359ebd23231a0efd586c874c9de39a7a2b5eed9a653a9e154e9c948dce555 | 2026-09-12T19:38:28Z | LIN-PETSCAN-ADVERSARIAL | LOCAL_SOURCE | INVALIDATE_ON_ANY_PRODUCT_MUTATION; expires 2026-12-12 | L2-only; UI unbound | gap premises and scope |
| SRC-RECON | sources/RECONCILIATION.md | sha256:cdb86ee93ef8bb5c743f167f8aff5a2fd44aec033414954dde5d7fa1b19ca445 | 2026-09-12T19:38:28Z | LIN-PETSCAN-ADVERSARIAL | LOCAL_SOURCE | INVALIDATE_ON_ANY_PRODUCT_MUTATION; expires 2026-12-12 | point-in-time greps | refined gaps G2 G3 G14 G20 G21 |
| SRC-THRESH | sources/THRESHOLDS.md | sha256:c90cc6aedfc72b986cc19133ad10e279408fe41975aa880381b85172676d56ef | 2026-09-12T19:38:28Z | LIN-PETSCAN-ADVERSARIAL | LOCAL_SOURCE | INVALIDATE_ON_ANY_PRODUCT_MUTATION; expires 2026-12-12 | inventory only | threshold mapping PH-03 |
| SRC-TRACE | sources/TRACE_HARNESS.kt | sha256:3dd79cdd41de30ed9790c0b95585fa320b7fbcf24a69dc004419c80cead82367 | 2026-09-12T19:38:28Z | LIN-L2-RUNTIME | LOCAL_RUNTIME | INVALIDATE_ON_ANY_PRODUCT_MUTATION; expires 2026-12-12 | Robolectric shadows | regression baseline |
| SRC-AAB | sources/app-release.aab | sha256:28dd9bedfbd27c1a3ce08bd1986bf8853cdf83a695520e519d93b2000f6af72e | 2026-09-12T19:38:28Z | LIN-RELEASE-PIPELINE | LOCAL_RUNTIME | INVALIDATE_ON_REBUILD; expires 2026-12-12 | test-signed | release parity anchor |

## Epistemic fact ledger

| ID | Statement kind | Verification state | Verified statement or bounded inference | Source IDs/lineage | Material effect | Impact | Sufficiency oracle/result | Limitation | Plan use |
|---|---|---|---|---|---|---|---|---|---|
| FACT-SUITE30 | FACT | VERIFIED | suite 30/30 green 2026-09-12 | SRC-TRACE/LIN-L2-RUNTIME | entry baseline | HIGH | ORACLE-SUITE30/SATISFIED | L2; VM uncovered | entry gates |
| FACT-TRACE2X | FACT | VERIFIED | pipeline trace reproduced 2x with fail-closed storm-flag fallback | SRC-TRACE/LIN-L2-RUNTIME | regression oracle | HIGH | ORACLE-TRACE2X/SATISFIED | ranges not SLAs | fusion/consumption oracles |
| FACT-AAB | FACT | VERIFIED | AAB validated 3-perm manifest clean dex | SRC-AAB/LIN-RELEASE-PIPELINE | parity anchor | MEDIUM | ORACLE-AAB/SATISFIED | test-signed | device phase baseline |
| FACT-VM1218 | FACT | VERIFIED | ViewModel 1218 lines zero tests | SRC-SOT/LIN-PETSCAN-ADVERSARIAL | file budget | HIGH | ORACLE-VM1218/SATISFIED | lines only | fusion budget |

## Contract registry

| ID | Owner | Implementation-independent predicate | Violation behavior | Verification gate | Permitted claim |
|---|---|---|---|---|---|
| CTR-NO-MOCK | AUTH-OWNER | new tests use real collaborators under Robolectric with negative cases | FAIL_CLOSED | T-EXIT-01 | L2 behavioral pinning |
| CTR-FAIL-CLOSED | AUTH-OWNER | selected-vs-actual bookkeeping and deterministic fallback intact after mutation | FAIL_CLOSED | T-EXIT-04 | fallback preserved |
| CTR-CEILING-L2 | AUTH-OWNER | no hardware perf battery or store claims beyond L2 evidence | FAIL_CLOSED | T-EXIT-06 | bounded claims |
| CTR-BUDGET | AUTH-OWNER | ViewModel ends mutating phases at or below 1218 lines; no new prod file over 500 | FAIL_CLOSED | T-EXIT-04 | size discipline |

## Gap traceability

| Gap ID | SOT source | Severity | Exactly one owner phase | Disposition | Closure oracle/artifact | Residual claim limit |
|---|---|---:|---|---|---|---|
| GAP-G1 | SRC-SOT | P0 | PH-01 | implement | T-EXIT-01 / A-01 | L2 pinning only |
| GAP-G12 | SRC-SOT | P1 | PH-02 | implement | T-EXIT-02 / A-02 | L2 truth only |
| GAP-G14 | SRC-SOT | P2 | PH-02 | implement | T-EXIT-02 / A-02 | L2 truth only |
| GAP-G17 | SRC-SOT | P2 | PH-02 | implement | T-EXIT-02 / A-02 | L2 truth only |
| GAP-G2 | SRC-SOT | P0 | PH-03 | implement | T-EXIT-03 / A-03 | L2 gating; no actuation |
| GAP-G7 | SRC-SOT | P1 | PH-03 | implement | T-EXIT-03 / A-03 | L2 gating only |
| GAP-G21 | SRC-RECON | P2 | PH-03 | implement | T-EXIT-03 / A-03 | signal defined; no OS claim |
| GAP-G3 | SRC-SOT | P0 | PH-04 | implement | T-EXIT-04 / A-04 | L2 integration; no perf |
| GAP-G6 | SRC-SOT | P1 | PH-04 | implement | T-EXIT-04 / A-04 | L2 integration only |
| GAP-G13 | SRC-SOT | P1 | PH-05 | implement | T-EXIT-05 / A-05 | L2 binding; no store copy |
| GAP-G18 | SRC-SOT | P1 | PH-06 | validate-or-block | T-EXIT-06 / A-06 | observations; unblock evidence |
| GAP-G19 | SRC-SOT | P1 | PH-06 | validate-or-block | T-EXIT-06 / A-06 | observations; unblock evidence |

## Authority and scope fence

| Authority ID | Sovereign actor/source | Exact prospective scope | Allowed effects | Forbidden effects | Expiry/revocation | Evidence |
|---|---|---|---|---|---|---|
| AUTH-OWNER | local-agent (designated by repository owner 2026-09-12) | this plan revision phases targets and gates; no external commitment | START EVIDENCE PASS RETRY BLOCK FAIL INVALIDATION UNBLOCK | store submission production signing public URLs legal attestation | expires 2026-12-12; revocation by owner statement | plan adoption (this document) |

Cross-repo edges: none (single repository; toolchain at /media/juan/DATA/.toolchains is environment, not source).

## Canonical decision registry

| Decision ID | Exact question | Alternatives considered | Adopted decision | Sovereign authority ref | Evidence/source refs | Effective revision/date | Graph effects |
|---|---|---|---|---|---|---|---|
| D-FUSION | Which decision path survives fusion? | keep-dual; rewrite-VM-first; canonical-B | Canonical path is BiomimeticImmuneSystem via Dendritic reasoning entry; legacy DecisionCell entry retired after parity; display cells retained | AUTH-OWNER | SRC-SOT §3-5, SRC-RECON clobber | r1 2026-09-12 | DEC-FUSION; constrains PH-04 |
| D-HOMEO | How is homeostasis consumed? | actuator-coupling; diagnostics-demotion; gating-only | macroState plus STRESSED plus forceDeterministic gate escalation-deny in guardrails; no actuator wiring | AUTH-OWNER | SRC-SOT §8, SRC-THRESH | r1 2026-09-12 | DEC-HOMEO; constrains PH-03 |
| D-ACTIVATE | Activate bundle and designate appender? | stay-DRAFT vs activate-with-human-appender vs activate-with-local-agent | activate now with local-agent appender per owner authorization 2026-09-12 | AUTH-OWNER | owner authorization message 2026-09-12 | r2 2026-09-12 | none; enables START |
| D-REHARDEN | Re-execute PH-01 after F1-F5 harness hardening? | keep-closed vs amend-and-reexecute | amend A-01 and re-execute PH-01 under fresh bundle MANIFEST-VWATCHER-04; prior chain frozen in superseded/MANIFEST-03 | AUTH-OWNER | adversarial verdict 2026-09-12 + owner order | r4 2026-09-12 | none; re-runs PH-01 |
| D-FLAGS | Wire readers for boundary flags, or rename them diagnostic-only? | wire-into-guardrails; rename-diagnostic-only | wire readers into guardrail gating; rename/reclassify recorded via phase DECIDE only if wiring proves vacuous, flags must not remain write-only | AUTH-OWNER | SRC-RECON G21 | r1 2026-09-12 | DEC-FLAGS; constrains PH-03 |

## Adopted execution requirements

| ID | Normative requirement | Canonical authority/source ref | Deterministic gate | Failure/control outcome |
|---|---|---|---|---|
| `AA-0` | No production placeholder, fake success, mock replacement, silent exception, empty pass, or weakened assertion. | AUTH-OWNER | T-EXIT-01 | PHASE_FAIL/REPAIR |
| `AA-1` | Verify target paths, symbols, callers, consumers, schemas, and runtime identity before mutation. | AUTH-OWNER | T-ENTRY-SUITE | PHASE_FAIL/REPAIR |
| `AA-2` | Inventory governed primitives before introducing a new abstraction. | AUTH-OWNER | T-EXIT-04 | DECIDE on new abstraction |
| `AA-3` | Bind material premises to complete sources, locators, fingerprints, freshness, lineage, and limitations. | AUTH-OWNER | GATE-CLOSE | DECIDE on gap |
| `AA-4` | Preserve external and authority contracts unless a separately authorized phase changes them. | AUTH-OWNER | GATE-CLOSE | DECIDE on widening |
| `AA-5` | Bound effects, define rollback, and append immutable transition and evidence events. | AUTH-OWNER | phase exit gates | PHASE_FAIL/REPAIR |
| `AA-6` | Test actual behavior, negative and failure cases, and the real boundary required by the claim. | AUTH-OWNER | T-EXIT-01..06 | PHASE_FAIL/REPAIR |
| `AA-7` | Ingest canonical decisions and CI/runtime observations, invalidate affected descendants, and expose the next control outcome. | AUTH-OWNER | GATE-CLOSE | DECIDE or WAIT_FOR_UNBLOCK |
## Status mapping binding

The versioned file named by `status_contract.status_mapping_ref` is the sole status-mapping authority. This plan does not carry a second inline mapping. Preserve the native value beside the canonical value in every event and validate the file's semantic fingerprint plus its raw manifest binding. An unmapped, lossy, or ambiguous value yields `DECIDE`.

## Phase PH-01 - G1 test harness

```yaml
phase:
  graph_node: PH-01
  initial_state: PENDING
  depends_on: []
  owns_gaps: [GAP-G1]
  authority_refs: [AUTH-OWNER]
  graph_fingerprint: f88be9b10b0635a0fb7283fd5ca5bae5ed0d85d93dc61e596ee82b733091b036
  material_sources_fingerprint: ac5617eae9c2139f6482d2909bdbc31a293dbc4851e24764f748b3503416088b
  status_mapping_fingerprint: c4c0c029649c642710a99ad2f5b26ef1ac871613f1e1158468b9153122dcb434
  allowed_targets: [T-TESTS]
  forbidden_effects: [NEURAL_MODEL_WIRING, PRODUCTION_MUTATION, STRUCTURAL_REFACTOR]
  entry_gates: [T-ENTRY-SUITE]
  exit_gates: [T-EXIT-01]
  rollback_or_recovery: delete added test files under app/src/test
  input_artifacts: [P-SOT, P-TRACE]
  closure_artifacts: [A-01]
  claim_ceiling: ViewModel scoring sim quarantine exam paths are pinned by integration tests; no behavior claim beyond L2
```

State: (1) current behavior verified in SOT §3/§14 with zero structural refactor of the ViewModel — observability only; (2) delta is additive tests only; (3) mutations bounded to `app/src/test/java/com/example/viewmodel/` plus documented synthetic-copy inputs; (4) tests cross refreshRealTelemetry scoring fail-closed sim-gating exam-gating quarantine and clobber paths; adversarial: sim-bypass-must-fail test asserting repo rejects `[SIM]`; (5) focused `./gradlew testDebugUnitTest --tests "com.example.viewmodel.*" --tests "com.example.petscan.*"` plus cumulative full suite; oracles are PASS with zero failures; (6) raw evidence is JUnit XML under `app/build/test-results/` retained in tree-adjacent CI; fingerprints are toolchain §2; (7) rollback deletes added files; failure yields PHASE_FAIL/REPAIR; residual is coverage delta in exit evidence. Invariants preserved: 30/30 suite, trace baseline event order, fallback bookkeeping.

## Phase PH-02 - Honesty microfixes

```yaml
phase:
  graph_node: PH-02
  initial_state: PENDING
  depends_on: [PH-01]
  owns_gaps: [GAP-G12, GAP-G14, GAP-G17]
  authority_refs: [AUTH-OWNER]
  graph_fingerprint: f88be9b10b0635a0fb7283fd5ca5bae5ed0d85d93dc61e596ee82b733091b036
  material_sources_fingerprint: ac5617eae9c2139f6482d2909bdbc31a293dbc4851e24764f748b3503416088b
  status_mapping_fingerprint: c4c0c029649c642710a99ad2f5b26ef1ac871613f1e1158468b9153122dcb434
  allowed_targets: [T-BOUND12, T-ENG17, T-TELE14]
  forbidden_effects: [NEURAL_MODEL_WIRING, NEW_DEPENDENCY, SCOPE_WIDENING]
  entry_gates: [T-ENTRY-SUITE]
  exit_gates: [T-EXIT-02]
  rollback_or_recovery: restore snapshot copies under docs/archive/plans/20260912/snapshots/PH-02
  input_artifacts: [P-SOT]
  closure_artifacts: [A-02]
  claim_ceiling: intent launch semantics isReal semantics and backend mirror are truthful under L2
```

State: snapshot targets pre-mutation; G12 launches the settings intent or relabels `PREPARED` (one behavior, tested via shadow introspection); G14 splits availability from hardware-reality flag on the two proven sites; G17 mirrors computed selection; negative tests cover denied-path semantics; no fix may raise the claim ceiling — L2 shadows stay labeled; cumulative suite stays green. Invariants preserved: fallback bookkeeping, trace baseline.

## Phase PH-03 - Homeostasis consumption

```yaml
phase:
  graph_node: PH-03
  initial_state: PENDING
  depends_on: [PH-02]
  owns_gaps: [GAP-G2, GAP-G21, GAP-G7]
  authority_refs: [AUTH-OWNER]
  graph_fingerprint: f88be9b10b0635a0fb7283fd5ca5bae5ed0d85d93dc61e596ee82b733091b036
  material_sources_fingerprint: ac5617eae9c2139f6482d2909bdbc31a293dbc4851e24764f748b3503416088b
  status_mapping_fingerprint: c4c0c029649c642710a99ad2f5b26ef1ac871613f1e1158468b9153122dcb434
  allowed_targets: [T-ENG03, T-GUARD03]
  forbidden_effects: [ACTUATOR_WIDENING, NEURAL_MODEL_WIRING, NEW_OS_CAPABILITY]
  entry_gates: [T-ENTRY-SUITE, T-EXIT-02]
  exit_gates: [T-EXIT-03]
  rollback_or_recovery: restore snapshot copies under docs/archive/plans/20260912/snapshots/PH-03
  input_artifacts: [P-SOT]
  closure_artifacts: [A-03]
  claim_ceiling: macroState and regulatory outputs gate escalation under L2; no actuation claim
```

State: implements D-HOMEO strictly deny-only/gating (no SO actuation, no new capability); implements D-FLAGS by wiring boundary-flag readers into guardrail gating — rename/reclassify diagnostic-only only via recorded phase DECIDE if wiring proves vacuous, write-only end-state forbidden; plumbs `forceDeterministic`/`imposedCooldownMs`; falsifier is router-selection divergence pre/post on identical observations; trace baseline preserved modulo intended gating deltas.

## Phase PH-04 - Pipeline fusion

```yaml
phase:
  graph_node: PH-04
  initial_state: PENDING
  depends_on: [PH-03]
  owns_gaps: [GAP-G3, GAP-G6]
  authority_refs: [AUTH-OWNER]
  graph_fingerprint: f88be9b10b0635a0fb7283fd5ca5bae5ed0d85d93dc61e596ee82b733091b036
  material_sources_fingerprint: ac5617eae9c2139f6482d2909bdbc31a293dbc4851e24764f748b3503416088b
  status_mapping_fingerprint: c4c0c029649c642710a99ad2f5b26ef1ac871613f1e1158468b9153122dcb434
  allowed_targets: [T-CHAN04, T-ORCH04, T-VM04]
  forbidden_effects: [ARTIFICIAL_LINE_REDUCTION, BEHAVIOR_REGRESSION, NET_PRODUCTION_LINE_GROWTH, NEURAL_MODEL_WIRING]
  entry_gates: [T-ENTRY-SUITE, T-EXIT-03]
  exit_gates: [T-EXIT-04]
  rollback_or_recovery: restore snapshot copies under docs/archive/plans/20260912/snapshots/PH-04
  input_artifacts: [P-SOT]
  closure_artifacts: [A-04]
  claim_ceiling: single decision path with single case authority under L2; no perf claim
```

State: step zero reproduces the VM:389 clobber in A-04 before any mutation; then implements D-FUSION with BiomimeticImmuneSystem canonical, legacy entry retired only after parity proof with fallback/FAIL-CLOSED intact; single case authority defined (system incidents win with sim/manual namespaced, or adopted variant recorded); bus subscription for coordination events or contract shrink to used variants; trace-sequence comparison pre/post must match modulo intended consolidation; VM ≤1218 is budget ceiling not design goal — artificial line-cutting forbidden, split boundary (scoring/simulation siblings) only via DECIDE if growth required.

## Phase PH-05 - UI claim binding

```yaml
phase:
  graph_node: PH-05
  initial_state: PENDING
  depends_on: [PH-04]
  owns_gaps: [GAP-G13]
  authority_refs: [AUTH-OWNER]
  graph_fingerprint: f88be9b10b0635a0fb7283fd5ca5bae5ed0d85d93dc61e596ee82b733091b036
  material_sources_fingerprint: ac5617eae9c2139f6482d2909bdbc31a293dbc4851e24764f748b3503416088b
  status_mapping_fingerprint: c4c0c029649c642710a99ad2f5b26ef1ac871613f1e1158468b9153122dcb434
  allowed_targets: [T-UI05, T-VM05]
  forbidden_effects: [NEURAL_MODEL_WIRING, NEW_NAVIGATION, NEW_SCREEN]
  entry_gates: [T-ENTRY-SUITE, T-EXIT-04]
  exit_gates: [T-EXIT-05]
  rollback_or_recovery: restore snapshot copies under docs/archive/plans/20260912/snapshots/PH-05
  input_artifacts: [P-SOT]
  closure_artifacts: [A-05]
  claim_ceiling: every listed UI string is backend-bound or reworded under L2
```

State: immediate post-fusion priority; each of the 15 SOT §12 strings ends BACKEND_BOUND, CONDITIONALLY_TRUE, or REWORDED/REMOVED with per-string semantics tests (no screenshots); no static text may state dynamic state as fact; copy-only in ViewModel; banners and null-defaults first.

## Phase PH-06 - Device validation

```yaml
phase:
  graph_node: PH-06
  initial_state: PENDING
  depends_on: [PH-02, PH-03, PH-04, PH-05]
  owns_gaps: [GAP-G18, GAP-G19]
  authority_refs: [AUTH-OWNER]
  graph_fingerprint: f88be9b10b0635a0fb7283fd5ca5bae5ed0d85d93dc61e596ee82b733091b036
  material_sources_fingerprint: ac5617eae9c2139f6482d2909bdbc31a293dbc4851e24764f748b3503416088b
  status_mapping_fingerprint: c4c0c029649c642710a99ad2f5b26ef1ac871613f1e1158468b9153122dcb434
  allowed_targets: []
  forbidden_effects: [PRODUCTION_MUTATION, SCOPE_WIDENING]
  entry_gates: [T-ENTRY-DEVICE, T-EXIT-05]
  exit_gates: [T-EXIT-06]
  rollback_or_recovery: no mutation exists; discard device notes on failure
  input_artifacts: [P-SOT]
  closure_artifacts: [A-06]
  claim_ceiling: device observations only; no store submission claim
```

State: validation only, mutates nothing; without a device the phase is BLOCKED — never PASS, never partial-pass; findings must not contaminate the local implementation plan (recorded in A-06 only); with a device records Nano states download warmup latency quota plus perf battery Doze permission UX with per-item unblock notes where incomplete.

## Phase PH-CLOSE - Cumulative closure

```yaml
phase:
  graph_node: PH-CLOSE
  initial_state: PENDING
  depends_on: [PH-01, PH-02, PH-03, PH-04, PH-05, PH-06]
  owns_gaps: []
  authority_refs: [AUTH-OWNER]
  graph_fingerprint: f88be9b10b0635a0fb7283fd5ca5bae5ed0d85d93dc61e596ee82b733091b036
  material_sources_fingerprint: ac5617eae9c2139f6482d2909bdbc31a293dbc4851e24764f748b3503416088b
  status_mapping_fingerprint: c4c0c029649c642710a99ad2f5b26ef1ac871613f1e1158468b9153122dcb434
  allowed_targets: []
  forbidden_effects: [PRODUCTION_MUTATION]
  entry_gates: [T-EXIT-01, T-EXIT-02, T-EXIT-03, T-EXIT-04, T-EXIT-05, T-EXIT-06]
  exit_gates: [GATE-CLOSE]
  rollback_or_recovery: no mutation exists; reopen owning phase on gate failure
  input_artifacts: []
  closure_artifacts: []
  claim_ceiling: plan closure candidacy only; operational closure remains unverified
```

State: replays all exit gates fresh, re-verifies SOT fingerprints against mutated tree, records residual limitations, and either closes candidacy or reopens the owning phase.

## Execution topology

```yaml
orchestration:
  pattern: LINEAR_WITH_DEVICE_TAIL
  state_is_serialized: true
  fan_out:
    applicability: NOT_APPLICABLE_WITH_REASON
    candidate_packet_schema: NOT_APPLICABLE_WITH_REASON
    branches: []
  canonical_appender_ref: human-operator
  canonical_appender_serializes: [EVIDENCE, TRANSITION]
  deterministic_fan_in: NOT_APPLICABLE
  convergence_oracle: NOT_APPLICABLE
  max_iterations: NOT_APPLICABLE
  time_cost_bound: NOT_APPLICABLE
  repair_control_outcome: REPAIR
  loop_exhaustion_phase_state: NOT_APPLICABLE
  loop_exhaustion_control_outcome: DECIDE
  external_dependency_phase_state: BLOCKED
  external_dependency_control_outcome: WAIT_FOR_UNBLOCK
  handoff_schema: handoff fields per AGENTIC_PLAN_STANDARD.md orchestration law restatement
```

Reason: PH-02 through PH-05 share read-only context but mutate adjacent production files; coordinated mutation is serialized. No branches are declared, so the placeholder branch is removed and this reason recorded.

## Acceptance matrix

| Capability/claim | Test layer | Real dependency | Command/probe | Raw artifact | Pass condition | Limitation |
|---|---|---|---|---|---|---|
| Suite baseline green | L2 unit | toolchain §2 | `./gradlew testDebugUnitTest` | JUnit XML | 30/30 0 failures | VM paths uncovered |
| G1 harness pins VM | L2 integration | Robolectric | focused plus cumulative suite | JUnit XML | new tests PASS suite green | synthetic-copy inputs documented |
| Honesty microfixes | L2 integration | shadows | honesty suite | JUnit XML | PASS suite green | no hardware intent proof |
| Consumption gates escalation | L2 integration | router selection | consumption suite | JUnit XML | PASS suite green | no actuation |
| Fusion single path | L2 integration | trace harness | fusion suite plus `wc -l` | JUnit XML plus line count | PASS suite green VM at or below 1218 | sequence comparison only |
| UI strings bound | L2 semantics | Compose semantics | uibinding suite | JUnit XML | 15/15 dispositions PASS | no screenshots |
| Device posture recorded | L3 device | physical device | checklist A-06 | DEVICE_NOTES.md | recorded or WAIT_FOR_UNBLOCK with evidence | hardware-dependent |
| Cumulative closure | replay | all gates fresh | GATE-CLOSE | ledger heads | ALL_PASS | candidacy only |

## Threat register

| Risk ID | Failure/bypass | Trigger | Affected nodes/claim | Preventive control | Negative test | Failure phase state | Next control outcome |
|---|---|---|---|---|---|---|---|
| R-REGRESS | fusion changes fallback behavior | PH-04 mutation | C-04 CTR-FAIL-CLOSED | trace-sequence comparison plus suite | fallback-divergence test | IN_PROGRESS | REPAIR |
| R-SCOPE | ViewModel grows past budget | any VM mutation | CTR-BUDGET | `wc -l` exit sub-gate | budget test | IN_PROGRESS | REPAIR |
| R-DEVICE-ABSENT | no hardware for PH-06 | empty adb | C-06 GAP-G18 GAP-G19 | T-ENTRY-DEVICE probe first | absent-device probe | BLOCKED | WAIT_FOR_UNBLOCK |
| R-DRIFT | SOT invalidated mid-plan | product mutation outside plan | all gaps | fingerprint re-check at each entry | parity probe | IN_PROGRESS | DECIDE |

Risk failure pairs are closed per skill law; PASS states never describe a triggered failure.

## Refutation register

| Hypothesis/claim | Strongest counterexample | Search/experiment | Oracle | Result artifact | Disposition |
|---|---|---|---|---|---|
| homeostasis has zero effect | VM:361-374 condition mapping | A2 consumer grep | mapping exists | PETSCAN_RECONCILIATION.md | accepted-refinement DISPLAY_AND_TRIAGE_LABEL |
| 10 of 12 bus payloads dead | definition-file self-count | A4 recount excluding definition file | 2 constructed | PETSCAN_RECONCILIATION.md | confirmed |
| memory persists via Room | zero disk IO and zero Room refs | A6/A7 greps | empty | PETSCAN_RECONCILIATION.md | documentation-error 5 files superseded |
| throttle/isolate enforced | zero readers in main | B7 grep | empty | PETSCAN_RECONCILIATION.md | refuted becomes G21 |
| clobber between pipelines | VM:389 overwrite semantics | B5 writer grep | overwrite confirmed | PETSCAN_RECONCILIATION.md | confirmed sharpens G3 |

## Product graph contract (conditional)

Applicability determination: NOT_APPLICABLE. Evaluated trigger: plan mutates no persisted data knowledge reasoning process causal or hypergraph artifact; immune event flow is ephemeral transport without graph semantics. Authority ref: AUTH-OWNER. Evidence refs: [SRC-SOT]. Limitations: [L2-only assessment].

## Operational runbook

Degradation: any exit-gate failure yields PHASE_FAIL/REPAIR inside owning phase; repeated failure escalates to DECIDE with evidence. Disconnect/replay: N/A (no streams; L2 reruns are idempotent). Reconciliation: entry probes re-run T-ENTRY-SUITE plus SOT fingerprint check; mismatch yields DECIDE. Stale evidence: source expiry 2026-12-12 or product mutation invalidates bundle (v1: fresh bundle, no inheritance). Recovery/rollback: per-phase snapshot restore; test-only phases delete added files. Authority loss: owner revocation yields DECIDE on all pending phases. Invalidation propagation: EXPLICIT_INVALIDATES_WITH_PHASE_DEPENDENTS_V1 over graph INVALIDATES reachability (none declared; SOT change starts a fresh bundle).

## Phase evidence schema

```yaml
canonical_evidence_contract:
  top_level_keys: [artifacts, authority_id, bundle_id, claim_ceiling, claim_refs, evidence_id, experiment, freshness, gate_evaluation, graph_fingerprint, invalidation, kind, limitations, lineage_id, manifest_fingerprint, material_sources_fingerprint, oracle, phase_id, plan_id, plan_specification_fingerprint, previous_record_hashes, producer, record_hash, record_type, recorded_at, result, schema, sequence, source_grade, source_refs, statement_kind, status_mapping_fingerprint, subject_ref, sufficiency, summary, verification_state]
  experiment_keys: [boundary, commands, controlled_variables, deployment_fingerprint, environment, environment_fingerprint, hypothesis, independent_variables, input_refs, raw_observation_refs, reflection]
  gate_evaluation_keys: [aggregation, input_evidence_refs]
  gate_input_keys: [evidence_id, record_hash, subject_ref]
  artifact_keys: [artifact_id, media_type, path, produced_at, sha256, size]
  invalidation_keys: [affected_node_ids, affected_phase_ids, revalidation_gate_ids, traversal_policy, trigger_node_ids]
  invalidation_policy: EXPLICIT_INVALIDATES_WITH_PHASE_DEPENDENTS_V1
```

Adopted verbatim as the closed evidence contract for this plan revision.

## File and documentation budgets

| Path | Kind | Current lines | Projected lines | Ceiling | Split boundary/record module | Closure oracle |
|---|---|---:|---:|---:|---|---|
| app/src/main/java/com/example/viewmodel/VWatcherViewModel.kt | production | 1218 | at most 1218 | 500 exceeded pre-plan; net-negative enforced | extract scoring and simulation siblings via DECIDE if growth required | T-EXIT-04 `wc -l` |
| app/src/main/java/com/example/immune/BiomimeticImmuneSystem.kt | production | 356 | at most 400 | 500 | none | T-EXIT-04 |
| app/src/main/java/com/example/reasoning/ResourceGuardrails.kt | production | 73 | at most 150 | 500 | none | T-EXIT-03 |
| docs/archive/plans/20260912/IMPLEMENTATION_PLAN.md | governed doc |Added at closure | below 1500 | 3000 | split canonical records before projections duplicate authority | validator |
