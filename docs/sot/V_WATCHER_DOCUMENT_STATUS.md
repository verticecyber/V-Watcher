# V-Watcher — Status Documental e Autoridade

**Estado:** ACTIVE  
**Data de corte:** 2026-09-14 09:10 -03:00  
**Autoridade:** `docs/sot/V_WATCHER_SOURCE_OF_TRUTH.md`  
**Regra:** documentos históricos não foram reescritos; conflitos são registrados em `SOT_CONSISTENCY_REPORT.md`.

## Estados controlados

| Estado | Uso |
|---|---|
| `ACTIVE` | Documento operacional corrente ou índice auxiliar subordinado ao SOT. |
| `HISTORICAL` | Preservado para contexto; não é autoridade corrente. |
| `OBSOLETE` | Superado explicitamente por documento/código posterior. |
| `CONTRADICTED` | Contém afirmação incompatível com código/configuração atual; preservar, não promover. |
| `PLANNED` | Rascunho, checklist externo ou ação ainda não executada. |
| `HISTORICAL_EVIDENCE` | Evidência datada de execução/ambiente anterior; não é prova corrente. |

## Resumo

| Estado | Quantidade |
|---|---:|
| `ACTIVE` | 24 |
| `HISTORICAL` | 25 |
| `OBSOLETE` | 11 |
| `CONTRADICTED` | 6 |
| `PLANNED` | 3 |
| `HISTORICAL_EVIDENCE` | 17 |

## Inventário completo

