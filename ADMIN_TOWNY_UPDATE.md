# HBZCleaner Admin-Only + Towny Junk Clearing Update

## Summary of Changes

The HBZCleaner plugin has been updated to implement **admin-only access** with **selective junk clearing** in Towny towns. This ensures that only server administrators can use the plugin, and within towns, only unwanted "junk" materials are cleared while protecting valuable player items and entities.

## Key Changes Made

### 1. Admin-Only Access 🔒

- **File**: `LRCommand.java` - Modified `hasPerm()` method
- **Change**: Added `if (!p.isOp()) return false;` check
- **Result**: Only server operators can use `/hbzlag` commands
- **Console**: Retains full access for automated systems

### 2. Junk Material Whitelist 🗑️

- **File**: `TownyIntegration.java` - Replaced entity protection with material whitelist
- **Added**: `ALLOWED_JUNK_MATERIALS` set containing 40+ common junk materials
- **Logic**:
  - **Wilderness**: Clear everything (normal behavior)
  - **Towns**: Only clear whitelisted junk items + hostile mobs
  - **Protection**: All valuable items, tools, armor, and living entities protected

### 3. Updated Permissions 👑

- **File**: `plugin.yml` - Changed all permission defaults
- **Before**: Mixed `default: true` and `default: op`
- **After**: All permissions now `default: op`
- **Impact**: Reinforces admin-only access at permission level

### 4. Enhanced Help System 📚

- **File**: `Help.java` - Updated all command descriptions
- **Added**: `§c§l[ADMIN ONLY]` prefix to all commands
- **Context**: Makes it clear that commands are restricted
- **Towny Info**: Updated to reflect junk-clearing behavior

### 5. Improved Status Messages 💬

- **File**: `LRCommand.java` - Updated `/hbzlag towny` command output
- **Removed**: Player permission checks (no longer relevant)
- **Added**: Clear explanation of junk-clearing behavior
- **Display**: Shows what's protected vs what can be cleared

## Junk Materials Whitelist

The following materials can be cleared in Towny towns:

### Building Waste

```
COBBLESTONE, DEEPSLATE, DIORITE, ANDESITE, GRANITE
STONE, DIRT, GRAVEL, SAND, RED_SAND
NETHERRACK, BLACKSTONE, BASALT, TUFF, CALCITE
COBBLED_DEEPSLATE, POLISHED_BLACKSTONE, SMOOTH_BASALT
```

### Food/Organic Waste

```
ROTTEN_FLESH, BONE, STRING, SPIDER_EYE, GUNPOWDER
POISONOUS_POTATO, KELP, SEAGRASS, BAMBOO
WHEAT_SEEDS, BEETROOT_SEEDS, MELON_SEEDS, PUMPKIN_SEEDS
```

### Mob Drops

```
LEATHER, FEATHER, EGG, MUTTON, BEEF, PORKCHOP
CHICKEN, RABBIT, COD, SALMON, TROPICAL_FISH, PUFFERFISH
```

## Protection Behavior

### ✅ **Allowed to Clear in Towns**

- Junk items from whitelist above
- Hostile mobs (zombies, skeletons, etc.)
- Items dropped from mining/building common blocks

### 🛡️ **Protected in Towns**

- All living entities (villagers, animals, pets)
- Valuable items (tools, weapons, armor, ores)
- Infrastructure (item frames, paintings, armor stands)
- Any items NOT on the junk whitelist

### 🌍 **Wilderness Behavior**

- No restrictions - all entities/items can be cleared
- Normal HBZCleaner functionality

## Usage Examples

### Admin Check Status

```bash
/hbzlag towny
# Shows: Admin access granted, explains junk-clearing in towns
```

### Area Clearing

```bash
/hbzlag clear area c:5          # Clear 5x5 chunks (junk only in towns)
/hbzlag clear area b:100 hostile # Clear hostile mobs in 100 block radius
```

### Regular Commands (Admin Only)

```bash
/hbzlag status    # Server performance overview
/hbzlag tps       # Current server TPS
/hbzlag entities  # Entity limiter status
```

## Technical Implementation

1. **Permission Check**: `player.isOp()` required for all commands
2. **Towny Detection**: Automatic detection with graceful fallback
3. **Material Check**: Items checked against `ALLOWED_JUNK_MATERIALS` set
4. **Area Integration**: All area clearing respects town boundaries automatically
5. **Entity Limiter**: Automated cleanup also respects Towny protection

## Benefits

- 🔒 **Security**: Prevents accidental clearing by non-admins
- 🏘️ **Player Protection**: Valuable items safe in towns
- 🗑️ **Cleanup Efficiency**: Still removes common junk materials
- ⚔️ **Safety**: Hostile mobs can still be cleared
- 🔧 **Admin Control**: Full functionality for server management
- 📊 **Transparency**: Clear feedback about what's protected

This update strikes the perfect balance between server maintenance needs and player protection, ensuring that admins can clean up junk while preserving everything players care about in their towns.
