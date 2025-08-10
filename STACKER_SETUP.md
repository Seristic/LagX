# Entity Stacker Quick Setup Guide

## Quick Start

### To enable blaze stacking:

1. **Edit config.yml**:

```yaml
stacker:
  # Enable the stacker system
  enabled: true # ← Change from false to true

  # Enable debug logging (optional, for troubleshooting)
  debug: false

  # Maximum stack size for different types
  max_stack_size:
    items: 64
    mobs: 8 # ← Blazes will stack up to 8 per stack
    spawners: 5

  # Maximum number of stacks per entity type in a chunk
  max_stacks_per_chunk: 4 # ← Max 4 stacks of blazes per chunk

  # Stacking range - how close entities need to be to stack (in blocks)
  stacking_range: 5.0

  # Whitelist of stackable entity types (any not listed won't stack)
  stackable_entities:
    - BLAZE # ← Add this line to enable blaze stacking
    # - ZOMBIE   # ← Uncomment to enable zombie stacking
    # - SKELETON # ← Uncomment to enable skeleton stacking
```

2. **Apply changes**:
   - **Option A**: Restart the server
   - **Option B**: Run `/hbzlag stacker reload` (requires `hbzlag.stacker` permission)

## Verification

### Check if it's working:

```bash
# Check stacker status
/hbzlag stacker info

# Debug current chunk (shows entities and stacks)
/hbzlag stacker debug

# Manual stacking test
/hbzlag stacker stack 50
```

### Expected results:

- **Server log**: "Entity Stacker enabled - Max stack sizes: Items=64, Mobs=8, Spawners=5"
- **Blazes spawn**: Within 5 blocks of each other should automatically combine
- **Visual feedback**: Stacked blazes show "[x2] Blaze", "[x8] Blaze", etc.
- **Multiple stacks**: When more than 8 blazes, creates multiple stacks (up to 4 per chunk)

## Advanced Configuration

### Common Entity Types

```yaml
stackable_entities:
  # Hostile Mobs
  - BLAZE
  - ZOMBIE
  - SKELETON
  - CREEPER
  - SPIDER
  - WITCH
  - ENDERMAN

  # Passive Mobs
  - COW
  - PIG
  - SHEEP
  - CHICKEN
  - RABBIT

  # Farm Animals
  - VILLAGER
  - IRON_GOLEM
```

### Performance Tuning

```yaml
stacker:
  enabled: true
  debug: false # Enable for troubleshooting

  max_stack_size:
    items: 64 # Higher = fewer item entities
    mobs: 16 # Higher = fewer mob entities
    spawners: 5 # Keep low for spawner performance

  max_stacks_per_chunk: 6 # Higher = more entities per chunk
  stacking_range: 3.0 # Lower = tighter stacking requirements
```

## Troubleshooting

### Stacker not working?

1. **Check enabled**: `/hbzlag stacker info` - should show "Enabled: true"
2. **Check entity type**: Make sure entity is in `stackable_entities` list
3. **Check distance**: Entities must be within `stacking_range` (default 5 blocks)
4. **Check chunk limit**: Max `max_stacks_per_chunk` stacks per entity type
5. **Enable debug**: Set `debug: true` and check console for detailed logs
6. **Test reload**: `/hbzlag stacker reload` after config changes

### Common issues:

- **"Nothing stacks"**: Entity type not in whitelist or stacker disabled
- **"Only some stack"**: Check distance requirements and chunk limits
- **"Reload doesn't work"**: Make sure to use `/hbzlag stacker reload`, not `/hbzlag reload`
- **"Too many entities"**: Lower `max_stack_size` or `max_stacks_per_chunk`

### Debug commands:

```bash
# Detailed stacker information
/hbzlag stacker debug

# Check what's stackable in current chunk
/hbzlag count entities

# Force stacking in area
/hbzlag stacker stack 25
```

## Performance Benefits

### Before Stacking:

- 100 individual blazes = 100 entity updates per tick
- High CPU usage from pathfinding and AI
- Memory usage scales linearly with entity count

### After Stacking:

- 100 blazes = 12-13 stacks (8 entities each) = 13 entity updates per tick
- **87% reduction** in entity processing overhead
- Maintains full functionality (combat, drops, AI)
- Visual feedback shows actual entity count

### Recommended Settings:

**High Performance Server**:

```yaml
max_stack_size:
  mobs: 32
max_stacks_per_chunk: 2
stacking_range: 8.0
```

**Balanced Server**:

```yaml
max_stack_size:
  mobs: 16
max_stacks_per_chunk: 4
stacking_range: 5.0
```

**Vanilla-like Experience**:

```yaml
max_stack_size:
  mobs: 8
max_stacks_per_chunk: 6
stacking_range: 3.0
```

- If stacker still shows as disabled after reload, restart the server
- Only BLAZE entities will stack (all others are filtered out)
- Stacking range is 5 blocks by default
- Max stack size for mobs is 32 by default
