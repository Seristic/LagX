# 🎉 HBZCleaner v1.0.1 - Complete Original Release

**The ultimate server optimization plugin for modern Minecraft servers**

## 🚀 What is HBZCleaner?

HBZCleaner is a comprehensive server optimization plugin designed for Paper and Folia servers. It provides advanced lag prevention, intelligent entity management, and performance monitoring with a modern, secure interface.

## ✨ Key Features

### 🛡️ **Admin Security**
- **Operator-only access** - All commands require OP status for maximum security
- **Comprehensive permissions** - Granular control over every feature
- **Console compatibility** - Full functionality for automated systems

### 🏠 **Smart Towny Integration**
- **Claim protection** - Valuable items and entities safe in towns
- **Selective junk clearing** - Only removes unwanted materials in protected areas
- **40+ junk materials** - Comprehensive whitelist of common waste items
- **Automatic detection** - Seamless integration with existing Towny setups

### 🎨 **Modern Interface**
- **Interactive help system** - Hover tooltips with detailed command information
- **Clickable pagination** - Easy navigation through command lists
- **Adventure API components** - Rich, modern text formatting
- **Real-time feedback** - Clear status indicators and health metrics

### ⚡ **Folia Compatibility**
- **Thread-safe operations** - Built for modern multi-threaded servers
- **Region-aware scheduling** - Optimized for Folia's threading model
- **Cross-region safety** - Prevents threading violations and crashes

### 🔧 **Advanced Features**
- **Entity limiting with buffers** - Smart prevention of lag-causing spawns
- **Live configuration reload** - Update settings without server restart
- **Villager AI optimization** - Improve performance of trading systems
- **Predictive TPS monitoring** - Advanced performance analysis
- **Protocol-based cleanup** - Modular lag prevention system

## 📋 Commands Overview

All commands use the `/hbzlag` prefix and support intuitive aliases:

### Core Performance
- `/hbzlag help(h) [page]` - Interactive help with hover tooltips
- `/hbzlag status(st)` - Quick server health overview
- `/hbzlag tps` - Real-time TPS monitoring
- `/hbzlag ram` - Memory usage statistics
- `/hbzlag gc` - Advanced garbage collection

### Entity Management
- `/hbzlag entities [options]` - Entity limiter with presets (basic/advanced/custom)
- `/hbzlag villagers [options]` - Villager optimization controls
- `/hbzlag clear(c) [type]` - Protected clearing with Towny integration
- `/hbzlag count(ct) [type]` - Entity/item counting across worlds

### Admin Tools
- `/hbzlag master(m)` - Comprehensive performance analysis
- `/hbzlag world(w) <world>` - Detailed world statistics
- `/hbzlag protocol(pr) <options>` - Advanced protocol management
- `/hbzlag towny(town)` - Protection status and junk clearing info
- `/hbzlag reload(rl)` - Live configuration reload

## 🛡️ Towny Protection System

### What's Protected in Towns:
- **Living entities** - Villagers, animals, pets
- **Valuable items** - Tools, weapons, armor, ores
- **Infrastructure** - Item frames, paintings, armor stands
- **Player belongings** - Anything not on the junk whitelist

### What Can Be Cleared in Towns:
- **Building waste** - Cobblestone, dirt, stone variants
- **Food scraps** - Rotten flesh, seeds, basic food items
- **Mob drops** - Common drops like leather, feathers
- **Hostile mobs** - Zombies, skeletons, creepers (always clearable)

### Wilderness Areas:
- **No restrictions** - Full HBZCleaner functionality
- **Complete clearing** - All entities and items can be removed

## 🔐 Security & Permissions

All permissions require operator status (`default: op`):

- `hbzlag.help` - Access to help and information
- `hbzlag.status` - Performance monitoring commands
- `hbzlag.clear` - Entity and item clearing operations
- `hbzlag.entities` - Entity limiter management
- `hbzlag.villagers` - Villager optimization
- `hbzlag.towny` - Towny integration features
- `hbzlag.reload` - Configuration reload capability

## ⚙️ Installation

1. **Download** `HBZCleaner-v1.0.1.jar` from this release
2. **Place** the JAR file in your server's `plugins/` folder
3. **Start** your server to generate configuration files
4. **Configure** settings in `plugins/HBZCleaner/config.yml`
5. **Use** `/hbzlag help` to explore all features

## 📋 Requirements

- **Minecraft:** 1.20.6+
- **Server:** Paper or Folia
- **Java:** 17+
- **Optional:** Towny plugin for claim protection features

## 📊 Performance Benefits

- **Reduced RAM usage** through intelligent cleanup
- **Improved TPS** via entity limiting and optimization
- **Lag prevention** with predictive monitoring
- **Chunk optimization** for memory efficiency
- **Thread safety** for multi-core servers

## 🔄 Configuration Highlights

- **Entity limits** - Configurable per-world and per-chunk
- **Buffer systems** - Prevent constant triggering of limits
- **Junk whitelist** - Customizable protected materials in towns
- **Automation** - Scheduled cleanup and optimization
- **Integration** - Seamless Towny compatibility

## 🐛 Bug Fixes & Improvements

- Fixed entity clearing compatibility with modern Bukkit API
- Resolved Folia threading violations in entity management
- Enhanced help system with proper permission display
- Improved Towny integration with smart material detection
- Optimized memory usage and garbage collection

## 🎯 Perfect For

- **Large servers** with performance concerns
- **Towny servers** needing protected claim cleanup
- **Folia servers** requiring thread-safe optimization
- **Admin teams** wanting secure, powerful tools
- **Professional setups** demanding reliable performance

---

**HBZCleaner v1.0.1** - Professional server optimization for the modern Minecraft community

*Built with ❤️ for server administrators who demand the best*
