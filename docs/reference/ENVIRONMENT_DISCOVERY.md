# V-Watcher — Environment Discovery (2026-09-12)

> Temporary discovery artifact (Phase 2). Truth-first snapshot of the real host.
> Claim ceiling: what is below was **observed** via shell/reads on this Linux host, not inferred.

## Host

- `pwd`: `/media/juan/DATA/V-watcher`
- git root: `NOT_A_GIT_REPO` (`fatal: not a git repository`, stops at filesystem boundary)
- OS: Linux 7.0.0-31-generic x86_64, OpenJDK 21.0.12
- `ANDROID_HOME` / `ANDROID_SDK_ROOT`: both empty

## Project (observed from `app/build.gradle.kts`, `gradle/libs.versions.toml`)

- namespace `com.example`, applicationId `com.aistudio.vwatcher.hkmv`
- `compileSdk = 36 minor 1`, `targetSdk = 36`, `minSdk = 24`
- AGP `9.1.1`, Kotlin `2.2.10`, KSP `2.3.5`, Gradle wrapper dist `9.3.1`
- `gradlew` script: **ABSENT** (only `gradle/wrapper/gradle-wrapper.properties` exists)
- System `gradle` binary: **ABSENT**. Build via Gradle is **NOT_INVESTIGATED/BLOCKED** until wrapper or toolchain is installed
- Firebase AI (`firebase-ai` via BOM 34.17.0) is a dependency (= cloud path exists); **no** `com.google.mlkit:genai-prompt`, **no** `litertlm-android`, **no** MediaPipe LLM dependency in `libs.versions.toml`

## Capability matrix (this host)

| Capability | Status | Evidence |
|---|---|---|
| `opencode` CLI (`opencode --version`) | UNAVAILABLE | `command not found: opencode` (session host runs, CLI binary absent; `~/.opencode` has only backups + SYSTEM_PROMPT.md) |
| Android CLI (`android --version`) | UNAVAILABLE | `command not found: android`; `android skills find genai/prompt` **not executable** |
| Official skill `ml-kit-genai-prompt-api` install | NOT_INVESTIGATED (blocked) | blocked by missing Android CLI; recorded as dependency, not installed |
| Android SDK build-tools / platforms | UNAVAILABLE | `/usr/lib/android-sdk` contains **only** `platform-tools`; no `build-tools/`, no `platforms/`, no `sdkmanager/aapt/bundletool` |
| ADB | AVAILABLE_BUT_LIMITED | `Android Debug Bridge 1.0.41 v34.0.4-debian`; `adb devices -l` → **empty list** |
| Emulator / AVDs | UNAVAILABLE | `command not found: emulator` |
| Physical device with AICore | NOT_INVESTIGATED | no device attached; cannot probe `com.google.android.aicore` from Linux host |
| AICore availability surface (code) | AVAILABLE (code probe only) | `GeminiNanoBackend` probes `PackageManager.getPackageInfo("com.google.android.aicore")` + `enabled` flag; honest UNAVAILABLE path exists |
| ML Kit GenAI Prompt API (dep) | UNAVAILABLE | not in dependency catalog; documented integration path only (see skill §6) |
| LiteRT-LM runtime (dep/artifact) | UNAVAILABLE | no `litertlm-android` dep; no `.litertlm` weights in repo; `python3 -c "import litert_lm"` → ModuleNotFoundError |
| MediaPipe LLM / TFLite | UNAVAILABLE | no dep; only a code comment mentions `LlmInference` as future hook |
| Gemma weights in project | UNAVAILABLE | `GemmaBackend` looks for `filesDir/models/gemma-2b-it-cpu.bin` (pre-Gemma-4 name); no weights shipped |
| Gemma 4 mobile artifacts (E2B/E4B) | NOT_INVESTIGATED | no artifact present; official paths documented in skill §7–9, none downloaded |
| Ollama (dev-machine only) | AVAILABLE | `ollama 0.32.13`; 12 models cached (qwen3/qwen2.5-coder/etc.); **NOT** an on-device Android runtime — eval/dev aid only |
| Kotlin CLI | UNAVAILABLE | `command not found: kotlin` (Gradle/KSP path is the real toolchain) |
| Unit-test sources | AVAILABLE | `ExampleUnitTest`, `ExampleRobolectricTest`, `BiomimeticImmuneSystemTest`, `BiomimeticAdversarialImmuneTest`, `HandoffFoundationTest`, screenshot tests |
| `.opencode/skills/` | UNAVAILABLE (to be created) | `ls .opencode/` → no such directory |
| `docs/` | UNAVAILABLE (to be created) | `ls docs/` → no such directory |

## Organism (observed, reuse — do not duplicate)

