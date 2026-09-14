# Privacy Policy — TECHNICAL DRAFT (not legal advice, not published)

```text
HUMAN_LEGAL_REVIEW_REQUIRED
PUBLIC_URL_REQUIRED  (public HTTPS, non-geofenced, no-auth)
IN_APP_LINK_REQUIRED (link from app, e.g. Diagnostics/About screen)
PLAY_CONSOLE_LINK_REQUIRED
```

Owner placeholders `[HUMAN FILL: controller identity, jurisdiction, contact, effective date]` must be completed by the human operator with legal review before any store submission. Do not publish this file as-is.

## 1. What V-Watcher is

V-Watcher is a foreground-only, offline device-health observer. It reads on-device hardware/app signals, assesses them with on-device deterministic rules, and shows the results to the user. **There is no account, no login, no cloud backend operated by the app.**

## 2. Data collected (all from public Android APIs, all processed on-device)

- Battery signals (level, temperature, health, charging state) — `BatteryManager`.
- Memory signals (free/total RAM, low-memory flag) — `ActivityManager`.
- Network *state* (transport type, validated flag, VPN flag). No traffic content, no browsing history — `ConnectivityManager`.
- Installed-app inventory (package names, versions, install times, requested-permission lists) — `PackageManager` under `QUERY_ALL_PACKAGES`.
- App-behavior signals (foreground/background events, aggregate foreground times, past 24 h) — `UsageStatsManager` under `PACKAGE_USAGE_STATS`, **only if the user grants access in system Settings**; the app works in degraded mode without it.
- Device descriptors shown in diagnostics (manufacturer, model, Android version, patch level).

## 3. What is NOT collected

Location, contacts, media, microphone, camera, SMS/call logs, advertising ID, crash reports (no crash SDK), analytics events. No data from other apps' private storage.

## 4. Processing, storage, retention, deletion

- Processing is local and ephemeral: observations live in RAM for the session; immune-memory patterns live in RAM and are lost on process death. There is no database, no account, no backup of user data (explicit backup excludes ship in the app).
- Nothing is transmitted anywhere: the release build contains no network endpoints and no analytics/crash SDKs (see `docs/release/NETWORK_ENDPOINT_INVENTORY.md`, `docs/release/THIRD_PARTY_SDK_AUDIT.md`).
- Deletion: uninstalling the app or clearing app data removes everything, because nothing leaves the device. In-app, immune-memory entries can be cleared from the Memory screen [HUMAN FILL: confirm screen/action exists at release].
- On-device AI (when present): Gemini Nano via system AICore and/or local Gemma weights run fully on-device; inputs never leave the phone for inference. Cloud AI is not used. [HUMAN FILL: re-confirm per release; any future cloud feature needs a policy amendment first.]

## 5. Permissions and why

- `ACCESS_NETWORK_STATE`: read connection state for health display. No traffic interception.
- `QUERY_ALL_PACKAGES`: enumerate installed apps to inspect their declared permissions locally (antivirus-like core function). Inventory never leaves the device and is never sold or shared for ads/analytics.
- `PACKAGE_USAGE_STATS` (special access, user-granted in Settings): understand app activity to establish behavioral baselines. Optional; denial only reduces one health signal.

## 6. Third parties and sharing

No data is shared with third parties. No ads, no analytics, no attribution, no crash-reporting SDKs ship in the release build.

## 7. Security

Sandboxed app, no exported components except the launcher activity, no secrets bundled, no persistent store to breach. Release builds are signed and distributed via Google Play App Signing.

## 8. Children

The app is not directed at children. [HUMAN FILL: target audience declaration + age band in Play Console.]

## 9. Changes and contact

[HUMAN FILL: change-notification process, support e-mail/URL, controller name and address, jurisdiction, effective date.]
