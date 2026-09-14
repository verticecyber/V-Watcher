# PETSCAN — Threshold Matrix (2026-09-12, adversarially verified)

No value below is endorsed as correct; this is the full inventory (§15/§28).

| Value | Location | Purpose | Consumer | Test | Duplicate / conflict |
|---|---|---|---|---|---|
| temp >42.0°C | PRR:35, Baseline:31, Deterministic:53, VM score:167 | thermal danger | evidence/candidates/score | Immune:61 (46.5°C) | consistent 42; Resolution uses ≤40.0 (`:60-72`) — conflict |
| temp ==0.0f | Dendritic:45 | missing-sensor flag | missingInfo | none | only site that treats 0 as missing |
| mem low / used>88% | Baseline:48, Deterministic:54 | memory pressure (assess) | candidates/assessment | Immune:61 | vs deny-level 92 below |
| mem low / used>92% / avail<250MB | Guardrails:42, Homeostasis:128 | deny inference / STRESSED | router/homeostasis | Handoff:82 (batt only) | 88 vs 92; 200 (PRR:49, Regulatory:85) vs 250 (Homeo/Macro) |
| heap >200MB (V-Watcher) | Homeostasis:129 | self-destabilizing | STRESSED | none | only site |
| battery <15% + discharging | Guardrails:33, Homeostasis:127 | deny / STRESSED | router/homeostasis | Handoff:93-104 (10%) | consistent; Baseline `minHealthyBattery 10%` dead (never read) |
| powersave + level>50% | Baseline:63, Deterministic:56 | divergence rule | candidates | none | consistent pair |
| msg rate >25/s (self) / storm >30/s | Homeostasis:129 / Bus:60 | destabilize / storm flag | STRESSED / veto | Adversarial:123 (35 msgs) | 25 vs 30 mismatch |
| conf gates 85/80/80/80 | T-helper:67, Cytotoxic:58, Memory:132, Dendritic:184 | escalate/execute/commit | pipeline | T-helper:210, Cytotoxic adversarial | consistent 80 floor; heuristic, uncalibrated |
| memory commit <80 reject / 30 s debounce | Memory:132,149 | write gates | repo | none | untested (G1) |
| cooldowns 15 s reg / 2.5–3 s guard / 30 s stale / 5 s timeout | Regulatory:36, Guardrails:24, Sentinel:38, Router:43 | rate limits | veto/router/freshness | Regulatory:248 | 4 clocks, no shared table |
| score arithmetic 97−15−12−1−6n−25−5, clamp 55–100 | VM:165-172 | health score | UI | none (G1) | untested |
| net `contains(socket/packet/burst)` | Deterministic:55 | network anomaly | assessment | trace live (SUSPICIOUS/88) | caller-string, not packets |
| `takeLast(20)/take(15)`, 24 h window | AppUsage:62,101-116 | event caps | UI lists | none | hardcoded |
| storm window 1 s, replay 20, buffer 128, extra 64 | Bus:34-40,53-57 | transport | stats | storm probe | DROP_OLDEST ⇒ drops ~0 structurally |
