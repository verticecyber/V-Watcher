# PETSCAN — Executive Summary (2026-09-12, L2 runtime evidence)

Method: 4 parallel code probes (telemetry/sentinel/homeostasis, immune/bus/memory, reasoning/router/boundary, viewmodel/UI/tests) + static scans (`tools/petscan/static_scan.sh`) + live Robolectric trace (`app/src/test/.../petscan/PetScanTraceTest.kt`, kept as harness). No product behavior changed.

## O que temos hoje?

Um observador foreground-only com telemetria real (6 providers), pipeline imune determinístico funcional (12 células orquestradas de forma síncrona), roteador de reasoning com fallback honesto, boundary de atuação que recusa o que o sandbox proíbe, e UI clínica extensa. Build + 30 testes verdes + lint limpo.

## O que realmente funciona? (PROVEN, com números do trace)

- `observeNow()` real: 181 ms, 6 providers + provenance + freshness (`obs_1789238031091_411807d9`).
- Cadeia Sentinel→PRR→Bus→T-Helper→Homeostase: `busTotal=2`, macroState computado (trace deu `DEGRADED/60` sob Robolectric — fail-closed disparou de verdade, não simulado).
- Roteador: `selected=GEMINI_NANO → actual=DETERMINISTIC, FALLBACK/GEMINI_NANO_UNAVAILABLE`, 9 ms wall; regra determinística de socket disparou `SUSPICIOUS/88` ao vivo.
- Storm: 40 msgs em 99 ms → `rate=40.0, storm=true, dropped=0`. **Detecta; não contém** (só flag).
- Atuação real: só `RECLAIM_INTERNAL_CACHE` (arquivos próprios) + flags em memória; kill externo corretamente `UNAVAILABLE`; `NAVIGATE_APP_SETTINGS` monta Intent mas **não chama startActivity**.

## O que está integrado vs quebrado

- Integrado: telemetria→sentinel→células→homeostase→UI; roteador→determinístico; guardrails→roteador; regulação→T-helper (só `isPermitted`; `forceDeterministic/cooldown` ignorados).
- Quebrado/morto: NK 100% desfibrada (`internalCorruptions` sempre 0); `RECOVERING` inalcançável (branch morta); `DeviceTelemetryProvider` + `startContinuousObserving` sem callers; `reasoningResponses/telemetryEvents` flows sem coletores; `RESOURCE_GUARD_DEFINED` nunca produzido (`RESOURCE_GUARD_DENIED`, `NONE` mortos); segunda pipeline legada no ViewModel paralela à `BiomimeticImmuneSystem`; Nano/Gemma retornam Assessment canned (stubs honestos, não inferência).
- Memória: RAM-only, sem expiração, sem limite — e relatórios JSON antigos afirmam Room/SQLite (stale, contradito pelo código).

## Maiores lacunas / maior risco

1. **ViewModel sem cobertura**: scoring, simulação, quarantine, exam — zero testes; 4 defaults esverdeados (`HOMEOSTATIC`, `ACTIVE`, `Healthy` hardcoded, `isRealDeviceDataActive` sempre true).
2. **Homeostase DISPLAY-ONLY**: 8 dimensões computadas, nenhuma decisão as consome (guardrails e células re-derivam thresholds próprios — 4 conjuntos inconsistentes: 88% vs 92%, 200 vs 250 MB).
3. **Resolução pode travar**: exige `totalBytesReclaimed>0` cumulativo; reclaim típico é 0/0.
4. UI promete mais que o backend em ~15 pontos (TLS/certificado/assinaturas/decoys/`0 uncontained`/eficiência/medidor de memória `<40ms`).

## Próximo passo de maior valor

Cobrir `refreshRealTelemetry` + пиipeline de decisão com testes de integração (fecha lacuna 1 e ancora 2–4), depois fundir as duas pipelines e ligar NK + `forceDeterministic`. Detalhe em `PETSCAN_NEXT_STEPS.md`.

## O que não mexer ainda

Células vivas (PRR, T-Helper, Cytotoxic gates, Regulatory veto, Dendritic gate), roteador, guardrails, boundary — o esqueleto de segurança funciona; mexer sem o harness de testes acima é regressão certa.

## O que precisa de dispositivo físico

Nano (AICore), Gemma (pesos+runtime), métricas de performance/bateria, Doze/standby,.Permission UX real, pre-launch. Tudo L3–L5 está em `NOT_VERIFIED`/`NOT_AVAILABLE_IN_ENVIRONMENT`.

## O que ainda é só arquitetura

“Inferência local” (Nano/Gemma), “contenção/barreira” (só flags), “decoys”, “memória persistente”, “watcher contínuo” (sem background), “24/7/autonomous action”.
