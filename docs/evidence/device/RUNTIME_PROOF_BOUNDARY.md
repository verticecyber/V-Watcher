# Runtime Proof Boundary

**Data da Auditoria:** 2026-09-14  
**Ambiente:** Google AI Studio Cloud Build & Runtime Platform  
**Documento de Limite Epistêmico e de Prova Técnica**

---

## 1. BOUNDARY STATEMENT

The AI Studio environment provides an external Streaming Android Emulator through an `APP_DEPLOY_BRIDGE`.

The build container cannot access the emulator through a local ADB target, socket, Logcat API or screenshot API.

Therefore:

- **APK deployment is VERIFIED.**
- **Android runtime existence is PLATFORM_REPORTED.**
- **Independent runtime execution is NOT_VERIFIED.**
- **Physical-device execution is NOT_VERIFIED.**
- **Host JVM/Robolectric execution remains independently VERIFIED.**

**No runtime claim may be promoted above this boundary without new evidence.**

---

## 2. DETAILED CLASSIFICATION

| Dimensão | Classificação Técnica | Evidência Direta | Limitação Observada |
|---|---|---|---|
| **Build & Toolchain** | `VERIFIED` | Gradle 9.3.1 / Temurin JDK 21 gerou APK de 17.3MB (SHA256: `9bd86f36...`) | Nenhuma no host de compilação |
| **Testes de Contrato Android** | `VERIFIED (L2_HOST_JVM)` | 63/63 testes Robolectric aprovados sem falhas | Executados em runtime simulado (Shadows JVM), não em SO Android nativo |
| **Entrega de Artefato (Deploy)** | `VERIFIED (L3_APP_DEPLOY_VERIFIED)` | Control-plane API expõe `/build/outputs/apk/debug/app-debug.apk` que é consumido pelo orquestrador | Transferência de binário verificada; não atesta execução de código dentro do SO |
| **Existência do AVD** | `PLATFORM_REPORTED` | Orquestrador da plataforma inicia o Streaming Emulator e renderiza canvas WebRTC no navegador | Container não possui comunicação de rede direta com a VM do emulador |
| **Execução de Runtime Android** | `PLATFORM_REPORTED` | A plataforma relata inicialização da Activity e exibe o app no streaming para o usuário | O container não pode atestar independentemente o ciclo de vida do processo |
| **Observabilidade de Runtime** | `UNAVAILABLE_FROM_CONTAINER` | Sem socket ADB reverso, sem endpoint de Logcat e sem API de screenshot no control-plane | Toda observabilidade direta do agente encerra-se na fronteira do container gVisor |
| **Prova Independente de Runtime** | `NOT_VERIFIED` | Nenhuma captura independente de Logcat, pid ou dumpsys gerada pelo container | Requer canal direto de instrumentação atualmente ausente no ambiente |
| **Dispositivo Físico** | `NOT_VERIFIED / REJECTED` | `adb devices -l` retorna lista vazia; o streaming é comprovadamente AVD na nuvem | Afirmações de execução em dispositivo físico são categoricamente proibidas |

---

## 3. COMPONENT LEVEL REALITY

### 3.1 Telemetria (`com.example.telemetry.*`)
- **Code:** `IMPLEMENTED`
- **Host Execution (Robolectric):** `VERIFIED`
- **External Android Runtime:** `NOT_VERIFIED`
- **Physical Device:** `NOT_VERIFIED`

### 3.2 Sentinel (`com.example.sentinel.AndroidSentinel`)
- **`observeNow()`:** Implemented = `true`, Integrated = `true`, Host Verified = `true`, Android Runtime Verified = `false`
- **`startContinuousObserving()`:** Implemented = `true`, Integrated = `false`
- **Background Watch:** Implemented = `false` (sem Foreground Service no Manifest)

### 3.3 Raciocínio Determinístico E2E
- **Host JVM / Robolectric:** `VERIFIED` (5 regras clínicas auditadas, latência <10ms, fallback explícito)
- **External AVD Runtime:** `PLATFORM_REPORTED`
- **Independent Device Proof:** `NOT_VERIFIED`

### 3.4 Modelos On-Device (AICore / Gemini Nano / Gemma)
- **`AICORE_CONTAINER`:** `ABSENT` (não existe no container de compilação)
- **`AICORE_AVD`:** `NOT_VERIFIED` (inferido como ausente em imagens padrão de AVD, mas sem inspeção direta de pacote no SO do emulador)
- **`GEMINI_NANO`:** `STUB_HONEST` (probe de pacote presente no código; aciona fallback determinístico quando ausente)
- **`GEMMA`:** `STUB_HONEST` (`current_code_status: STUB`, `host_environment: unavailable`, `external_avd: not_verified`)

---

## 4. STRICT PROMOTION CEILING

Nenhuma afirmação pública, claim de marketing ou documentação interna pode ultrapassar o teto estrito:
- **Teto Máximo Comprovado:** `L2_HOST_JVM_VERIFIED` + `L3_APP_DEPLOY_VERIFIED`.
- O deploy do APK está confirmado e reproduzível.
- A execução no Android é **relatada pela plataforma** e visível ao usuário, mas **não verificada de forma independente pelo agente**.
