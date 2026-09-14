# Third-Party SDK Audit — V-Watcher (2026-09-12)

Method: `gradle/libs.versions.toml` + `app/build.gradle.kts:78-140` vs. import grep over `app/src/main`.

## Verdict up front (updated 2026-09-12: STRIPPED, verified by `assembleDebug` build)

Dead SDK surface was removed from `app/build.gradle.kts` after proving zero imports in `main+test+androidTest` sources. `assembleDebug` EXIT=0 post-strip.

| Library | Version | Vendor | Purpose | Data access | Network | Privacy / policy impact | License |
|---|---|---|---|---|---|---|---|
| Compose BOM + material3/icons/ui/tooling/activity/navigation/lifecycle | BOM 2024.09.00 etc. | Google/AndroidX | UI | UI state only | No | None | Apache-2.0 |
| core-ktx 1.18.0 | AndroidX | Compat APIs | API shims | No | None | Apache-2.0 |
| Room runtime/ktx/compiler 2.7.0 | AndroidX | Local DB | **UNUSED — no `@Database/@Entity/@Dao` in main** | No | Dead dep → remove | Apache-2.0 |
| Retrofit 2.12.0 + Moshi converter + Moshi 1.15.2 (+codegen) | Square | HTTP/JSON | **UNUSED in main** | Latent | Dead dep → remove | Apache-2.0 |
| OkHttp + logging-interceptor 4.10.0 | Square | HTTP | **UNUSED in main** | Latent | Dead dep → remove | Apache-2.0 |
| Firebase BOM 34.17.0: `firebase-ai`, `appcheck-recaptcha`, `appcheck-debug` | Google | Cloud AI / integrity | **UNUSED in main**; `google-services.json` ABSENT; `googleServices.missing.passthrough=WARN` | Would be cloud AI + attestation | **Highest policy impact**: User Data policy explicitly covers third-party AI (Jul-2026 clarification); cloud AI = disclosure + consent + Data Safety rows. `appcheck-debug` must never ship in release | Apache-2.0 (Firebase ToS apply) |
| Coroutines 1.10.2 | JetBrains | Concurrency | In-memory | No | None | Apache-2.0 |
| Secrets Gradle plugin 2.0.1 + google-services plugin 4.5.0 | Google | Build-time secrets / Firebase wiring | Reads `.env` at build | No | Risk only if real key committed (none; `.env.example` placeholder, `.env` gitignored) | Apache-2.0 |
| KSP 2.3.5, Roborazzi 1.59.0, Robolectric 4.16.1, JUnit | various | Build/test | Test-only | No | None | Apache-2.0/MIT |
| ML Kit GenAI / LiteRT-LM / MediaPipe | — | Google | On-device AI | **NOT DECLARED** | No | Nothing to declare; Nano/Gemma are stubs (see skill docs) | — |

No ads, analytics, crash-reporting, attribution, or remote-config SDKs. No native (`.so`) libraries, no ABI config needed.
