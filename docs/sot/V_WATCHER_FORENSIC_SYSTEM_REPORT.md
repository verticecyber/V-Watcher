# Relatório Forense do Sistema V-Watcher

**Data da auditoria:** 2026-09-14  
**Repositório:** `/media/juan/DATA/V-watcher`  
**Branch:** `main`  
**Commit observado:** `1c6ebaa` (`chore(repo): organize documentation and release evidence`)  
**Escopo:** Android, Gradle, Kotlin, recursos, testes, artefatos já presentes, documentação de evidência e verificações seguras no host.  
**Regra de prova:** código atual e saídas observadas prevalecem sobre planos, snapshots e relatórios históricos. Nenhum segredo foi reproduzido; valores sensíveis são tratados como **REDACTED**.

## 1. Veredicto executivo

O repositório contém um aplicativo Android Compose compilável em princípio, com ingestão foreground de telemetria real via APIs Android, um pipeline determinístico de análise, um barramento imune interno, telas funcionais e testes JVM/Robolectric previamente executados. O limite real é muito menor que a narrativa biomimética:

| Área | Classificação forense |
|---|---|
| App launcher/UI Compose | **IMPLEMENTADO; integração de estado funcional em código; runtime atual NOT VERIFIED** |
| Telemetria de bateria, RAM, rede, inventário e sistema | **IMPLEMENTADO; real quando executado no Android; prova física NOT VERIFIED** |
| Usage Stats | **IMPLEMENTADO PARCIAL**: depende de concessão especial do usuário |
| Sentinel `observeNow()` | **IMPLEMENTADO e integrado ao ViewModel** |
| Observação contínua | **IMPLEMENTADO como API, NÃO INTEGRADO**: nenhum caller de `startContinuousObserving()` foi encontrado |
| Backend determinístico | **IMPLEMENTADO, integrado e testado por artefatos JVM** |
| Gemini Nano | **DETECÇÃO HEURÍSTICA IMPLEMENTADA; inferência STUB/PARCIAL/BROKEN COMO REIVINDICAÇÃO DE MODELO** |
| Gemma | **PROBE DE ARQUIVO IMPLEMENTADO; runtime e inferência STUB/PLANNED** |
| Router/fallback | **IMPLEMENTADO e testado em JVM; dependência neural nunca comprovada** |
| Memória | **RAM-only, seed + gates; não é persistência durável; funcional apenas durante o processo** |
| Células imunes | **IMPLEMENTADAS em grande parte; integração do pipeline central funcional em código; várias mensagens/branches são mortas** |
| Actuation | **PARCIAL e honesta**: cache próprio, flags internas e Intent de Settings são reais; matar app externo é explicitamente **UNAVAILABLE** |
| Decoys/honeypots/diversão de tráfego | **NÃO IMPLEMENTADO**; UI declara isso |
| Isolamento de terceiros | **NÃO IMPLEMENTADO em nível de OS**; apenas flag de revisão no app |
| Background watcher | **NÃO IMPLEMENTADO**; produto é foreground-only |
| Testes | **63 testes, 0 falhas/erros em XML pré-existente; execução nesta sessão BLOQUEADA pelo JDK sem `javac`** |
| APK/AAB | **Artefatos pré-existentes encontrados; build corrente não reproduzível neste host** |
| Dispositivo/emulador | **NÃO DISPONÍVEL**: `adb devices -l` retornou somente cabeçalho |
| Segurança/segredos | **Nenhum segredo real encontrado no scan executado; configuração contém apenas placeholder comentado e referências a ambiente** |

**Conclusão:** o sistema atual é um **observador foreground local, predominantemente determinístico, com UI e pipeline imune experimental**. Não é comprovado como antivírus, firewall, sandbox, honeypot, agente background, sistema de isolamento de outros apps ou runtime de Gemini Nano/Gemma.

## 2. Legenda de estados usada neste relatório

- **IMPLEMENTADO:** há código concreto no repositório.
- **INTEGRADO:** há caminho de chamada real entre componentes atuais.
- **FUNCIONAL:** comportamento executável demonstrado por teste ou artefato verificável.
- **TESTADO:** há teste/saída observável; o ambiente e o nível de prova são explicitados.
- **REAL:** usa API/estado do host, não fixture estática, quando o caminho é executado.
- **MOCK/SIMULAÇÃO/SEED:** dados sintéticos, demonstrativos ou pré-carregados.
- **PARCIAL:** somente parte do contrato é executada.
- **STUB:** contrato e aparência existem, mas a operação central não existe.
- **PLANNED:** descrito como futuro, sem implementação atual.
- **UNKNOWN/NOT VERIFIED:** não foi possível provar.
- **BROKEN:** a reivindicação ou caminho não funciona como descrito, ou a verificação corrente falhou.

## 3. Estado Git e método

Comandos executados:

```text
git --no-pager status --short --branch
## main

git --no-pager log -8 --oneline --decorate
1c6ebaa (HEAD -> main, origin/main) chore(repo): organize documentation and release evidence
c873a92 Initial commit
```

Antes da criação deste arquivo, o worktree estava limpo (`git status --short` sem linhas). O relatório é a única modificação intencional desta auditoria.

Foram inspecionados:

1. `app/build.gradle.kts`, `build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`, `gradle.properties`.
2. `app/src/main/AndroidManifest.xml`, recursos XML e todos os fontes Kotlin sob `app/src`.
3. Todos os testes em `app/src/test` e `app/src/androidTest`.
4. `README.md`, `SECURITY.md`, `.env.example` e documentação de release/SOT/evidência.
5. Artefatos existentes sob `app/build`.
6. Checks seguros: Git, `./gradlew --version`, testes/build/lint tentados, static scan existente, scan de strings/secrets, `adb`.

## 4. Identidade, configuração e pacote

**Evidência primária:** `app/build.gradle.kts:9-16,21-49,65-95`.

