# V-Watcher — Fonte Operacional de Verdade (SOT)

**Estado:** ACTIVE  
**Data de corte:** 2026-09-14 09:10 -03:00  
**Repositório:** `/media/juan/DATA/V-watcher`  
**Branch/commit observado:** `main` / `1c6ebaa` (`chore(repo): organize documentation and release evidence`)  
**Escopo:** código, configuração, testes, artefatos e documentação presentes no repositório.  
**Alteração de produto:** nenhuma. Este SOT e os artefatos adjacentes são documentação operacional.

> Este documento descreve o que o repositório atual sustenta, o que não sustenta e qual prova ainda falta. Não é marketing, especificação futura nem roadmap. Um documento histórico preserva seu valor histórico, mas não substitui este SOT.

> **THIS DOCUMENT IS NOT A ROADMAP.**  
> **THIS DOCUMENT IS NOT A DESIGN PROPOSAL.**  
> **THIS DOCUMENT IS NOT MARKETING.**  
> **THIS DOCUMENT IS NOT A SUBSTITUTE FOR RUNTIME EVIDENCE.**  
> Esta SOT descreve o maior claim que o repositório atual pode sustentar honestamente.

## 1. Governança e autoridade

### 1.1 Hierarquia de autoridade

Em caso de conflito, aplicar esta ordem:

1. **Código atual alcançável no caminho de execução** (`app/src/main`, manifest e configuração efetivamente aplicada).
2. **Testes atuais e seus resultados datados**, distinguindo teste executado agora de XML histórico.
3. **Artefato construído e inspeção independente** (APK/AAB, manifest dump, hashes), sempre com data e origem.
4. **Relatórios forenses e evidência reproduzível** que apontem para o código atual.
5. **Documentação operacional de release e referência.**
6. **Snapshots, planos, contratos, seeds e textos históricos.**

Nenhuma afirmação de nível inferior pode elevar o estado de uma afirmação superior. Um comentário “real”, um nome de classe, um seed, uma resposta canned ou um relatório antigo não provam execução real.

### 1.2 Estados de verdade usados neste SOT

- **IMPLEMENTED:** existe código concreto.
- **INTEGRATED:** existe caller alcançável no caminho atual.
- **TESTED:** sustentado por teste JVM/Robolectric ou traço histórico datado; não prova hardware.
- **PARTIAL:** apenas parte do contrato é executada.
- **NOT_VERIFIED:** não foi possível executar ou confirmar no ambiente atual.
- **PLANNED:** não há mecanismo atual; a capacidade é futura ou apenas proposta.
- **CONTRADICTED:** a afirmação do documento não corresponde ao código/configuração atuais.
- **SEED/SIMULATION:** dados criados pelo produto para inicialização ou demonstração, não observação do dispositivo.
- **UNAVAILABLE:** o nível de prova exige compilador, dispositivo ou outro recurso ausente.

Os estados de inventário documental são controlados separadamente em `V_WATCHER_DOCUMENT_STATUS.md`: `ACTIVE`, `HISTORICAL`, `OBSOLETE`, `CONTRADICTED`, `PLANNED`, `HISTORICAL_EVIDENCE`.

### 1.3 Regras de prova

1. **Presença não é execução.** Código, dependência catalogada, classe ou comentário não demonstram runtime.
2. **Execução JVM não é prova física.** Robolectric testa contratos Android em um host simulado.
3. **Proveniência deve acompanhar o valor.** Fallback, permissão ausente e degradação não podem ser descritos como sensor físico normal.
4. **Fallback deve ser explícito.** `selectedBackend` e `actualBackendUsed` são distintos; a substituição deve manter status e motivo.
5. **Ação deve ser limitada ao mecanismo real.** Flag em memória não é isolamento de SO; `UNAVAILABLE` não é sucesso.
6. **Seeds e simulação não podem alimentar claims de detecção real.** O prefixo `[SIMULATION]` é parte do contrato de honestidade.
7. **Artefato histórico não é build atual.** XML, APK, AAB ou relatório com data anterior deve manter data e hash.
8. **Sem `javac` e sem target ADB não há prova de build corrente nem de dispositivo.** Nesta corte, `./gradlew testDebugUnitTest` foi bloqueado por falta de `JAVA_COMPILER`; `adb devices` não listou targets.

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
| `INTERNET` | não declarado | `AndroidManifest.xml` |
| Background service/worker/receiver/provider | não encontrado no manifest atual | `AndroidManifest.xml` |
| Persistência de casos/padrões | não existe; repository é RAM-only | `LocalImmuneMemoryRepository.kt`, regras de backup |
| Backend de modelo efetivamente seguro | determinístico | `DeterministicBackend.kt`, router |

