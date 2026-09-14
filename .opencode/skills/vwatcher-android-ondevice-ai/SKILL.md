---
name: vwatcher-android-ondevice-ai
description: Use when implementing, debugging, validating, or extending V-Watcher Android on-device AI, Gemini Nano/AICore, ML Kit GenAI Prompt API, Gemma 4 local inference, LiteRT-LM, model routing, privacy-preserving local reasoning, fallback behavior, or AI runtime telemetry.
license: Apache-2.0
compatibility: Android V-Watcher Kotlin/Compose project with local OpenCode agents
metadata:
  domain: android-ai
  project: v-watcher
  grounding: android-dev-google-ai-edge
  last-verified: 2026-09-12
---

# vwatcher-android-ondevice-ai

> **A Verdade é o Caminho.**
>
> Implement what exists. Measure what executes. Declare explicitly what you cannot prove.
>
> Never confuse: implementation with execution; availability with use; fallback with
> primary-model success; emulation with physical hardware; API availability with runtime
> availability; code intent with observed effect.
>
> Every technical recommendation respects the claim ceiling. If you did not observe it
> on a real runtime, label it `DOCUMENTED` / `NOT_TESTED` / `UNAVAILABLE` — never `done`.

## 0. When to load this skill

Load on any of:

- `Implement Gemini Nano support for V-Watcher` → load skill, follow §1.
- `Add local Gemma 4 E4B reasoning` → load skill, follow §2–§4.
- `Why is Gemini Nano unavailable?` → load skill, follow §1 + §11 + §15.
- `Measure local inference latency` → load skill, follow §12.
- `Route between deterministic, Nano and Gemma` → load skill, follow §5 + §10.

## 1. Gemini Nano / AICore trail (platform model, NOT a bundled weight)

Chain (verified 2026-09-08, `developers.google.com/ml-kit/genai/prompt/android/get-started`):

```text
Android App → ML Kit GenAI Prompt API → AICore → Gemini Nano
```

Rules:

- Dependency (current, Beta): `implementation("com.google.mlkit:genai-prompt:1.0.0-beta4")`. Requires API 26+. V-Watcher `minSdk=24` (see `app/build.gradle.kts:18`) — guard Nano path with runtime API check; never lower minSdk silently to satisfy one API.
- Entry: `val generativeModel = Generation.getClient()` (Kotlin) / `GenerativeModelFutures.from(Generation.INSTANCE.getClient())` (Java).
- Gate **every** session on `checkStatus()` → `FeatureStatus.AVAILABLE | DOWNLOADABLE | DOWNLOADING | UNAVAILABLE`. Never call inference before `AVAILABLE`.
  - `DOWNLOADABLE` → `download()` (Kotlin `Flow<DownloadStatus>`: `DownloadStarted/DownloadProgress/DownloadCompleted/DownloadFailed`; Java `DownloadCallback`). Handle progress, failure, retry with backoff.
  - `UNAVAILABLE` → Nano unsupported OR AICore config not fetched yet. Route to §5 fallback, record `GEMINI_NANO_UNAVAILABLE`.
- `warmup()` before first inference to cut cold-start cost. Inference: `generateContent(...)` (full) vs `generateContentStream(...)` (chunks). Optional per-request: `temperature, seed, topK, candidateCount, maxOutputTokens`.
- Limits (documented): input **< 4000 tokens** (~3000 EN words, count via `countTokens`); avoid outputs **> 4K tokens**; AICore enforces **per-app inference quota**.
- Beta/Alpha siblings — use only when the task needs them, each with its own guard: system instructions Beta (`SystemInstruction`, Nano V3+ devices, keep <150 words, do NOT combine with prefix caching); prefix caching Experimental; structured output Alpha (needs KSP + extra deps); thinking mode Beta; model selection page exists (`.../prompt/android/select-model`).
- Setup failures are real: AICore bind `4-CONNECTION_ERROR/601`, `3-PREPARATION_ERROR/606-FEATURE_NOT_FOUND` (config not downloaded yet — wait/retry on network, reboot speeds it), `1-DOWNLOAD_ERROR` (DNS/offline). **Not supported on unlocked-bootloader devices.** Always `checkStatus()` before showing AI UI so users never see raw AICore errors.
- V-Watcher mapping: project-local `reasoning/ReasoningModels.kt:3` `ModelReadiness` (READY/PREPARING/UNAVAILABLE/ERROR) is **not** ML Kit `FeatureStatus`. `reasoning/GeminiNanoBackend.kt:37` currently probes `PackageManager("com.google.android.aicore")` + `enabled` flag as a *pre-ML-Kit* heuristic. When you add the real dependency, `checkStatus()` becomes the authority and the package probe becomes a diagnostic hint only. Keep the honest UNAVAILABLE path (`GeminiNanoBackend.kt:51-56`).

