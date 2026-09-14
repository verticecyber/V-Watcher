# Network Endpoint Inventory — V-Watcher (2026-09-12)

Method: grep over `app/src/main` for `http://|https://|BASE_URL|retrofit|okhttp|URL(|socket|WebView|DNS` + import scan for `firebase|retrofit2|okhttp3`.

## Result: ZERO network endpoints in shipped code

| Host | Purpose | When used | Data sent | Encryption | Optional? | Offline? |
|---|---|---|---|---|---|---|
| *(none)* | — | — | — | — | — | App is fully offline-capable today |

Evidence:

- Only URL-like strings in main source are developer-docs comments (`developer.android.com/...` in XML samples, `schemas.android.com` namespaces) — no runtime hosts.
- `NetworkTelemetryProvider` reads **state** (`ConnectivityManager/NetworkCapabilities`), never opens sockets.
- ViewModel simulation/case strings mentioning "socket burst to non-whitelisted node" (`VWatcherViewModel.kt:508-618`) are **narrative simulation copy**, not network calls.
- No `WebView`, no `cleartextTraffic` flags, no `networkSecurityConfig` file (default HTTPS-only posture; nothing to downgrade it).

## Latent (not shipped, must gate any future wiring)

- `firebase-ai` (BOM 34.17.0), `retrofit`, `okhttp`, `moshi` are Gradle-declared but **unimported in `app/src/main`** — see `THIRD_PARTY_SDK_AUDIT.md`. Any future use (cloud Gemini via Firebase AI Logic, model-weight download) must append rows here **before** release, with host, payload, TLS, retention, and Data Safety mapping.
- Rule: unknown endpoint at release time = release blocker until explained (this file is the gate).
