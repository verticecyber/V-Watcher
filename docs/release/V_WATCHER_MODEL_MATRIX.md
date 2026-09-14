# V-Watcher Model Matrix (living — update on every device run)

> One row per (device × capability). `DOCUMENTED` ≠ `TESTED`. No `yes` without evidence.

## Legend

- `DOCUMENTED_CAPABILITY`: vendor docs claim support.
- `DEVICE_CAPABILITY`: OS/hardware plausibly supports (API level, RAM, AICore package).
- `ACTUAL_RUNTIME_CAPABILITY`: runtime gate passed (`checkStatus`, `Engine.initialize`).
- `ACTUALLY_TESTED`: inference executed and result observed with trace.

## Matrix (2026-09-12 baseline — Linux dev host, no Android device)

| Capability | Device | API | Runtime | Model | Documented | Device | Runtime | Tested | Notes |
|---|---|---|---|---|---|---|---|---|---|
| AICore | — (no device) | Prompt API `checkStatus` | AICore | Nano | YES (`get-started` 2026-09-08) | UNKNOWN | NOT_RUN | NO | `adb devices` empty; cannot probe `com.google.android.aicore` |
| Gemini Nano infer | — | `generateContent/Stream` | AICore | Nano | YES (beta4, API 26+) | UNKNOWN | NOT_RUN | NO | code probe only (`GeminiNanoBackend.kt:37`); needs dep + device |
| Gemma 4 E2B | — | LiteRT-LM `Engine` | LiteRT-LM CPU/GPU/NPU | E2B `mobile-ct` / `.litertlm` 2583 MB | YES (`ai.google.dev/gemma`, litert-lm overview) | UNKNOWN | NOT_RUN | NO | no artifact in repo; current id `gemma-2b-it-cpu.bin` is pre-Gemma-4 |
| Gemma 4 E4B | — | LiteRT-LM `Engine` | LiteRT-LM | E4B `.litertlm` 3654 MB | YES | UNKNOWN | NOT_RUN | NO | needs flagship RAM (~2.5 GB);Burns 2.2 text-only |
| Offline inference | — | — | — | — | YES (Gallery runs offline; ADK `GenaiPrompt` offline) | UNKNOWN | NOT_RUN | NO | airplane-mode proof required |
| Tool calling | — | LiteRT-LM tools / ADK `@Tool` / Gemma4 FC | LiteRT-LM / ADK | E2B/E4B/FunctionGemma | YES | UNKNOWN | NOT_RUN | NO | must run `automaticToolCalling=false` + boundary mediation |
| AppFunctions expose | — | `AppFunctionManager` | OS (API 36+) | — | PREVIEW (`appfunctions` 2026-09-08) | UNKNOWN | NOT_RUN | NO | status `candidate`; project minSdk 24 → gating required |

## Reference perf (orientation only — NOT V-Watcher measurements)

- Gemma4-E2B (S26 Ultra): CPU 557 prefill / 47 decode tk/s, TTFT 1.8 s, peak 1733 MB; GPU 3808/52, TTFT 0.3 s.
- Gemma4-E4B (S26 Ultra): CPU 195/18, GPU 1293/22.
- Source: `developers.google.com/edge/litert-lm/overview` (2026-09-04). Re-measure on target before routing decisions.

## Project code truth (rung assessment)

| Claim | Rung reached | Evidence |
|---|---|---|
| `GeminiNanoBackend` detects AICore | rung 1–2 (declared + package heuristics) | `GeminiNanoBackend.kt:37-63`; real gate (`checkStatus`) not wired (no dep) |
| `GeminiNanoBackend.execute` = Nano inference | NOT_REACHED (contract stub) | returns canned `Assessment` (`:125-145`); no `generateContent` call |
| `GemmaBackend` detects weights | rung 1–2 (file probe) | `GemmaBackend.kt:38-66` looks for legacy `gemma-2b-it-cpu.bin` |
| `GemmaBackend.execute` = Gemma inference | NOT_REACHED (contract stub) | canned `Assessment` (`:121-141`); `LlmInference`/LiteRT-LM hook is a comment (`:78`) |
| Router fallback honesty | rung 5–6 (executed + observed in JVM tests) | `ReasoningRouter.kt:106-216` records selected vs actual + reasons |

## Next measurements (in order)

1. Attach Android 16 device or emulator with AICore → `checkStatus()` × `download()` × `warmup()` × `generateContent` → fill Nano row + perf.
2. Download E2B `.litertlm` mobile artifact (hash + size) → `Engine.initialize()` on target → TTFT/tok-s/peak → fill E2B row.
3. Airplane-mode repeat of 1–2 → fill offline row.
4. `automaticToolCalling=false` tool round-trip through `AndroidRealityBoundary` → fill tool-calling row.
