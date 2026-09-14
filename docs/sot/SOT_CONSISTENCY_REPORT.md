# V-Watcher — Relatório de Consistência do SOT

**Estado:** ACTIVE  
**Data de corte:** 2026-09-14 09:10 -03:00  
**Fonte canônica:** `docs/sot/V_WATCHER_SOURCE_OF_TRUTH.md`  
**Commit observado:** `1c6ebaa`  
**Escopo:** busca de claims fortes nos documentos atuais, reconciliação com código/configuração/testes e registro de conflitos. Documentos históricos não foram reescritos.

## 1. Método e checks

Foram lidos o relatório forense existente, os arquivos Kotlin mínimos solicitados, manifest, `app/build.gradle.kts`, `gradle/libs.versions.toml`, todos os testes Kotlin inventariados, documentos de `docs/sot`, `docs/release`, `docs/evidence` e `docs/archive`. Também foram executados:

- `git status --short` e `git log -5 --oneline --decorate`;
- `./tools/validation/petscan/static_scan.sh` — PASS;
- `git diff --check` — PASS;
- `./gradlew testDebugUnitTest` — BLOQUEADO ao criar `compileDebugUnitTestJavaWithJavac`: a toolchain não fornece `JAVA_COMPILER`/`javac`;
- `adb devices` — sem dispositivos/emuladores listados;
- busca de claims fortes e dependências/sinks no conteúdo de `docs` e `app/src`.

O XML pré-existente de 2026-09-12 registra 63/63, mas é `HISTORICAL_EVIDENCE`; não foi promovido como execução desta corte.

## 2. Resultado executivo

| Área | Resultado |
|---|---|
| SOT canônico | Criado em português, self-contained e limitado ao estado observado. |
| Código/configuração atual | Sustenta observação foreground sob demanda, seis providers, pipeline determinístico/biomimético, fallback explícito, actuation própria limitada e memória RAM-only. |
| Prova corrente | Sem build/test corrente por ausência de `javac`; sem device por ADB sem target. Teto público: L2 histórico/código-testável, não prova física. |
| Conflitos fortes | Room/SQLite, execução física/READY de modelos e build/release corrente aparecem em artefatos históricos; foram marcados `CONTRADICTED` ou `HISTORICAL_EVIDENCE`. |
| Claims proibidos | Nano/Gemma executados, 24/7/background, isolamento externo, decoy, persistência, integridade criptográfica, produção pronta e device verified. |

## 3. Conflicts documentais confirmados

### C-001 — Memória Room/SQLite versus repository RAM-only

**Documentos:**

- `docs/evidence/validation/CLAIM_PROOF_MATRIX.json`;
- `docs/evidence/validation/MEMORY_VALIDATION.json`;
- `docs/evidence/validation/LIFECYCLE_VALIDATION.json`;
- snapshots/planos que descrevem entidades ou cold reload.

**Claim encontrado:** casos/padrões seriam inseridos em Room/SQLite e recuperados após nova instância.  
**Código atual:** `LocalImmuneMemoryRepository` usa `MutableStateFlow` e `ConcurrentHashMap`; `context` não é usado para escrita; `app/build.gradle.kts` não aplica Room; regras de backup descrevem estado sem persistência.  
**Estado:** `CONTRADICTED` para uso atual; arquivos permanecem preservados como histórico.  
**Ação documental:** não reescrever os JSONs históricos; apontar para `VW-C015` e para o SOT.

### C-002 — Gemini Nano/Gemma READY e inferência versus stubs atuais

**Documento:** `docs/evidence/validation/MODEL_BACKEND_VALIDATION.json`.  
**Claim encontrado:** cenários “available” relatam `actual_backend_executed` como Nano/Gemma.  
**Código atual:** Nano só faz probe de pacote AICore, constrói prompt sem enviar a runtime e retorna assessment canned; Gemma só faz probe de arquivo e retorna assessment canned sem LiteRT/MediaPipe/TFLite. Não há runtime/pesos no repositório.  
**Estado:** `CONTRADICTED` como prova de inferência atual; o arquivo é `HISTORICAL_EVIDENCE`.  
**Ação documental:** somente `DETERMINISTIC` pode ser descrito como backend executado no host atual.