| Campo | Valor observado | Estado |
|---|---|---|
| `rootProject.name` | `V-Watcher` (`settings.gradle.kts:18`) | IMPLEMENTADO |
| Namespace | `com.example` (`app/build.gradle.kts:9`) | IMPLEMENTADO, identidade técnica genérica |
| `applicationId` | `com.aistudio.vwatcher.hkmv` (`:12`) | IMPLEMENTADO; identidade de produção ainda provisória conforme `docs/release/RELEASE_READINESS_MATRIX.md` |
| `minSdk` | 24 (`:13`) | CONFIGURADO |
| `targetSdk` | 36 (`:14`) | CONFIGURADO; comportamento em Android 16 NOT VERIFIED |
| `compileSdk` | 36 minor 1 (`:9`) | CONFIGURADO |
| `versionCode/name` | `1` / `1.0` (`:15-16`) | CONFIGURADO, sem estratégia de release comprovada |
| Activity launcher | `.MainActivity`, exported, MAIN/LAUNCHER (`AndroidManifest.xml:24-38`) | IMPLEMENTADO |
| Backup | habilitado no manifest, mas domínios excluídos em `backup_rules.xml` e `data_extraction_rules.xml` | CONFIGURADO; postura stateless |
| Release signing | env `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD` (`:21-38`) | IMPLEMENTADO de forma segura; assinatura Play real NOT VERIFIED |
| Debug signing | `debug.keystore` local e credenciais padrão Android (`:39-44`) | FUNCIONAL para debug local; não é prova de distribuição |
| Internet | não declarada | ZERO endpoint de rede do app, conforme manifest |
| Plugins efetivamente usados | Android application, Kotlin Compose, Roborazzi (`app/build.gradle.kts:1-4`) | IMPLEMENTADOS |
| Plugins catalogados mas não aplicados no módulo | secrets/google-services no `build.gradle.kts:3-7`, sem uso real no app | INERTES/DEAD SURFACE |

### Permissões

`app/src/main/AndroidManifest.xml:8-20` declara somente:

1. `ACCESS_NETWORK_STATE`: leitura de estado/capacidades da rede.
2. `QUERY_ALL_PACKAGES`: inventário local de pacotes.
3. `PACKAGE_USAGE_STATS`: acesso especial para `UsageStatsManager`.

Não foram encontrados `INTERNET`, `VpnService`, `AccessibilityService`, `NotificationListenerService`, câmera, microfone, localização, storage, foreground service, receiver, provider ou worker no manifest. Portanto, inspeção profunda de pacotes, captura de tráfego, suspensão de apps externos e monitoramento background não são comprovados.

## 5. Inventário completo do código atual

Contagem observada:

- **55** arquivos Kotlin de produção em `app/src/main/java`.
- **12** arquivos Kotlin de teste unitário/Robolectric em `app/src/test`.
- **1** teste instrumentado em `app/src/androidTest`.
- **19** recursos em `app/src/main/res` (XML/WebP/PNG).
- **68** arquivos Kotlin no total sob `app/src` (produção + testes).
- `VWatcherViewModel.kt` possui 1.218 linhas; `BiomimeticImmuneSystem.kt` 378; `Sentinel.kt` 275; `AndroidRealityBoundary.kt` 197; `LocalImmuneMemoryRepository.kt` 202. Não há gate de tamanho Kotlin equivalente ao gate Python da constituição.

### Produção por pacote

| Pacote/arquivo | Conteúdo | Estado forense |
|---|---|---|
| `com/example/MainActivity.kt` | Activity única, Compose, tabs e dialogs | IMPLEMENTADO/INTEGRADO |
| `communication/CommunicationChannel.kt` | flows, contadores, ring buffers de 20, router | IMPLEMENTADO/INTEGRADO |
| `communication/CommunicationModels.kt` | status, fallback, provenance, requests/responses | IMPLEMENTADO |
| `domain/DeviceState.kt` | snapshot agregado de domínio | IMPLEMENTADO |
| `model/VWatcherModels.kt` | enums e DTOs da UI | IMPLEMENTADO |
| `sentinel/CanonicalObservation.kt` | contrato canônico, freshness e provenance | IMPLEMENTADO |
| `sentinel/Sentinel.kt` | coleta dos seis providers e modo contínuo | `observeNow` INTEGRADO; modo contínuo NÃO INTEGRADO |
| `telemetry/BatteryProvider.kt` | BatteryManager/PowerManager | REAL quando Android executa |
| `telemetry/DeviceResourceProvider.kt` | ActivityManager + heap JVM | REAL/PARCIAL |
| `telemetry/NetworkTelemetryProvider.kt` | ConnectivityManager/NetworkCapabilities | REAL para link; inspeção de pacotes NÃO IMPLEMENTADA |
| `telemetry/AppInventoryProvider.kt` | PackageManager e permissões declaradas | REAL com `QUERY_ALL_PACKAGES` |
| `telemetry/AppUsageProvider.kt` | UsageStats 24h, acesso especial | REAL/PARCIAL/permission-gated |
| `telemetry/SystemStateProvider.kt` | Build, patch, locale, timezone, interatividade | REAL |
| `telemetry/DeviceTelemetryProvider.kt` | agregador alternativo de seis providers | IMPLEMENTADO, NÃO INTEGRADO; nenhum caller encontrado |
| `reasoning/ReasoningBackend.kt` | contrato backend | IMPLEMENTADO |
| `reasoning/ReasoningModels.kt` | readiness, assessment, engine state | IMPLEMENTADO |
| `reasoning/DeterministicBackend.kt` | regras térmicas, RAM, socket, power saver | IMPLEMENTADO/INTEGRADO/TESTADO por XML prévio |
| `reasoning/GeminiNanoBackend.kt` | probe de pacote AICore e resposta canned | DETECÇÃO PARCIAL; inferência STUB |
| `reasoning/GemmaBackend.kt` | probe de arquivo e resposta canned | DETECÇÃO PARCIAL; inferência STUB |
| `reasoning/ReasoningRouter.kt` | seleção, timeout, mutex e fallback explícito | IMPLEMENTADO/INTEGRADO |
| `reasoning/ResourceGuardrails.kt` | bateria, RAM, cooldown, contexto imune | IMPLEMENTADO/INTEGRADO |
| `reasoning/OnDeviceReasoningEngine.kt` | façade e estado de backend | IMPLEMENTADO; warmup/release sem ciclo de vida da Activity comprovado |
| `immune/ImmuneEventContract.kt` | 12 roles, payloads e mensagens | IMPLEMENTADO; muitos payloads não despachados |
| `immune/ImmuneBus.kt` | SharedFlow, buffer 128, storm flag >30/s | IMPLEMENTADO; storm não muda o fluxo por si só |
| `immune/BiomimeticImmuneSystem.kt` | orquestração PRR→regulatório→células→resolução | IMPLEMENTADO/INTEGRADO em `refreshRealTelemetry` |
| `immune/PatternRecognitionCell.kt` | thermal, memória, rede não validada | IMPLEMENTADO |
| `immune/NeutrophilCell.kt` | throttle interno em estresse agudo | IMPLEMENTADO |
| `immune/MacrophageCell.kt` | limpa arquivos `vwatcher_temp_` do próprio cache | IMPLEMENTADO, efeito dependente de artefatos existentes |
| `immune/DendriticCell.kt` | monta antígeno e envia request ao router | IMPLEMENTADO/INTEGRADO |
| `immune/THelperCoordinatorCell.kt` | veto/regra de evidência/escalada | IMPLEMENTADO |
| `immune/BCell.kt` | matching textual contra memória | IMPLEMENTADO, heurístico |
| `immune/NaturalKillerCell.kt` | API de integridade interna/externa | IMPLEMENTADO, caller produtivo não encontrado |
| `immune/CytotoxicEffectorCell.kt` | gates de freshness/confidence/evidence | IMPLEMENTADO/PARCIAL |
| `immune/RegulatoryTCell.kt` | storm/bateria/RAM/cooldown | IMPLEMENTADO/INTEGRADO |
| `immune/ResolutionCell.kt` | transições graduais e baseline | IMPLEMENTADO; resolução depende de ciclos posteriores |
| `immune/HomeostasisModel.kt` | sete dimensões e macrostate | IMPLEMENTADO/INTEGRADO |
| `immune/ImmuneCells.kt` | Sentinel/Receptor/Context/Response/Memory legados | IMPLEMENTADO, em grande parte DEAD/legacy |
| `immune/AndroidRealityBoundary.kt` | ações próprias e Settings Intent | IMPLEMENTADO/PARCIAL |
| `memory/LocalImmuneMemoryRepository.kt` | seed, gates e estruturas em memória | IMPLEMENTADO; NÃO persistente |
| `viewmodel/VWatcherViewModel.kt` | estado, refresh, exame, simulação, flags de UI | IMPLEMENTADO; mistura real + seed + simulação |
| `ui/components/ClinicalComponents.kt` | cards, gauge, células, canvas biomimético | IMPLEMENTADO visual |
| `ui/screens/*.kt` | Home, Exam, Cases, Immune, Memory, Diagnostics, sheets | IMPLEMENTADO visual/estado |
| `ui/screens/UiClaimBindings.kt` | normalização de claims backend→UI | IMPLEMENTADO |
| `ui/theme/*.kt` | tema Compose e cores | IMPLEMENTADO |

