# Commands

Main command aliases: `/lagx`, `/hbzcleaner`, `/hbz`, `/hcleaner`

Performance command aliases: `/lagxperf`, `/hbzperf`, `/hbzperformance`, `/hbztps`

Tip: Use `/lagx help` for interactive, paginated help in-game.

Core commands:

- `/lagx help <page>` — Show help pages.
- `/lagx tps` — Show current TPS.
- `/lagx ram` — Show JVM memory usage.
- `/lagx gc` — Run GC and show reclaimed memory.
- `/lagx status` — Quick status (players, chunks, entities, TPS).
- `/lagx master` — Full status (server-wide overview).
- `/lagx world <world>` — World stats (chunks, entities).
- `/lagx unload <world>` — Request unload for all chunks in a world.
- `/lagx ping [player]` — Show ping for a player (or self).
- `/lagx protocol [list|run <id> [count:true|false]]` — List or run cleanup protocols.
- `/lagx clear <items|entities|type|area> ...` — Clear items/entities, by type, or area.
- `/lagx count <items|entities|type|area> ...` — Count instead of clear.
- `/lagx entities [info|count|stats]` — Entity limiter overview and stats.
- `/lagx preset [info|set <basic|advanced|custom>]` — Switch limiter preset.
- `/lagx limiter [status|info|reload|enable|disable]` — Control entity limiter.
- `/lagx villagers [status|reload|enable|disable|optimize|stats]` — Villager optimizer.
- `/lagx stacker [info|debug|reload|stack <radius>]` — Entity stacker tools.
- `/lagx towny [status|info]` — Towny integration information.
- `/lagx warnings [status|on|off|toggle]` — Protocol warning broadcasts.
- `/lagx reload` — Reload plugin configuration.

Performance command:

- `/lagxperf` — Summary performance view.
- `/lagxperf regions` — Top regions by utilisation.
- `/lagxperf memory` — Heap and non-heap memory details.
- `/lagxperf threads` — Thread inventory (Folia-oriented grouping).
- `/lagxperf world <name>` — World-specific metrics and top entities.
- `/lagxperf history` — TPS history with trend graph.
- `/lagxperf full` — Run all performance views at once.

Examples:

- `/lagx clear items` — Remove all ground items across all worlds.
- `/lagx count entities hostile` — Count hostile entities across all worlds.
- `/lagx clear entities peaceful world_nether` — Clear peaceful mobs in `world_nether`.
- `/lagx clear type ZOMBIE,SKELETON` — Clear specific entity types.
- `/lagx clear area b:50 hostile` — Clear hostile mobs within 50 blocks around you.
- `/lagx protocol run cc_items false` — Execute the items cleanup (clearing mode).
