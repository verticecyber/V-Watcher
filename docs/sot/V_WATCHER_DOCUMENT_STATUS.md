# V-Watcher Document Status Registry

**Status:** ACTIVE  
**Data:** 2026-09-14  

Este inventário classifica todos os documentos de documentação, validação e planos do repositório conforme seu grau de autoridade e temporalidade.

| Documento | Estado | Justificativa |
|---|---|---|
| `docs/sot/V_WATCHER_SOURCE_OF_TRUTH.md` | **ACTIVE (AUTORIDADE MÁXIMA)** | Fonte primária da verdade operacional do repositório atual. |
| `docs/sot/V_WATCHER_SOT.md` | **ACTIVE (REFERÊNCIA TÉCNICA)** | Especificação de gaps e matriz de auditoria técnica. |
| `docs/sot/V_WATCHER_SOT_UNIFIED.md` | **ACTIVE (CONSOLIDADO)** | Versão densa e unificada das verdades operacionais. |
| `docs/evidence/AI_STUDIO_RUNTIME_VALIDATION.md` | **ACTIVE (EVIDÊNCIA CORRENTE)** | Relatório detalhado da execução limpa, compilação, testes e probes no ambiente AI Studio. |
| `docs/evidence/device/DEVICE_AUDIT.json` | **ACTIVE (EVIDÊNCIA CORRENTE)** | Snapshot do ambiente de dispositivo (container ADB e streaming emulator). |
| `docs/evidence/runtime/TOOLCHAIN_AND_BUILD.json` | **ACTIVE (EVIDÊNCIA CORRENTE)** | Registro com hashes de compilação, versão do JDK, Gradle e testes. |
| `docs/evidence/models/REASONING_PROBE.json` | **ACTIVE (EVIDÊNCIA CORRENTE)** | Provas de disponibilidade e status dos backends de raciocínio. |
| `docs/evidence/e2e/PIPELINE_VERIFICATION.json` | **ACTIVE (EVIDÊNCIA CORRENTE)** | Verificação do pipeline e reality boundary. |
| `docs/sot/V_WATCHER_CLAIM_MATRIX.json` | **ACTIVE** | Matriz de claims estáveis e tetos públicos. |
| `docs/sot/V_WATCHER_COMPONENT_REGISTRY.json` | **ACTIVE** | Registro de componentes e callers. |
| `docs/sot/V_WATCHER_PROOF_MATRIX.json` | **ACTIVE** | Matriz de níveis de prova L0 a L7. |
| `docs/evidence/device/RUNTIME_PROOF_BOUNDARY.md` | **ACTIVE (FRONTEIRA DE PROVA)** | Especificação formal dos limites de prova entre container, deploy bridge e AVD externo. |
| `docs/evidence/device/AI_STUDIO_ANDROID_BRIDGE_AUDIT.md` | **ACTIVE (EVIDÊNCIA CORRENTE)** | Auditoria técnica do APP_DEPLOY_BRIDGE, isolamento do container e portas locais. |
| `docs/evidence/device/ANDROID_RUNTIME_OBSERVABILITY_REPORT.md` | **ACTIVE (EVIDÊNCIA CORRENTE)** | Relatório detalhado dos limites de observabilidade no streaming emulator. |
| `docs/evidence/historical/REAL_DEVICE_VALIDATION_REPORT.md` | **HISTORICAL** | Relatório de ciclo anterior; alegações de Room/SQLite são obsoletas. |
| `docs/evidence/historical/TRUTH_FIRST_VALIDATION_REPORT.md` | **HISTORICAL** | Relatório de ciclo anterior com dados legados. |
| `docs/archive/plans/**` | **HISTORICAL / ARCHIVE** | Planos e snapshots de fases arquivadas. |
| `docs/release/**` | **ACTIVE (RELEASE DOCS)** | Documentos de preparação e auditoria de release para Play Store. |