| Caminho | Estado | Motivo |
|---|---|---|
| `README.md` | `ACTIVE` | Índice documental; aponta o SOT como autoridade em caso de conflito. |
| `V_WATCHER_PHYSICAL_SCOPE_AUDIT.md` | `HISTORICAL_EVIDENCE` | Auditoria/escopo anterior; preservado e subordinado ao SOT canônico. |
| `V_WATCHER_PHYSICAL_SCOPE_SOT.md` | `HISTORICAL_EVIDENCE` | Auditoria/escopo anterior; preservado e subordinado ao SOT canônico. |
| `archive/petscan/PETSCAN_CLAIM_RECONCILIATION.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/petscan/PETSCAN_COMPONENT_MATRIX.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/petscan/PETSCAN_EVIDENCE_INDEX.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/petscan/PETSCAN_EXECUTIVE_SUMMARY.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/petscan/PETSCAN_GAPS.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/petscan/PETSCAN_INTEGRATION_MAP.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/petscan/PETSCAN_NEXT_STEPS.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/petscan/PETSCAN_SYSTEM_MAP.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/AGENTIC_PLAN_STANDARD.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/DECISIONS.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/EVIDENCE_REGISTRY.jsonl` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/IMPLEMENTATION_PLAN.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/PHASE_EVALUATION_BRIEF.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/PLAN_GRAPH.yaml` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/PLAN_PROGRESS.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/PLAN_RECORDS_MANIFEST.yaml` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/PROGRESS_PROJECTION.json` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/STATUS_MAPPING.yaml` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/TRANSITION_LEDGER.jsonl` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/WORK_IN_PROGRESS.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/WORK_PROJECTION.json` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/sources/RECONCILIATION.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/sources/SOT.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/sources/THRESHOLDS.md` | `HISTORICAL` | Documento preservado no arquivo; não é instrução nem autoridade corrente. |
| `archive/plans/20260912/superseded/MANIFEST-03/EVIDENCE_REGISTRY.jsonl` | `OBSOLETE` | Plano arquivado explicitamente como superseded; não representa estado operacional atual. |
| `archive/plans/20260912/superseded/MANIFEST-03/PLAN_PROGRESS.md` | `OBSOLETE` | Plano arquivado explicitamente como superseded; não representa estado operacional atual. |
| `archive/plans/20260912/superseded/MANIFEST-03/PLAN_RECORDS_MANIFEST.yaml` | `OBSOLETE` | Plano arquivado explicitamente como superseded; não representa estado operacional atual. |
| `archive/plans/20260912/superseded/MANIFEST-03/PROGRESS_PROJECTION.json` | `OBSOLETE` | Plano arquivado explicitamente como superseded; não representa estado operacional atual. |
| `archive/plans/20260912/superseded/MANIFEST-03/README.txt` | `OBSOLETE` | Plano arquivado explicitamente como superseded; não representa estado operacional atual. |
| `archive/plans/20260912/superseded/MANIFEST-03/TRANSITION_LEDGER.jsonl` | `OBSOLETE` | Plano arquivado explicitamente como superseded; não representa estado operacional atual. |
| `archive/plans/20260912/superseded/MANIFEST-03/WORK_IN_PROGRESS.md` | `OBSOLETE` | Plano arquivado explicitamente como superseded; não representa estado operacional atual. |
| `archive/plans/20260912/superseded/MANIFEST-03/WORK_PROJECTION.json` | `OBSOLETE` | Plano arquivado explicitamente como superseded; não representa estado operacional atual. |
| `evidence/historical/REAL_DEVICE_VALIDATION_REPORT.md` | `HISTORICAL_EVIDENCE` | Relatório histórico de validação; preservado, mas limitado ao ambiente e data originais. |
| `evidence/historical/TRUTH_FIRST_VALIDATION_REPORT.md` | `HISTORICAL_EVIDENCE` | Relatório histórico de validação; preservado, mas limitado ao ambiente e data originais. |
| `evidence/validation/ACTUATOR_VALIDATION.json` | `HISTORICAL_EVIDENCE` | Artefato de evidência datado; alguns claims são contraditos pelo código atual e não devem ser promovidos. |
| `evidence/validation/AUTOIMMUNE_EXPERIMENT.json` | `HISTORICAL_EVIDENCE` | Artefato de evidência datado; alguns claims são contraditos pelo código atual e não devem ser promovidos. |
| `evidence/validation/CLAIM_PROOF_MATRIX.json` | `CONTRADICTED` | Afirma Room/SQLite persistente e outros estados que o código atual não implementa; preservado como histórico. |
| `evidence/validation/END_TO_END_IMMUNE_TRACE.jsonl` | `HISTORICAL_EVIDENCE` | Artefato de evidência datado; alguns claims são contraditos pelo código atual e não devem ser promovidos. |
| `evidence/validation/HOMEOSTASIS_EXPERIMENTS.json` | `HISTORICAL_EVIDENCE` | Artefato de evidência datado; alguns claims são contraditos pelo código atual e não devem ser promovidos. |
| `evidence/validation/LIFECYCLE_VALIDATION.json` | `CONTRADICTED` | Afirma reload/persistência Room que contradiz o repository RAM-only atual; preservado como histórico. |
| `evidence/validation/MEMORY_VALIDATION.json` | `CONTRADICTED` | Descreve banco Room/SQLite e cold re-query inexistentes no código atual; preservado como histórico. |
| `evidence/validation/MODEL_BACKEND_VALIDATION.json` | `CONTRADICTED` | Relata execução READY de Nano/Gemma que o código atual não implementa como runtime de inferência; preservado como histórico. |
| `evidence/validation/NEGATIVE_FINDINGS.json` | `HISTORICAL_EVIDENCE` | Artefato de evidência datado; alguns claims são contraditos pelo código atual e não devem ser promovidos. |
| `evidence/validation/PERFORMANCE_RESULTS.json` | `HISTORICAL_EVIDENCE` | Artefato de evidência datado; alguns claims são contraditos pelo código atual e não devem ser promovidos. |
| `evidence/validation/REAL_DEVICE_VALIDATION.json` | `HISTORICAL_EVIDENCE` | Artefato de evidência datado; alguns claims são contraditos pelo código atual e não devem ser promovidos. |
| `evidence/validation/RESOLUTION_VALIDATION.json` | `HISTORICAL_EVIDENCE` | Artefato de evidência datado; alguns claims são contraditos pelo código atual e não devem ser promovidos. |
| `evidence/validation/STORM_EXPERIMENT.json` | `HISTORICAL_EVIDENCE` | Artefato de evidência datado; alguns claims são contraditos pelo código atual e não devem ser promovidos. |
| `reference/ENVIRONMENT_DISCOVERY.md` | `ACTIVE` | Documento de referência auxiliar; subordinado ao SOT para estado atual. |
| `reference/EXTERNAL_VERIFICATION_BRIEF.md` | `ACTIVE` | Documento de referência auxiliar; subordinado ao SOT para estado atual. |
| `reference/V_WATCHER_SKILL_INSTALL_REPORT.md` | `HISTORICAL` | Relatório de instalação/ambiente datado. |
| `reference/metadata.json` | `ACTIVE` | Documento de referência auxiliar; subordinado ao SOT para estado atual. |
| `release/NETWORK_ENDPOINT_INVENTORY.md` | `ACTIVE` | Documento operacional de release/configuração; claims devem respeitar o claim ceiling do SOT. |
| `release/PERMISSIONS_AUDIT.md` | `ACTIVE` | Documento operacional de release/configuração; claims devem respeitar o claim ceiling do SOT. |
| `release/PLAY_CONSOLE_ACTIONS.md` | `PLANNED` | Checklist/ações externas de Console; não são prova de execução ou publicação. |
| `release/PLAY_STORE_READINESS.md` | `PLANNED` | Checklist/ações externas de Console; não são prova de execução ou publicação. |
| `release/PRIVACY_DATA_MAP.md` | `ACTIVE` | Documento operacional de release/configuração; claims devem respeitar o claim ceiling do SOT. |
| `release/PRIVACY_POLICY_DRAFT.md` | `PLANNED` | Rascunho jurídico; requer revisão humana, URL pública e confirmação no release. |
| `release/RELEASE_BUILD.md` | `CONTRADICTED` | Afirma build/test/lint verdes como estado operacional, mas a reprodução atual está bloqueada por ausência de javac; o relato datado permanece preservado. |
| `release/RELEASE_GATE.md` | `ACTIVE` | Documento operacional de gates; deve ser lido com o SOT e com a limitação de build/device desta corte. |
| `release/RELEASE_PACKAGE.md` | `CONTRADICTED` | Afirma validação de artefato e 27/27 em 2026-09-12; isso é histórico e não prova a árvore/corte atual, bloqueada por javac. |
| `release/RELEASE_READINESS.md` | `ACTIVE` | Documento operacional de gates; deve ser lido com o SOT e com a limitação de build/device desta corte. |
| `release/RELEASE_READINESS_MATRIX.md` | `ACTIVE` | Documento operacional de gates; deve ser lido com o SOT e com a limitação de build/device desta corte. |
| `release/RELEASE_REMEDIATION_REPORT.md` | `ACTIVE` | Registro operacional de remediações; não substitui a prova de build ou device. |
| `release/SECURITY_BASELINE.md` | `ACTIVE` | Documento operacional de release/configuração; claims devem respeitar o claim ceiling do SOT. |
| `release/THIRD_PARTY_LICENSE_AUDIT.md` | `ACTIVE` | Documento operacional de release/configuração; claims devem respeitar o claim ceiling do SOT. |
| `release/THIRD_PARTY_SDK_AUDIT.md` | `ACTIVE` | Documento operacional de release/configuração; claims devem respeitar o claim ceiling do SOT. |
| `release/V_WATCHER_DATA_INVENTORY.md` | `ACTIVE` | Documento operacional de release/configuração; claims devem respeitar o claim ceiling do SOT. |
| `release/V_WATCHER_MODEL_MATRIX.md` | `ACTIVE` | Documento operacional de release/configuração; claims devem respeitar o claim ceiling do SOT. |
| `release/V_WATCHER_ONDEVICE_AI_RUNTIME.md` | `ACTIVE` | Documento operacional de release/configuração; claims devem respeitar o claim ceiling do SOT. |
| `sot/PETSCAN_RECONCILIATION.md` | `HISTORICAL_EVIDENCE` | Evidência/método PETSCAN anterior; útil como histórico e não como autoridade corrente. |
| `sot/PETSCAN_THRESHOLD_MATRIX.md` | `HISTORICAL_EVIDENCE` | Evidência/método PETSCAN anterior; útil como histórico e não como autoridade corrente. |
| `sot/V_WATCHER_CLAIM_MATRIX.json` | `ACTIVE` | Autoridade/artefato operacional da corte atual; reconciliado ou gerado nesta atualização. |
| `sot/V_WATCHER_COMPONENT_REGISTRY.json` | `ACTIVE` | Autoridade/artefato operacional da corte atual; reconciliado ou gerado nesta atualização. |
| `sot/V_WATCHER_DOCUMENT_STATUS.md` | `ACTIVE` | Este inventário documental; define estados controlados e a posição histórica de cada arquivo. |
| `sot/V_WATCHER_EVIDENCE_INDEX.md` | `HISTORICAL_EVIDENCE` | Evidência/método PETSCAN anterior; útil como histórico e não como autoridade corrente. |
| `sot/V_WATCHER_FORENSIC_SYSTEM_REPORT.md` | `ACTIVE` | Autoridade/artefato operacional da corte atual; reconciliado ou gerado nesta atualização. |
| `sot/V_WATCHER_GAP_DELTA.md` | `HISTORICAL_EVIDENCE` | Evidência/método PETSCAN anterior; útil como histórico e não como autoridade corrente. |
| `sot/V_WATCHER_PROOF_MATRIX.json` | `ACTIVE` | Autoridade/artefato operacional da corte atual; reconciliado ou gerado nesta atualização. |
| `sot/SOT_CONSISTENCY_REPORT.md` | `ACTIVE` | Relatório de conflitos e claims fortes desta corte; subordinado ao SOT canônico. |
| `sot/V_WATCHER_SOT.md` | `OBSOLETE` | SOT anterior; preservado para rastreabilidade e superseded pelo SOT canônico atual. |
| `sot/V_WATCHER_SOT_AUDIT_REPORT.md` | `OBSOLETE` | SOT anterior; preservado para rastreabilidade e superseded pelo SOT canônico atual. |
| `sot/V_WATCHER_SOT_UNIFIED.md` | `OBSOLETE` | SOT anterior; preservado para rastreabilidade e superseded pelo SOT canônico atual. |
| `sot/V_WATCHER_SOURCE_OF_TRUTH.md` | `ACTIVE` | Autoridade/artefato operacional da corte atual; reconciliado ou gerado nesta atualização. |

## Regras de leitura

1. Um documento `ACTIVE` não pode elevar o nível de prova acima de L2 nesta corte.
2. `HISTORICAL_EVIDENCE` conserva o que foi observado na data original, inclusive resultados que o código atual não reproduz.
3. `OBSOLETE` e `CONTRADICTED` não devem ser citados como estado atual, mas não devem ser apagados.
4. `PLANNED` descreve trabalho/ação externa; não é implementação.
5. O relatório de consistência é o índice de conflitos; o SOT canônico é a fonte final de verdade operacional.
