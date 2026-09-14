# V-Watcher On-Device AI Runtime (grounded 2026-09-12)

> Living technical memory for local agents. Code paths are project truth; vendor docs are API truth. Neither replaces a device measurement.

## 1. The two runtimes (never conflate)

| | Gemini Nano (platform model) | Gemma 4 (bundled/downloaded model) |
|---|---|---|
| Provisioning | Ships with device via **AICore**; app accesses through **ML Kit GenAI Prompt API** | Open weights; app ships/fetches artifact and runs it in **LiteRT-LM** (or legacy MediaPipe) |
| App dependency | `com.google.mlkit:genai-prompt:1.0.0-beta4` (Beta, API 26+) | `com.google.ai.edge.litertlm:litertlm-android` + `.litertlm` weights |
| Availability gate | `Generation.getClient().checkStatus()` → `AVAILABLE/DOWNLOADABLE/DOWNLOADING/UNAVAILABLE` + `download()` Flow + `warmup()` | File present + valid mobile variant + `Engine.initialize()` success |
| V-Watcher class | `reasoning/GeminiNanoBackend.kt` (AICore package probe = pre-ML-Kit heuristic) | `reasoning/GemmaBackend.kt` (file probe for `gemma-2b-it-cpu.bin` = pre-Gemma-4 id) |
| Label on success | `Gemini response` | `Gemma response` (only after real binding; today: stub) |

## 2. Nano integration recipe (ML Kit Prompt API)

1. Add dep, `minSdk` guard (project is 24 < 26 required) + `checkStatus()` before any UI.
2. `DOWNLOADABLE` → `download().collect{...}` with progress/failure; `AVAILABLE` → optional `warmup()` → `generateContent` / `generateContentStream`.
3. Per-request knobs: `temperature, seed, topK, candidateCount, maxOutputTokens`; count with `countTokens`; keep input <4000 tokens, avoid >4K outputs; respect per-app quota.
4. Advanced (each gated): system instructions Beta (Nano V3+, <150 words, no prefix-caching combo), prefix caching Experimental, structured output Alpha (+KSP), thinking mode Beta.
5. Errors: bind `601`, config-race `606` (wait on network / reboot), download DNS failure, **unlocked bootloader = unsupported**.
6. Wire result into `ReasoningResponse(actualBackendUsed=GEMINI_NANO, executionMode=model, provenance.isRealOnDeviceModel=true)`; any deviation → fallback path with `FallbackReason`.

## 3. Gemma 4 integration recipe (LiteRT-LM)

1. Pick variant by hardware: E2B default, E4B only on flagship RAM (mobile loads 1.1 vs 2.5 GB). 12B/31B/26B-A4B are not phone targets.
2. Fetch **mobile** weights: `-mobile-transformers` / `-mobile-ct` (QAT `wNa8o8`) or `.litertlm` from `litert-community` (`gemma-4-E2B-it-litert-lm` 2583 MB). GGUF = desktop eval only.
3. `Engine(EngineConfig(modelPath, backend, cacheDir?))` → `initialize()` off-UI-thread (≤10 s) → `createConversation(ConversationConfig(...))` → `sendMessage` / `sendMessageAsync` Flow. `close()` both. GPU manifest needs `libvndksupport/libOpenCL` native-lib entries; NPU needs `nativeLibraryDir`.
4. Tools only via `ToolSet/@Tool/@ToolParam` registered in `ConversationConfig`; prefer `automaticToolCalling=false` so `AndroidRealityBoundary` mediates. Thinking via `ThinkingConfig`; MTP via `ExperimentalFlags.enableSpeculativeDecoding` on GPU.
5. Validate six rungs (§4 of skill) and record artifact hash/size/source before claiming `MODEL_LOADED`.

## 4. Router, guardrails, boundary (project truth)

- `ReasoningRouter` (`reasoning/ReasoningRouter.kt:38`): Nano READY → Gemma READY → deterministic; `selectedBackend` vs `actualBackendUsed` always recorded; 5 s timeout; `Mutex` anti-concurrency.
- `ResourceGuardrails` (`reasoning/ResourceGuardrails.kt:22`): deny on battery<15% discharging, mem>92%/lowMemory, cooldown 2.5–3 s. `HomeostasisEngine.evaluate` adds thermal/storm/self-destabilization dimensions.
- `AndroidRealityBoundary` (`immune/AndroidRealityBoundary.kt:32`): only certified actions actuate; external kill → `UNAVAILABLE`; unknown → `DENIED`.
- `DeterministicBackend` (`reasoning/DeterministicBackend.kt:19`): always-READY baseline, `isRealOnDeviceModel=false`.

## 5. Privacy + offline

Local-first: sentinel → immune bus → homeostasis → deterministic → local AI. Cloud (`firebase-ai`) is explicit-only. ADK hybrid pattern: cloud orchestrator + on-device `GenaiPrompt` sub-agents for sensitive slices; no embedded keys.

## 6. AppFunctions (candidate)

Experimental, API 36+. No implementation until a concrete V-Watcher tool hypothesis + `adb shell cmd app_function` probe on a real Android 16 device. Status field: `candidate`.

## 7. Sources (pinned)

- Prompt API get-started (2026-09-08): `https://developers.google.com/ml-kit/genai/prompt/android/get-started`
- System instructions (2026-07-15): `https://developers.google.com/ml-kit/genai/prompt/android/system-instructions`
- LiteRT-LM overview / Kotlin (2026-09-04): `https://developers.google.com/edge/litert-lm/overview`, `https://developers.google.com/edge/litert-lm/android`
- Gemma 4 overview (2026-07-08): `https://ai.google.dev/gemma/docs/core`
- ADK Android (2026-09-08): `https://developer.android.com/ai/adk`
- AppFunctions (2026-09-08): `https://developer.android.com/ai/appfunctions`
- Gallery / LiteRT-LM code: `https://github.com/google-ai-edge/gallery`, `https://github.com/google-ai-edge/LiteRT-LM`
- Weights: `https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm`, `https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm`
