# AI Studio Device Capability Matrix

**Auditoria:** 2026-09-14  
**Objeto:** Mapeamento de capacidades do ambiente atual (Build Container + Streaming Android Emulator) versus requisitos para execução e observabilidade.

---

## 1. Matriz de Capacidades do Ambiente

| Capacidade | Ambiente Atual (Container + Streaming AVD) | Suportado? | Evidência Técnica | Ambiente Exigido |
|---|---|---|---|---|
| **Build Android (Gradle/JDK)** | Container Cloud Run (gVisor) | **SIM** | `gradle :app:assembleDebug` bem-sucedido (APK 17MB gerado, 63/63 testes unitários passam) | Toolchain JVM 21 + Android SDK Platform-tools instalados |
| **Deploy de Aplicativo** | `APP_DEPLOY_BRIDGE` | **SIM** | Control Plane API expõe `/build/outputs/apk/debug/app-debug.apk`; orquestrador da plataforma instala no emulador após o turno do agente | AI Studio Platform Orchestrator |
| **Execução Android Runtime** | Streaming Android Emulator externo | **SIM** | APK instalado e executado no AVD da nuvem do Google; interface Compose renderizada via WebRTC no navegador | Instância AVD na infraestrutura Google Cloud |
| **Logs de Compilação/Dev** | Container stdout/stderr | **SIM** | `/dev/logs` e tarefas locais capturam stdout do Gradle e Nginx | Local gVisor Container |
| **Runtime Logcat do Android** | Isolamento gVisor / Sem socket ADB | **NÃO** | Daemon ADB local `tcp:5037` não possui targets (`adb devices -l` vazio); `control-plane-api` não implementa bridge de logcat | Túnel de porta ADB reverso ou serviço de exportação de logcat no control-plane |
| **Captura de Tela (Screenshot CLI)** | Renderização visual restrita ao cliente WebRTC | **NÃO** (para o agente) | Binário `control-plane-api` não possui rotas ou símbolos de screenshot; UI é visível ao usuário no navegador | Endpoint de captura de framebuffer no control-plane ou bridge ADB |
| **Acesso Shell ao Android** | Restrito ao shell do container | **NÃO** (para o AVD) | Comandos do agente rodam no Linux do container (`Linux 4.19.0-gvisor x86_64`), não no sistema operacional do AVD | Conexão `adb shell` ativa |
| **Telemetria de Bateria** | `BatteryProvider` via `ACTION_BATTERY_CHANGED` | **SIM** (no AVD e Host) | Registrado broadcast receiver; no emulador retorna status de AC/Virtual | Android Runtime operacional |
| **Telemetria de Recursos** | `DeviceResourceProvider` via `ActivityManager` | **SIM** (no AVD e Host) | Lê `getMemoryInfo()` da memória alocada para o sistema | Android Runtime operacional |
| **Telemetria de Rede** | `NetworkTelemetryProvider` via `ConnectivityManager` | **SIM** (no AVD e Host) | Lê capacidades ativas da interface de rede virtual do AVD | Permissão `ACCESS_NETWORK_STATE` declarada |
| **Inventário de Apps** | `AppInventoryProvider` via `PackageManager` | **SIM** (no AVD e Host) | Lê pacotes instalados com filtro de sistema | Permissão `QUERY_ALL_PACKAGES` declarada |
| **Uso de Apps** | `AppUsageProvider` via `UsageStatsManager` | **GATED** | Requer concessão explícita pelo usuário em *Special App Access -> Usage Access* | Permissão `PACKAGE_USAGE_STATS` concedida no SO |
| **Estado do Sistema** | `SystemStateProvider` via `android.os.Build` | **SIM** (no AVD e Host) | Lê propriedades de arquitetura e versão do SO Android | Android Framework |
| **Raciocínio Determinístico** | `DeterministicBackend` (5 regras clínicas) | **SIM** | Executado com sucesso tanto em testes unitários quanto em produção, com latência <10ms | JVM / Android Runtime |
| **Google AICore** | `com.google.android.aicore` | **NÃO** | Pacote ausente na imagem genérica do AVD do Google Cloud; restrito a hardware Pixel com chip Google Tensor | Dispositivo físico Pixel 8/Pro/9 ou imagem AVD específica com pacote AICore pré-instalado |
| **Gemini Nano On-Device** | `GeminiNanoBackend` | **NÃO** (Ambiente Bloqueado) | Ausência do serviço AICore; fallback determinístico engajado com transparência total | Ambiente com suporte a AICore operacional |
| **Gemma Local On-Device** | `GemmaBackend` | **NÃO** (Ambiente Bloqueado) | Arquivo de pesos (`models/gemma-2b-it-cpu.bin`) ausente no storage privado do app | Download/injeção de pesos (1.5GB+) e runtime compatível |

---

## 2. Requisitos Específicos para Subir de Nível de Prova

### Para atingir L3/L4 (Android Runtime & Emulator E2E Observável Diretamente pelo Agente):
1. Disponibilização de socket ADB reverso no container (`adb connect localhost:<port>`).
2. Ou inclusão de endpoint de observabilidade no `control-plane-api` (ex: `/android/logcat`, `/android/screenshot`, `/android/telemetry`).

### Para atingir validação de Gemini Nano:
1. Imagem de sistema Android com `com.google.android.aicore` ativo e NPU virtualizada/hardware Tensor.
2. Autorização de modelo on-device para a assinatura do APK.

### Para atingir validação de Gemma:
1. Inclusão dos pesos pré-quantizados no diretório de assets ou download em storage local.
2. Memória RAM suficiente no emulador (mínimo 4GB disponíveis para alocação do modelo).