### Testes

Arquivos atuais:

1. `consumption/ConsumptionTest.kt` — 8 métodos.
2. `ExampleRobolectricTest.kt` — 1.
3. `ExampleUnitTest.kt` — 1.
4. `fusion/FusionTest.kt` — 4.
5. `GreetingScreenshotTest.kt` — 1.
6. `HandoffFoundationTest.kt` — 5.
7. `honesty/HonestyTest.kt` — 5.
8. `immune/BiomimeticAdversarialImmuneTest.kt` — 9.
9. `immune/BiomimeticImmuneSystemTest.kt` — 10.
10. `petscan/PetScanTraceTest.kt` — 3.
11. `uibinding/UiBindingTest.kt` — 9.
12. `viewmodel/ViewModelHarnessTest.kt` — 7.
13. `androidTest/.../ExampleInstrumentedTest.kt` — 1 fonte instrumentada.

O XML existente `app/build/test-results/testDebugUnitTest/*.xml` soma **63 testes, 0 skipped, 0 failures, 0 errors**, com timestamps de 2026-09-12. Isso é evidência de uma execução anterior, não de execução bem-sucedida nesta sessão.

## 6. Arquitetura real e fluxo de execução

### Topologia efetiva

```text
MainActivity
  -> VWatcherApp
    -> VWatcherViewModel.uiState
      -> refreshRealTelemetry()
        -> AndroidSentinel.observeNow()
          -> Battery/Resource/Network/Inventory/Usage/System providers
        -> CommunicationChannel.dispatchTelemetry()
        -> BaselineEngine.establishInitialBaseline/evaluateSnapshot()
        -> BiomimeticImmuneSystem.processObservation()
          -> SentinelCell
          -> ImmuneBus
          -> PRR
          -> RegulatoryTCell
          -> Neutrophil (se estresse)
          -> BCell
          -> THelper
          -> Dendritic
            -> CommunicationChannel.sendReasoningRequest()
              -> ReasoningRouter
                -> Gemini Nano se READY
                -> Gemma se READY
                -> DeterministicBackend caso contrário
          -> Cytotoxic/AndroidRealityBoundary quando elegível
          -> Macrophage
          -> Resolution
          -> LocalImmuneMemoryRepository
          -> HomeostasisEngine
      -> uiState real + cases + cells + telemetry
```

**Evidência:** `VWatcherViewModel.kt:76-111,113-394`; `Sentinel.kt:50-183`; `BiomimeticImmuneSystem.kt:97-349`; `ReasoningRouter.kt:38-216`.

### End-to-end trace nominal

1. `VWatcherViewModel` inicializa `AndroidSentinel`, `AndroidOnDeviceReasoningEngine`, `LocalImmuneMemoryRepository` e `BiomimeticImmuneSystem` (`VWatcherViewModel.kt:76-105`).
2. No `init`, chama `reasoningEngine.checkAvailability()` e `refreshRealTelemetry()` (`:100-111`).
3. `AndroidSentinel.observeNow()` cria `observationId`, coleta seis providers serialmente, registra `ProviderHealth`, provenance e freshness (`Sentinel.kt:50-183`).
4. O ViewModel despacha a observação para o canal, cria `DeviceTelemetrySnapshot`, estabelece/evalua baseline e chama uma única entrada imune (`VWatcherViewModel.kt:113-155`).
5. O PRR gera evidência somente para temperatura >42°C, low memory/<200MB ou rede não validada (`PatternRecognitionCell.kt:27-96`).
6. O T-Helper decide se escala; em evidência não vazia recomenda `ISOLATE_INTERNAL_SUBSYSTEM` (`THelperCoordinatorCell.kt:38-103`).
7. Dendritic monta contexto e envia `ReasoningRequest` (`DendriticCell.kt:38-91`).
8. O Router retorna modelo apenas se backend estiver READY e guardrails permitirem; caso contrário retorna `ReasoningResponse` explícita com `actualBackendUsed=DETERMINISTIC` (`ReasoningRouter.kt:38-216`).
9. O effector executa somente dentro dos gates, mas o argumento usado pela pipeline é `subsystem=EXPERIMENTAL_SOCKETS`; não há VpnService/socket interception correspondente (`BiomimeticImmuneSystem.kt:176-207`; `AndroidRealityBoundary.kt:34-196`).
10. O caso é criado/atualizado em mapa RAM; resolução exige observação posterior sem evidência e normalização (`BiomimeticImmuneSystem.kt:210-307`; `ResolutionCell.kt:33-106`).
11. Homeostasis gera macrostate e o ViewModel alimenta guardrails deny-only (`BiomimeticImmuneSystem.kt:311-349`; `VWatcherViewModel.kt:326-394`).
12. UI renderiza `uiState` com collectors lifecycle-aware (`MainActivity.kt:44-66`).

