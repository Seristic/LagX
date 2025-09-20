# Villager Optimization

LagX can optimize villager AI to reduce server load in high-density areas.

Config (`villager_optimization.*`):

- `enabled` — Master toggle
- `ai_tick_reduction` — 1=normal, 2=half, 4=quarter speed
- `villagers_per_chunk_threshold` — Start optimizing above this count
- `disable_pathfinding_after_ticks` — Turn off pathfinding if idle too long
- `reduce_profession_changes` — Reduce job switching
- `limit_breeding.enabled|max_villagers_per_chunk|breeding_cooldown_ticks`
- `optimize_inventory_checks` — Smarter inventory checks
- `optimize_sleep_behavior` — Reduce unnecessary sleep AI

Commands:

- `/lagx villagers status` — Show configuration and state
- `/lagx villagers reload` — Reload its configuration
- `/lagx villagers enable|disable` — Toggle optimizer
- `/lagx villagers optimize [world]` — Manually optimize all villagers in a world
- `/lagx villagers stats` — World-by-world counts and max per chunk

Permissions:

- `hbzcleaner.villagers`
