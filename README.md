# HBZCleaner

HBZCleaner is a comprehensive server optimization plugin designed for modern Minecraft servers running Paper and Folia. It provides advanced lag prevention, entity management, and performance monitoring with an intuitive admin interface.

## Features

### 🚀 Performance Optimization
HBZCleaner reduces server lag through intelligent resource management, entity limiting, and automated cleanup systems. Monitor your server's health with real-time TPS tracking and memory usage statistics.

### 🏠 Towny Integration
Smart protection system that respects Towny claims while allowing selective cleanup of junk materials. Protects valuable player items in towns while removing common waste materials.

### 🎨 Modern User Interface
Interactive help system with hover tooltips, clickable pagination, and rich Adventure API components. Clean, professional interface that makes server administration intuitive.

### ⚡ Folia Compatibility
Built with modern threading in mind, HBZCleaner works seamlessly on both Paper and Folia servers with region-aware scheduling and thread-safe operations.

### 🛡️ Admin Security
Comprehensive permission system with admin-only access controls. Prevents accidental usage by non-operators while maintaining full functionality for server management.

### 🔄 Live Configuration
Reload plugin settings without server restart using the built-in config reload system. Make changes on-the-fly without disrupting player experience.

## Commands

All commands use the `/hbzlag` prefix and support shortened aliases shown in parentheses:

**Core Commands:**
- `/hbzlag help(h) [page]` - Interactive help system with hover tooltips
- `/hbzlag status(st)` - Quick server health overview
- `/hbzlag tps` - Current server TPS with health indicators
- `/hbzlag ram` - Detailed memory usage statistics
- `/hbzlag gc` - Run garbage collection to free memory

**Management Commands:**
- `/hbzlag clear(c) [type]` - Clear entities/items with Towny protection
- `/hbzlag count(ct) [type]` - Count entities/items in worlds
- `/hbzlag entities [options]` - Entity limiter management with presets
- `/hbzlag villagers [options]` - Villager AI optimization controls
- `/hbzlag preset [basic|advanced|custom]` - Switch entity limiter presets

**Admin Tools:**
- `/hbzlag master(m)` - Comprehensive server performance overview
- `/hbzlag world(w) <world>` - Detailed world statistics
- `/hbzlag unload(u) <world>` - Unload chunks in specified world
- `/hbzlag protocol(pr) <options>` - Advanced protocol management
- `/hbzlag towny(town)` - Towny integration status and protection info
- `/hbzlag reload(rl)` - Reload configuration without restart

## Permissions

All permissions require operator status (`default: op`):

- `hbzlag.help` - Access to help commands
- `hbzlag.status` - Server status and performance commands  
- `hbzlag.master` - Comprehensive performance overview
- `hbzlag.tps` - TPS monitoring and health checks
- `hbzlag.clear` - Entity and item clearing operations
- `hbzlag.entities` - Entity limiter management
- `hbzlag.villagers` - Villager optimization controls
- `hbzlag.towny` - Towny integration features
- `hbzlag.reload` - Configuration reload capability

## Installation

1. Download HBZCleaner.jar
2. Place in your server's `plugins/` folder
3. Restart your server
4. Configure settings in `plugins/HBZCleaner/config.yml`
5. Use `/hbzlag help` to get started

## Configuration

HBZCleaner creates a comprehensive configuration file with settings for:
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

**HBZCleaner** - Professional server optimization for modern Minecraft servers.