### O que não ocorre nesse trace

- Nenhum socket é aberto pelo app.
- Nenhum pacote é inspecionado.
- Nenhum tráfego é redirecionado a decoy.
- Nenhum processo de terceiro é morto ou suspenso.
- Nenhum serviço background mantém observação.
- Nenhum modelo neural é executado pelo código atual.
- Nenhum estado sobrevive garantidamente à morte do processo.

## 7. Sentinel e telemetria

### Sentinel

`AndroidSentinel.observeNow()` é uma implementação concreta, com seis coletores, `ProviderHealth`, `FreshnessInfo`, provenance e `SharedFlow` de eventos (`Sentinel.kt:30-183`). Isso é **IMPLEMENTADO/INTEGRADO** no caminho foreground.

`startContinuousObserving()` existe (`Sentinel.kt:188-210`), usa `scope.launch`, loop e `delay(intervalMs)`, mas grep de call sites encontrou apenas a definição. Logo é **API IMPLEMENTADA, integração NÃO VERIFICADA/NÃO EXISTENTE**.

### Providers e limites

| Provider | Fonte | Verdade | Limite |
|---|---|---|---|
| Battery | `BatteryManager`, `PowerManager` (`BatteryProvider.kt:11-110`) | REAL em dispositivo | fallback de Intent nulo marca `isReal=false`, mas fornece valores de conforto |
| Recursos | `ActivityManager.MemoryInfo` + `Runtime` (`DeviceResourceProvider.kt:9-66`) | REAL/PARCIAL | heap do V-Watcher não é RAM física; fallback JVM é degraded |
| Rede | `ConnectivityManager`/`NetworkCapabilities` (`NetworkTelemetryProvider.kt:9-79`) | REAL para transporte/capacidade | `advancedPacketInspectionAvailable=false`; requereria VPN autorizada |
| Inventário | `PackageManager.getInstalledPackages(GET_PERMISSIONS)` (`AppInventoryProvider.kt:11-83`) | REAL, condicionado a visibilidade/política | permissões declaradas, não uso runtime ou tráfego |
| Usage | `UsageStatsManager` últimas 24h (`AppUsageProvider.kt:11-142`) | REAL se usuário conceder | sem grant retorna `PERMISSION_REQUIRED`, `isReal=false` |
| Sistema | `Build`, patch, `PowerManager` (`SystemStateProvider.kt:9-52`) | REAL | descriptors, não attestation criptográfica |

### Baseline e fail-closed

O ViewModel reduz score em memória baixa, temperatura >42°C, candidatos, falhas críticas e observação degradada (`VWatcherViewModel.kt:157-199`). Falha de provider não é convertida silenciosamente em “saudável”; isso é uma decisão correta e **IMPLEMENTADA**. Contudo, alguns providers usam valores fallback (por exemplo Battery Intent nulo com 100%/25°C), portanto a UI deve preservar o status de disponibilidade e não tratar o valor fallback como medição real.

## 8. Sentinel, comunicação e observabilidade

`DefaultCommunicationChannel`:

- despacha telemetria e requests (`CommunicationChannel.kt:57-99`);
- preserva correlation IDs, status e provenance;
- mantém ring buffers de 20 eventos/respostas (`:49-54,57-99`);
- expõe `SharedFlow`s com replay 1.

Classificação: **IMPLEMENTADO/INTEGRADO no refresh**. Entretanto, grep encontrou somente o collector de `bus.messages` em `BiomimeticImmuneSystem.kt:80-91`; não há collector produtivo para `telemetryEvents` ou `reasoningResponses`. Os flows existem, mas não constituem observabilidade persistente.

`ImmuneBus` tem buffer 128, `DROP_OLDEST`, contador de drops e storm quando a taxa passa de 30/s (`ImmuneBus.kt:24-88`). Isso é **IMPLEMENTADO**, mas o storm é principalmente um sinal: o `RegulatoryTCell` lê o estado durante `processObservation`; não existe um daemon independente que encerre ou recupere um storm.

## 9. Reasoning, Gemini Nano, Gemma e matriz de modelos

### Router

`ReasoningRouter.selectBackend()` prioriza Nano, depois Gemma, e cai deterministicamente; `routeAndExecute()` registra timeout, lock, falha e backend real (`ReasoningRouter.kt:38-216`). A distinção `selectedBackend` versus `actualBackendUsed` é um ponto forte e **IMPLEMENTADO/TESTADO por testes prévios**.

### Gemini Nano

`GeminiNanoBackend.checkAvailability()` procura apenas `com.google.android.aicore` via `PackageManager` (`GeminiNanoBackend.kt:37-63`). `initialize()` somente verifica se o pacote está enabled (`:65-90`). Não há dependência ML Kit GenAI, `checkStatus()`, download, warmup de modelo, AIDL binding ou `generateContent`.

Em `execute()` (`:92-173`), o código:

- cria provenance alegando `isRealOnDeviceModel=true`;
- se não READY, retorna erro/fallback;
- se READY, constrói uma string em `buildPromptFromObservation()` (`:175-186`), mas não envia a prompt a runtime;
- retorna assessment constante `NORMAL`, confidence 92, `NO_ACTION`.

**Classificação:** probe de disponibilidade **PARCIAL/HEURÍSTICA**; inferência **STUB**; claim de modelo executado **BROKEN/NOT VERIFIED**. O campo de provenance é enganoso se a condição artificial de “pacote instalado/enabled” ocorrer sem runtime de inferência.

