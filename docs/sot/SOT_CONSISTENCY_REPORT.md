# SOT Consistency & Honesty Report

**Data:** 2026-09-14  
**Ambiente:** Google AI Studio Cloud Build & Runtime  

## 1. Conflitos Identificados e Resolvidos

1. **Alegação de Banco de Dados Room / SQLite:**
   - *Conflito:* Documentos históricos (`REAL_DEVICE_VALIDATION_REPORT.md`, `CLAIM_PROOF_MATRIX.json` legado) mencionavam persistência em SQLite/Room.
   - *Resolução:* O código atual (`LocalImmuneMemoryRepository.kt`) e `app/build.gradle.kts` não contêm importações ou dependências de Room. A persistência é estritamente **RAM-only** (`MutableStateFlow`). Os documentos históricos foram marcados como `HISTORICAL`.
2. **Alegação de Execução de Gemini Nano / Gemma:**
   - *Conflito:* Nomenclaturas em comentários sugeriam capacidade de inferência neural imediata.
   - *Resolução:* A auditoria confirmou que `GeminiNanoBackend.kt` e `GemmaBackend.kt` são probes que honestamente detectam a ausência de AICore e de arquivos de pesos no ambiente de container, delegando imediatamente ao `DeterministicBackend` sem mascarar o resultado.
3. **Alegação de Execução em Dispositivo Físico:**
   - *Conflito:* Menções legadas a testes em dispositivos físicos.
   - *Resolução:* No ambiente do AI Studio, o container possui o daemon ADB rodando em `tcp:5037`, mas não há dispositivo físico conectado diretamente a ele (`adb devices -l` é vazio). O app é transmitido via Streaming Android Emulator no navegador.

## 2. Claims Proibidos (Teto de Honestidade Estrito)

- É **PROIBIDO** afirmar que o Gemini Nano ou o Gemma executaram inferência neural no ambiente atual.
- É **PROIBIDO** afirmar que o aplicativo possui antivírus ativo com capacidade de encerrar processos de outros aplicativos (`KILL_EXTERNAL_PROCESS` é `UNAVAILABLE`).
- É **PROIBIDO** afirmar que existe monitoramento contínuo em segundo plano ativo no sistema operacional (não há Foreground Service ou WorkManager configurado).
- É **PROIBIDO** afirmar que os dados de histórico de incidentes persistem após o encerramento do processo.

## 3. Estado Atual dos Componentes

Todos os componentes críticos no caminho de execução (`AndroidSentinel`, `BiomimeticImmuneSystem`, `ReasoningRouter`, `DeterministicBackend`, `AndroidRealityBoundary`, `LocalImmuneMemoryRepository`) foram validados através de 63 testes unitários e de integração na JVM/Robolectric, alcançando 100% de aprovação no host (`L2_HOST_JVM_VERIFIED`).

---

## 4. SOT Delta: Correção da Fronteira de Prova de Runtime

### BEFORE
- O deploy bem-sucedido via `APP_DEPLOY_BRIDGE` gerava a tentação de classificar o runtime como `ANDROID_RUNTIME: VERIFIED` ou promover a telemetria e o E2E para "comprovados no dispositivo" (`PROVEN`), apenas porque o APK foi entregue e a plataforma reportou a execução no emulador.

### EVIDENCE
- O daemon ADB local (`tcp:5037`) no container tem lista de dispositivos vazia (`adb devices -l` = vazio).
- O control-plane expõe `/build/outputs/apk/debug/app-debug.apk` e o orquestrador da plataforma instala o APK no Streaming Android Emulator da nuvem.
- Não existe canal reverso de Logcat, API de screenshot ou socket ADB conectado ao container do agente.
- **Princípio Epistêmico:** *Deploy confirmado não é prova independente de runtime observado.*

### CORRECTION
- Criação formal de `docs/evidence/device/RUNTIME_PROOF_BOUNDARY.md`.
- Reclassificação explícita:
  - `ANDROID_DEPLOYMENT`: `VERIFIED` (`L3_APP_DEPLOY_VERIFIED`)
  - `ANDROID_RUNTIME_EXECUTION`: `PLATFORM_REPORTED` (`L3_PLATFORM_RUNTIME_REPORTED`)
  - `ANDROID_RUNTIME_OBSERVABILITY`: `UNAVAILABLE_FROM_CONTAINER`
  - `ANDROID_RUNTIME_INDEPENDENT_PROOF`: `NOT_VERIFIED`
  - `PHYSICAL_DEVICE`: `false` / `NOT_VERIFIED`
- Componentes e telemetria mantêm teto estrito: `CODE_IMPLEMENTED`, `HOST_TESTED (L2)`, `DEVICE_RUNTIME_NOT_VERIFIED`.
- E2E corrigido de `PROVEN` genérico para `HOST_JVM_VERIFIED` / `PLATFORM_REPORTED` / `INDEPENDENT_DEVICE_PROOF: NOT_VERIFIED`.
- Modelos neurais separados: `AICORE_CONTAINER: ABSENT`, `AICORE_AVD: NOT_VERIFIED`.

### AFTER
- Coerência total em todo o conjunto SOT:
  - `BUILD`: `VERIFIED`
  - `TESTS`: `VERIFIED` (63/63 Robolectric)
  - `DEPLOY`: `VERIFIED` (APP_DEPLOY_BRIDGE)
  - `AVD_EXISTENCE`: `PLATFORM_REPORTED`
  - `APP_EXECUTION`: `PLATFORM_REPORTED`
  - `RUNTIME_OBSERVABILITY`: `UNAVAILABLE_FROM_CONTAINER`
  - `ANDROID_RUNTIME_INDEPENDENT_PROOF`: `NOT_VERIFIED`
  - `PHYSICAL_DEVICE`: `NOT_VERIFIED / REJECTED`
  - `CURRENT_MAX_PROOF_LEVEL`: `L2_HOST_JVM_VERIFIED` (+ `L3_APP_DEPLOY_VERIFIED` para entrega de artefato)

