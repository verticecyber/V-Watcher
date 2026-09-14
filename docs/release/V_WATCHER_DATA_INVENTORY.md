# V-Watcher Data Inventory (for Play Data Safety, 2026-09-12)

> Pre-fill for the Play Console Data Safety form. Must be re-verified against the release APK (and stripped deps) before submission. No data below leaves the device today.

| Data type | Source | Collected? | Processed locally? | Stored? | Shared? | Third party? | Optional / required | Retention | Deletion | Encryption | User disclosure | Play Data Safety category |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Battery signals (level/temp/health) | BatteryManager | Yes (ephemeral) | Yes | No (RAM) | No | No | Required (core) | Session only | Process death | N/A (no store/transit) | In-app vitals UI | Device IDs → **not collected**; health signals → declare under *App activity / Device* only if form requires; confirm latest taxonomy at filing |
| Memory signals | ActivityManager | Yes (ephemeral) | Yes | No | No | No | Required | Session | Process death | N/A | In-app vitals UI | Same as above |
| Network state (transport/validated/VPN flag) | ConnectivityManager | Yes (ephemeral) | Yes | No | No | No | Required | Session | Process death | N/A | In-app vitals UI | Same |
| Installed-app inventory (names/versions/permissions) | PackageManager | Yes (ephemeral) | Yes | No | No | No | Required (core antivirus-like) | Session | Process death | N/A | **Prominent disclosure required** (sensitive) + listing description | *Installed apps* — declare; never ads/analytics |
| App usage (24h foreground events) | UsageStatsManager | Yes, only if granted | Yes | No | No | No | **Optional** (degrades gracefully) | Session | Process death | N/A | **Prominent disclosure + Settings consent** | *App activity* — declare as optional |
| Device identifiers (manufacturer/model/OS/patch) | SystemStateProvider | Yes (ephemeral, shown in UI) | Yes | No | No | No | Required | Session | Process death | N/A | In-app diagnostics | *Device or other IDs* — confirm taxonomy; no advertising ID collected |
| Diagnostics/crash/logs | — | **No** (no crash SDK, no `Log.*` in main) | — | — | — | — | — | — | — | — | — | Declare *none* |
| Location/contacts/media/accounts | — | **No** (no permissions, no APIs) | — | — | — | — | — | — | — | — | — | Declare *none* |
| Model inputs/outputs | Local reasoning | Ephemeral | Yes | No | No | No | Optional (AI features) | Session | Process death | N/A | Feature-level disclosure when wired | Reassess when ML Kit/LiteRT deps land |
| Firebase/Retrofit traffic | **Dependencies removed 2026-09-12** (were declared, never imported) | — | — | — | — | — | — | — | — | — | — | Nothing to declare |

Responder owner: human operator (Play Console filing). Technical contact: this file + `PRIVACY_DATA_MAP.md`.
