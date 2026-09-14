# V-Watcher Skill Install Report (2026-09-12)

> **A Verdade é o Caminho.** No invented execution. Blocked steps are reported, not skipped.

## Required fields

- `skill_path`: `.opencode/skills/vwatcher-android-ondevice-ai/SKILL.md` (frontmatter `name: vwatcher-android-ondevice-ai`, YAML parses OK, 24 sections + invocation index)
- `skill_discovered`: YES — host announced `New skills are available: vwatcher-android-ondevice-ai` on 2026-09-12 after file creation (no restart needed)
- `official_android_skill_installed`: NO — `android` CLI is `UNAVAILABLE` (`command not found`), so `android skills find genai|prompt` and project-scope install of `ml-kit-genai-prompt-api` could not run. Official skill recorded in `SKILL.md §23` as a knowledge dependency to install once the CLI exists. Not copied blindly (per spec §3).
- `android_cli_version`: UNAVAILABLE (binary absent)
- `opencode_version`: CLI binary absent (`command not found: opencode`); session host itself runs (skill discovery works). Recorded as `HOST_RUNNING / CLI_UNAVAILABLE`.
- `project_sdk`: AGP `9.1.1`, Kotlin `2.2.10`, KSP `2.3.5`, Gradle wrapper dist `9.3.1` (**no `gradlew` script, no system `gradle`**), Java 21.0.12, SDK: only `platform-tools` (adb 34.0.4-debian), **no build-tools/platforms/sdkmanager**
- `min_sdk`: `24` (note: ML Kit Prompt API requires API 26+ → Nano path needs runtime guard)
- `target_sdk`: `36`
- `compile_sdk`: `36 minor 1`
- `gemini_nano_path`: `Android App → ML Kit GenAI Prompt API (com.google.mlkit:genai-prompt:1.0.0-beta4, NOT in deps yet) → AICore → Gemini Nano`. Code probe: `app/src/main/java/com/example/reasoning/GeminiNanoBackend.kt:37` (PackageManager `com.google.android.aicore` + enabled flag; honest UNAVAILABLE path). Real gate after dep added: `Generation.getClient().checkStatus()` AVAILABLE/DOWNLOADABLE/DOWNLOADING/UNAVAILABLE + `download()` + `warmup()` + `generateContent[/Stream]`.
- `gemma4_path`: `Artifact (.litertlm mobile / -mobile-ct) → LiteRT-LM Engine (com.google.ai.edge.litertlm:litertlm-android, NOT in deps yet) → Gemma 4 E2B (default) / E4B (flagship only)`. Code probe: `app/src/main/java/com/example/reasoning/GemmaBackend.kt:38` looks for legacy `filesDir/models/gemma-2b-it-cpu.bin` (pre-Gemma-4 id — migration tracked in `docs/release/V_WATCHER_MODEL_MATRIX.md`); runtime binding (MediaPipe/LiteRT-LM) is a labeled stub (`:78`). No weights in repo.
- `litert_status`: UNAVAILABLE (no dep, no `.litertlm`, `import litert_lm` → ModuleNotFoundError). Integration recipe + 9-step pre-code checklist in `SKILL.md §3`, grounded on `developers.google.com/edge/litert-lm/{overview,android}` (2026-09-04).
- `device_status`: `adb devices -l` → empty; `emulator` → absent; AICore/Gemma on-device proof → NOT_INVESTIGATED (no device). Ollama 0.32.13 + 12 cached models is dev-machine-only, not an Android runtime.
- `tests`: NOT_RUN — `./gradlew testDebugUnitTest` impossible (no `gradlew`, no SDK build-tools/platforms). Existing L1 sources inventoried (`ExampleUnitTest`, `ExampleRobolectricTest`, `BiomimeticImmuneSystemTest`, `BiomimeticAdversarialImmuneTest`, `HandoffFoundationTest`). Test-layer contract (L1–L5, declare layer per result) in `SKILL.md §18`.
- `build`: NOT_RUN — `./gradlew assembleDebug` impossible for the same reason (`ls gradlew` → missing). `android info/describe/docs` likewise blocked (no CLI).
- `limitations`: (1) no Gradle toolchain/SDK → build+tests unvalidated; (2) no Android CLI → official skill not installed; (3) no device/emulator → Nano/Gemma execution unproven, matrix rows `ACTUALLY_TESTED=NO`; (4) `GeminiNanoBackend.execute` / `GemmaBackend.execute` return canned Assessments = contract stubs, not inference proof (§17); (5) Gemma id is pre-Gemma-4; (6) AppFunctions = `candidate` (preview, API 36+). Allowed now without faking: architecture, contracts, detection, fallback, docs, stubs (§33 of spec).

## Invocation test (skill comprehension check)

Agent with skill loaded distinguishes on demand: **Nano/AICore** = platform model via `checkStatus`+AICore (Beta dep, API 26+, quota, `warmup`); **Gemma 4** = open-weight family E2B/E4B-mobile via LiteRT-LM `.litertlm`, six-rung proof, never conflated with Nano; **fallback** = deterministic baseline with `selected vs actual + FallbackReason`, never cross-labeled; **runtime** = Prompt-API/AICore vs LiteRT-LM vs JVM-deterministic with lifecycle + close discipline; **evidence** = trace_id/model/runtime/latency/provenance per `ReasoningResponse`; **authority** = every action crosses `AndroidRealityBoundary`, LLM never self-authorizes. ✅ PASS (by construction of §§1–10, re-verifiable via the five trigger prompts in §0).

## Status flags (spec §35 — honest values)

```text
SKILL_CREATED = YES
SKILL_INSTALLED = YES (.opencode/skills/vwatcher-android-ondevice-ai/SKILL.md)
SKILL_DISCOVERABLE = YES (host announcement observed)
OFFICIAL_ANDROID_GROUNDING_VERIFIED = YES (6 doc families fetched 2026-09-12, pinned in ENVIRONMENT_DISCOVERY.md + runtime doc §7)
PROJECT_COMPATIBILITY_CHECKED = YES (SDK/AGP/Kotlin/minSdk-26-conflict/deps/organism mapped; see discovery + matrix)
BUILD_VALIDATED = NO (toolchain absent — see limitations)
TESTS_VALIDATED = NO (same blocker)
LIMITATIONS_DECLARED = YES (this report + matrix + discovery)
FINAL_STATUS = PARTIAL (skill + grounding + docs complete; build/test/device proof pending toolchain)
```

`IMPLEMENTATION COMPLETE` is deliberately NOT claimed. Next unblock order: install Android SDK build-tools/platforms + `gradlew` wrapper → `./gradlew testDebugUnitTest && ./gradlew assembleDebug` → install Android CLI → `android skills find genai` → install `ml-kit-genai-prompt-api` project-scope → attach AICore device → fill matrix rows.

## Outputs delivered

```text
.opencode/skills/vwatcher-android-ondevice-ai/SKILL.md
docs/release/V_WATCHER_ONDEVICE_AI_RUNTIME.md
docs/release/V_WATCHER_MODEL_MATRIX.md
ENVIRONMENT_DISCOVERY.md (phase-2 artifact)
V_WATCHER_SKILL_INSTALL_REPORT.md (this file)
```
