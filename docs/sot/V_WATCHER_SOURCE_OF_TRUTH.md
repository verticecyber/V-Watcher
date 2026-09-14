# V-Watcher — Fonte Operacional de Verdade (SOT)

**Estado:** ACTIVE  
**Data de corte:** 2026-09-14 12:35 UTC  
**Ambiente:** Google AI Studio Cloud Build & Runtime Environment  
**Branch/commit observado:** importação limpa do repositório `verticecyber/V-Watcher`  
**Escopo:** código, configuração, testes, artefatos e documentação presentes no repositório.  
**Alteração de produto:** nenhuma. Este SOT e os artefatos adjacentes são documentação operacional.

---

## 1. Governança e autoridade

### 1.1 Hierarquia de autoridade

Em caso de conflito, aplicar esta ordem:

1. **Código atual alcançável no caminho de execução** (`app/src/main`, manifest e configuração efetivamente aplicada).
2. **Testes atuais e seus resultados datados**, distinguindo teste executado agora de XML histórico.
3. **Artefato construído e inspeção independente** (APK/AAB, manifest dump, hashes), sempre com data e origem.
4. **Relatórios forenses e evidência reproduzível** que apontem para o código atual.
5. **Documentação operacional de release e referência.**
6. **Snapshots, planos, contratos, seeds e textos históricos.**

### 1.2 Estados de verdade usados neste SOT

- **IMPLEMENTED:** existe código concreto.
- **INTEGRATED:** existe caller alcançável no caminho atual.
- **TESTED:** sustentado por teste JVM/Robolectric executado com sucesso; não prova hardware físico.
- **PARTIAL:** apenas parte do contrato é executada.
- **NOT_VERIFIED:** não foi possível executar ou confirmar no ambiente atual.
- **PLANNED:** não há mecanismo atual; a capacidade é futura ou apenas proposta.
- **CONTRADICTED:** a afirmação do documento não corresponde ao código/configuração atuais.
- **SEED/SIMULATION:** dados criados pelo produto para inicialização ou demonstração, não observação do dispositivo.
- **UNAVAILABLE:** o nível de prova exige compilador, dispositivo ou outro recurso ausente.

### 1.3 Regras de prova

1. **Presença não é execução.** Código, dependência catalogada, classe ou comentário não demonstram runtime.
2. **Execução JVM não é prova física.** Robolectric testa contratos Android em um host simulado.
3. **Proveniência deve acompanhar o valor.** Fallback, permissão ausente e degradação não podem ser descritos como sensor físico normal.
4. **Fallback deve ser explícito.** `selectedBackend` e `actualBackendUsed` são distintos; a substituição deve manter status e motivo.
5. **Ação deve ser limitada ao mecanismo real.** Flag em memória não é isolamento de SO; `UNAVAILABLE` não é sucesso.
6. **Seeds e simulação não podem alimentar claims de detecção real.** O prefixo `[SIMULATION]` é parte do contrato de honestidade.
7. **Artefato histórico não é build atual.** APK/AAB gerados nesta corte possuem hash e timestamp verificados.
8. **Dispositivo e Fronteira de Runtime:** No container de build do AI Studio, o daemon ADB está ativo em `tcp:5037`, mas não há dispositivos locais conectados via socket ADB (`adb devices` vazio). A visualização do app é realizada pelo Streaming Android Emulator da plataforma no navegador via `APP_DEPLOY_BRIDGE`. **Deploy confirmado não é prova independente de runtime observado**: a entrega do APK é `VERIFIED`, a execução no AVD é `PLATFORM_REPORTED`, a observabilidade direta a partir do container é `UNAVAILABLE_BY_DESIGN` e a prova independente de runtime é `NOT_VERIFIED`. Claims de "dispositivo físico conectado" ou "runtime Android independente comprovado" são categoricamente rejeitados.

---

## 2. Identidade e configuração observadas