### Gemma

`GemmaBackend` procura `filesDir/models/gemma-2b-it-cpu.bin` ou `filesDir/gemma-2b-it-cpu.bin` (`GemmaBackend.kt:38-66`). `initialize()` marca READY se o arquivo existir, com comentário explícito de que MediaPipe/LiteRT não está implementado (`:68-89`). `execute()` devolve assessment constante `NORMAL`, confidence 90 (`:91-169`).

Não há pesos no repositório, dependência LiteRT-LM/MediaPipe ou validação de formato/hash. **Classificação:** file probe **PARCIAL**; runtime/inferência **STUB/PLANNED**; READY poderia ser falso-positivo se um arquivo arbitrário existir.

### Determinístico

`DeterministicBackend` é o backend funcional de fato (`DeterministicBackend.kt:19-142`):

- temperatura >42°C → `SUSPICIOUS`;
- socket/packet/burst no candidato → `SUSPICIOUS`;
- low memory ou >88% → `BENIGN_ANOMALY`;
- power saver com bateria >50% → `BENIGN_ANOMALY`;
- senão `NORMAL`.

Ele marca `isRealOnDeviceModel=false`, `executionMode=deterministic`, incrementa contador e usa valores da observação. **IMPLEMENTADO/INTEGRADO/TESTADO em JVM; não é IA neural.**

### Matriz de realidade

| Backend/capacidade | Declarado | Detectado | Inicializado | Inferência real | Testado em dispositivo |
|---|---:|---:|---:|---:|---:|
| Gemini Nano/AICore | SIM em docs/código | heurística por pacote | somente `ApplicationInfo.enabled` | **NÃO** | **NÃO** |
| Gemma 2B legado | SIM em código | arquivo não vazio | flag booleana | **NÃO** | **NÃO** |
| Determinístico | SIM | sempre READY | SIM | regras, não modelo | **NÃO** |
| Offline | pretendido | sem rede declarada | parcial | somente determinístico comprovado | **NÃO** |
| Tool calling | PLANNED | não há runtime | NÃO | NÃO | NÃO |

A matriz oficial em `docs/release/V_WATCHER_MODEL_MATRIX.md` e `docs/release/V_WATCHER_ONDEVICE_AI_RUNTIME.md` confirma a mesma conclusão: as linhas neurais são stubs e a prova física está ausente. Os documentos são corroborativos, não substituem execução.

## 10. Estado, memória e persistência

`LocalImmuneMemoryRepository` cria `MutableStateFlow` de patterns/cases, carrega três seeds em `init`, aplica gates de sintético, campos incompletos, confidence <80, telemetria indisponível/erro e debounce de 30s (`LocalImmuneMemoryRepository.kt:32-202`).

**Realidade:**

- é memória do processo (`StateFlow` + `ConcurrentHashMap`);
- não usa Room, SQLite, DataStore, SharedPreferences ou arquivo para patterns/cases;
- `context` é armazenado, mas não usado para persistir;
- `addCase`/`resolveCase` atualizam apenas RAM;
- backup rules explicitamente excluem banco/arquivo e documentam stateless (`app/src/main/res/xml/backup_rules.xml:1-8`, `data_extraction_rules.xml:1-15`);
- morte do processo perde casos e padrões adicionados;
- os três seeds voltam em cada nova instância.

Logo, “memória imune” é **IMPLEMENTADA/PARCIAL/RAM-ONLY**, não persistência durável. O antigo `docs/evidence/validation/CLAIM_PROOF_MATRIX.json` afirma Room/SQLite persistente, mas isso contradiz o código atual e deve ser tratado como evidência histórica obsoleta ou inconsistente.

## 11. Células imunes e actuation

### Pipeline realmente conectado

`BiomimeticImmuneSystem.processObservation()` (`:97-349`) chama, em ordem:

1. `SentinelCell.observe`.
2. `ImmuneBus.dispatch(TelemetryArrival)`.
3. `PatternRecognitionCell.scan`.
4. `ImmuneBus.dispatch(PatternDetected)` se houver evidência.
5. `RegulatoryTCell.evaluateEscalationRequest`.
6. `NeutrophilCell.respondToAcuteStress`.
7. `BCell.bindAntibody`.
8. `THelperCoordinatorCell.coordinate`.
9. `DendriticCell.assembleAntigenPresentation` + `presentToReasoningSubstrate`.
10. `CytotoxicEffectorCell.executeAuthorizedAction` quando elegível.
11. `MacrophageCell.scavengeAndRecycle`.
12. `ResolutionCell.verifyBaselineReturn`.
13. `MemoryCell` + repository.
14. `HomeostasisEngine.evaluate`.

Isso é **INTEGRADO em código** e coberto por testes unitários/Robolectric prévios. Não é prova de comportamento em hardware.

### Actuators

`AndroidRealityBoundary.executeRealAction()` (`AndroidRealityBoundary.kt:34-196`) implementa:

- `THROTTLE_INTERNAL_INFERENCE`: flag interna real;
- `ISOLATE_INTERNAL_SUBSYSTEM`: conjunto interno em memória;
- `RECLAIM_INTERNAL_CACHE`: remove apenas arquivos próprios `vwatcher_temp_*`, soma bytes;
- `NAVIGATE_APP_SETTINGS`: lança Intent real;
- `RESET_SUBSYSTEM_ISOLATION`: limpa flags;
- ação desconhecida: `DENIED`;
- `KILL_EXTERNAL_PROCESS`: `UNAVAILABLE`, explicitando sandbox Android e recomendando Settings (`:151-177`).

Portanto “isolamento” de app de terceiro é **não implementado**. `VWatcherViewModel.isolateApp()` só altera `AppRecord`, cria case e declara “nenhuma ação OS” (`VWatcherViewModel.kt:754-798`).

### Células parcialmente mortas

