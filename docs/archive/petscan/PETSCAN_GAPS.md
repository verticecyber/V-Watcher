# PETSCAN — Gap Register (2026-09-12)

| Gap | Area | Evidence | Severity | Dependency | Next step |
|---|---|---|---|---|---|
| G1 ViewModel untested (scoring/sim/quarantine/exam) | Testing | probe 4 §1–3; 0 tests touch VM | P0 | none | Integration tests pinning score/condition/sim-gating |
| G2 Homeostasis display-only | Architecture | zero consumers; guardrails re-derive | P0 | G1 | Consume macroState in guardrails OR downgrade to diagnostics panel |
| G3 Dual pipelines (legacy cells vs Biomimetic system) | Architecture | VM:83-152 vs :348 | P0 | G1 | Fuse into one path; DecisionCell vs Dendritic single reasoning entry |
| G4 NK unwired (`internalCorruptions`≡0) | Integration | zero callers | P1 | G1 | Wire integrity checks or remove cell + corruption branch |
| G5 Resolution stall on zero-reclaim | Logic | `:227` cumulative flag; typical 0/0 | P1 | none | Per-incident proof or time-bounded recovery |
| G6 Bus subscriber-less; storm flag-only | Architecture | no-op collector; 10 dead payloads | P1 | G3 | Subscribe coordination to bus OR shrink contract to used events |
| G7 `forceDeterministic`/cooldown ignored | Integration | Regulatory outputs dropped | P1 | G2 | Plumb into router/guardrails |
| G8 Threshold sprawl (88/92, 200/250, 15s/2.5s/30s) | Policy | 4 overlapping sets | P1 | G2 | Single threshold table + rationale doc |
| G9 RECOVERING unreachable; dead enums/fns | Debt | homeostasis `:210`; `NONE/DENIED` dead; 6 dead fns | P2 | none | Delete or activate with tests |
| G10 Memory unbounded, no expiry | Hardening | no TTL/cap; lists grow | P1 | none | Cap + TTL + delete path |
| G11 Dendritic throw aborts tail | Robustness | no try/catch `:181` | P1 | none | Scope try/catch per stage; always snapshot |
| G12 `NAVIGATE_APP_SETTINGS` EXECUTED-without-launch | Honesty | no `startActivity` | P1 | none | Launch it or relabel `PREPARED` |
| G13 15 UI overclaims | Honesty | probe 4 §2 | P1 | none | Bind-or-reword each (checklist in next-steps) |
| G14 `isReal=true` on degraded/denied paths | Honesty | Usage-denied, Resource-fallback | P2 | none | Split `isReal` vs `isAvailable` semantics |
| G15 Stale Room/SQLite claims in old reports | Docs | 5 files contradict code | P2 | none | Annotate/supersede stale reports |
| G16 No collectors on channel flows | Observability | zero `.collect` | P2 | G6 | Collect to ring-buffer log or remove flows |
| G17 Engine `:109` stale-backend mirror | Bug | uses old `it.selectedBackend` | P2 | none | One-line fix + test |
| G18 Perf/battery unmeasured; L3–L5 absent | Validation | no device | P1 ext | device | Device lab + pre-launch |
| G19 Nano/Gemma execution | Capability | stubs | P1 ext | device + weights | ML Kit wiring / LiteRT-LM (skill docs ready) |
| G20 `throttle`/`isolate` flags unenforced | Semantics | bool/set only | P2 | G2 | Define enforcement or rename to `flagged` |
