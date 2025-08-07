# Entity Limiter Buffer System Update

## Problem Solved

The EntityLimiter was only removing entities down to the exact limit, causing it to constantly trigger when near the limit. Now it removes entities down to a buffer below the limit to prevent constant re-triggering.

## Changes Made

### 1. Buffer Configuration Added 🎯

- **File**: `EntityLimiter.java` - Added `chunkBuffer` and `worldBuffer` fields
- **Config**: `config.yml` - Added buffer settings with sensible defaults
- **Purpose**: Define how many entities below the limit to target when cleaning

### 2. Enhanced Enforcement Logic 🔧

- **World Limits**: Now removes entities down to `(worldLimit - worldBuffer)`
- **Chunk Limits**: Now removes entities down to `(chunkLimit - chunkBuffer)`
- **Logging**: Added informative logging when limits are enforced
- **Prevention**: Stops constant triggering at exact limits

### 3. Configuration Options ⚙️

```yaml
entity_limiter:
  # Buffer settings - how many entities below the limit to target when cleaning
  chunk_buffer: 5 # Remove entities down to (limit - 5) in chunks
  world_buffer: 50 # Remove entities down to (limit - 50) in worlds
```

### 4. Status Reporting 📊

- **Command**: `/hbzlag entities status` now shows buffer information
- **Display**: Shows both world and chunk buffer values
- **Format**: `Buffers: World(-50) Chunk(-5)`

## How It Works

### Before (Problem)

```
Limit: 100 entities
Current: 105 entities
Action: Remove 5 entities → 100 entities
Result: Next spawn triggers limit again immediately
```

### After (Solution)

```
Limit: 100 entities
Buffer: 10 entities
Current: 105 entities
Action: Remove 15 entities → 90 entities (100 - 10)
Result: Room for 10 more entities before next trigger
```

## Example Scenarios

### World Limit Example

- **World Limit**: 2000 entities
- **World Buffer**: 50 entities
- **Current**: 2150 entities
- **Action**: Remove 200 entities (2150 - 1950 = 200)
- **Result**: 1950 entities remaining
- **Benefit**: Next 50 spawns won't trigger the limiter

### Chunk Limit Example

- **Chunk Limit**: 30 entities
- **Chunk Buffer**: 5 entities
- **Current**: 35 entities in chunk
- **Action**: Remove 10 entities (35 - 25 = 10)
- **Result**: 25 entities remaining
- **Benefit**: Next 5 spawns won't trigger the limiter

## Configuration Details

### Default Values

```yaml
chunk_buffer: 5 # Conservative buffer for chunks
world_buffer: 50 # Larger buffer for worlds (more entities)
```

### Customization

- **Small Servers**: Reduce buffers (chunk: 2-3, world: 20-30)
- **Large Servers**: Increase buffers (chunk: 8-10, world: 100-200)
- **No Buffer**: Set to 0 for exact limit behavior (not recommended)

## Benefits

1. **🚫 Prevents Spam**: No more constant limit triggering
2. **⚡ Better Performance**: Less frequent cleanup operations
3. **📊 Predictable**: Clear buffer space before next enforcement
4. **🔧 Configurable**: Adjust buffers based on server needs
5. **🔍 Transparent**: Status command shows buffer settings
6. **📝 Logged**: Enforcement actions are logged with details

## Usage Examples

### Check Current Status

```bash
/hbzlag entities status
# Shows: Buffers: World(-50) Chunk(-5)
```

### Monitor Enforcement

Check server logs for messages like:

```
World world has 2150 entities (limit: 2000), removing 200 to reach target: 1950
```

### Adjust Configuration

Edit `config.yml` and reload:

```yaml
chunk_buffer: 3 # Smaller buffer
world_buffer: 25 # Smaller buffer
```

This update ensures smooth entity management without the constant triggering that was happening when entities reached the exact limit.