## 2. Gemma 4 local (packaged/downloaded model, NOT Nano)

Gemma 4 is a **family of open-weight models**, not a synonym of Gemini Nano (verified 2026-07-08, `ai.google.dev/gemma/docs/core`):

```text
E2B / E4B (mobile-edge) · 12B (unified multimodal) · 31B (dense) · 26B A4B (MoE, 4B active/token)
```

- Mobile rule: `PRIMARY_SMALL_LOCAL = Gemma 4 E2B / E4B` **only after** hardware check. E2B targets mid-range phones; E4B needs flagship RAM. Official mobile load (LiteRT-LM): E2B ~1.1 GB (0.84 text-only), E4B ~2.5 GB (2.2 text-only). 12B/31B/26B-A4B are **not** phone targets.
- Capabilities that matter to V-Watcher: native audio+vision on E2B/E4B/12B, 128K context (small) / 256K (medium), built-in function calling + system-role support + thinking modes + MTP draft models. But **tool calling ≠ authority** (§7).
- Current code gap (truth): `reasoning/GemmaBackend.kt:26` says `Google Gemma 2B On-Device` and looks for `filesDir/models/gemma-2b-it-cpu.bin` — a **pre-Gemma-4 identifier**. Do not relabel it "Gemma 4" until a real Gemma 4 artifact + runtime is wired. Track the rename as a migration task in `docs/V_WATCHER_MODEL_MATRIX.md`.

## 3. LiteRT-LM runtime (for Gemma 4, when supported)

LiteRT-LM is the production orchestration layer over LiteRT for edge LLMs (verified 2026-09-04, `developers.google.com/edge/litert-lm/{overview,android}` + `github.com/google-ai-edge/LiteRT-LM`). Nano never uses it; Gemma 4 does.

- Artifact: `implementation("com.google.ai.edge.litertlm:litertlm-android:latest.release")` (check Maven for pinned version; never float `latest.release` in release builds). JVM variant `litertlm-jvm` is for desktop tooling only.
- Model files: `.litertlm` from HuggingFace `litert-community` (e.g. `gemma-4-E2B-it-litert-lm` 2583 MB, `gemma-4-E4B-it-litert-lm` 3654 MB). Reference perf (S26 Ultra): E2B CPU prefill 557 / decode 47 tk/s, GPU 3808/52; E4B CPU 195/18, GPU 1293/22. Treat as orientation, measure on target (§12).
- Minimal Kotlin shape (do not invent beyond this):
  `Engine(EngineConfig(modelPath, backend=Backend.CPU()/GPU()/NPU(...), cacheDir?, visionBackend?, audioBackend?))` → `engine.initialize()` (**≤10 s, always off UI thread**) → `engine.createConversation(ConversationConfig(systemInstruction, samplerConfig, tools, thinkingConfig, ...))` → `sendMessage(...)` sync / `sendMessageAsync(...)` callback or `Flow` (recommended with coroutines). `Conversation` and `Engine` are `AutoCloseable` — always `close()/use{}`.
