# HBZCleaner Towny Integration Features

## Overview

HBZCleaner now includes Towny integration that implements **admin-only** access with **selective junk clearing** in towns. Only server administrators (operators) can use HBZCleaner commands, and within Towny claims, only whitelisted "junk" materials are cleared while protecting valuable entities and items.

## Features Added

### 1. TownyIntegration Utility Class

- **Location**: `com.seristic.hbzcleaner.util.TownyIntegration`
- **Purpose**: Centralized Towny API interaction and protection logic

#### Protected Entity Types in Towns:

- **Always Protected**: Villagers, Iron Golems, Item Frames, Paintings, Armor Stands, Horses, Donkeys, Mules, Llamas, Cats, Wolves, Parrots, Axolotls
- **Protected if Named/Tamed**: Cows, Pigs, Sheep, Chickens, Rabbits, Bees, Goats, Foxes

#### Key Methods:

- `isEntityProtected(Entity)` - Check if entity should be protected from clearing
- `isInTown(Location)` - Check if location is within a town
- `getTownName(Location)` - Get town name at location
- `canPlayerClearInTown(Player, Location)` - Check player permissions
- `getProtectionInfo(Location)` - Get detailed protection information

### 2. Area Clearing Protection

- **Enhanced Commands**: `/hbzlag clear area` and `/hbzlag count area`
- **Protection Logic**: Entities in towns are automatically protected from area clearing
- **Chunk Protection**: Both chunk-based (`c:5`) and block-based (`b:100`) clearing respect Towny claims
- **Permission Checks**: Only town residents and mayors can clear entities in their towns

### 3. Entity Limiter Integration

- **Automatic Protection**: Entity limiter now respects Towny protection
- **Protected Removal**: Protected entities are skipped during automated cleanup
- **Applies to**: World limits, chunk limits, all removal strategies (oldest, random)

### 4. New Towny Command

- **Command**: `/hbzlag towny` or `/hbzlag town`
- **Permission**: `hbzlag.towny` (default: true)
- **Features**:
  - Shows current location's town protection status
  - Displays whether player can clear entities in current location
  - Provides detailed protection information
  - Shows town name and plot ownership if applicable

### 5. Enhanced Help and Tab Completion

- **Help Integration**: Towny command added to help system
- **Tab Completion**: Full tab completion support for towny/town commands
- **Documentation**: Updated help text includes Towny functionality

## Configuration

### Plugin Dependencies

- **Soft Dependency**: Towny is optional - plugin works without it
- **Automatic Detection**: TownyIntegration automatically detects if Towny is available
- **Graceful Fallback**: If Towny is not present, protection checks are skipped

### Build Configuration

- **Gradle**: Added Towny dependency as `compileOnly`
- **Repository**: Added glaremasters-repo for Towny artifacts
- **Version**: Compatible with Towny 0.100.0.0+

## Usage Examples

### Check Towny Protection

```
/hbzlag towny
```

Shows:

- Current town (if in one)
- Whether you can clear entities
- Protection status explanation

### Area Clearing with Protection

```
/hbzlag clear area c:5        # Clear 5x5 chunks (protects town entities)
/hbzlag clear area b:100 hostile # Clear hostile mobs in 100 block radius
```

### Entity Management

- Entity limiter automatically protects valuable town entities
- Area clearing respects claim boundaries
- Villagers and pets are always protected in towns

## Technical Details

### Thread Safety

- **Folia Compatible**: All Towny checks are async-safe
- **Error Handling**: Graceful error handling for Towny API exceptions
- **Performance**: Efficient caching and minimal API calls

### Protection Logic

1. **Wilderness Areas**: No protection (normal clearing behavior)
2. **Town Areas**:
   - Always protect valuable entities (villagers, pets, etc.)
   - Protect named/tamed animals
   - Respect plot ownership
   - Check player permissions

### Integration Points

- `LRCommand.handleAreaClear()` - Area clearing protection
- `EntityLimiter.canRemoveEntity()` - Automated cleanup protection
- `LaggRemover.onEnable()` - TownyIntegration initialization

## Benefits

1. **Claim Protection**: Players' valuable entities are safe from cleanup
2. **Permission Respect**: Only authorized players can clear entities in towns
3. **Automatic Detection**: Works seamlessly with existing Towny setups
4. **Backward Compatible**: Fully functional without Towny installed
5. **Performance Friendly**: Minimal overhead when Towny is not present
