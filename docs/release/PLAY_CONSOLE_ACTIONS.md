# Play Console Actions — operator checklist (2026-09-12)

External-only items. Nothing here can be completed from this directory.

| Action | Why | Input required | Status | Owner | Dependency |
|---|---|---|---|---|---|
| Determine account type (personal post-2023-11-13 vs older/org) | Decides whether 12 testers × 14 days gate applies (Play help 14151465) | Console account age/type | OPEN | Human | None |
| Run closed test (12 opted-in × 14 continuous days) if gated | Production-access prerequisite | Tester cohort + 14 days | OPEN | Human | Signed AAB from this repo |
| File `QUERY_ALL_PACKAGES` Permissions Declaration Form | High-risk permission (Play help 10158779) | Core-function justification (antivirus-like local inspection) + listing copy — see `docs/release/PERMISSIONS_AUDIT.md` | OPEN | Human | Release AAB + store description |
| Prominent disclosure + consent for usage-stats/inventory | User Data policy for sensitive data | In-app disclosure screens (partially implemented: `AppUsageProvider` strings) | PARTIAL (code) / OPEN (Console proof) | Tech + human | Release build |
| Publish privacy policy URL + link in-app + Console | P0/P1 store requirement | `docs/release/PRIVACY_POLICY_DRAFT.md` → legal review → public HTTPS URL | OPEN | Human legal | Legal review |
| File Data Safety form | Mandatory declaration incl. third-party SDKs | `docs/release/V_WATCHER_DATA_INVENTORY.md` post-strip verification | OPEN | Human | Final AAB (deps stripped) |
| Content rating questionnaire | Unrated apps not permitted | 10 min questionnaire | OPEN | Human | Release build |
| Target audience + children/ads answers | Mandatory | Audience decision (general, not child-directed, no ads) | OPEN | Human | None |
| Store listing (descriptions, graphics, screenshots, contact, category) | Mandatory; copy must mirror real behavior (foreground-only, in-app flags, `[SIMULATION]` labels) | Human copy + device screenshots | OPEN | Human | Release build on device |
| Enroll Play App Signing + upload key | Signs what users install | Key ceremony outside repo | OPEN | Human | None |
| Review pre-launch report | Auto-generated on AAB upload (stability/compat/perf/a11y) | AAB upload to internal track | OPEN | Human + tech | Signed AAB |
| Production access application | Unlocks production track | Closed-test evidence + form answers | OPEN | Human | Closed test |

All code/config/documentation inputs for the rows above are ready in this repo; remaining work is accounts, filings, and calendar time.
