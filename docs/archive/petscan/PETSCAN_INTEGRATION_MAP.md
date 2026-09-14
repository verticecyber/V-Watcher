# PETSCAN — Integration Map (2026-09-12)

## E2E trace (L2 Robolectric, `petscan/PetScanTraceTest.kt` — kept as harness)

```text
PERTURBATION (observeNow) → TELEMETRY (6 providers, 181 ms) → SENTINEL (obs_…_411807d9)
→ PRR (evidence) → BUS (dispatch ×2, rate 2.0, storm=false, dropped=0)
→ COORDINATION (T-helper gate) → REASONING (0 calls — gate correctly idle)
→ HOMEOSTASIS (DEGRADED/60 — fail-closed fired on shadowed provider)
→ UI state (not exercised in harness; VM untested)
Storm probe: 40 msgs / 99 ms → rate 40.0, storm=true, dropped=0 (DETECTS, no containment)
Router probe: preferred=null → selected=GEMINI_NANO → actual=DETERMINISTIC,
  FALLBACK/GEMINI_NANO_UNAVAILABLE, 9 ms wall; deterministic socket rule → SUSPICIOUS/88 live
```

## Transition gaps

| A → B | Verdict | Evidence |
|---|---|---|
| Telemetry → Sentinel | PROVEN_CONNECTED | `observeNow` sequential + try/catch per provider; trace 181 ms |
| Sentinel → PRR | PROVEN_CONNECTED | `:103` unconditional |
| PRR → Bus | PROVEN_CONNECTED (conditional) | `:106` only if evidence non-empty |
| Bus → cells | BROKEN (no functional subscriber) | sole collector no-op `:73`; cells get evidence via direct args, not bus |
| Bus → Regulatory/Homeostasis | INDIRECTLY_CONNECTED | via `stats.value` polling, not events |
| T-Helper → Dendritic → Router | PROVEN_CONNECTED (gated) | `:172,181`; trace reasoningCalls=0 when idle |
| Router → Deterministic | PROVEN_CONNECTED | trace 9 ms + honesty asserts |
| Router → Nano/Gemma | NOT_AVAILABLE_IN_ENVIRONMENT | states never READY; execute paths canned |
| Cells → Boundary | PROVEN_CONNECTED | 4 wired cells; NK unwired |
| Boundary → OS effect | PARTIAL | 1 real (own cache), flags, 1 intent-not-sent, honest refusals |
| Action → New telemetry | STATIC_ONLY | next `observeNow` may reflect; no closed-loop proof |
| Resolution → Memory | PROVEN_CONNECTED (conditional) | `:251-262`; stalls when reclaim=0 |
| Memory → B-Cell | PROVEN_CONNECTED (weak) | live hit rate ~0 |
| Homeostasis → any decision | BROKEN (display-only) | zero consumers beyond UI mapping |
| VM → everything | PROVEN_CONNECTED, UNTESTED | `:116-403` runs it all; no test pins behavior |
| Sim/Exam → organism | BROKEN BY DESIGN? | sim bypasses system + repo gates; exam hardcodes Healthy |

## Orphans / dead paths

`DeviceTelemetryProvider.captureSnapshot`, `start/stopContinuousObserving`, NK full, `attemptContainment`, `consolidateIncidents`, `RESET_SUBSYSTEM_ISOLATION` (nothing un-throttles), `Regulatory.reset`, channel flows collectors, 10/12 bus payloads, `RESOURCE_GUARD_DENIED`/`NONE`, `COLLECTION_FAILED` event, `RECOVERING` branch, `minHealthyBatteryPercent`, learned package set, `whyExplainingAppId`/`activeTab`(VM copy), `decoys`/`examCategories`/`healthHistory` updates, `captureStart`, `observationSummary` (built, dropped).

## Lifecycle / failure

- Rotation: VM + flows survive; all `remember` UI state resets (no `rememberSaveable`); VM `activeTab` dead duplicate.
- Process death: TOTAL loss (no SavedState/persist); reseed + re-observe on launch. No leak (no static holders beyond app-scoped).
- Failure: provider throw → synthetic zeros, still emits (FAIL_CLOSED via DEGRADED only for battery-UNAV/ERR, resources-ERR, network-ERR — other ERRORs flow as data); dendritic throw → tail abort + stale snapshot (FAIL_OPEN-ish gap); guard denies → deterministic (FAIL_CLOSED good); cytotoxic denies → safe; NK path → N/A (dead).
- Security surface: 1 exported activity; no IPC/providers/deeplinks/WebView/native/network; `QUERY_ALL_PACKAGES` + usage-stats are the exposure (local-only); backup excludes explicit; no logs/secrets. Malicious third party gets nothing (no interfaces). Self-threat: unbounded RAM lists (memory repo, bus replay is bounded).
