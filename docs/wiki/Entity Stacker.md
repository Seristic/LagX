# Entity Stacker

The Entity and Item Stacker reduces entity counts by merging nearby entities and items into stacks, improving performance.

Config highlights (`stacker.*`):

- `enabled` — Master toggle
- `debug` — Verbose logs
- `max_stack_size.items|mobs|spawners` — Per-stack caps
- `max_stacks_per_chunk` — Caps stacks of same type per chunk
- `max_stacks_near_spawner` — Limit stacks near spawners
- `stacking_range` — Merge distance (blocks)
- `display_format` — Name format using `%amount%`
- `kill_behavior.single_kill` — Only one entity is removed per kill when true
- `spawner.enabled|spawn_count|auto_stack` — Spawner behavior
- `stackable_entities` — Whitelist (empty => no stacking)
- `stackable_items` — Whitelist of items (default empty)

Commands:

- `/lagx stacker info` — Show stacker status and counts
- `/lagx stacker debug` — Debug info blob
- `/lagx stacker reload` — Reload stacker config
- `/lagx stacker stack [radius]` — Force stacking around player (default 50, 1-200)

Permissions:

- `hbzcleaner.stacker`
