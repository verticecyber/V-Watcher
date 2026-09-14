# Privacy Data Map — V-Watcher (2026-09-12, code-first)

Flow: `INPUT → COLLECTION → PROCESSING → STORAGE → MODEL → NETWORK → THIRD PARTY → RETENTION → DELETION`.

## Collection (all on-device, all via public Android APIs)

- Battery: `BatteryProvider.kt` (level, temp, health, plug, saver) — sticky `ACTION_BATTERY_CHANGED`, no permission.
- Memory/CPU: `DeviceResourceProvider.kt` (`ActivityManager.MemoryInfo` + JVM heap) — no permission.
- Network **state only**: `NetworkTelemetryProvider.kt` (transport, validated, VPN flag) — `ACCESS_NETWORK_STATE`.
- Installed apps: `AppInventoryProvider.kt` (package names, labels, versions, install times, requested-permission lists) — `QUERY_ALL_PACKAGES`.
- App behavior: `AppUsageProvider.kt` (24h foreground/background events, top-15 aggregates) — `PACKAGE_USAGE_STATS` special grant; denied → explicit `PERMISSION_REQUIRED` degradation.
- System: `SystemStateProvider.kt` (manufacturer, model, Android version, patch, locale — verify file for exact fields before Data Safety filing).

## Processing / model

- `AndroidSentinel.observeNow()` → `CanonicalObservation` with per-provider provenance + freshness (30 s staleness gate).
- Deterministic rules + immune cells in-process; Gemini Nano / Gemma backends are **contract stubs** (no model executes, nothing leaves device for inference).
- No cloud calls: Firebase/Retrofit declared but unimported — zero transmission paths exist in `app/src/main`.

## Storage / retention / deletion

- **No persistence layer**: no Room entities, no SharedPreferences/DataStore, no files written (memory repo = `StateFlow` RAM only, `LocalImmuneMemoryRepository.kt:32-38`; seed patterns are compiled-in constants). Process death = full data loss (also = privacy-positive, stability-negative — see readiness matrix).
- Cache dir: only touched by `RECLAIM_INTERNAL_CACHE` cleanup of own `vwatcher_temp_*` files; nothing sensitive written.
- Backup: `allowBackup=true` with **empty sample rules** — backs up an (almost) empty dataset today; must still define explicit excludes before release (P2).
- Deletion story today: kill process / clear app data (nothing else exists). No accounts → account-deletion rules N/A; document before any account feature.

## Principle check

"Observe amplamente. Retenha minimamente. Compartilhe seletivamente." — **technically demonstrated**: broad observation (6 providers) → RAM-only retention → zero sharing (no endpoints, no SDK exfiltration). What remains is proving the negatives at release: no undisclosed SDK traffic (strip dead deps), prominent disclosures for the two sensitive permissions, public privacy policy.
