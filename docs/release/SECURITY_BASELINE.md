# Security Baseline — V-Watcher (2026-09-12)

| Area | Finding | Evidence | Rating |
|---|---|---|---|
| Secrets | No real keys in repo. `.env` gitignored; `.env.example` placeholder only; signing passwords via env (`app/build.gradle.kts:28-38`) | grep `API_KEY|password|secret` → only example/placeholder + env refs | PASS (keep; never commit `local.properties`/`.env`) |
| Logging | **Zero** `Log.*`/`println` in `app/src/main` | grep → no matches | PASS (re-check after debug sessions) |
| Crypto / storage | No encrypted store because **no persistent store at all** (no Room/prefs/files with user data) | import grep → no room/datastore/sharedprefs in main | PASS w/ note (add EncryptedStore deliberately if persistence lands) |
| IPC / exported | Only MainActivity `exported=true` + LAUNCHER filter; no providers/receivers/services/deep links/FileProvider | `AndroidManifest.xml:27-37` | PASS |
| Intent handling / deep links | None exist | manifest grep | N/A |
| WebView / JS bridge | None | grep | N/A |
| Database / backups | No DB; `allowBackup=true` + empty sample rules (`res/xml/backup_rules.xml`, `data_extraction_rules.xml`) | files all-comments | **P2**: define explicit include/exclude before release |
| Clipboard / screenshots | No clipboard APIs; `FLAG_SECURE` absent (clinical data on screen is non-sensitive telemetry; acceptable, document) | grep | P3 (consider `FLAG_SECURE` only if sensitive rows added) |
| Debuggable | No `android:debuggable` in manifest (correct — build type controls it); release type must be verified `debuggable=false` on the AAB | manifest | PASS (verify on artifact) |
| Network security | No `networkSecurityConfig`, no cleartext flags (default-deny cleartext posture); no cert pinning (no endpoints to pin) | manifest+grep | PASS (add config + pinning only with first endpoint) |
| Input validation / serialization | Moshi declared-unused; no deserialization of untrusted input in main; memory-write gates reject synthetic/low-confidence writes (`LocalImmuneMemoryRepository.kt:111-156`) | code | PASS |
| Native code | None, no ABI config | — | N/A |
| Debug surface | No overlays/test menus/mock endpoints; `[SIMULATION]` flows are labeled user-facing demo content, and synthetic writes are persistence-blocked | ViewModel + memory gates | **P1 copy fix APPLIED 2026-09-12** (`isolateApp` strings now in-app-only truthful); remaining `[SIMULATION]` labels must stay visible and be mirrored in store copy |

Release-signing note: no key material in repo (good). Debug keystore referenced but absent; release key via env/operator → `RELEASE_REQUIRES_OPERATOR_SIGNING` (see `RELEASE_BUILD.md`).
