# Help System Improvement

## Overview

The HBZCleaner help system has been completely redesigned to provide a cleaner, more informative interface using hover tooltips instead of cluttered text.

## Before vs After

### Before (Cluttered)

```
§c§l[ADMIN ONLY] §e/hbzlag clear(c):§7 Removes various entities/items from worlds.
§c§l[ADMIN ONLY] §e/hbzlag count(ct):§7 Counts various entities/items in worlds.
```

### After (Clean with Hover)

```
/hbzlag clear(c)
/hbzlag count(ct)
```

When players hover over commands, they see:

- **Description**: Detailed explanation of what the command does
- **Permission**: The exact permission node required
- **Access Level**: "ADMIN ONLY" or "All players"

## Technical Implementation

### Modern Adventure API

- Uses `net.kyori.adventure.text.Component` for modern Minecraft text components
- Implements `HoverEvent.showText()` for clean hover tooltips
- Compatible with Paper 1.20.6+ and Folia

### HoverCommand Class

```java
public static class HoverCommand {
    public final String command;
    public final String description;
    public final String permission;
    public final boolean adminOnly;

    public Component toComponent() {
        // Creates hover text with description, permission, and access level
    }
}
```

## Benefits

1. **Cleaner Interface**: No more cluttered "[ADMIN ONLY]" text
2. **More Information**: Hover shows detailed description, permission node, and access level
3. **Better UX**: Players can quickly scan commands and get details on-demand
4. **Future-Proof**: Uses modern Adventure API compatible with latest Minecraft/Paper
5. **Extensible**: Easy to add new command information or modify hover content

## Command Examples

All commands now show with clean formatting:

- `/hbzlag help(h) <num>` - Hover: Lists all available commands | Permission: hbzcleaner.help | ADMIN ONLY
- `/hbzlag status(st)` - Hover: Quick server health overview | Permission: hbzcleaner.status | ADMIN ONLY
- `/hbzlag clear(c)` - Hover: Removes various entities/items from worlds | Permission: hbzcleaner.clear | ADMIN ONLY

## Compatibility

- **Paper 1.20.6+**: Full support with Adventure API
- **Folia**: Compatible with threading model
- **Console**: Falls back to simple text format for console users
- **Legacy**: Maintains all existing functionality while improving presentation
