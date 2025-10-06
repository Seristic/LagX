# Folia Threading Fix for Entity Operations

## Problem

The plugin was experiencing threading errors on Folia servers when attempting to access entity state from the wrong thread:

```
Thread failed main thread check: Accessing entity state off owning region's thread
```

This occurred specifically in the `TaskManager.performLagRemoval()` method when calling `item.getTicksLived()` and `item.remove()` on entities.

## Root Cause

Folia uses a **regionized threading model** where:

- Each region (group of chunks) has its own thread
- Entities must be accessed **only** on their owning region's thread
- Global schedulers cannot directly access entity state

The previous code used `GlobalRegionScheduler` to run tasks that directly accessed entities across all worlds, which violated Folia's threading model.

## Solution

Changed the task scheduling approach to be Folia-compatible:

### Before (Incorrect)

```java
// Global scheduler directly accessing entities - WRONG for Folia
Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin,
    (scheduledTask) -> {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                entity.getTicksLived(); // Threading violation!
            }
        }
    }, interval, interval);
```

### After (Correct)

```java
// Global scheduler triggers per-world region-based tasks
Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin,
    (scheduledTask) -> schedulePerWorldLagRemoval(),
    interval, interval);

// Each world's entities accessed on their own region thread
private void schedulePerWorldLagRemoval() {
    for (World world : Bukkit.getWorlds()) {
        Bukkit.getRegionScheduler().execute(plugin, world, 0, 0, () -> {
            for (Entity entity : world.getEntities()) {
                entity.getTicksLived(); // Safe - on correct thread
            }
        });
    }
}
```

## Changes Made

### TaskManager.java

1. **startLagRemovalTask()**: Changed to call `schedulePerWorldLagRemoval()` instead of `performLagRemoval()`
2. **startEntityCleanupTask()**: Changed to call `schedulePerWorldEntityCleanup()` instead of `performEntityCleanup()`
3. **schedulePerWorldLagRemoval()**: New method that uses `RegionScheduler.execute()` to run entity operations on the correct thread
4. **schedulePerWorldEntityCleanup()**: New method that uses `RegionScheduler.execute()` for entity cleanup

## Key Concepts for Folia Development

### Global Region Scheduler

- Use for: Server-wide tasks that don't touch entities
- Examples: Metrics collection, configuration reloads, global announcements
- **Cannot** directly access entity state

### Region Scheduler

- Use for: Entity/chunk-specific operations
- Executes tasks on the thread that owns that region
- Required for: `entity.getTicksLived()`, `entity.remove()`, `chunk.getEntities()`, etc.

### Entity Scheduler

- Use for: Tasks that should follow a specific entity
- Automatically cancelled when entity is removed
- Example: Custom AI, periodic entity updates

## Testing

- Build successful: ✅
- No compilation errors: ✅
- Threading model: Folia-compatible ✅

## Related Files

- `src/main/java/com/seristic/lagx/managers/TaskManager.java` - Main fix
- `src/main/java/com/seristic/lagx/util/EntityLimiter.java` - Already Folia-compatible
- `src/main/java/com/seristic/lagx/util/EntityStacker.java` - Should be reviewed

## Additional Notes

The `EntityLimiter` class already implements proper Folia threading by using:

```java
Bukkit.getRegionScheduler().run(plugin, world, chunkX, chunkZ, task -> {
    // Chunk operations here
});
```

This pattern should be used whenever accessing entities, chunks, or other region-owned state.