- `NaturalKillerCell.inspectComponentIntegrity()` existe, mas grep não encontrou caller de produção; contador tende a permanecer zero.
- `ImmunePayload` declara `RapidResponseRequested`, `AntigenPresented`, `ReasoningDispatched`, `ReasoningConcluded`, `CompromiseSuspected`, `CoordinationDirective`, `AntibodyMatch`, `EffectorActionRequest`, `EffectorActionCompleted`, `CleanupCompleted`, `RegulatorySuppression` e `ResolutionTransition`, mas a pipeline atual despacha apenas `TelemetryArrival`/`PatternDetected` de forma efetiva.
- `ImmuneCells.kt` mantém `ReceptorCell`, `ContextCell` e `ResponseCell` legados; o ViewModel comenta que a pipeline antiga foi aposentada (`VWatcherViewModel.kt:82-89`), porém as classes continuam no inventário.

## 12. Homeostase, thresholds e falhas de lógica

`HomeostasisEngine` representa energia, memória, rede, estabilidade, segurança, integridade, atividade imune e carga de incidentes (`HomeostasisModel.kt:1-296`). Ele pode retornar `UNKNOWN`, `DEGRADED`, `STRESSED`, `ACTIVE_DEFENSE`, `RECOVERING`, `WATCH` ou `HOMEOSTATIC`.

Pontos comprovados:

- provider crítico indisponível/erro produz fail-closed (`HomeostasisModel.kt:109-124`);
- battery <15% discharging e memória <250MB geram estresse (`:151-188`);
- storm >25 msg/s, heap V-Watcher >200MB ou `busStorming` marcam auto-destabilização (`:147-149`);
- guardrails usam limiares próprios: low battery <15, memory low ou >92%, cooldown 2500ms (`ResourceGuardrails.kt:24-113`);
- PRR usa `<200MB`, resolução usa `>=250MB` e temperatura `<=40°C`, e outras regras usam >42°C, >88%, >92%.

Isso é **IMPLEMENTADO**, mas os thresholds não são uma política única; há bandas divergentes e nenhuma calibração física. Confidence 80/85/92/98 é score heurístico, não probabilidade calibrada. `docs/evidence/validation/NEGATIVE_FINDINGS.json` reconhece essa limitação.

## 13. UI e claims

`MainActivity.kt:29-211` entrega uma Activity Compose com cinco tabs: Home, Exam, Cases, Immune e Memory; abre diagnostics, exam dialog e notifications sheet.

### UI funcional

- renderização baseada em `uiState` via `collectAsStateWithLifecycle`;
- botões chamam `runDeviceCheck`, `simulateUnusualActivity`, `refreshRealTelemetry`, `isolateApp`, `releaseApp`, `resolveCase`;
- `UiClaimBindings.kt:16-97` evita alguns claims verdes antes da observação e explicita “RAM-only”/“no integrity attestation”.

### UI seed/simulada

`createInitialState()` (`VWatcherViewModel.kt:880-1218`) preenche antes da primeira observação:

- quatro apps fictícios, incluindo `com.android.updater.sim`;
- permissões, conexões, decoys e case histórico;
- três patterns de memória;
- sete pontos de histórico;
- notificação de telemetria “initialized”.

Se o inventário real vier vazio, `refreshRealTelemetry()` mantém a lista anterior em vez de retornar lista vazia (`VWatcherViewModel.kt:239-254`). Isso é uma mistura **SEED + REAL** que pode aparentar dados do dispositivo antes da prova.

`simulateUnusualActivity()` (`:493-751`) é uma sequência explicitamente `[SIMULATION]`, com `delay()` e caso sintético. É **DEMO FUNCIONAL**, não detecção. A UI de decoy corrige o risco e diz expressamente que não há desvio (`ImmuneSystemScreen.kt:216-239`).

`HealthExamScreen.kt:260-286` declara que histórico é “Illustrative seed values; history is not persisted on-device”. Essa declaração é correta.

## 14. Segurança, privacidade e dependências

### Segurança

Scan executado:

```text
git ls-files '*.env' 'local.properties' '*keystore*' '*.jks' '*.aab' '*.apk'
```

Não retornou material rastreado sensível. Scan de literais encontrou somente:

- referências a plugin de secrets em `gradle/libs.versions.toml`;
- `.env.example` com placeholder comentado para a chave Gemini (valor omitido/redigido);
- variáveis de ambiente de signing em `app/build.gradle.kts`.

Não foi exposto nenhum valor secreto neste relatório. Ainda assim:

- `debug.keystore` existe no repositório e é esperado para debug; não é segredo de produção;
- `local.properties` existe no workspace, mas não foi lido nem reproduzido;
- regras de segurança e `.gitignore` proíbem credenciais, APK/AAB e local properties.

### Privacidade

O manifest não tem `INTERNET`; a coleta atual é local. `QUERY_ALL_PACKAGES` e `PACKAGE_USAGE_STATS` são sensíveis e exigem justificativa/disclosure de Play Console. O app enumera permissões declaradas de terceiros, não prova que foram usadas. Não há política legal final/URL comprovada; `docs/release/PRIVACY_POLICY_DRAFT.md` possui placeholders humanos.

### Dependências declaradas

`gradle/libs.versions.toml` lista Compose, lifecycle, coroutines, navigation, Room, Retrofit, Moshi, OkHttp, Camera, Location, DataStore, Firebase, credentials e outros. O `app/build.gradle.kts:65-95` usa apenas Compose, lifecycle, coroutines, testes, Robolectric e Roborazzi.

Classificação:

- dependências usadas: **INTEGRADAS** para UI/testes;
- Room/Retrofit/Moshi/OkHttp/Firebase/Camera/Location/DataStore: **catalogadas ou removidas do app; não usadas no caminho atual**;
- ML Kit GenAI/LiteRT-LM/MediaPipe: **não declaradas**, portanto Nano/Gemma não podem ser runtime reais;
- license notices/OSS notices: **NOT VERIFIED**.

## 15. Testes, build, instalação e runtime

### Testes encontrados

Artefatos anteriores:

```text
app/build/test-results/testDebugUnitTest/*.xml
prior XML totals: tests=63 skipped=0 failures=0 errors=0
```

Esses XMLs comprovam uma execução anterior em 2026-09-12, incluindo suites de consumo, honestidade, fusão, immune adversarial, homeostase, UI binding, ViewModel e trace. Não comprovam hardware.

### Checks desta sessão

`./gradlew --version` **PASSOU**:

```text
Gradle 9.3.1
Kotlin 2.2.21
Launcher JVM: 21.0.12
```

`./gradlew test --no-daemon --console=plain` **BLOQUEADO**:

