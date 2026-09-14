# Release Readiness Matrix — V-Watcher (2026-09-12)

| Area | Requirement | Current state | Evidence | Gap | Severity | Action | Status |
|---|---|---|---|---|---|---|---|
| Target API | 36+ from 2026-08-31 | targetSdk 36, compileSdk 36.1 | `app/build.gradle.kts:12-24` + Play help 11926878 | 34→36 behavior audit on device | P2 | Run behavior-change checklist on Android 16 device | PASS (config) / NOT_VERIFIED (behavior) |
| Release build | `.aab` via `bundleRelease` | bundleRelease EXIT 0, AAB 11 MB sha `28dd9bed…`, validate 0 | `RELEASE_PACKAGE.md` + AAB | None local | — | PASS |
| Signing | Upload key + Play App Signing, no secrets in repo | Env-driven; local NOT-FOR-PLAY test key; no key material in repo | `app/build.gradle.kts:26-49`, AAB sig | Real upload key + App Signing | P1 | Operator runbook (`PLAY_CONSOLE_ACTIONS.md`) | REQUIRES_HUMAN_SIGNING |
| App ID | Stable production ID | `com.aistudio.vwatcher.hkmv` provisional-style; full inventory done (no providers/links/Room to break) | gradle + grep | Identity decision | P1 | Decide before first upload | REQUIRES_HUMAN_DECISION |
| Versioning | Monotonic versionCode, tags | vCode 1 / vName 1.0, git init + main, no commit/tags yet | `git status` | First commit + tag strategy | P1 | Commit + tag at release | LOCAL_GIT_READY |
| Manifest | Minimal, justified, unexported | 3 perms in artifact, INTERNET removed, backup rules wired | AAB manifest dump | QUERY declaration filing | P1 | Console form | PASS w/ Console action |
| Permissions policy | Declaration for high-risk | Justified + disclosure strings; forms unfiled | `PERMISSIONS_AUDIT.md` | Filing | P1 | Console form | REQUIRES_PLAY_CONSOLE |
| Privacy/Data Safety | Map + inventory + policy + form | Map + post-strip inventory + technical draft; policy URL + form open | `PRIVACY_POLICY_DRAFT.md`, `V_WATCHER_DATA_INVENTORY.md` | Legal review + URL + filing | **P0 ext** | Human legal + filing | DATA_SAFETY_TECHNICAL_BASELINE_READY |
| Network | Known endpoints only | Zero endpoints, artifact-confirmed | AAB dump + dex scan | None | — | Keep gate | PASS |
| AI/model privacy | Local-only proof | Stubs, no deps/weights/traffic | skill docs + SDK audit | None until wiring | — | Re-audit on wiring | PASS |
| SDK/licences | Needed-only + notices | Dead surface stripped + build-verified; notices duty listed | `THIRD_PARTY_SDK_AUDIT.md`, build outputs | OSS notices screen before stable | P2 | Add notices mechanism | PASS w/ P2 |
| Security | Baseline clean | Repo + dex scans clean; signed; debuggable=false | `SECURITY_BASELINE.md`, AAB | None | — | — | PASS |
| Deception risk | No misleading claims | All isolation strings in-app-truthful, test-verified | `VWatcherViewModel.kt`, 27/27 tests | Store-copy mirroring | P1 ext | Human copy | PASS (code) |
| Quality/perf/stability/a11y/adaptive | Core + Adaptive bars | 27/27 unit, lint 0 errors; device metrics unrun | test XML, lint XML | Device lab + pre-launch | P2 | Upload + lab | PARTIAL |
| Doze/backup/deletion | Declared posture | Foreground-only; explicit stateless excludes; RAM-only data | XMLs + AAB manifest | None | — | — | PASS |
| Store material/ratings/access | Complete listing | Icon only; checklist ready | `PLAY_STORE_READINESS.md`, `PLAY_CONSOLE_ACTIONS.md` | Full pack | P1 ext | Human | REQUIRES_PLAY_CONSOLE |
| Testing gate (12×14) | Account-dependent | Unknown account type | Play help 14151465 | Determine + run | P1 process | Human | REQUIRES_PLAY_CONSOLE |
| Pre-launch report | Submitted AAB | Test-signed AAB ready to upload | `RELEASE_PACKAGE.md` | Upload with operator key | P2 | Internal track | READY_TO_UPLOAD |
| Repo hygiene/CI | Clean + reproducible | git init/main/ignores; wrapper restored; no CI/lint config | `git status` | CI workflow (optional) | P3 | Add later | PARTIAL |
