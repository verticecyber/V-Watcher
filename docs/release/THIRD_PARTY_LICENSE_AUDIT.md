# Third-Party License Audit — V-Watcher (2026-09-12)

All versions from `gradle/libs.versions.toml`. "Google ≠ no obligations": Apache-2.0 requires license **notice** redistribution; Firebase adds ToS; test-only deps still ship notices via standard tooling.

| Dependency | License | Notice required | Redistribution | Risk |
|---|---|---|---|---|
| AndroidX (core, lifecycle, activity, navigation, Room, compose via BOM 2024.09.00) | Apache-2.0 | Yes (NOTICE via About/libraries) | Permitted | Low — add open-source notices screen or `oss-licenses` plugin before release |
| Compose Material3/Icons, Accompanist (commented) | Apache-2.0 | Yes | Permitted | Low |
| Kotlin 2.2.10, Coroutines 1.10.2 | Apache-2.0 | Yes | Permitted | Low |
| Retrofit/OkHttp/Moshi 1.15.2–2.12.0/4.10.0 | Apache-2.0 | Yes | Permitted | Low — moot after dead-dep removal |
| Firebase BOM 34.17.0 (`firebase-ai`, `appcheck-*`) | Apache-2.0 + Google Firebase ToS / AI terms | Yes + ToS acceptance | Permitted w/ ToS | **Medium** — GenAI terms + data-processing terms apply if wired; App Check debug provider must not ship |
| AGP 9.1.1, KSP, Secrets plugin, google-services plugin | Apache-2.0 (build-time only) | No (not distributed) | — | None |
| Robolectric/Roborazzi/JUnit/Espresso (test) | MIT/Apache/EPL (test scope) | No (not shipped) | — | None |
| Gemma 4 weights | **NOT PRESENT** — Gemma Terms of Use + prohibited-use policy will apply on download | — | — | Track at integration time; do not assume Apache |
| LiteRT-LM / ML Kit (future) | Apache-2.0 (SDK) | Yes | Permitted | Low when added |

Action: add license-notice mechanism (e.g. `com.google.android.gms:play-services-oss-licenses`) OR bundle `NOTICE` file before stable; re-audit on any new dep.
