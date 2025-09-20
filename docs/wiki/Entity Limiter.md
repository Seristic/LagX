# Entity Limiter

The Entity Limiter enforces per-chunk and per-world entity caps and can automatically trim entities when limits are exceeded.

Quick start:

- Preset mode: `entity_limiter.preset_mode: "basic" | "advanced" | "custom"`
- Control at runtime:
  - `/lagx preset info` — Show current preset and caps
  - `/lagx preset set <basic|advanced|custom>` — Switch preset
  - `/lagx limiter [status|info|reload|enable|disable]`

Presets:

- Basic
  - `total_entities_per_chunk`: 30
  - `total_entities_per_world`: 1500
  - `overflow_action`: `remove_oldest | remove_random | prevent_spawn`
- Advanced
  - `world_limits.default`: 2000
  - `chunk_limits.total_per_chunk`: 50
  - `chunk_limits.hostile_per_chunk`: 15
  - `chunk_limits.passive_per_chunk`: 20
  - `chunk_limits.item_per_chunk`: 30
  - `overflow_action`: `prevent_spawn`
- Custom: fully override via `entity_limiter.custom_config.*`

Other settings:

- `check_interval` — Ticks between enforcement
- `chunk_buffer` — Clean down to (limit - buffer)
- `world_buffer` — Clean down to (limit - buffer)

Commands:

- `/lagx entities info` — Show limiter configuration summary
- `/lagx entities count` — Global count by entity type
- `/lagx entities stats` — Per-world counts and averages

Permissions:

- `lagx.entities`
- `lagx.preset` (for preset switching)