O versionamento e o `applicationId` são configuração existente, não aprovação de publicação. Identidade de produção, assinatura Play, política pública, Data Safety e Console permanecem decisões/ações humanas externas.

## 3. Runtime atual: caminho efetivo

O caminho foreground integrado é:

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
              -> Gemini Nano somente se READY e guardrails permitirem
              -> Gemma somente se READY e guardrails permitirem
              -> DeterministicBackend em fallback
        -> CytotoxicEffectorCell quando os gates passam
        -> AndroidRealityBoundary
        -> MacrophageCell quando há pressão de memória
        -> ResolutionCell
        -> LocalImmuneMemoryRepository
        -> HomeostasisEngine
      -> atualização de uiState e UI Compose
```

### O que esse caminho faz

- Captura uma observação sob demanda em foreground.
- Coleta seis blocos de telemetria e anexa `ProviderHealth`, freshness e provenance.
- Avalia regras determinísticas de temperatura, memória, rede/padrões e power-save.
- Encaminha evidência convergente para a orquestração biomimética.
- Consulta o router; no ambiente atual, Nano e Gemma não foram provados como runtime de inferência.
- Pode limpar somente o cache próprio, manter flags internas, abrir Settings ou recusar ações fora do sandbox.
- Calcula homeostase e a usa como sinal deny-only para guardrails.
- Mantém casos e padrões adicionais apenas na memória do processo.

### O que esse caminho não faz

- Não mantém observação contínua integrada ao ciclo de vida da Activity/ViewModel.
- Não abre sockets, captura pacotes, implementa VPN, proxy ou firewall.
- Não redireciona tráfego para decoy/honeypot.
- Não mata, suspende ou isola um processo de terceiro em nível de Android.
- Não fornece persistência durável de casos/padrões.
- Não comprova execução de Gemini Nano ou Gemma.
- Não prova comportamento de bateria, Doze, térmica, rotação, morte de processo ou Android físico nesta corte.

## 4. Sentinel e telemetria

`AndroidSentinel.observeNow()` é implementado e chamado pelo ViewModel. A coleta atual é serial e usa seis providers:

| Provider | O que lê | Limite operacional |
|---|---|---|
| `BatteryProvider` | `BatteryManager`/`PowerManager`: nível, carga, temperatura, saúde e power-save | fallback de Intent ausente existe; deve permanecer `isReal=false`/degradado |
| `DeviceResourceProvider` | `ActivityManager.MemoryInfo` e heap do processo | heap do V-Watcher não é RAM física; fallback JVM é parcial/degradado |
| `NetworkTelemetryProvider` | transporte, validação, VPN e capacidades via `ConnectivityManager` | não inspeciona pacotes, conteúdo ou tráfego por aplicação |
| `AppInventoryProvider` | pacotes instalados e permissões declaradas via `PackageManager` | declaração não prova uso, intenção, tráfego ou integridade |
| `AppUsageProvider` | Usage Stats das últimas 24 h, se acesso especial concedido | sem grant retorna `PERMISSION_REQUIRED`; não é telemetria livre |
| `SystemStateProvider` | Build, patch, locale, timezone e interatividade | descritores do sistema, não atestação criptográfica |

`CanonicalObservation` carrega timestamp, freshness, disponibilidade, diagnóstico e seis registros de provenance. A existência de `isReal=true` no código de um provider é uma intenção de classificação; a prova física ainda requer dispositivo e comparação independente.

A API `startContinuousObserving()` existe, mas não há caller integrado encontrado. Portanto: **API IMPLEMENTADA; watcher contínuo NÃO INTEGRADO**.

## 5. Reasoning e contratos de modelo

### 5.1 Router e guardrails

`ReasoningRouter` prioriza Nano, depois Gemma e, na ausência de backend READY, usa `DeterministicBackend`. Também trata lock de concorrência, timeout, falha de inicialização e motivo de fallback. `ResourceGuardrails` bloqueia por:

- bateria abaixo de 15% e descarregando;
- memória low ou uso acima de 92%;
- cooldown mínimo (2.500 ms no engine atual; a classe aceita configuração);
- veto regulatório, cooldown regulatório, inferência throttled, homeostase estressada ou contenção ativa.

Essas regras são código alcançável e têm cobertura JVM/Robolectric histórica. São heurísticas e thresholds não calibrados em hardware.

### 5.2 Contrato Gemini Nano

**Código:** `GeminiNanoBackend.kt`.

- Procura o pacote `com.google.android.aicore` por `PackageManager`.
- Considera `READY` se o pacote existir, estiver habilitado e a rotina de inicialização marcar a flag local.
- Não há dependência ML Kit GenAI, binding AIDL, `GenerativeModel`, `generateContent`, download/verificação de modelo ou chamada de runtime.
- `buildPromptFromObservation()` constrói texto, mas o texto não é enviado a um runtime de inferência.
- Se o caminho READY for artificialmente alcançado, `execute()` retorna assessment constante `NORMAL`, confiança 92 e `NO_ACTION`.

**Verdade operacional:** probe heurística PARCIAL; inferência neural NÃO IMPLEMENTADA/STUB; `isRealOnDeviceModel=true` no objeto de provenance não é prova de modelo executado e deve ser tratado como risco de sobredeclaração. Não é permitido afirmar “Gemini Nano executado” ou resultado neural observado.

### 5.3 Contrato Gemma

**Código:** `GemmaBackend.kt`.

- Procura `filesDir/models/gemma-2b-it-cpu.bin` ou `filesDir/gemma-2b-it-cpu.bin`.
- Um arquivo não vazio pode elevar o estado a `READY`; não há validação de formato, hash, origem ou peso compatível.
- Não há LiteRT-LM, MediaPipe LLM Inference, sessão TFLite ou runtime de geração.
- `execute()` retorna assessment constante `NORMAL`, confiança 90 e `NO_ACTION` quando a flag local é READY.

**Verdade operacional:** file probe PARCIAL; runtime e inferência NÃO IMPLEMENTADOS/STUB; não há pesos no repositório nem prova de execução. Não é permitido afirmar “Gemma executado”, “Gemma 4” ou resultado neural observado.

### 5.4 Backend determinístico

`DeterministicBackend` é o backend funcional atual e declara `isRealOnDeviceModel=false`. Usa valores da observação e regras explícitas:

- temperatura acima de 42 °C → `SUSPICIOUS`;
- candidato contendo `socket`, `packet` ou `burst` → `SUSPICIOUS`;
- low memory ou uso acima de 88% → `BENIGN_ANOMALY`;
- power-save com bateria acima de 50% → `BENIGN_ANOMALY`;
- caso contrário → `NORMAL`.

Confianças (75–96) são scores heurísticos, não probabilidades calibradas.

## 6. Sistema biomimético, bus e homeostase

`BiomimeticImmuneSystem.processObservation()` é integrado ao refresh e instancia 12 células de domínio, incluindo Sentinel, PRR, Neutrophil, Macrophage, Dendritic, NK, T-Helper, B-Cell, Cytotoxic, Regulatory, Resolution e Memory.

### Bus

`ImmuneBus` usa `SharedFlow` com replay 20, buffer extra 128, `DROP_OLDEST`, contagem de drops e flag de storm acima de 30 mensagens/s. O storm é um sinal; não há daemon independente que interrompa ou recupere automaticamente um storm. O collector produtivo encontrado registra evidência `PatternDetected` em um ledger limitado a oito observações.

O contrato enumera mais payloads do que os efetivamente despachados no caminho principal. Isso é superfície morta/parcial, não prova de comunicação entre todas as células.

### Homeostase

`HomeostasisEngine` calcula dimensões de energia, memória, rede, estabilidade, segurança, integridade, atividade imune e carga de incidentes. Pode produzir `UNKNOWN`, `DEGRADED`, `STRESSED`, `ACTIVE_DEFENSE`, `RECOVERING`, `WATCH` ou `HOMEOSTATIC`.

Limites importantes:

- provider crítico ausente/erro → estado degradado/fail-closed;
- bateria <15% descarregando ou memória baixa/<250 MB → estresse;
- heap do V-Watcher >200 MB, storm ou >25 msg/s → auto-destabilização;
- estabilidade usa contagens hardcoded zero em parte;
- segurança usa `isDeviceSecure=true` sem atestação;
- rede marca `isAnomalous=false` por construção;
- não há calibração física nem política única de thresholds.

A homeostase é consumida para rótulo/triagem e para sinais deny-only dos guardrails. Ela não é prova de saúde clínica, segurança do dispositivo, causalidade externa ou controle autônomo do Android.

## 7. Memória

`LocalImmuneMemoryRepository`:

- carrega três seeds em cada instância;
- mantém patterns e cases em `MutableStateFlow`;
- usa `ConcurrentHashMap` para timestamps/provenance;
- rejeita simulação, campos incompletos, confiança <80%, telemetria indisponível/erro e duplicata dentro de 30 s;
- adiciona/resolvem dados somente em RAM;
- não usa Room, SQLite, DataStore, SharedPreferences ou arquivo para cases/patterns;
- perde dados adicionais na morte do processo;
- não possui TTL, limite de tamanho ou delete de memória dinâmica.

O texto “cryptographic provenance” é nomenclatura sem mecanismo criptográfico correspondente. A proveniência é um DTO verificável em memória, não assinatura criptográfica.

## 8. Actuation e limite Android

`AndroidRealityBoundary` implementa somente mecanismos próprios ou honestamente limitados:

| Ação | Resultado sustentado |
|---|---|
| `THROTTLE_INTERNAL_INFERENCE` | flag interna em memória; o engine lê a flag via `GuardrailContext` no refresh seguinte |
| `ISOLATE_INTERNAL_SUBSYSTEM` | conjunto interno em memória; não isola processo/serviço Android externo |
| `RECLAIM_INTERNAL_CACHE` | remove arquivos próprios `vwatcher_temp_*`, soma bytes e chama `System.gc()` |
| `NAVIGATE_APP_SETTINGS` | cria e envia `ACTION_APPLICATION_DETAILS_SETTINGS`; testado por sombra Robolectric histórica |
| `RESET_SUBSYSTEM_ISOLATION` | limpa flags/conjunto interno |
| `KILL_EXTERNAL_PROCESS` | `UNAVAILABLE`, com motivo do sandbox e alternativa Settings |
| ação desconhecida | `DENIED` |

`VWatcherViewModel.isolateApp()` é uma marcação de revisão **in-app**, reversível, sem ação OS sobre o app alvo. Não há decoy, suspensão de terceiro, firewall ou kill externo.

## 9. UI, seeds e simulação

### UI ligada a estado

`UiClaimBindings.kt` e testes de binding sustentam que:

- badge de dados aparece somente após observação;
- homeostase nula aparece como `EVALUATING`, não como verde;
- confiança ausente aparece como `—`;
- contagem de casos é derivada da lista;
- categorias de exame são recalculadas a partir do estado atual;
- rede declara “transport state only; no per-app TLS verification”;
- sistema declara “No integrity attestation performed”;
- memória declara “RAM-only; nothing persisted”.

Essa ligação é teste de função/UI no host; não é validação visual em dispositivo físico.

### Seeds de inicialização

`createInitialState()` preenche apps, permissões, conexões, decoys, case, três padrões, histórico de sete pontos e notificações antes da primeira observação. Esses valores são **SEED** e não devem ser apresentados como leitura do dispositivo. Se o inventário real vier vazio, o caminho atual conserva a lista anterior, o que mantém uma mistura seed/real e exige cautela na interpretação.

### Simulação

`simulateUnusualActivity()` cria uma sequência artificial com delays, casos, apps e notificações marcados `[SIMULATION]`. É uma demonstração de UI/estado; não é detecção, contenção ou prova de ação real.

### Exame manual

`runDeviceCheck()` mostra uma sequência de progresso e, ao final, captura uma nova observação e interpola números reais disponíveis. O fluxo de passos é UI/orquestração, não uma certificação integral de aplicações, permissões, rede, integridade ou histórico.

## 10. Segurança, privacidade e release

- O manifest não declara `INTERNET`; não há sink de rede no caminho atual.
- O app lê rede local, inventário e Usage Stats conforme permissões; não prova inspeção de conteúdo nem uso de permissões declaradas.
- `QUERY_ALL_PACKAGES` e `PACKAGE_USAGE_STATS` exigem justificativa/disclosure externos.
- Plugins e aliases de dependência catalogados (Room, Retrofit, Moshi, OkHttp, Firebase, Camera, Location, DataStore e outros) não são dependências efetivamente usadas pelo módulo atual.
- Assinatura Play, política final, Data Safety, listing, pré-launch report e decisão de identidade são externos e não provados por este SOT.
- APK/AAB presentes em `app/build` são artefatos históricos/pré-existentes; não foram reconstruídos nesta corte.

## 11. Testes e checks desta corte

### Inventário

Há 12 arquivos Kotlin em `app/src/test` e um teste instrumentado em `app/src/androidTest`. As suites cobrem Sentinel/proveniência, router/fallback, guardrails, células, adversarial, fusão, ViewModel, UI binding, trace e smoke.

O XML pré-existente registra **63 testes, 0 skipped, 0 failures, 0 errors** em execução datada anterior (2026-09-12). Isso é `HISTORICAL_EVIDENCE`, não execução corrente.

### Checks executados nesta atualização

| Check | Resultado | Interpretação |
|---|---|---|
| `git status --short` / `git log` | PASS | branch/commit observados; o relatório forense já estava não rastreado antes desta atualização |
| `./tools/validation/petscan/static_scan.sh` | PASS | scan estático executou; encontra thresholds espalhados, payloads, collectors e `rawTemp`, não prova runtime |
| `git diff --check` | PASS | sem erro de whitespace no diff observado |
| `./gradlew testDebugUnitTest` | BLOQUEADO | toolchain `/usr/lib/jvm/java-21-openjdk-amd64` não fornece `JAVA_COMPILER`/`javac` |
| `adb devices` | sem targets | `adb` existe, mas nenhum dispositivo/emulador está conectado |

**Não declarar:** build atual verde, testes atuais verdes, instalação, launch, ADB, Nano/Gemma, bateria, latência, Doze, térmica ou produção. Os únicos números de testes/build presentes são históricos e devem conservar data/hash.

## 12. Claim ceiling

O teto público desta corte é:

- **Permitido com qualificação:** app Android Compose foreground; observação sob demanda local; seis providers com provenance; regras determinísticas; router com fallback explícito; bus/storm flag; gates adversariais; cache próprio; Settings Intent; memória RAM-only; simulação explicitamente marcada.
- **Permitido somente como implementação/código:** pipeline biomimético, homeostase heurística, guardrails deny-only, UI e contratos de backends.
- **Não permitido como claim atual:** “Gemini Nano executado”, “Gemma executado”, “IA neural observada”, “antivírus/firewall”, “watcher 24/7”, “isolamento de outro app”, “kill de processo externo”, “decoy/honeypot ativo”, “memória persistente”, “integridade criptograficamente verificada”, “TLS por app verificado”, “assinaturas verificadas”, “zero uso de recursos”, “latência calibrada”, “produção pronta” ou “device verified”.

## 13. Próximas provas necessárias (não são roadmap de produto)

1. **Toolchain:** disponibilizar JDK com `javac`, repetir `testDebugUnitTest`, build e lint; registrar comando, versão, hash e resultado.
2. **Artefato:** reconstruir APK/AAB da mesma árvore e repetir manifest dump, strings/secrets scan e validação do bundle.
3. **Emulador/ADB:** instalar APK reconstruído, capturar launch, lifecycle, permissões, Settings Intent e morte/rotação de processo.
4. **Dispositivo físico:** comparar Battery/Memory/Network/Package/Usage com `adb shell dumpsys` e registrar serial, modelo, API e timestamps.
5. **Nano:** dispositivo AICore compatível, API/runtime real, status de modelo, prompt enviado, resposta, provenance e fallback.
6. **Gemma:** runtime real, pesos compatíveis verificados por hash/origem, execução e erro; remover qualquer READY heurístico sem isso.
7. **Operação:** medir térmica, bateria, Doze, latência, memória e escalabilidade; não inferir esses números de Robolectric.
8. **Persistência, se requerida:** decidir explicitamente se memory/cases continuam RAM-only; qualquer persistência futura exigirá código, migração, exclusão, backup e prova fria.

## 14. Snapshot operacional machine-readable

```yaml
sot_version: "1.0"
project_commit: "1c6ebaa96cf478e1d08053e9d8189e743967b3ea"
branch: "main"
runtime_type: "foreground_on_demand_deterministic_biomimetic_experimental"
foreground_watch: "IMPLEMENTED INTEGRATED PARTIAL"
continuous_watch: "IMPLEMENTED NOT_INTEGRATED"
background_watch: "PLANNED"
deterministic_reasoning: "IMPLEMENTED INTEGRATED TESTED"
gemini_nano: "IMPLEMENTED PARTIAL STUB NOT_VERIFIED"
gemma: "IMPLEMENTED PARTIAL STUB NOT_VERIFIED"
durable_memory: "PLANNED NOT_VERIFIED"
external_isolation: "UNAVAILABLE"
network_inspection: "PLANNED NOT_IMPLEMENTED"
immune_pipeline: "IMPLEMENTED INTEGRATED TESTED"
ui: "IMPLEMENTED INTEGRATED NOT_VERIFIED"
device_verified: false
e2e_verified: false
build_reproducible: false
test_reproducible: false
claim_ceiling: "foreground local telemetry and deterministic biomimetic analysis"
```

Os arquivos adjacentes são a forma estruturada deste SOT:

- `V_WATCHER_CLAIM_MATRIX.json`: claims com IDs estáveis, localização, prova, limites e teto público.
- `V_WATCHER_COMPONENT_REGISTRY.json`: componentes, callers, dependências, outputs, persistência e prova de dispositivo.
- `V_WATCHER_PROOF_MATRIX.json`: níveis L0–L7 e atribuição conservadora por claim/componente.
- `V_WATCHER_DOCUMENT_STATUS.md`: inventário de documentos com estado controlado e motivo.
- `SOT_CONSISTENCY_REPORT.md`: conflitos, claims fortes, claims proibidos e documentos stale.

## 15. CURRENT TRUTH

**CURRENT TRUTH:** V-Watcher é um aplicativo Android Compose foreground que captura observações locais sob demanda por seis providers e conduz um pipeline determinístico/biomimético experimental em memória. O backend determinístico é o único caminho de raciocínio comprovado no host; Gemini Nano e Gemma têm apenas probes/contratos e respostas canned, sem inferência neural comprovada. Actuation limita-se a recursos próprios, flags internas, limpeza de cache próprio e abertura de Settings; ações contra terceiros são indisponíveis. Cases/patterns adicionais são RAM-only. Seeds e simulação existem e devem permanecer explicitamente separados de dados reais. Não há prova corrente de build, dispositivo, execução física, watcher contínuo, persistência, decoy, inspeção profunda de rede, isolamento de terceiros ou produção.