### C-003 — Build/test/lint corrente versus artefatos de 2026-09-12

**Documentos:** `docs/release/RELEASE_BUILD.md`, `docs/release/RELEASE_PACKAGE.md`, partes de `RELEASE_READINESS*` e auditorias físicas.  
**Claim encontrado:** build, testes e lint verdes como estado corrente.  
**Código/ambiente atual:** tentativa corrente de `./gradlew testDebugUnitTest` falha antes da compilação por ausência de `javac`; `adb devices` não possui target.  
**Estado:** os números datados permanecem `HISTORICAL_EVIDENCE`; claims de corrente/reprodutibilidade são `CONTRADICTED` nesta corte.  
**Ação documental:** manter data/hash e não usar “build atual verde” no SOT ou comunicação pública.

### C-004 — Settings Intent “built-not-sent” versus código/teste atual

**Documento:** `docs/sot/V_WATCHER_SOT.md` (versão anterior).  
**Claim encontrado:** `NAVIGATE_APP_SETTINGS` construiria Intent, mas não o enviaria.  
**Código atual:** `AndroidRealityBoundary.executeRealAction` chama `context.startActivity(intent)` e `HonestyTest` verifica `nextStartedActivity`.  
**Estado:** claim anterior `CONTRADICTED`; documento inteiro é `OBSOLETE` como SOT, preservado.  
**Ação documental:** SOT atual registra Settings como ação implementada/testável e ainda não provada em device.

### C-005 — Quantidade de providers e pipeline antigo

**Documentos:** `docs/sot/V_WATCHER_SOT.md` e `V_WATCHER_SOT_UNIFIED.md`.  
**Claim encontrado:** versões anteriores alternam entre cinco/seis providers e descrevem dual pipeline/clobber como estado atual.  
**Código atual:** `AndroidSentinel` coleta seis providers; ViewModel comenta a aposentadoria do caminho legado e usa `BiomimeticImmuneSystem` como entrada central. Seeds e cases podem coexistir, mas a fusão atual preserva cases manuais em vez de simplesmente apagar todos.  
**Estado:** versões antigas `OBSOLETE`; detalhes que não correspondem ao código são `CONTRADICTED`.  
**Ação documental:** usar o SOT atual e o registry.

### C-006 — Storm “contido” versus storm apenas sinalizado

**Documentos:** experimentos/relatórios PETSCAN e evidências antigas.  
**Claim encontrado:** proteção de storm pode ser lida como contenção automática.  
**Código atual:** `ImmuneBus` calcula taxa, flag e drops; o `RegulatoryTCell` pode vetar escalada quando consultado, mas não existe daemon que interrompa ou recupere o fluxo sozinho.  
**Estado:** `PARTIAL`; apenas detecção/flag e veto sob consulta são atuais.  
**Ação documental:** linguagem permitida: “storm detectado/sinalizado”; proibida: “storm contido/recuperado automaticamente”.

## 4. Claims fortes encontrados e decisão

| Claim textual/semântico | Onde aparece | Decisão do SOT |
|---|---|---|
| “Real device telemetry” | release/SOT/evidence | Permitido somente como código preparado/observação em Android, com `device_verified=false`; não como prova física desta corte. |
| “Gemini Nano/Gemma available/executed” | `MODEL_BACKEND_VALIDATION.json`, docs de runtime | Proibido como estado atual; probes/stubs e `UNPROVEN`/`CONTRADICTED`. |
| “Room/SQLite persistence” | evidência histórica | Proibido como estado atual; `CONTRADICTED`. |
| “Production/release ready” | release docs | Somente checklist/estado histórico; sem build corrente, signing Play, Console, política final ou device. |
| “24/7/autonomous watcher” | planos, UI/narrativa | Proibido: API contínua sem caller, sem service/worker/receiver. |
| “isolate/kill external app” | casos/simulação/actuator docs | Proibido: sandbox retorna `UNAVAILABLE`; ViewModel só marca revisão in-app. |
| “decoy/honeypot/traffic diversion” | seeds/cases históricos | Proibido: DTO/UI seed sem mecanismo de rede. |
| “TLS/certified/verified/signatures/integrity” | UI/documentos históricos | Proibido: binding atual declara limites e não há attestation/signature check. |
| “cryptographic provenance” | `LocalImmuneMemoryRepository.kt` | Rebaixado: é DTO de provenance; sem assinatura/hash criptográfico. |
| “storm protection handled” | PETSCAN/evidence | Rebaixado a `activeStormDetected`/veto quando consultado; sem contenção automática. |
| “0 uncontained/healthy” antes da observação | seeds/old UI docs | Proibido sem estado backend; bindings atuais usam `EVALUATING`/contagens. |