```text
Could not create task ':app:compileDebugUnitTestJavaWithJavac'
Toolchain installation '/usr/lib/jvm/java-21-openjdk-amd64'
does not provide the required capabilities: [JAVA_COMPILER]
```

Diagnóstico observado:

```text
javac: command not found
/usr/lib/jvm/java-21-openjdk-amd64/bin/javac: No such file or directory
```

`./gradlew assembleDebug --no-daemon --console=plain` **BLOQUEADO pelo mesmo motivo**, em `compileDebugJavaWithJavac`.

`./gradlew lintDebug --no-daemon --console=plain` **BLOQUEADO pelo mesmo motivo**, antes da compilação completa.

O relatório pré-existente `app/build/reports/lint-results-debug.txt` registra lint anterior com warnings, não erros fatais, incluindo:

- `ClinicalComponents.kt:1523` `DefaultLocale`;
- `AndroidManifest.xml:31` `RedundantLabel`;
- versões não mais recentes no catalog (`AGP 9.1.1`, Compose BOM 2024.09.00 e outras).

### Artefatos pré-existentes

Encontrados, mas não produzidos nesta sessão:

| Artefato | Tamanho | SHA-256 | Estado |
|---|---:|---|---|
| `app/build/outputs/apk/debug/app-debug.apk` | 17M | `04ec4a838eedd53c47f5d0ace1c49d77dd363187227bdadb4eae0387b848ca67` | BUILD HISTÓRICO/PRESENTE; instalação não verificada |
| `app/build/outputs/bundle/release/app-release.aab` | 11M | `28dd9bedfbd27c1a3ce08bd1986bf8853cdf83a695520e519d93b2000f6af72e` | BUILD HISTÓRICO/PRESENTE; assinatura Play não verificada |

`output-metadata.json` confirma `applicationId=com.aistudio.vwatcher.hkmv`, `versionCode=1`, `versionName=1.0`, `minSdk=24`.

### Instalação/runtime

Não foi feita instalação: não há dispositivo conectado, e a compilação corrente falhou por ausência de `javac`. Não há prova de launch, rotação, Doze, morte de processo, permission UX, consumo, latência, battery drain, thermal behavior ou Android 16 behavior changes.

## 16. Disponibilidade de dispositivo

Comando:

```text
adb devices -l
List of devices attached
```

Nenhum dispositivo/emulador foi listado. `adb` existe em `/usr/bin/adb`, mas não há target. Portanto:

- Nano/AICore: **NOT VERIFIED**;
- Gemma: **NOT VERIFIED**;
- telemetria contra `dumpsys`: **NOT VERIFIED**;
- instalação/launch/UI: **NOT VERIFIED**;
- rede/Doze/thermal/foreground lifecycle: **NOT VERIFIED**;
- prova independente de bateria, RAM e tráfego: **NOT VERIFIED**.

Isso coincide com `docs/evidence/validation/REAL_DEVICE_VALIDATION.json`, que classifica a prova física como bloqueada por ausência de ADB/hardware.

## 17. Static scan e observabilidade

O script existente `tools/validation/petscan/static_scan.sh` executou com sucesso. Achados relevantes:

- um hit de `TEMP` em `BatteryProvider.kt:56`, que é nome local `rawTemp`, não placeholder;
- thresholds espalhados (`<15`, `<200`, `<250`, `>42`, `>88`, `>92`, etc.);
- 12 variantes de payload declaradas, mas apenas uma pequena parte despachada;
- apenas um collector de `bus.messages` na produção;
- `startActivity` real em `AndroidRealityBoundary` e telas de Settings;
- delays da simulação de 700–1900ms;
- classes/funções candidatas a órfãs.

O scan não encontrou credenciais reais. “Zero mock” na documentação não deve ser interpretado como ausência de dados sintéticos: `createInitialState()` e `simulateUnusualActivity()` são explicitamente seed/simulação.

## 18. Matriz de realidade final

| Capacidade | Código | Integração | Execução | Teste JVM | Teste real-device | Veredito |
|---|---|---|---|---|---|---|
| Launcher Compose | SIM | SIM | não reproduzida agora | screenshot prévio | NÃO | implementado, runtime NOT VERIFIED |
| Bateria | SIM | SIM | lógica concreta | provider testável | NÃO | real condicionado a Android |
| RAM | SIM | SIM | lógica concreta | prévio | NÃO | real/parcial |
| Rede transport | SIM | SIM | lógica concreta | prévio | NÃO | real, sem inspeção |
| App inventory | SIM | SIM | lógica concreta | prévio | NÃO | real condicionado à política |
| Usage Stats | SIM | SIM | permission-gated | prévio | NÃO | parcial |
| Sentinel one-shot | SIM | SIM | foreground | prévio | NÃO | implementado |
| Sentinel continuous | SIM | NÃO | não chamado | não provado | NÃO | skeleton |
| Baseline rules | SIM | SIM | deterministic | prévio | NÃO | funcional em JVM |
| Nano package probe | SIM | SIM via router | não há AICore | unavailable path prévio | NÃO | parcial |
| Nano inference | contrato | não há runtime | canned assessment | NÃO como inferência | NÃO | stub/broken claim |
| Gemma file probe | SIM | SIM via router | sem weights | unavailable path prévio | NÃO | parcial |
| Gemma inference | contrato | não há runtime | canned assessment | NÃO como inferência | NÃO | stub/planned |
| Deterministic backend | SIM | SIM | SIM | SIM | NÃO | implementado |
| Fallback bookkeeping | SIM | SIM | SIM | SIM | NÃO | funcional em JVM |
| Immune bus | SIM | SIM | SIM | SIM | NÃO | implementado parcial |
| Homeostasis | SIM | SIM | SIM | SIM | NÃO | heurístico, não calibrado |
| Cache reclaim | SIM | condicional | próprio cache | SIM lógico | NÃO | actuation real parcial |
| Settings Intent | SIM | SIM | não executada agora | SIM via Robolectric prévio | NÃO | funcional em teste |
| Kill external process | explicitamente não | não | UNAVAILABLE | SIM de honestidade | NÃO | correto não implementado |
| Decoy/honeypot | DTO/UI | NÃO | não | UI only | NÃO | não implementado |
| Memory persistence | não | não | RAM-only | gates testáveis | NÃO | não persistente |
| Background watch | não | não | não | não | NÃO | não implementado |

## 19. Gaps críticos, riscos e classificação

