# Configuration

File: `plugins/HBZCleaner/config.yml`

Key sections:

## Plugin prefix and general
- `prefix`: Chat prefix for plugin messages (default `&6&lLagX &7&l>>&r `)
- `splashScreen`: Show startup splash
- `auto-update`: Auto-check and install updates

## Stacker
- `stacker.enabled`: Toggle entity/item stacking
- `stacker.debug`: Stack debug logs
- `stacker.max_stack_size.items|mobs|spawners`: Caps per stack type
- `stacker.max_stacks_per_chunk`: Max stacks per chunk per type
- `stacker.max_stacks_near_spawner`: Spawner proximity cap
- `stacker.stacking_range`: Distance to merge stacks
- `stacker.display_format`: Name format, uses `%amount%`
- `stacker.kill_behavior.single_kill`: Single kill or full stack
- `stacker.spawner.enabled|spawn_count|auto_stack`: Spawner behavior
- `stacker.stackable_entities`: Whitelist of stackable entity types
- `stacker.stackable_items`: Whitelist of stackable item materials

## Item frame optimization
- `item_frame_optimization.enabled|debug`: Exclude frames from limits

## Player death protection
- `player_death_protection.enabled` — Protect items after death
- `player_death_protection.radius` — Radius to protect
- `player_death_protection.duration_seconds` — Time protected

## Automatic cleanup
- `auto-lag-removal.run` — Enable periodic cleanup
- `auto-lag-removal.every` — Minutes between runs
- `lag_protocols.low_ram` — Protocols to run on low RAM
- `lag_protocols.low_tps` — Protocols to run on low TPS
- `lag_protocols.periodically` — Protocols to run on schedule

## Protocol warnings
- `protocol_warnings.enabled` — Master toggle
- `protocol_warnings.cc_items.time|stages` — Warning timing/messages
- `protocol_warnings.cc_entities.time|stages` — Warning timing/messages

## Chunk management
- `autoChunk` — Unload chunks in empty worlds
- `noSpawnChunks` — Remove spawn chunks
- `nosaveworlds` — Worlds to skip auto-save

## Lag detection & AI
- `ai.*` — Advanced AI prediction system (experimental)
- `smartlagai` — Smart lag detection toggle
- `smartaicooldown` — Cooldown between AI actions (minutes)
- `TPS` — Trigger TPS for chat-triggered AI
- `RAM` — Trigger RAM remaining for chat-triggered AI (MB)

## Entity limiter
- `thinMobs` — Cancel spawns when a chunk exceeds `thinAt`
- `thinAt` — Max entities per chunk before canceling spawns
- `entity_limiter.enabled` — Toggle limiter
- `entity_limiter.preset_mode` — `basic|advanced|custom`
- `entity_limiter.basic_preset.*` — Total caps and overflow action
- `entity_limiter.advanced_preset.*` — Per-world and per-chunk caps
- `entity_limiter.custom_config.*` — Custom caps
- `entity_limiter.check_interval` — Ticks between enforcement
- `entity_limiter.chunk_buffer|world_buffer` — Target below-limit buffers

## Villager optimization
- `villager_optimization.enabled` — Toggle
- `villager_optimization.ai_tick_reduction` — Lower AI tick frequency
- `villager_optimization.villagers_per_chunk_threshold` — Threshold for optimizations
- `villager_optimization.disable_pathfinding_after_ticks` — Stop pathfinding on idle
- `villager_optimization.reduce_profession_changes` — Reduce job changes
- `villager_optimization.limit_breeding.*` — Population caps and cooldown
- `villager_optimization.optimize_inventory_checks` — Reduce scans
- `villager_optimization.optimize_sleep_behavior` — Sleep AI tweaks

## Player-triggered lag management
- `doRelativeAction` — Localized cleanup for a complaining player
- `doOnlyItemsForRelative` — Items-only localized cleanup
- `dontDoFriendlyMobsForRelative` — Skip peaceful mobs in local cleanup
- `localLagRadius` — Local cleanup radius (blocks)
- `localThinPercent` — Percent to remove
- `localLagRemovalCooldown` — Cooldown between local actions (seconds)
- `localLagTriggered` — Min entities for local cleanup to trigger
- `chatDelay` — Anti-spam chat delay (ticks)

Examples:
- To run periodic item cleanup: set `auto-lag-removal.run: true`, `auto-lag-removal.every: 10`, and keep `lag_protocols.periodically.cc_items` entry.
- To switch to advanced limiter: set `entity_limiter.preset_mode: "advanced"` and tune caps.