| Item | Estado atual | Fonte |
|---|---|---|
| Nome do projeto | `V-Watcher` | `settings.gradle.kts` |
| Namespace | `com.example` | `app/build.gradle.kts` |
| `applicationId` | `com.aistudio.vwatcher.hkmv` | `app/build.gradle.kts` |
| `minSdk` / `targetSdk` | 24 / 36 | `app/build.gradle.kts` |
| `compileSdk` | 36, minor 1 | `app/build.gradle.kts` |
| Versão | `versionCode 1`, `versionName 1.0` | `app/build.gradle.kts` |
| Activity exportada | `.MainActivity`, `MAIN` + `LAUNCHER` | `AndroidManifest.xml` |
| Permissões | `ACCESS_NETWORK_STATE`, `QUERY_ALL_PACKAGES`, `PACKAGE_USAGE_STATS` | `AndroidManifest.xml` |
| `INTERNET` | não declarado (zero network egress) | `AndroidManifest.xml` |
| Background service/worker/receiver/provider | não encontrado no manifest atual | `AndroidManifest.xml` |
| Persistência de casos/padrões | não existe; repository é RAM-only | `LocalImmuneMemoryRepository.kt` |
| Backend de modelo efetivamente operacional | determinístico com fallback explícito | `DeterministicBackend.kt`, router |

---

## 3. Runtime atual: caminho efetivo

```text
MainActivity / VWatcherApp
  -> VWatcherViewModel.uiState
    -> refreshRealTelemetry()
      -> AndroidSentinel.observeNow()
        -> BatteryProvider
        -> DeviceResourceProvider
        -> NetworkTelemetryProvider
        -> AppInventoryProvider
        -> AppUsageProvider
        -> SystemStateProvider
      -> CommunicationChannel.dispatchTelemetry()
      -> BaselineEngine.establishInitialBaseline()/evaluateSnapshot()
      -> BiomimeticImmuneSystem.processObservation()
        -> SentinelCell
        -> ImmuneBus
        -> PatternRecognitionCell
        -> RegulatoryTCell
        -> NeutrophilCell quando há estresse
        -> BCell
        -> THelperCoordinatorCell
        -> DendriticCell
          -> CommunicationChannel.sendReasoningRequest()
            -> ReasoningRouter
              -> Gemini Nano se READY (UNAVAILABLE no ambiente -> fallback)
              -> Gemma se READY (UNAVAILABLE no ambiente -> fallback)
              -> DeterministicBackend (EXECUTADO como baseline)
        -> CytotoxicEffectorCell quando os gates passam
        -> AndroidRealityBoundary
        -> MacrophageCell quando há pressão de memória
        -> ResolutionCell
        -> LocalImmuneMemoryRepository
        -> HomeostasisEngine
      -> atualização de uiState e UI Compose
```

---

## 4. Sentinel e telemetria

| Provider | O que lê | Limite operacional |
|---|---|---|
| `BatteryProvider` | `BatteryManager`/`PowerManager`: nível, carga, temperatura, saúde e power-save | Em fallback de intent: `isReal=false`, `isDegraded=true` |
| `DeviceResourceProvider` | `ActivityManager.MemoryInfo` e heap do processo | Heap não é RAM total física; devidamente segregado |
| `NetworkTelemetryProvider` | transporte, validação, VPN e capacidades via `ConnectivityManager` | Não inspeciona pacotes de terceiros nem payload |
| `AppInventoryProvider` | pacotes instalados e permissões declaradas via `PackageManager` | Declaração não prova uso de permissão em runtime |
| `AppUsageProvider` | Usage Stats das últimas 24 h | Sem concessão especial retorna `PERMISSION_REQUIRED` |
| `SystemStateProvider` | Build, patch, locale, timezone e interatividade | Descritores do sistema operacional |

API `startContinuousObserving()` existe, mas não há caller integrado na interface. Portanto: **API IMPLEMENTADA; watcher contínuo NÃO INTEGRADO**.

---

## 5. Reasoning e contratos de modelo

1. **`DeterministicBackend`:**
   - Implementado, integrado, testado e verificado ao vivo (`isRealOnDeviceModel = false`).
   - Avalia regras clínicas de temperatura, memória, sockets e economia de energia.
2. **`GeminiNanoBackend`:**
   - Detecta ausência de AICore (`com.google.android.aicore`).
   - Estado: `UNAVAILABLE`.
   - Inferência neural: **NÃO EXECUTADA / STUB**.
   - O roteador faz fallback explícito para o backend determinístico.
3. **`GemmaBackend`:**
   - Detecta ausência do arquivo de pesos `gemma-2b-it-cpu.bin`.
   - Estado: `UNAVAILABLE`.
   - Inferência neural: **NÃO EXECUTADA / STUB**.
   - O roteador faz fallback explícito para o backend determinístico.

---

## 6. Actuation e limites do Android

