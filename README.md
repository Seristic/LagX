# LagX

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.6+-green.svg)](https://papermc.io/)
[![Paper](https://img.shields.io/badge/Paper-Supported-blue.svg)](https://papermc.io/)
[![Folia](https://img.shields.io/badge/Folia-Compatible-purple.svg)](https://papermc.io/software/folia)

LagX is a comprehensive server optimization plugin designed for modern Minecraft servers running Paper and Folia. It provides advanced lag prevention, entity management, performance monitoring, and server optimization tools with an intuitive admin interface. Use `/lagx` as the primary command.

## 🚀 Features

### Core Performance Optimization

- **Smart Entity Management** - Configurable limits with buffer systems to prevent lag spikes
- **Memory Optimization** - Intelligent garbage collection and memory monitoring
- **Real-time TPS Monitoring** - Health indicators with trend analysis
- **Automated Cleanup Systems** - Scheduled maintenance with customizable protocols
- **Advanced Chunk Management** - Optimal memory usage and unloading strategies

### 🧠 Intelligent Systems

- **Entity Stacker** - Stack identical entities to reduce server load while maintaining functionality
- **Villager Optimizer** - AI optimizations for villager-heavy servers
- **Entity Limiter** - Preset-based entity limiting with overflow management
- **Smart Lag Detection** - Proactive identification of performance bottlenecks
- **Predictive Analysis** - AI-powered performance prediction and automated responses

### 🏠 Towny Integration

- **Smart Protection** - Respects Towny claims while allowing selective cleanup
- **Configurable Filtering** - Protect valuable items while removing junk materials
- **Reflection-based** - Soft dependency that works with or without Towny installed
- **Safe Operations** - No hard dependencies, graceful fallback when Towny unavailable

### ⚡ Modern Architecture

- **Folia Compatibility** - Region-aware scheduling and thread-safe operations
- **Paper Optimized** - Takes advantage of Paper-specific APIs and features
- **Adventure API** - Modern text components with hover tooltips and click events
- **Permission System** - Comprehensive wildcard support with centralized checks

## � Project History

LagX began as a community-driven evolution of the excellent LaggRemover plugin. While it shares the same core optimization philosophy, LagX has been completely rewritten with:

- Modern Folia-native architecture
- Enhanced performance algorithms
- New features like villager optimization and entity stacking
- Comprehensive testing and CI/CD pipeline

**Special thanks to the original LaggRemover authors** for creating such a solid foundation for server optimization!

> *If you're interested in the original project, check out [LaggRemover](https://dev.bukkit.org/projects/lagg-remover) and its [community fork](https://github.com/RIvance/LaggRemoverRemastered).*

## �📦 Installation

### Requirements

- **Java 21** or higher
- **Paper** or **Folia** 1.20.6+ server
- **Optional**: Towny (for enhanced protection features)

### Setup

1. Download the latest `LagX-X.X-folia.jar` from releases
2. Place in your server's `plugins/` directory
3. Start/restart your server
4. Configure `plugins/LagX/config.yml` as needed
5. Use `/lagx reload` to apply configuration changes

## 🎮 Commands & Permissions

### Core Commands

All commands use the `/lagx` prefix with the following permissions:

| Command             | Permission    | Description                             |
| ------------------- | ------------- | --------------------------------------- |
| `/lagx help [page]` | `lagx.help`   | Interactive help system with pagination |
| `/lagx status`      | `lagx.status` | Quick server health overview            |
| `/lagx tps`         | `lagx.tps`    | Current TPS with health indicators      |
| `/lagx ram`         | `lagx.ram`    | Detailed memory usage statistics        |
| `/lagx gc`          | `lagx.gc`     | Manual garbage collection               |
| `/lagx reload`      | `lagx.reload` | Reload plugin configuration             |

### Entity Management

| Command                    | Permission       | Description                                |
| -------------------------- | ---------------- | ------------------------------------------ |
| `/lagx clear [type]`       | `lagx.clear`     | Clear entities/items with Towny protection |
| `/lagx count [type]`       | `lagx.clear`     | Count entities/items in worlds             |
| `/lagx entities [preset]`  | `lagx.entities`  | Entity limiter management                  |
| `/lagx villagers [action]` | `lagx.villagers` | Villager optimizer controls                |

### Entity Stacker

| Command                        | Permission     | Description                         |
| ------------------------------ | -------------- | ----------------------------------- |
| `/lagx stacker info`           | `lagx.stacker` | Stacker statistics and status       |
| `/lagx stacker debug`          | `lagx.stacker` | Debug information for current chunk |
| `/lagx stacker reload`         | `lagx.stacker` | Reload stacker configuration        |
| `/lagx stacker stack [radius]` | `lagx.stacker` | Manual stacking in radius           |

### Permission Wildcards

- `lagx.*` - All permissions
- `lagx.admin` - All admin permissions

## ⚙️ Configuration

### Entity Stacker Setup

Enable entity stacking to reduce server load:

```yaml
stacker:
  enabled: true # Enable the stacker system
  debug: false # Enable debug logging
  max_stack_size:
    items: 64 # Maximum items per stack
    mobs: 8 # Maximum mobs per stack
    spawners: 5 # Maximum spawner stack size
  max_stacks_per_chunk: 4 # Maximum stacks of same type per chunk
  stacking_range: 5.0 # Distance for stacking (blocks)

  # Only listed entities will stack
  stackable_entities:
    - BLAZE # Add entity types to enable stacking
    # - ZOMBIE
    # - SKELETON
```

### Entity Limiter Presets

Choose from three preset modes:

**Basic Mode** - Simple hard limits for all entities:

```yaml
entity_limiter:
  preset_mode: basic
  basic_preset:
    total_entities_per_chunk: 30
    total_entities_per_world: 1500
    overflow_action: remove_oldest
```

**Advanced Mode** - Separate limits by entity type:

```yaml
entity_limiter:
  preset_mode: advanced
  advanced_preset:
    chunk_limits:
      total_per_chunk: 50
      hostile_per_chunk: 15
      passive_per_chunk: 20
      item_per_chunk: 30
```

**Custom Mode** - Full customization available.

### Towny Integration

```yaml
# Automatically detected if Towny is installed
# Protects items in claims while allowing cleanup of junk materials
# No configuration required - works out of the box
```

## 🔧 Entity Stacker Guide

The Entity Stacker reduces server load by combining identical entities:

### How It Works

1. **Automatic Stacking** - Entities of same type within range combine
2. **Smart Limits** - Multiple smaller stacks instead of oversized ones
3. **Chunk Limits** - Maximum stacks per chunk prevents concentration
4. **Visual Feedback** - Stacked entities show "[x8] Blaze" format

### Example with Blazes

With `max_stack_size.mobs: 8` and `max_stacks_per_chunk: 4`:

- **Individual blazes** automatically combine into stacks of 8
- **Multiple stacks** created when more blazes spawn (up to 4 stacks)
- **Maximum per chunk**: 32 blazes (4 stacks × 8 entities)
- **Overflow handling**: New blazes won't stack if chunk is at limit

### Testing Commands

```bash
# Check stacker status
/lagx stacker info

# Debug current chunk
/lagx stacker debug

# Reload configuration
/lagx stacker reload

# Manual stacking
/lagx stacker stack 50
```

## 🐛 Troubleshooting

### Entity Stacker Not Working

1. **Check enabled status**: `/lagx stacker info`
2. **Verify entity whitelist**: Ensure entity type is in `stackable_entities`
3. **Check range**: Entities must be within `stacking_range` (default 5 blocks)
4. **Debug output**: Enable `debug: true` and check console logs
5. **Test reload**: `/lagx stacker reload` after config changes

### Performance Issues

1. **Check TPS**: `/lagx tps` for current performance
2. **Memory usage**: `/lagx ram` for memory statistics
3. **Entity counts**: `/lagx count` to identify problem areas
4. **Adjust limits**: Lower entity limits if needed
5. **Enable cleanups**: Ensure auto-lag-removal is enabled

## 📚 Documentation

Comprehensive documentation lives in `docs/wiki/` and mirrors the GitHub Wiki structure:

- `docs/wiki/Home.md` — overview and quick links
- `docs/wiki/Installation.md` — requirements and setup
- `docs/wiki/Commands.md` — full command list for `/lagx` and `/lagxperf`
- `docs/wiki/Permissions.md` — permission nodes (`lagx.*`, `lagxperf.*`)
- `docs/wiki/Configuration.md` — all config keys explained
- `docs/wiki/Protocols.md` — protocol system and built-ins
- `docs/wiki/Entity Limiter.md` — limiter presets and runtime control
- `docs/wiki/Entity Stacker.md` — stacking system usage
- `docs/wiki/Villager Optimization.md` — villager AI tuning
- `docs/wiki/Towny Integration.md` — Towny-aware cleanup behavior
- `docs/wiki/Performance Command.md` — performance reports and history
- `docs/wiki/Modules.md` — extend LagX with drop-in modules
- `docs/wiki/Troubleshooting.md` and `docs/wiki/FAQ.md`

If you prefer GitHub Wiki, copy these files into the repo's Wiki or set up a sync.

### Configuration Problems

1. **Syntax errors**: Check YAML indentation and formatting
2. **Reload failed**: Use `/lagx reload` and check console for errors
3. **Permissions**: Ensure `lagx.*` or specific permissions are granted
4. **Plugin conflicts**: Check for conflicts with other entity plugins

## 📊 Performance Impact

LagX is designed for minimal performance overhead:

- **Efficient Algorithms** - O(1) entity lookups and smart caching
- **Async Processing** - Heavy operations run on async threads
- **Region Scheduling** - Folia-compatible region-aware operations
- **Memory Conscious** - Minimal memory footprint with automatic cleanup
- **Configurable Impact** - Adjust check intervals and limits as needed

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🔗 Links

- **Issues**: [GitHub Issues](https://github.com/Seristic/LagX/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Seristic/LagX/discussions)
- **Paper**: [PaperMC](https://papermc.io/)
- **Folia**: [Folia Documentation](https://papermc.io/software/folia)

---

### Built with ❤️ for the Minecraft server community

- Entity limiting and buffer systems
- Towny integration and junk material whitelist
- Performance monitoring thresholds
- Automated cleanup schedules
- Permission and security settings

## Compatibility

- **Minecraft:** 1.20.6+
- **Server Software:** Paper, Folia
- **Java:** 17+
- **Optional Dependencies:** Towny (for claim protection)

## Support

For support, feature requests, or bug reports, please visit our GitHub repository or contact the development team.

---

**LagX** - Professional server optimization for modern Minecraft servers.
