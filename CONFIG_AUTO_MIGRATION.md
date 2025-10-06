# Config Auto-Migration Fix

## Problem

When uploading LagX-0.7.1.jar to the server, it was still using the old config version 0.1.7 instead of the new 0.2.0 config with all the improvements.

## Root Cause

Bukkit/Folia plugins **do not** automatically replace existing config files. When a config.yml already exists on the server, the plugin keeps using it even if it's outdated.

Additionally, there was a hardcoded version mismatch:

- `LagX.java` had `CONFIG_VERSION = "0.1.7"` (wrong)
- `config.yml` had `version: 0.2.0` (correct)

## Solution

Implemented automatic config migration in `ConfigurationManager`:

### How It Works

1. **On Plugin Startup:**

   - Check if `config.yml` exists
   - Load it and read the `version` field
   - Compare with expected version (`0.2.0`)

2. **If Version Mismatch:**

   - Log warning about outdated config
   - Rename old config to `config.yml.backup-{version}`
   - Generate fresh config with all new settings
   - Load the new config

3. **If Version Matches:**
   - Load existing config normally
   - Add any missing default values

### Changes Made

#### LagX.java

```java
// Before
public static final String CONFIG_VERSION = "0.1.7";

// After
public static final String CONFIG_VERSION = "0.2.0";
```

#### ConfigurationManager.java

```java
public void initialize() {
    // Check if config exists and if it's outdated
    boolean configExists = configFile.exists();

    if (configExists) {
        // Load existing config to check version
        FileConfiguration existingConfig = YamlConfiguration.loadConfiguration(configFile);
        String existingVersion = existingConfig.getString("version", "0.0.0");

        if (!CONFIG_VERSION.equals(existingVersion)) {
            plugin.getLogger().warning("Config version mismatch! Found: " + existingVersion + ", Expected: " + CONFIG_VERSION);
            plugin.getLogger().warning("Backing up old config and generating new one...");

            // Backup old config
            File backup = new File(plugin.getDataFolder(), "config.yml.backup-" + existingVersion);
            if (configFile.renameTo(backup)) {
                plugin.getLogger().info("Old config backed up to: " + backup.getName());
                configExists = false; // Treat as if config doesn't exist
            }
        }
    }

    // Save default config if it doesn't exist (or was backed up)
    if (!configExists) {
        plugin.saveDefaultConfig();
        plugin.getLogger().info("Generated new config file with version " + CONFIG_VERSION);
    }

    // ... rest of initialization
}
```

## What Happens When You Upload The New JAR

### First Time (with old 0.1.7 config on server):

```
[LagX] Config version mismatch! Found: 0.1.7, Expected: 0.2.0
[LagX] Backing up old config and generating new one...
[LagX] Old config backed up to: config.yml.backup-0.1.7
[LagX] Generated new config file with version 0.2.0
[LagX] Configuration loaded successfully
```

Your old settings are preserved in `plugins/LagX/config.yml.backup-0.1.7`

### Subsequent Restarts (with 0.2.0 config):

```
[LagX] Configuration loaded successfully
```

No migration needed - versions match!

## Benefits

✅ **Automatic Migration:** No manual config deletion needed
✅ **Backup Safety:** Old config is saved, not deleted
✅ **Always Up-to-Date:** Servers always get the latest config format
✅ **New Features:** Map protection and other new settings are included
✅ **Default Values:** Optional features properly disabled by default

## Testing

1. Stop your server
2. Replace old `LagX-0.7.0.jar` with new `LagX-0.7.1.jar`
3. Start server
4. Check console for migration messages
5. Verify `plugins/LagX/config.yml` shows `version: 0.2.0`
6. Check for `config.yml.backup-0.1.7` as a safety backup

## Config Version History

| Version | Features                                                                                                                                                   |
| ------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 0.1.7   | Original config from HBZCleaner rebrand                                                                                                                    |
| 0.2.0   | • Added `prevent_upward_stacking`<br>• Added `map-protection` section<br>• Disabled `doRelativeAction` by default<br>• Added anti-corruption feature notes |

## Future Updates

This migration system will handle all future config updates automatically. When we release version 0.3.0, the same process will happen:

- Backup `config.yml.backup-0.2.0`
- Generate fresh config with 0.3.0 features
- No manual intervention needed
