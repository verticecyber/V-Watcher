# V-Watcher — Brief de Verificação Externa (autocontido, 2026-09-12)

> Propósito: permitir que um analista externo verifique o estado real do projeto sem acesso ao
> histórico de conversa. Tudo abaixo é verificável no diretório `/media/juan/DATA/V-watcher`
> e na toolchain irmã `/media/juan/DATA/.toolchains`. Nenhuma afirmação depende de memória do agente.

---

## 1. O que é o projeto

App Android (Kotlin/Compose, single-activity) chamado V-Watcher: observador foreground-only de saúde
do dispositivo com metáfora de "sistema imunológico" (células determinísticas in-process), telemetria
real via APIs públicas do Android, roteador de reasoning com fallback determinístico, UI clínica em
5 abas. Sem backend, sem contas, sem rede, sem persistência.

- `applicationId = "com.aistudio.vwatcher.hkmv"` (PROVISÓRIO — decisão humana pendente antes do 1º upload)
- `namespace = "com.example"`, `compileSdk 36.1`, `targetSdk 36`, `minSdk 24`
- AGP 9.1.1, Kotlin 2.2.10, Gradle 9.3.1 (wrapper regenerado), JDK Temurin 21.0.12.1+1
- `versionCode 1`, `versionName "1.0"`, sem tags; git inicializado (`main`) sem commit

## 2. Estado de release (com prova)

| Nível | Valor | Prova |
|---|---|---|
| BUILD_READY | YES | `assembleDebug`, `bundleRelease`, `testDebugUnitTest`, `lintRelease` EXIT 0 |
| TECHNICALLY_RELEASE_READY | YES | AAB 11.016.375 B, sha256 `28dd9bedfbd27c1a3ce08bd1986bf8853cdf83a695520e519d93b2000f6af72e`, `bundletool validate` 0 |
| PLAY_POLICY_READY | YES técnico | permissões justificadas, disclosures, zero exfiltração; filings em checklist |
| PLAY_CONSOLE_READY / PRODUCTION_READY | NO | conta, filings, listing, signing de produção — operador humano |

Testes: **30/30, 0 falhas** (`testDebugUnitTest`). Lint release: 0 errors (49 warnings informativos).
Artefato: `app/build/outputs/bundle/release/app-release.aab`; manifest do AAB contém exatamente
`ACCESS_NETWORK_STATE, QUERY_ALL_PACKAGES, PACKAGE_USAGE_STATS`; `debuggable` ausente (=false);
backup rules ligadas; assinatura de TESTE local (CN=NOT FOR PLAY, chave em /tmp, fora do repo);
scan de segredos no dex limpo (zero `AIza`/chaves/`GEMINI_API_KEY`).

## 3. Arquitetura real (verificada por leitura integral + trace runtime L2)

Pipeline viva: `AndroidSentinel.observeNow()` (6 providers, ~181 ms) → `PatternRecognitionCell` →
`ImmuneBus` (2 dispatches) → `THelperCoordinatorCell` (gate) → `DendriticCell` → `ReasoningRouter` →
`DeterministicBackend` → `HomeostasisEngine` → UI. Trace verbatim (harness mantido em
`app/src/test/java/com/example/petscan/PetScanTraceTest.kt`):

```text
nominal: observeMs=181 macroState=DEGRADED confidence=60 busTotal=2 storm=false dropped=0 reasoningCalls=0
storm:   40 msgs / 99 ms → rate=40.0 storm=true dropped=0   (DETECTA, NÃO contém)
router:  selected=GEMINI_NANO actual=DETERMINISTIC FALLBACK/GEMINI_NANO_UNAVAILABLE, 9 ms; regra socket → SUSPICIOUS/88
```

Notas de leitura: `DEGRADED/60` no nominal é artefato de shadow Robolectric e **prova que o
fail-closed dispara de verdade**; `reasoningCalls=0` prova que o gate de escalada fica idle sem evidência.

## 4. O que funciona vs o que é esqueleto/dívida

FUNCIONA: telemetria real (bateria/recursos/rede/inventário/uso-com-consentimento), fail-closed,
roteador+fallback honesto com `selected vs actual`, guardrails (15%/92%/2,5 s), vetos regulatórios,
gates do efetor (fresh/conf≥80/evidência), recusa honesta de kill externo (`UNAVAILABLE`), reclaim
de cache próprio, gates de escrita da memória (rejeita `[SIM]`, <80%, duplicatas 30 s).

ESQUELETO/DÍVIDA: inferência Nano/Gemma = Assessment canned (detecção honesta, sem chamada de modelo);
célula NK 100% desfibrada (zero callers); homeostase DISPLAY-ONLY (8 dimensões, zero consumidores);
2 pipelines paralelas (legada no ViewModel + `BiomimeticImmuneSystem`); bus sem subscriber funcional
(10/12 payloads mortos); storm = só flag; `NAVIGATE_APP_SETTINGS` monta Intent sem `startActivity`;
resolução travável em reclaim 0/0; `RECOVERING` inalcançável; thresholds inconsistentes (88 vs 92,
200 vs 250 MB); memória RAM-only sem TTL/limite; ViewModel (1218 linhas) com ZERO testes.

