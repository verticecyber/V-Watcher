# WORK_IN_PROGRESS - VWATCHER-IMPL-20260912

This Markdown file is a non-authoritative, validated view of its canonical JSON projection.

```yaml
derived_view:
  schema: vertice.work_view.v1
  view_id: WORK-VIEW-VWATCHER-01
  source_projection: work_projection
  narrative_is_authoritative: false
  projection:
    authority_refs: []
    blocked_phases: []
    bundle_id: VWATCHER-IMPL-B1
    claim_ceiling: structural record validity and replayed closure eligibility only;
      source truth, implementation behavior, runtime parity, authority actuality,
      and operational closure remain unverified
    closure_eligible: false
    conflicts: []
    current_native_state: null
    current_phase: null
    current_state: null
    event_count: 2
    evidence_count: 5
    evidence_head_hash: f5d35efc3812d1b97a8e48d06004dca5893310f323a162c68a6f90e566f57ba6
    evidence_registry_id: EVIDENCE-VWATCHER-01
    executor: null
    generator:
      generator_id: validate_plan_records.py
      version: 1
    graph_fingerprint: 464c3ee83e274003c2fdb8f509fb4aa0e09d6f8491b4f73a3fd74a287fead505
    handoff:
      artifact_refs: []
      authority_refs: []
      canonical_state: null
      evidence_refs: []
      forbidden_actions:
      - BLOCK
      - FAIL
      - INVALIDATION
      - PASS
      - PASS_WITH_LIMITATIONS
      - RETRY
      - START
      - UNBLOCK
      from: null
      invalidated_node_ids: []
      last_event_ref: null
      native_state: null
      next_control_outcome: PROCEED
      phase_id: null
      residuals: []
      to: null
    ledger_head_hash: 6b5e3a52c8b6784116789b1a721ce1373fe106d3e8bd229318aa3ecdd2e0cd68
    ledger_id: LEDGER-VWATCHER-01
    manifest_fingerprint: 67aab5724b78c07acccff754a40d4daf5c94b8a3dfd8072c4f501883dce92963
    material_sources_fingerprint: 1ac148a5216a4aac340a6a639c15bbca1dd067673a0933cd5ff5ee11b102ba9e
    next_actions:
      PH-01:
      - INVALIDATION
      PH-02:
      - BLOCK
      - START
      PH-03:
      - BLOCK
      - START
      PH-04:
      - BLOCK
      - START
      PH-05:
      - BLOCK
      - START
      PH-06:
      - BLOCK
      - START
      PH-CLOSE:
      - BLOCK
      - START
    next_control_outcome: PROCEED
    phase_last_events:
      PH-01:
        event_hash: 6b5e3a52c8b6784116789b1a721ce1373fe106d3e8bd229318aa3ecdd2e0cd68
        event_id: EVT-PH01-PASS
      PH-02: null
      PH-03: null
      PH-04: null
      PH-05: null
      PH-06: null
      PH-CLOSE: null
    phase_residuals:
      PH-01: []
      PH-02: []
      PH-03: []
      PH-04: []
      PH-05: []
      PH-06: []
      PH-CLOSE: []
    phase_states:
      PH-01: PHASE_PASS
      PH-02: PENDING
      PH-03: PENDING
      PH-04: PENDING
      PH-05: PENDING
      PH-06: PENDING
      PH-CLOSE: PENDING
    plan_id: VWATCHER-IMPL-20260912
    plan_specification_fingerprint: 4cf55fb728f5644fda66b2c9f26f76ecf98a851de9f27eb54315460184c936ce
    projection_fingerprint: 3b6fe8427e97ae8b799942ce765e8147a820c3f2f62ec73ba4fc2f9039dd8404
    projection_generated_at: '2026-09-12T19:27:24Z'
    projection_id: WORK-VWATCHER-01
    replay_valid: true
    schema: vertice.work_projection.v1
    stale_evidence: []
    status_mapping_fingerprint: 7cdda5b3b954843873ef48bafa8924695ade9a81284b1ba606584880f13aa3dc
    status_vocabulary_id: vwatcher-native-v1
    work_envelope: null
    workspace:
      dirty: false
      foreign_deltas: []
      mutations_started: false
      observed_at: '2026-09-12T19:27:24Z'
      observed_revision: uncommitted-tree
      owned_deltas: []
      protected_paths: []
```
