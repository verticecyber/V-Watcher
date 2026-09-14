# Play Store Readiness — V-Watcher (2026-09-12)

> Console-side track. Nothing below can be proven from this directory; each item names its owner + next action.

| Item | State | Owner | Next action |
|---|---|---|---|
| App name (`V-Watcher`, `strings.xml`) | Draft, matches behavior | Human | Confirm + reserve in Console |
| Short/full description | **Missing** | Human + tech (this repo constrains claims: foreground-only, in-app review flags, simulation-labeled demo, local-only data) | Draft from real behavior only; mirror `[SIMULATION]` disclosure |
| Icon / feature graphic / screenshots | Icon XML present (`mipmap-anydpi-v26` + drawables); graphic/screenshots missing | Human | Generate from release build on device |
| Privacy policy URL (public HTTPS, no-auth, linked in-app + Console) | **Missing → P0/P1 blocker** | Human (legal) + tech (§"policy inputs" below) | Publish URL; add in-app link (About/diagnostics screen) |
| Support contact / developer identity | Missing | Human | Console setup |
| Category | Undecided (`Tools` recommended) | Human | Console setup |
| Content rating questionnaire | Not filled (no Console) | Human | Fill; expected Everyone/Everyone-10 (no objectionable content, no ads) — Console decides |
| Target audience / children | Not declared; no child-directed design, no ads | Human | Declare 18+ general / not for children |
| Data Safety form | Pre-fill ready (`V_WATCHER_DATA_INVENTORY.md`) | Human files, tech verifies on APK | File after dep strip; include third-party AI clause if Firebase wired (Jul-2026 policy) |
| Permissions declarations (`QUERY_ALL_PACKAGES` + usage-stats disclosure) | Code justified (`PERMISSIONS_AUDIT.md`); forms not filed | Human files | File Permissions Declaration Form; prominent in-app disclosure already partially present (`AppUsageProvider` strings) — extend to inventory |
| App access instructions | N/A (no login) — reviewer opens app | — | Note in Console |
| Testing: closed test 12 × 14 days | **REQUIRES_PLAY_CONSOLE** — applies iff owner account is personal + created after 2023-11-13 (`support.google.com/googleplay/android-developer/answer/14151465`) | Human | Determine account type → run closed track → apply for production access |
| Pre-launch report | NOT_READY (needs AAB upload) | Human + tech | Upload internal track → triage stability/compat/perf/a11y findings |
| Play App Signing + upload key | Not provisioned (`RELEASE_REQUIRES_OPERATOR_SIGNING`) | Human | Enroll in Console; store upload key outside repo |

## Privacy-policy technical inputs (for the human drafter — do not invent owner data)

Collect: 6 on-device telemetry families (battery/memory/net-state/app-inventory/app-usage/system), all RAM-ephemeral, zero transmission, zero third-party sharing today. Use: on-device health assessment + immune-memory baselines. Sharing: none. Security: no persistent store; sandbox; no secrets in app. Retention/deletion: session-only; uninstall/clear-data erases. Permissions: `QUERY_ALL_PACKAGES` (inventory inspection, local-only) + `PACKAGE_USAGE_STATS` (optional behavioral baseline, Settings-granted). AI: Nano/Gemma not active; deterministic local rules only. Children: not directed. Contact + controller identity + jurisdiction: **[HUMAN FILL]**.
