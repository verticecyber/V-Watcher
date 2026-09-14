# PETSCAN — Claim Reconciliation (2026-09-12)

| Claim (source) | Verdict | Evidence |
|---|---|---|
| Real device telemetry (README/UI `REAL DEVICE DATA`) | PARTIALLY_PROVEN | 5 providers real; SystemState static; synthetic fallbacks labeled; banner shows during seed/degraded |
| Immune coordination 12 cells (docs/UI) | PARTIALLY_PROVEN | 11 wired (NK dead), sync pipeline live, bus observability-only |
| Homeostasis engine (UI `HOMEOSTASIS ENGINE`) | CODE_ONLY (display-only) | computed, zero decision consumers |
| Gemini Nano integrated | DOCUMENTATION_ONLY + stub | detection path real; inference canned; never READY here |
| Gemma local | DOCUMENTATION_ONLY + stub | file probe real; no runtime; id pre-Gemma-4 |
| Self-regulation (throttle/containment/barrier) | PARTIALLY_PROVEN | flags + own-cache reclaim real; no OS enforcement; barrier = label |
| Memory/antibodies persist (old JSONs: Room/SQLite) | FALSE_OR_STALE | RAM-only; reseed on launch; old reports contradict code |
| Autonomous action (isolate/contain/decoy) | FALSE_OR_STALE (as OS action) | in-app flags only (fixed copy); decoys Standby/0 always |
| `0 uncontained threats`, `Certified/Verified`, TLS claims, `<40ms`, efficiency numbers, `MEM <35ms`, static Healthy categories/history | FALSE_OR_STALE / UNKNOWN | hardcoded literals or transport-level over-reads; 15 flagged points (`PETSCAN_COMPONENT_MATRIX` + probe 4) |
| Zero exfiltration / local-only | CODE_ONLY (plausible, unproven) | zero sinks in code + artifact; no test pins it |
| `DETERMINISTIC MODE` vs `AICORE READY` badge | PROVEN | backed by `checkAvailability` |
| Fallback disclosure (Selected vs Actual) | PROVEN | DiagnosticScreen + trace |
| NK honesty (refuse external kill) | PROVEN (unit) but UNUSED in production path | adversarial test; zero runtime callers |
| 24/7 watcher / continuous monitoring | FALSE_OR_STALE | no background components; foreground-only |
| Tests 30/30 green | PROVEN, coverage PARTIAL | substrates covered; VM/UI-wiring uncovered |

## Maturity scores (0 absent → 5 production-proven)

```text
FOUNDATION 3 (build/test/lint/gate proven; no CI/tags)
TELEMETRY 4 (real + fail-closed proven L2; scoped-visibility caveat)
COORDINATION 3 (pipeline live; bus subscriber-less; NK dead; dual-pipeline debt)
REASONING 3 (router+deterministic proven; neural stubs)
ACTUATION 2 (own-cache real; rest flags/refusals; 1 intent-not-sent)
HOMEOSTASIS 2 (implemented, not consumed)
MEMORY 2 (gated RAM store; no expiry/persist)
SECURITY 3 (surface minimal, scans clean; no device proof)
PRIVACY 3 (local-only by construction; policy/filings external)
LIFECYCLE 2 (rotation partial; death total; by design but unconsented)
OBSERVABILITY 3 (provenance/freshness/stats real; flows collector-less)
TESTING 2 (30 green; VM + wiring gaps)
RELEASE 4 (AAB validated; Console-side open)
```