- Android manifest for GPU: `<uses-native-library android:name="libvndksupport.so" android:required="false"/>` + `libOpenCL.so`. NPU needs `nativeLibraryDir = context.applicationInfo.nativeLibraryDir` (see guide).
- Tools: `class X : ToolSet { @Tool fun f(@ToolParam p: String, ...): Map<String,Any> }` + optional `OpenApiTool`; register in `ConversationConfig(tools=...)`; `automaticToolCalling=false` for manual V-Watcher-mediated execution (§7). Thinking: `ThinkingConfig(enableThinking, thinkingTokenBudget)`; MTP: `@OptIn(ExperimentalApi::class) ExperimentalFlags.enableSpeculativeDecoding=true` (GPU recommended).
- Pre-code checklist (all 9, in order): 1) current doc page, 2) official sample (AI Edge Gallery `github.com/google-ai-edge/gallery`), 3) pinned artifact version, 4) Gradle coordinate, 5) Android compat, 6) weight format (`.litertlm` vs `-mobile-transformers/-mobile-ct`), 7) mobile variant (E2B first), 8) load lifecycle (init thread + close), 9) memory + concurrency budget. Skip any step → mark `NOT_INVESTIGATED`.

## 4. Model format / weights — six distinct claims

Never collapse into `Gemma available`. Prove each rung:

```text
model declared → artifact present → artifact valid → runtime initialized → inference executed → result observed
```

- Gemma 4 weight routing (official QAT collections): GGUF `-gguf` = desktop/Ollama-style eval only; server `-w4a16-ct`; speculative `-unquantized/-assistant`; **mobile = `-mobile-transformers` / `-mobile-ct`** (`wNa8o8` schema, 2-bit decode layers — E2B/E4B only). If the file in `filesDir` is not a mobile variant, say so.
- First question on any Gemma task: **which artifact really exists in this project?** (`ls`, hash, size, source URL). `GemmaBackend.kt:38` file-presence check is rung 2 only; rung 3+ (MediaPipe `LlmInference` or LiteRT-LM `Engine`) is still a stub comment (`GemmaBackend.kt:78`) — keep it labeled as such.

## 5. V-Watcher router (existing contract — evolve, don't fork)

Order: `DETERMINISTIC → GEMINI_NANO → GEMMA_LOCAL → DETERMINISTIC_DEGRADED` is advisory; the code authority is `reasoning/ReasoningRouter.kt:38` (Nano READY → Gemma READY → deterministic, each gated by `ResourceGuardrails.evaluate`). Keep code as truth; update docs when code changes, not vice versa.

- Every `ReasoningResponse` (`communication/CommunicationModels.kt:46`) must carry: `requested_backend` (= `request.preferredBackend ?: GEMINI_NANO`), `selected_backend`, `actual_backend_used`, `executionMode` (model|deterministic), `executionStatus`, `fallback_reason`, `failure_reason`/`errorMessage`, `latencyMs`, `model_identity` + `runtime_identity` (via `BackendModelProvenance`), `trace_id` (`requestId`/`correlationId`).
- Naming honesty: Nano answered → `Gemini response`; Gemma answered → `Gemma response`; deterministic answered → `deterministic baseline` — never cross-label. `isRealOnDeviceModel=false` on deterministic (`DeterministicBackend.kt:130`) is load-bearing; preserve it.
- Timeout/concurrency: default `timeoutMs=5000` (`CommunicationModels.kt:43`), `withTimeoutOrNull` + `Mutex` in router (`ReasoningRouter.kt:131-152`); timeout → `TIMEOUT + MODEL_TIMEOUT`, lock busy → `FALLBACK + CONCURRENCY_LOCKED`, model error → `FAILED + MODEL_INITIALIZATION_FAILED`. Never swallow these as success.

## 6. LLM is reasoning layer, never authority

Critical functions run **without** any neural model: telemetry (`telemetry/`), sentinel (`sentinel/Sentinel.kt`), homeostasis (`immune/HomeostasisModel.kt`), state machine, policy gates, authority checks, resource guards, resolution, memory integrity, security boundary, fallback. LLM may classify / correlate / suggest / summarize / prioritize / reason / call tools **only when explicitly authorized** — it can never self-grant authority.

