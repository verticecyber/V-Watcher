# Android Runtime Observability Report

**Data:** 2026-09-14  
**Status:** Concluído  
**Escopo:** Investigação aprofundada da camada de observabilidade, deploy do control-plane e limites de execução no Streaming Android Emulator.

---

## 1. ENVIRONMENT

- **Host do Agente:** Google Cloud Run container em sandbox gVisor (`Linux 4.19.0-gvisor x86_64`).
- **Toolchain:** Eclipse Adoptium Temurin-21.0.12, Gradle 9.3.1, AGP 9.1.1, Android SDK Platform-tools 37.0.1.
- **Rede Local:** Portas locais ativas:
  - `8080` (Nginx reverse proxy)
  - `8000` (Control Plane API Go binary)
  - `5037` (Daemon local ADB)
  - `25785` (Daemon Gradle JVM)
- **Target ADB:** `adb devices -l` retorna lista vazia (`List of devices attached`). Tentativa de conexão em `127.0.0.1:5555` resulta em `Connection refused`.

---

## 2. CONTROL_PLANE

- **Binário:** `/app/control-plane-api/control-plane-api` (Go 1.22+).
- **Rotas Mapeadas:**
  - Operacionais: `/health`, `/dev/status`, `/dev/logs`, `/dev/logs/buffered`, `/dev/exec`, `/dev/exec-stream`, `/dev/exec-input`, `/dev/exec-kill`, `/dev/install-package`, `/dev/env`, `/dev/lint`.
  - Sistema de Arquivos: `/fs/read`, `/fs/write`, `/fs/delete`, `/fs/move`, `/fs/list`, `/fs/hash`, `/fs/clear-app-directory`, `/fs/archive-build-artifact`.
  - Artefatos de Build: `/build/outputs/apk/debug/app-debug.apk` (`application/vnd.android.package-archive`), `/build/outputs/bundle/release/app-release.aab`.
  - Banco de Dados: `/cloudsql/startproxy`, `/cloudsql/update-schema`.
- **Rotas Inexistentes:** Não há rotas para `/screenshot`, `/logcat`, `/device_info`, `/launch`, `/shell` (direto no Android) ou `/event_stream` do runtime.

---

## 3. DEVICE

- **Tipo:** `EXTERNAL_STREAMING_EMULATOR` (Google Cloud Android Virtual Device).
- **Isolamento:** Desacoplado da rede do container. O emulador é hospedado na infraestrutura de streaming do Google Cloud e renderizado no navegador do usuário via protocolo WebRTC.
- **Identificação:** Pacote alvo `com.aistudio.vwatcher.hkmv`, targetSdk 36, minSdk 24.
- **Hardware:** Virtualizado (x86_64 ou arm64), sem unidade NPU física Tensor.

---

## 4. DEPLOY

- **Mecanismo:** `APP_DEPLOY_BRIDGE`.
- **Artefato:** `app/build/outputs/apk/debug/app-debug.apk`.
  - Tamanho: 17,347,499 bytes.
  - SHA256: `9bd86f3605d3add8091936559c20641c08176cc7aa9682cd9e6bc91e16c0e578`.
- **Orquestração:** Ao concluir o turno de escrita/compilação do agente, a plataforma AI Studio busca o APK no endpoint `/build/outputs/apk/debug/app-debug.apk` do container e comanda o deploy na sessão do Streaming Emulator.

---

## 5. LAUNCH

- **Mecanismo:** Automático pelo orquestrador do AI Studio.
- **Activity Inicial:** `.MainActivity` com intent filter `MAIN` e `LAUNCHER`.
- **Execução:** O sistema operacional Android do emulador inicializa o processo e instancia a árvore de UI Compose (`VWatcherApp`).
- **Evidência:** O container não recebe evento de confirmação via socket; a confirmação é visual no canvas do Streaming Emulator no navegador.

---

## 6. OBSERVABILITY

- **Mecanismos Existentes para o Agente:**
  - Build logs e output de comandos locais no container (`compile_applet`, `run_command`, `/dev/logs`).
  - Execução de suíte de testes Robolectric no host JVM (63 testes aprovados cobrindo contratos do framework).
