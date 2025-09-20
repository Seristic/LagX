# LagX Development Journal

## Future Features & TODOs

### Advanced AI System (Planned)

**Status**: Not implemented yet - config removed in v0.6.1 cleanup  
**Priority**: Medium  
**Target Version**: TBD

#### Planned Features:

- **Predictive Lag Detection**: Advanced AI that predicts lag before it happens
- **Machine Learning TPS Prediction**: Use historical data to predict server performance
- **Automated Response System**: Smart actions based on prediction confidence
- **Metric Collection**: Comprehensive server metric gathering and analysis

#### Removed Config (for reference):

```yaml
# Advanced AI Configuration (planned feature)
ai:
  enabled: true
  collection_interval: 30
  max_history_size: 1000
  tps_warning_threshold: 15.0
  tps_critical_threshold: 10.0
  auto_actions: true
  action_confidence_threshold: 0.7
  prediction_window: 5
```

#### Implementation Notes:

- Would require metric collection system
- Need to build TPS history tracking
- Requires prediction algorithm (possibly ML-based)
- Should integrate with existing protocol system
- Consider memory usage for history storage

#### Current AI Features (Implemented):

- ✅ **Basic Smart Lag AI**: `smartlagai` - responds to player "lag" chat messages
- ✅ **Player-Triggered Lag Management**: Local entity removal around lagging players
- ✅ **Chat-based Detection**: TPS and RAM thresholds for triggering cleanup
- ✅ **Cooldown System**: Prevents AI spam with `smartaicooldown`

---

## Package Migration (In Progress)

**Status**: Partially complete  
**Current**: Using transitional `LagX` class extending `HBZCleaner`  
**Target**: Full package rename from `com.seristic.hbzcleaner` to `com.seristic.lagx`

### Remaining Tasks:

- [ ] Move all Java files to new package structure
- [ ] Update all import statements
- [ ] Update module API references
- [ ] Test full build and functionality
- [ ] Update any hardcoded package references in configs/docs

---

## Repository Migration

**Status**: Pending  
**Current**: Repository name `HBZCleaner`  
**Target**: Repository name `LagX`

### Steps:

1. Rename repository on GitHub: `HBZCleaner` → `LagX`
2. Update local remotes: `git remote set-url origin https://github.com/Seristic/LagX.git`
3. Update documentation links
4. Notify users of the repository name change

---

---

## Config Cleanup Status (v0.6.1)

**Date**: 2025-09-20  
**Status**: Complete

### What Was Removed:

- `smartlagai` - Config loaded but usage unclear → Backed up to CONFIG_BACKUP.yml
- `smartaicooldown` - Limited implementation → Backed up to CONFIG_BACKUP.yml

### What Was Kept (Fully Working):

- ✅ **TPS/RAM Thresholds** (`TPS: 16.00`, `RAM: 100`) - Used for automatic protocol triggers
- ✅ **All Player-Triggered Lag Management** - Complete implementation in Events.java
- ✅ **Chat delay system** - Works with `lagx.nochatdelay` permission

### Implementation Verification:

All player-triggered lag management features are **fully implemented**:

- `doRelativeAction`, `localLagRadius`, `localThinPercent`, `localLagRemovalCooldown`
- `localLagTriggered`, `doOnlyItemsForRelative`, `dontDoFriendlyMobsForRelative`
- `chatDelay` with permission-based exemptions

The TPS/RAM thresholds integrate with the `lag_protocols` system for automatic cleanup.

## Version History Notes

- **v0.6.1**: Complete permission migration (`hbzcleaner.*` → `lagx.*`)
- **v0.6.1**: Config cleanup - removed unimplemented AI features
- **v0.6.1**: Full documentation wiki publishing
- **v0.6.1**: Complete user-facing rebrand to LagX