- `reasoning/ReasoningRouter.kt` — selects GEMINI_NANO → GEMMA → DETERMINISTIC; records `selectedBackend` vs `actualBackendUsed`, timeout 5000ms default, Mutex + `withTimeoutOrNull`, explicit `FallbackReason`
- `reasoning/GeminiNanoBackend.kt` — AICore package probe; states UNAVAILABLE/PREPARING/READY/ERROR (project-local `ModelReadiness`, **not** ML Kit `FeatureStatus` — do not conflate)
- `reasoning/GemmaBackend.kt` — file-presence probe (`models/gemma-2b-it-cpu.bin`); stub init (no MediaPipe/LiteRT binding yet)
- `reasoning/DeterministicBackend.kt` — always READY rule engine (THERMAL/MEMORY/NETWORK/POWER/EQUILIBRIUM); `isRealOnDeviceModel=false`
- `reasoning/ResourceGuardrails.kt` — battery<15% discharging deny, mem>92%/lowMemory deny, 2500–3000ms cooldown, `Mutex concurrencyLock`
- `reasoning/OnDeviceReasoningEngine.kt` — `checkAvailability/warmup/reason/release`, `SYSTEM_INSTRUCTION`, synthetic observation builder
- `communication/CommunicationModels.kt` — `ExecutionStatus` (SUCCESS/DEGRADED/FALLBACK/RESOURCE_DENIED/FAILED/TIMEOUT), `FallbackReason` (9 codes + NONE), `BackendModelProvenance`, `ReasoningRequest/Response` with `selectedBackend/actualBackendUsed/executionMode`
- `sentinel/Sentinel.kt` (`AndroidSentinel`) — 6 providers + provenance + freshness/staleness 30s
- `immune/ImmuneBus.kt` — capacity 128, replay 20, storm threshold 30/s
- `immune/HomeostasisModel.kt` — `HomeostasisEngine.evaluate()` macro-states incl. STRESSED/ACTIVE_DEFENSE/DEGRADED/UNKNOWN
- `immune/AndroidRealityBoundary.kt` — certified registry: THROTTLE_INTERNAL_INFERENCE, ISOLATE_INTERNAL_SUBSYSTEM, RECLAIM_INTERNAL_CACHE, NAVIGATE_APP_SETTINGS, RESET_SUBSYSTEM_ISOLATION; `KILL_EXTERNAL_PROCESS` → UNAVAILABLE (sandbox); unknown → DENIED
- `memory/LocalImmuneMemoryRepository.kt` — reuse, do not create parallel memory

## Official grounding fetched 2026-09-12 (sources, not copies)

- ML Kit Prompt API get-started (beta4 `com.google.mlkit:genai-prompt:1.0.0-beta4`, API 26+, `Generation.getClient()`, `checkStatus` AVAILABLE/DOWNLOADABLE/DOWNLOADING/UNAVAILABLE, `download()` Flow, `generateContent/Stream`, `warmup()`, temp/seed/topK/candidateCount/maxOutputTokens, <4000-token input, avoid >4K output, per-app quota, AICore bind errors, unlocked-bootloader unsupported) — `developers.google.com/ml-kit/genai/prompt/android/get-started` (updated 2026-09-08)
- System instructions Beta (Nano V3+, <150 words, don't combine with prefix caching) — `/system-instructions` (2026-07-15)
- LiteRT-LM overview + Kotlin guide (`litertlm-android`, `Engine/EngineConfig/Conversation`, CPU/GPU/NPU, `.litertlm`, `initialize()` ≤10s off-UI-thread, Flow/callback send, tools, ThinkingConfig, MTP flag, Gemma4-E2B 2583MB / E4B 3654MB perf table) — `developers.google.com/edge/litert-lm/{overview,android}` (2026-09-04)
- Gemma 4 overview (E2B/E4B/12B/31B/26B-A4B; E2B·E4B mobile; audio+vision native on E2B/E4B/12B; 128K small / 256K medium; function calling; mobile RAM E2B 1.1GB / E4B 2.5GB; QAT `-mobile-transformers/-mobile-ct` wNa8o8) — `ai.google.dev/gemma/docs/core` (2026-07-08)
- ADK Android (`google-adk-kotlin-core-android:0.1.0`, KSP, compileSdk 34+/minSdk 24+, `GenaiPrompt.create()`, hybrid cloud+on-device) — `developer.android.com/ai/adk` (2026-09-08)
- AppFunctions experimental preview (Android 16+/API 36, `@AppFunction`, `AppFunctionManager`, `EXECUTE_APP_FUNCTIONS`, on-device MCP, skill `github.com/android/skills/tree/main/device-ai/appfunctions`, `adb shell cmd app_function`) — `developer.android.com/ai/appfunctions` (2026-09-08)

## What this blocks / allows

- ALLOWED now: skill creation, docs, contract tests (Layer 1 JVM), runtime-detection code, fallback logic, instrumentation stubs — all without faking execution.
- BLOCKED now: Gradle build/test (no wrapper/toolchain/SDK), `android skills` install, on-device AICore/Gemma proof, emulator/device layers.