| Ação | Mecanismo | Status Sustentado |
|---|---|---|
| `THROTTLE_INTERNAL_INFERENCE` | Flag atômica em memória | `EXECUTED` |
| `ISOLATE_INTERNAL_SUBSYSTEM` | Conjunto em memória | `EXECUTED` |
| `RECLAIM_INTERNAL_CACHE` | Exclui `vwatcher_temp_*` do cache próprio e chama GC | `EXECUTED` |
| `NAVIGATE_APP_SETTINGS` | Dispara `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` | `EXECUTED` |
| `KILL_EXTERNAL_PROCESS` | Sandbox Android impede encerramento de apps de terceiros | **`UNAVAILABLE`** |
| Ação desconhecida | N/A | `DENIED` |

---

## 7. Memória e persistência

- `LocalImmuneMemoryRepository` é **RAM-only**.
- Contém 3 seeds iniciais.
- Validação estrita na inserção: rejeita simulações, campos incompletos, confiança <80%, telemetria em erro e duplicatas no intervalo de 30 segundos.
- Não usa Room, SQLite, DataStore ou SharedPreferences para casos e padrões dinâmicos.

---

## 8. Build e testes desta corte (2026-09-14)

- **Toolchain:** `Temurin-21.0.12+8`, `javac 21.0.12`, `Gradle 9.3.1`, `AGP 9.1.1`.
- **Build APK:** `gradle :app:assembleDebug` gerou `app-debug.apk` (17,347,499 bytes, SHA256 `9bd86f3605d3add8091936559c20641c08176cc7aa9682cd9e6bc91e16c0e578`). Nível de prova: **`L2_HOST_JVM_VERIFIED`**.
- **Suíte de Testes:** `gradle :app:testDebugUnitTest` executou **63 testes** com **100% de sucesso (0 falhas, 0 erros, 0 skipped)** em ~50 segundos. Nível de prova: **`L2_HOST_JVM_VERIFIED`**.
- **Topologia de Runtime & Conectividade:** Auditada formalmente em `docs/evidence/device/AI_STUDIO_ANDROID_BRIDGE_AUDIT.md` e `docs/evidence/device/RUNTIME_PROOF_BOUNDARY.md`:
  - **Classificação:** `APP_DEPLOY_BRIDGE` (Nível: **`L3_APP_DEPLOY_VERIFIED`**)
  - **Daemon ADB Local:** `tcp:5037` ativo no container, porém sem targets anexados (`List of devices attached` vazio).
  - **Mecanismo de Deploy:** O serviço `control-plane-api` expõe o binário APK na rota `/build/outputs/apk/debug/app-debug.apk`; a plataforma AI Studio orquestra a entrega do artefato para a sessão do Streaming Android Emulator exibido no navegador via WebRTC.
  - **Execução no AVD:** **`PLATFORM_REPORTED`** (a plataforma relata a inicialização da Activity e renderiza no navegador).
  - **Observabilidade Independente:** **`UNAVAILABLE_FROM_CONTAINER`** (o container não possui socket ADB nem streaming de Logcat bidirecional direto para o emulador).
  - **Prova Independente de Runtime:** **`NOT_VERIFIED`** (o agente não possui instrumentação direta para certificar o processo Android de forma independente do relato da plataforma).
  - **Dispositivo Físico:** **`NOT_VERIFIED / REJECTED`**.

---

## 9. CURRENT TRUTH

**CURRENT TRUTH:** O V-Watcher é um aplicativo Android Compose foreground com pipeline determinístico e biomimético experimental validado no host JVM via Robolectric (nível de prova `L2_HOST_JVM_VERIFIED`). O APK de depuração é compilado e entregue com sucesso à plataforma AI Studio via `APP_DEPLOY_BRIDGE` (`L3_APP_DEPLOY_VERIFIED`), onde a execução no Streaming Android Emulator é relatada pela plataforma (`PLATFORM_REPORTED`). A observabilidade independente do runtime Android a partir do container é indisponível por design (`UNAVAILABLE_FROM_CONTAINER`), tornando a prova independente de runtime Android `NOT_VERIFIED`. O backend determinístico é o único caminho de raciocínio operacional verificado no host; Gemini Nano e Gemma permanecem com o status honesto de `UNAVAILABLE` no container (e não verificados no AVD externo), sendo roteados para o fallback determinístico explícito sem interrupção. As ações de resposta restringem-se ao sandbox do app (limpeza de cache próprio, flags internas e abertura da tela de configurações do sistema); o encerramento forçado de processos de terceiros é categoricamente recusado com status `UNAVAILABLE`. Não há persistência em disco nem monitoramento em segundo plano implementados no momento.