## 7. Android Reality Boundary (mandatory traversal)

```text
MODEL → PROPOSED_ACTION → AUTHORITY_CHECK → POLICY_CHECK → ANDROID_REALITY_BOUNDARY → ACTUATE/REFUSE/UNAVAILABLE → OBSERVE RESULT
```

Authority: `immune/AndroidRealityBoundary.kt:32`. Certified actions: `THROTTLE_INTERNAL_INFERENCE, ISOLATE_INTERNAL_SUBSYSTEM, RECLAIM_INTERNAL_CACHE, NAVIGATE_APP_SETTINGS, RESET_SUBSYSTEM_ISOLATION`. `KILL_EXTERNAL_PROCESS` → `UNAVAILABLE` (sandbox, `AndroidRealityBoundary.kt:132`); unknown → `DENIED`. Model output that bypasses this class is a defect, even if the text looks correct.

## 8. Privacy / offline-first

Rule: **observe broadly, retain minimally, share selectively.** Pipeline is `raw telemetry → local processing → operational memory/antibody`, never `raw telemetry → cloud` by default. Cloud path exists only via `firebase-ai` and must be explicit + consented. ADK guidance (verified `developer.android.com/ai/adk`): prefer on-device `GenaiPrompt` (Nano) for privacy-sensitive sub-agents; cloud `Gemini` orchestrator only through backend/Firebase AI Logic, never with embedded keys. No-network behavior: telemetry → immune system → deterministic control → local AI if available. LLM is optional; organism is not.

## 9. Agentic shape

V-Watcher = `Deterministic Core + Telemetry + Immune Cells + Local AI + Governance`, never `LLM + prompt = app`. Gemma 4 function calling / LiteRT-LM tools / ADK `LlmAgent+@Tool` are reasoning conveniences; every call crosses §7. ADK Android shape (verified): `google-adk-kotlin-core-android:0.1.0` + KSP processor, `LlmAgent(name, model=Gemini|GenaiPrompt.create(generativeModel), instruction, tools=generatedTools())`, `InMemoryRunner.runAsync(...).collect{}` from a coroutine. Do not add ADK + ML Kit + LiteRT-LM at once — one runtime per change, measured.

## 10. AppFunctions — candidate, not implementation

Status: **experimental preview, Android 16+ (API 36) only** (verified `developer.android.com/ai/appfunctions`). V-Watcher `targetSdk=36/minSdk=24` → any AppFunctions work must be `RequiresApi(36)`-gated and recorded as `candidate|preview|supported|not-applicable`, never assumed on older devices. Surface: `@AppFunction @AppFunctionSerializable @AppFunctionServiceEntryPoint / AppFunctionService / AppFunctionManager.isAppFunctionEnabled / EXECUTE_APP_FUNCTIONS`, on-device MCP equivalent, skill `github.com/android/skills/tree/main/device-ai/appfunctions`, probe `adb shell cmd app_function list-app-functions`. Do NOT implement speculatively; file the benefit hypothesis (e.g. expose `createTask`-style triage as agent-callable tool) and stop.

## 11. Device capability matrix (measure, don't assert)

Build per device (`docs/V_WATCHER_MODEL_MATRIX.md`):

| Capability | Device | API | Runtime | Model | Status |
|---|---|---|---|---|---|
| AICore | … | Prompt API `checkStatus` | AICore | Nano | AVAILABLE/DOWNLOADABLE/DOWNLOADING/UNAVAILABLE + evidence |
| Gemini Nano | … | Prompt API | AICore | Nano | + `warmup` result, quota hits |
| Gemma 4 E2B | … | LiteRT-LM `Engine` | LiteRT-LM CPU/GPU/NPU | E2B mobile-ct | + TTFT, tok/s, peak MB |
| Gemma 4 E4B | … | LiteRT-LM `Engine` | LiteRT-LM | E4B mobile-ct | same |
| Offline inference | … | … | … | … | airplane-mode proof |
| Tool calling | … | … | … | … | auto vs manual |

