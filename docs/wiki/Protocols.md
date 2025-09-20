# Protocols

LagX provides a protocol system to execute cleanup and maintenance tasks. Protocols can be triggered via commands, low-TPS/low-RAM events, or scheduled runs.

Usage:
- List: `/lagx protocol list`
- Run: `/lagx protocol run <id> [count:true|false]`

Built-in protocols:

## cc_items
- ID: `cc_items`
- Category: CPU, RAM, NETWORK
- Purpose: Remove (or count) ground items across all worlds, a single world, or a chunk.
- Args (via parser keys):
  - `Count` (boolean, index 0): true to count only; false to execute removal
  - `World` (world, index 1): optional world target
  - `Chunk` (chunk, index 1): optional chunk target
- Returns: `{0: <int removedOrCounted>}`
- Death protection: Honors recent-death protected items via `PlayerDeathTracker`.

## cc_entities
- ID: `cc_entities`
- Category: CPU, RAM, NETWORK
- Purpose: Remove (or count) entities, optionally filtered by type set; across all worlds, a world, or a chunk.
- Args:
  - `Count` (boolean, index 0)
  - `ToClear` (EntityType[], index 1): pass `null` for all, or a list of types
  - `World` (world, index 2) | `Chunk` (chunk, index 2) | `AllWorlds` (boolean, index 2)
- Returns: `{0: <int removedOrCounted>}`
- Helpers:
  - `hostile`: predefined hostile EntityType[]
  - `peaceful`: predefined peaceful EntityType[]

## lr_gc
- ID: `lr_gc`
- Category: RAM
- Purpose: Force a garbage collection to free memory.
- Args: none
- Returns: `{0: <long freedMB>}`

## run_c
- ID: `run_c`
- Category: UNKNOWN
- Purpose: Run a console command.
- Args:
  - `Command` (string, index 0)
- Returns: `{}`

Scheduling with config:
- See `lag_protocols.low_ram|low_tps|periodically` in [[Configuration]] to tie protocols to triggers.
- Warning broadcasts are controlled via `protocol_warnings.*`.