- **Mecanismos Inexistentes para o Agente:**
  - Não há Logcat remoto acessível.
  - Não há RPC de captura de tela ou inspeção de processos no emulador.
- **Conclusão:** O runtime externo é visualmente observável pelo usuário no navegador, mas o agente não possui canal de telemetria reversa automatizada para o container gVisor.

---

## 7. TELEMETRY & SENTINEL

- **Implementação:** `AndroidSentinel.observeNow()` consolida 6 provedores com selo de proveniência (`ProviderProvenance`):
  1. `BatteryProvider` (via `ACTION_BATTERY_CHANGED`)
  2. `DeviceResourceProvider` (via `ActivityManager.MemoryInfo`)
  3. `NetworkTelemetryProvider` (via `ConnectivityManager`)
  4. `AppInventoryProvider` (via `PackageManager`)
  5. `AppUsageProvider` (gated por `PACKAGE_USAGE_STATS`)
  6. `SystemStateProvider` (via `android.os.Build`)
- **Status:** Testado e aprovado em nível L2 (Host JVM/Robolectric). No runtime externo, executa dentro do ciclo de vida Compose ativado pelo botão "Run Diagnostic Exam".

---

## 8. DETERMINISTIC_E2E

- **Caminho:** `Sentinel -> CanonicalObservation -> ImmuneBus -> DendriticCell -> ReasoningRouter -> DeterministicBackend -> AndroidRealityBoundary -> UI`.
- **Status:** Operacional. O backend determinístico processa as 5 regras clínicas de homeostase com latência inferior a 10ms.
- **Fallback:** Quando invocado com preferência para Gemini Nano ou Gemma, o router detecta a ausência dos pré-requisitos e aplica fallback explícito com motivo categorizado (`GEMINI_NANO_UNAVAILABLE` ou `GEMMA_UNAVAILABLE`).

---

## 9. ACTUATION (Reality Boundary)

- **Ações Permitidas e Seguras no Sandbox:**
  - `THROTTLE_INTERNAL_INFERENCE`: ajusta cadência interna do loop.
  - `ISOLATE_INTERNAL_SUBSYSTEM`: define flags em memória para suprimir provedores ruidosos.
  - `RECLAIM_INTERNAL_CACHE`: invoca limpeza de cache privado do app (`context.cacheDir.deleteRecursively()`).
  - `NAVIGATE_APP_SETTINGS`: dispara intent `ACTION_APPLICATION_DETAILS_SETTINGS`.
- **Ações Recusadas (Sandbox Enforcement):**
  - `KILL_EXTERNAL_PROCESS`: retorna estritamente `UNAVAILABLE` (o sistema Android impede que apps finalizem processos de terceiros).

---

## 10. AICORE, GEMINI_NANO & GEMMA

- **AICore (`com.google.android.aicore`):** `ENVIRONMENT_UNAVAILABLE`. Não presente na imagem AVD genérica do Google Cloud.
- **Gemini Nano:** `ENVIRONMENT_UNAVAILABLE`. O `GeminiNanoBackend` detecta a ausência do serviço e aciona o fallback determinístico transparente.
- **Gemma 2B:** `ENVIRONMENT_BLOCKED`. Pesos neurais (`models/gemma-2b-it-cpu.bin`) não empacotados no APK. O `GemmaBackend` detecta a ausência do arquivo e aciona o fallback determinístico.

---

## 11. LIMITATIONS

1. Não há ponte de debug/ADB entre o container gVisor e o Streaming Emulator.
2. O agente não possui comando de CLI para captura de screenshots do AVD.
3. Não há suporte a modelos neurais on-device na infraestrutura padrão de emuladores do AI Studio.

---

## 12. SOT_DELTA

- Atualizadas as matrizes de claims e prova para registrar a classificação formal de conectividade como `APP_DEPLOY_BRIDGE`.
- Mantido o nível de prova atual em **L2 (Host JVM/Robolectric)** para verificação automatizada pelo container, com reconhecimento do deploy no **Streaming Emulator** via interface visual do usuário no navegador.
- Rejeitada categoricamente qualquer tentativa de reivindicar conectividade com dispositivo físico ou presença ativa de AICore/Gemini Nano.
