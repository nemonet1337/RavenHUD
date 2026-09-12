# REVIEW.md

## What matters in this repository
- Client-only overlay. Do not change server or game rules, damage, or movement.
- Keep cfg color formats (ARGB for lines/alerts, RGB for ores) and the in-game color GUI (default key `P`).
- Stay a drop-in RSHUD customization plugin; do not fork the HUD into an independent client-state system without need.
- Prefer small, explicit fixes over broad refactors.

## Severity calibration
- Critical: crash loop on HUD render, hiding vanilla health or hunger so the player cannot see lethal state.
- Warning: cfg parse breakage, keybind conflicts, overlay mis-scale, untested TypeB vs Raven profile differences.
- Do not flag Gradle wrapper or resource-pack formatting.

## Verification expectations
- Render and cfg changes should be checked in-game for both TypeB and Raven profiles.
- Color/alpha parsing must keep accepting existing `mod_RSHUD_*.cfg` values.
- New keybinds must not swallow vanilla inventory or chat keys.