FALSO EM DOCS ANTIGOS: relatórios JSON/MD legados afirmam persistência Room/SQLite — o código é
RAM-only com reseed (`LocalImmuneMemoryRepository.kt:32-101`). UI tem ~15 overclaims mapeados
(TLS/certificado/assinaturas/decoys/`0 uncontained`/`<40ms`/eficiência/medidor estático).

## 5. Privacidade/rede (código como prova)

Zero endpoints (grep + dump do AAB confirmam; `INTERNET` removida do manifest em 2026-09-12);
Firebase/Retrofit/OkHttp/Room removidos do build após prova de não-uso (build+testes verdes pós-remoção);
dados só em RAM (morte do processo = apagamento); backup explicitamente excluído nas duas rotas de API;
`QUERY_ALL_PACKAGES` (inventário local, uso permitido "antivírus", declaração Console pendente) +
`PACKAGE_USAGE_STATS` (opcional, degradável, consentimento em Settings). Sem ads/analytics/crash SDK.
`NAVIGATE_APP_SETTINGS` etc. acima são as únicas arestas de honestidade remanescentes (G12).

## 6. Lacunas P0 (o que falta para o próximo estágio)

- G1: ViewModel sem cobertura (scoring/sim/quarentena/exam) — risco arquitetural nº 1.
- G2/G3: homeostase não consumida; duas pipelines — fundir em um caminho único de reasoning.
- G4–G7: ligar NK + `forceDeterministic`; assinantes reais no bus; prova de cleanup por incidente.
- G13: checklist dos 15 overclaims de UI (ligar ou reescrever).
- Externo (operador): app ID definitivo, URL de privacy policy + revisão jurídica, filings
  (Data Safety, declaração QUERY_ALL_PACKAGES), rating/audiência, listing, upload key + App Signing,
  trilha fechada 12×14 se a conta for pessoal nova, pre-launch report, laboratório em dispositivo físico
  (Nano/Gemma/métricas/Doze). Checklist operacional: `docs/release/PLAY_CONSOLE_ACTIONS.md`.

## 7. Como verificar (comandos, toolchain em `/media/juan/DATA/.toolchains`)

```bash
export JAVA_HOME=/media/juan/DATA/.toolchains/jdk-21.0.12.1+1
export GRADLE_USER_HOME=/media/juan/DATA/.toolchains/gradle-home
export ANDROID_HOME=/media/juan/DATA/.toolchains/android-sdk
./gradlew testDebugUnitTest        # esperado: 30/30, BUILD SUCCESSFUL
./gradlew lintRelease              # esperado: 0 errors
./gradlew bundleRelease            # requer KEYSTORE_PATH/STORE_PASSWORD/KEY_PASSWORD (alias `upload`)
./tools/petscan/static_scan.sh     # scans estáticos reproduzíveis
```

Documentos-fonte (todos em `docs/`): `PETSCAN_EXECUTIVE_SUMMARY`,
`PETSCAN_SYSTEM_MAP`, `PETSCAN_COMPONENT_MATRIX`, `PETSCAN_INTEGRATION_MAP`,
`PETSCAN_GAPS` (20 gaps G1–G20), `PETSCAN_NEXT_STEPS`, `PETSCAN_CLAIM_RECONCILIATION`
(scores de maturidade por dimensão), `PETSCAN_EVIDENCE_INDEX` (métodos, file:line anchors,
cobertura feature→teste), `RELEASE_GATE`, `RELEASE_READINESS_MATRIX`, `RELEASE_PACKAGE`
(SHA, receita de toolchain), `RELEASE_REMEDIATION_REPORT` (ledger por blocker), `PERMISSIONS_AUDIT`,
`PRIVACY_DATA_MAP`, `V_WATCHER_DATA_INVENTORY`, `NETWORK_ENDPOINT_INVENTORY`,
`THIRD_PARTY_SDK_AUDIT`, `THIRD_PARTY_LICENSE_AUDIT`, `SECURITY_BASELINE`, `PRIVACY_POLICY_DRAFT`
(não publicado, requer jurídico), `PLAY_CONSOLE_ACTIONS`, `PLAY_STORE_READINESS`, `RELEASE_READINESS`.

## 8. Limites desta verificação (teto de claim)

Evidência runtime é L2 (Robolectric/JVM) — L3–L5 (emulador/dispositivo/instrumentação) indisponíveis
(sem device/AVD). Afirmações sobre Nano/Gemma em hardware, bateria, Doze, UX de permissões e
pre-launch estão marcadas `NOT_AVAILABLE_IN_ENVIRONMENT` e não foram fingidas. Nenhum blocker
Conhecido foi ocultado; o que depende de humano/Console/infra está rotulado `REQUIRES_*`.
