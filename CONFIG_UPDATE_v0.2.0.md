# Configuration Update v0.2.0

## Changes Made

### Version Update
- Updated config version from **0.1.7** to **0.2.0**

### Cleaned Up File Structure
- Removed misplaced `src/main/java/config.yml` (config files should only be in `resources/`)
- Kept proper config location: `src/main/resources/config.yml`

### Optional Features Now Disabled by Default

#### 1. Player "Lag" Trigger (doRelativeAction)
**Changed:** `doRelativeAction: true` → `doRelativeAction: false`

**Reason:** This feature allows players to trigger lag cleanup by typing "lag" in chat. While useful, it can be abused by players to:
- Spam the system
- Remove entities they don't like
- Create performance issues if misused

**Recommendation:** Server administrators should manually enable this if they trust their player base.

#### 2. Map Protection
**Added new section with auto-scan disabled by default:**

```yaml
map-protection:
  auto-scan:
    enabled: false  # Disabled by default
    interval: 5
  require-permission: false  # Disabled by default
```

**Reason:** Map protection can interfere with legitimate map usage and item frame placements. It should be opt-in.

**Use Cases for Enabling:**
- Preventing unauthorized map art
- Protecting against map-based exploits
- Servers with strict content policies

### Features That Remain Enabled

These features are core performance optimizations and remain enabled by default:

- ✅ Entity and Item Stacker
- ✅ Auto-lag-removal (scheduled cleanup)
- ✅ Chunk management
- ✅ Entity limiter
- ✅ Villager optimization
- ✅ Item frame optimization
- ✅ Player death protection

## Configuration Philosophy

**Core Performance Features:** ON by default
- These directly improve server performance
- Low risk of interfering with gameplay
- Example: Entity limiting, chunk management, scheduled cleanup

**Optional/Restrictive Features:** OFF by default
- These modify player behavior or permissions
- Can interfere with intended gameplay
- Should be explicitly enabled by server admins
- Example: Map protection, player-triggered lag removal

## Migration Notes

If you were using `doRelativeAction` or expecting map protection, you'll need to explicitly enable these features in your config.yml after updating to v0.2.0.