## 5. Unresolved claims

1. **Nano em dispositivo compatível:** falta AICore real, API de geração, prompt enviada, resposta não canned e provenance verificável.
2. **Gemma:** falta decisão de runtime, pesos reais, hash/formato, carregamento e inferência.
3. **Telemetria física:** falta device identificado e comparação independente com `dumpsys`/instrumentação.
4. **Build corrente:** falta JDK completo com `javac`; artefatos presentes não foram reconstruídos nesta corte.
5. **Lifecycle:** falta prova de launch, rotation, process death, Doze e permission UX.
6. **Performance/energia:** faltam medições de bateria, térmica, memória, latência e carga repetida.
7. **Persistência:** estado atual é RAM-only; qualquer claim de durabilidade permanece não resolvido e não deve ser inferido de documentos antigos.
8. **Thresholds:** valores são heurísticos e divergentes entre PRR, guardrails, homeostase, resolução e ViewModel.
9. **Wiring incompleto:** `startContinuousObserving`, payloads do contrato, collectors de flows e NaturalKiller têm superfícies sem integração plena.
10. **Seeds em inventário vazio:** o fallback para lista anterior mantém risco de mistura seed/real e exige revisão de claim/UI futura.

## 6. Forbidden claims nesta corte

Os seguintes textos/ideias não podem ser apresentados como fatos atuais, mesmo que apareçam em histórico, seeds ou contratos:

- “Gemini Nano executou inferência”;
- “Gemma/Gemma 4 executou inferência”;
- “antivírus”, “firewall”, “sandbox” ou “honeypot” funcional;
- “watcher 24/7”, “background monitor” ou autonomia persistente;
- “isolou/suspendeu/matou outro app”;
- “tráfego foi desviado para decoy”;
- “memória persistida após reinício”;
- “proveniência criptográfica assinada”;
- “TLS por app verificado”, “certified”, “signatures verified”, “integrity attested”;
- “storm contido/recuperado automaticamente”;
- “latência/bateria/eficiência calibrada”;
- “device verified”, “production ready” ou “Play ready”.

## 7. Stale documents e classificação

O inventário completo, com estado controlado e motivo por caminho, está em `V_WATCHER_DOCUMENT_STATUS.md`. Os principais documentos que não devem ser citados como autoridade atual são:

- `docs/sot/V_WATCHER_SOT.md` — `OBSOLETE`;
- `docs/sot/V_WATCHER_SOT_UNIFIED.md` — `OBSOLETE`;
- `docs/evidence/validation/CLAIM_PROOF_MATRIX.json` — `CONTRADICTED`;
- `docs/evidence/validation/MEMORY_VALIDATION.json` — `CONTRADICTED`;
- `docs/evidence/validation/LIFECYCLE_VALIDATION.json` — `CONTRADICTED`;
- `docs/evidence/validation/MODEL_BACKEND_VALIDATION.json` — `CONTRADICTED`;
- `docs/release/RELEASE_BUILD.md` — `CONTRADICTED` para estado corrente;
- `docs/release/RELEASE_PACKAGE.md` — `CONTRADICTED` para estado corrente;
- `docs/archive/**` — `HISTORICAL` ou `OBSOLETE`, conforme o inventário.

Preservação é intencional: status não significa apagar, reescrever ou invalidar a evidência histórica na data em que foi produzida.

## 8. Decisão final de consistência

O conjunto documental é consistente somente quando o SOT canônico, a claim matrix, o component registry e a proof matrix são usados como autoridade atual e todos os artefatos datados são lidos dentro de seu ambiente original. A árvore atual sustenta um **app foreground local, determinístico e biomimético experimental em RAM**, não sustenta inferência neural, watcher contínuo, actuation contra terceiros, persistência durável, decoy ou prova física corrente.
