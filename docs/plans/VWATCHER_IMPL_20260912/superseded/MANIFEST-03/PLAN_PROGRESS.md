# PLAN_PROGRESS - VWATCHER-IMPL-20260912

This Markdown file is a non-authoritative, validated view of its canonical JSON projection.

```yaml
derived_view:
  schema: vertice.progress_view.v1
  view_id: PROGRESS-VIEW-VWATCHER-01
  source_projection: progress_projection
  narrative_is_authoritative: false
  projection:
    bundle_id: VWATCHER-IMPL-B1
    claim_ceiling: structural record validity and replayed closure eligibility only;
      source truth, implementation behavior, runtime parity, authority actuality,
      and operational closure remain unverified
    closure_eligible: false
    conflicts: []
    event_count: 2
    evidence_count: 5
    evidence_head_hash: f5d35efc3812d1b97a8e48d06004dca5893310f323a162c68a6f90e566f57ba6
    evidence_registry_id: EVIDENCE-VWATCHER-01
    generator:
      generator_id: validate_plan_records.py
      version: 1
    graph_fingerprint: 464c3ee83e274003c2fdb8f509fb4aa0e09d6f8491b4f73a3fd74a287fead505
    graph_validation:
      claim_ceiling: structural graph validity only; source truth, implementation
        behavior, runtime parity, authority actuality, and closure remain unverified
      errors_fingerprint: debf84af8d66827e1cbc6791aa686504e3116d8cb20f4697fef23108333061f8
      graph_fingerprint: 464c3ee83e274003c2fdb8f509fb4aa0e09d6f8491b4f73a3fd74a287fead505
      metrics:
        artifact_lineage_violations: 0
        closure_root_violations: 0
        dangling_endpoints: 0
        dependency_cycles: 0
        duplicate_edge_ids: 0
        duplicate_node_ids: 0
        edge_count: 163
        fact_provenance_violations: 0
        gap_closure_violations: 0
        gap_owner_reachability_violations: 0
        gap_ownership_violations: 0
        gate_input_cycles: 0
        invalid_edge_semantics: 0
        invalidation_cycles: 0
        missing_core_node_kinds: 0
        missing_verification_oracles: 0
        node_contract_violations: 0
        node_count: 71
        node_reference_violations: 0
        orphan_nodes: 0
        phase_exit_gate_violations: 0
        self_referential_edges: 0
        supersession_cycles: 0
        unauthorized_mutating_phases: 0
        unbound_gap_closers: 0
        unbounded_mutating_phases: 0
        undeclared_mutating_phases: 0
        unreachable_required_phases: 0
        unresolved_placeholders: 0
        unsupported_claims: 0
        unverified_claims: 0
        unverified_contracts: 0
        unverified_required_phases: 0
      valid: true
      validator_id: validate_plan_graph.py
      validator_version: 1
    ledger_head_hash: 6b5e3a52c8b6784116789b1a721ce1373fe106d3e8bd229318aa3ecdd2e0cd68
    ledger_id: LEDGER-VWATCHER-01
    manifest_fingerprint: 67aab5724b78c07acccff754a40d4daf5c94b8a3dfd8072c4f501883dce92963
    material_sources_fingerprint: 1ac148a5216a4aac340a6a639c15bbca1dd067673a0933cd5ff5ee11b102ba9e
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
    projection_fingerprint: 573ec209c3e58d7d1ddddbb169958932629106fb1a17ee436c8e7b0212643c32
    projection_generated_at: '2026-09-12T19:27:24Z'
    projection_id: PROGRESS-VWATCHER-01
    replay_valid: true
    schema: vertice.progress_projection.v1
    status_mapping_fingerprint: 7cdda5b3b954843873ef48bafa8924695ade9a81284b1ba606584880f13aa3dc
```
