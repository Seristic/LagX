# Performance Command

LagX includes a companion command for server performance insights. Use `/lagxperf` (aliases: `/hbzperf`, `/hbzperformance`, `/hbztps`).

Subcommands:

- `regions` — Top regions by utilization; click-to-teleport for players
- `memory` — Heap/non-heap breakdown and GC info
- `threads` — Thread counts with Folia roles (async/region/netty/worker)
- `world <name>` — World overview and top entities
- `history` — TPS history with color trend bar
- `full` — Show all sections in sequence
- `help` — Show help

Permissions:

- See [[Permissions#performance-command]]. Any `hbzperf.*` or `hbzcleaner.performance` grants access.