Separate four columns of truth: `DOCUMENTED_CAPABILITY | DEVICE_CAPABILITY | ACTUAL_RUNTIME_CAPABILITY | ACTUALLY_TESTED`. No `yes` without a test row.

## 12. Performance (numbers or it didn't happen)

Measure `cold_start, warm_start, model_load, warmup, first_token/TTFT, time_to_complete, tok/s, p50/p95/p99, memory_peak, battery_impact_if_measurable`. Ban `fast/slow/low-memory` as conclusions. Nano: `warmup()` exists to amortize first inference — A/B with and without. LiteRT-LM: report backend (CPU/GPU/NPU) + MTP on/off alongside every number. Store raw runs in `PERFORMANCE_RESULTS.json`, summarize in matrix doc.

## 13. Lifecycles (four clocks, not one)

```text
UNINITIALIZED → CHECKING → DOWNLOADABLE → DOWNLOADING → READY → WARMING → INFERENCE → DEGRADED / UNAVAILABLE → RELEASED
```

Track app / model / runtime / inference lifecycles separately (`ReasoningEngineState` in `ReasoningModels.kt:45` already splits nano/gemma/deterministic + `isWarm`). Release heavy models aggressively (`release()/shutdown()`); never hold E2B/E4B resident without a measured reason.

## 14. Resource guardrails

Signals: memory pressure, thermal, battery, network, CPU, storage, model availability (`ResourceGuardrails.kt:22`, `HomeostasisEngine.evaluate`). Degradation ladder: `NORMAL → LOCAL_REASONING → REDUCED_CONTEXT → DETERMINISTIC → DISABLE_OPTIONAL_AI`. Every step down logs a reason (`FallbackReason`: `BATTERY_CRITICAL_DISCHARGING <15%+discharging, SEVERE_MEMORY_PRESSURE >92%, RATE_LIMIT_COOLDOWN, CONCURRENCY_LOCKED…`); silent fallback is a bug.

## 15. Failure model (test each, record each)

`AICore unavailable/downloadable/download-failed, inference timeout, quota exhausted, model-load failure, Gemma unavailable/weights-missing/runtime-failure, OOM, thermal stress, process death, network/storage unavailable`. Per case: `expected | actual | fallback | evidence | severity`. Unlocked bootloader and fresh-AICore-config races (§1) belong here as first-class cases.

## 16. Truth-first logging

Every AI event: `trace_id, timestamp, backend, model, runtime, requested_action, authority, result, fallback_reason, evidence_refs`. Distinguish `MODEL_AVAILABLE / MODEL_LOADED / MODEL_EXECUTED / MODEL_FAILED / MODEL_NOT_AVAILABLE / FALLBACK_EXECUTED`. Never `ai=true`. Reuse `ReasoningResponse` + `BackendModelProvenance` + `CommunicationChannel` stats; extend, don't parallel-log.

## 17. No mocks in production paths

Mocks live only in `unit tests / deterministic tests / failure injection / contract tests`, explicitly labeled. Never mock `Gemini Nano executed / Gemma executed / telemetry observed / physical action executed`. Current `GeminiNanoBackend.execute` / `GemmaBackend.execute` return canned `Assessment`s after the readiness gate — treat them as **contract stubs pending real-runtime binding** (ML Kit `generateContent` / LiteRT-LM `sendMessage`), not as inference proof.

## 18. Test layers (declare the layer on every result)

L1 pure JVM → L2 Robolectric/Android runtime → L3 emulator → L4 physical device → L5 external instrumentation. This host today: L1 writable, L2–L5 blocked (no SDK/emulator/device — see `ENVIRONMENT_DISCOVERY.md`). XCTest-style cloud-device runs are L5, not L4.

## 19. Anti-hallucination / grounding rule