### P0 — bloqueadores de verdade do produto

1. **Neural execution inexistente:** Nano e Gemma devolvem avaliações canned; não há dependências/runtime/pesos.
2. **Sem prova física:** zero dispositivo/emulador, build corrente bloqueado por `javac`.
3. **Narrativa de watcher excede plataforma:** não há background service, receiver, VPN ou observação contínua integrada.
4. **Actuation externa inexistente:** app não consegue isolar, suspender ou matar terceiros.
5. **Memória não persistente:** reinício perde casos e padrões; claims de persistência histórica são inválidos para o código atual.

### P1 — risco de interpretação enganosa

1. Seeds aparecem antes de primeira observação (`createInitialState`).
2. Fallback para lista seed quando inventário real está vazio (`VWatcherViewModel.kt:239-254`).
3. `GeminiNanoBackend` e `GemmaBackend` podem marcar provenance como modelo real sem executar modelo.
4. “confidence” é score heurístico.
5. Decoy DTOs permanecem no estado inicial embora a UI declare que não existe pipeline.

### P2 — qualidade/operabilidade

1. Thresholds divergentes entre PRR, router, homeostasis e resolution.
2. Lifecycles `warmup`, `release`, `startContinuousObserving` sem wiring de Activity/ViewModel.
3. Flows de comunicação sem consumidores produtivos.
4. NK e diversos payloads mortos.
5. lint histórico com warnings; lint atual não rodou por toolchain.
6. Sem verificação de persistência, Doze, ANR, startup/frame metrics, accessibility real ou Play pre-launch.

## 20. Comandos para reproduzir a auditoria

Executar na raiz do repositório:

```bash
git status --short --branch
git log -8 --oneline --decorate
find app/src -type f | sort
./gradlew --version
./gradlew testDebugUnitTest --no-daemon --console=plain
./gradlew assembleDebug --no-daemon --console=plain
./gradlew lintDebug --no-daemon --console=plain
bash tools/validation/petscan/static_scan.sh
adb devices -l
grep -RInE 'TODO|FIXME|HACK|XXX|stub|fake|mock|dummy|placeholder' app/src
grep -RInE 'MediaPipe|LlmInference|AICore|GenerativeModel|Room|DataStore|VpnService|Socket|HttpURLConnection' app/src gradle
sha256sum app/build/outputs/apk/debug/app-debug.apk app/build/outputs/bundle/release/app-release.aab
```

Para desbloquear a validação:

```bash
# instalar JDK completo com javac, conforme política do ambiente
java -version
javac -version
./gradlew testDebugUnitTest --no-daemon --console=plain

# conectar Android 14/15/16 ou emulador
adb devices -l
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell dumpsys battery
adb shell dumpsys meminfo com.aistudio.vwatcher.hkmv
adb shell am start -n com.aistudio.vwatcher.hkmv/com.example.MainActivity
adb logcat --pid="$(adb shell pidof com.aistudio.vwatcher.hkmv)"
```

Os comandos de dispositivo acima **não foram executados com sucesso nesta auditoria** porque não havia target. Nenhuma saída futura deve ser reutilizada como prova sem timestamp, serial, build hash e captura independente.

## 21. Realidade atual em uma frase

**V-Watcher é um app Compose foreground com observação local real e pipeline determinístico/biomimético experimental; Nano/Gemma, background watching, deep network inspection, decoys, isolamento de terceiros e persistência durável são ausentes, enquanto a prova de build/runtime físico está bloqueada neste host.**

## 22. Metadados finais obrigatórios

```yaml
report_file: V_WATCHER_FORENSIC_SYSTEM_REPORT.md
report_language: pt-BR
audit_datetime_local: 2026-09-14
repository_root: /media/juan/DATA/V-watcher
branch: main
head_commit: 1c6ebaa
worktree_before_report: clean
worktree_after_report: report_created_only
production_kotlin_files: 55
unit_test_kotlin_files: 12
instrumentation_test_kotlin_files: 1
resource_files: 19
prior_test_xml_total: 63
prior_test_xml_failures: 0
prior_test_xml_errors: 0
current_test_execution: BLOCKED_NO_JAVAC
current_build_execution: BLOCKED_NO_JAVAC
current_lint_execution: BLOCKED_NO_JAVAC
adb_target: NONE
debug_apk_present: true
release_aab_present: true
debug_apk_currently_rebuilt: false
release_aab_currently_rebuilt: false
gemini_nano_inference: NOT_IMPLEMENTED_STUB
gemma_inference: NOT_IMPLEMENTED_STUB
deterministic_backend: IMPLEMENTED
background_observer: NOT_INTEGRATED
external_process_isolation: UNAVAILABLE_BY_ANDROID_SANDBOX
network_packet_inspection: NOT_IMPLEMENTED
decoy_pipeline: NOT_IMPLEMENTED
memory_persistence: RAM_ONLY
secrets_exposed_in_report: false
physical_device_proof: NOT_VERIFIED
final_verdict: PRE_RELEASE_FOREGROUND_DETERMINISTIC_SYSTEM; PHYSICAL_AND_NEURAL_PROOF_INCOMPLETE
```

## 23. Machine-readable final status

```text
REPORT_GENERATED_AT: 2026-09-14T08:57:32-03:00
GIT_COMMIT: 1c6ebaa96cf478e1d08053e9d8189e743967b3ea
GIT_BRANCH: main
WORKTREE_STATUS: report_created_only; report is untracked and all prior changes were clean
BUILD_STATUS: NOT VERIFIED; blocked by missing javac (Java runtime present, compiler absent)
TEST_STATUS: NOT VERIFIED in current session; blocked by missing javac; historical XML evidence is separately identified
DEVICE_STATUS: NOT VERIFIED; adb devices -l returned no targets
GEMINI_NANO_STATUS: detection probe present; runtime/inference not implemented and not verified
GEMMA_STATUS: weight-file probe present; runtime/inference not implemented and not verified
WATCHER_STATUS: foreground observeNow path implemented and connected; continuous observer API not integrated
SENTINEL_STATUS: implemented and invoked by VWatcherViewModel on refresh; background lifecycle not implemented
E2E_STATUS: NOT VERIFIED on a built APK or device
OVERALL_REALITY: pre-release foreground Android telemetry app with deterministic/biomimetic code; neural, background, device, and durable-storage claims remain unproven or absent
```