```text
DISCOVER → VERIFY CURRENT DOC → VERIFY PROJECT COMPAT → IMPLEMENT → BUILD → TEST
```

Never `REMEMBERED → ASSUMED → IMPLEMENTED`. Before any Android AI API, open the current official page (index in `docs/V_WATCHER_ONDEVICE_AI_RUNTIME.md` §Sources). Memory of an API is not authority when docs are dated 2026-07/09.

## 20. Local routing economy

Prefer `micro/deterministic → Nano → Gemma E2B/E4B` by measured cost/latency/capacity. Goal: **minimum sufficient intelligence**. Biggest model by default is a defect.

## 21. Agent contract (reuse existing types)

`ReasoningRequest` already carries `requestId/correlationId/observation/candidateAnomaly/promptInstruction/preferredBackend/timeoutMs`; result contract is `ReasoningResponse` + `Assessment` (`classification/confidence/severity/rationale/evidence/recommendedAction`). Map the spec's `InferenceRequest{traceId,contextRefs,task,constraints,allowedActions}` / `InferenceResult{traceId,model,runtime,result,confidenceBasis,evidenceRefs,suggestedActions,fallback}` onto these — extend fields, never duplicate the structs.

## 22. Evolve the organism (read before writing)

Before new classes, inspect in order: `AndroidSentinel` (§trail entry), `ImmuneBus`, `HomeostasisModel`, `ReasoningRouter`, `AndroidRealityBoundary`, `MemoryCell` (`memory/LocalImmuneMemoryRepository.kt`), cells, tests, schemas, logs. Second parallel AI architecture is rejected in review.

## 23. Official sources (pinned 2026-09-12)

- Android AI overview / Gemini Nano: `developer.android.com/ai/gemini-nano`, `developer.android.com/ai/overview`
- ML Kit Prompt API: `developers.google.com/ml-kit/genai/prompt/android/{get-started,system-instructions,prefix-caching,structured-output,thinking-mode,select-model}` + Kotlin/Java ref `com.google.mlkit.genai.prompt`
- Android skills registry pattern: official skill id `ml-kit-genai-prompt-api` via Android CLI (`android skills find genai|prompt`); Tessl mirror `tessl.io/registry/skills/github/android/skills/ml-kit-genai-prompt-api` is an index, not authority
- Gemma 4: `ai.google.dev/gemma/docs/core` + `.../core/model_card_4` + HF `collections/google/{gemma-4,gemma-4-qat-q4-0,gemma-4-qat-mobile}` + `litert-community/{gemma-4-E2B-it-litert-lm,gemma-4-E4B-it-litert-lm}`
- LiteRT-LM: `developers.google.com/edge/litert-lm/{overview,android}` + `github.com/google-ai-edge/LiteRT-LM` + Gallery `github.com/google-ai-edge/gallery`
- ADK Android: `developer.android.com/ai/adk` + `adk.dev/get-started/kotlin`
- AppFunctions: `developer.android.com/ai/appfunctions{,/add-appfunctions}` + skill `github.com/android/skills/tree/main/device-ai/appfunctions` + samples `github.com/android/appfunctions`
- Android CLI docs flow: `android info|describe|docs` (per CLI reference; CLI itself UNAVAILABLE on this host — see install report)

## 24. Validation before claiming done

1. YAML frontmatter parses (`name` exact `vwatcher-android-ondevice-ai`). 2. Path exact `.opencode/skills/vwatcher-android-ondevice-ai/SKILL.md`. 3. Skill discoverable (skill list shows it; restart host if needed). 4. Invocation test: agent summarizes Nano vs Gemma vs fallback vs runtime vs evidence vs authority correctly. 5. Build/tests: project has **no `gradlew`** and no SDK — run real commands (`./gradlew testDebugUnitTest`, `./gradlew assembleDebug` once toolchain exists; `android info/describe/docs` once CLI exists) and paste output into `V_WATCHER_SKILL_INSTALL_REPORT.md`. Missing toolchain is a reported limitation, never a skipped step.
