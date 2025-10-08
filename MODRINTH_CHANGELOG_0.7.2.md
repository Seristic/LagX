# LagX 0.7.2 - Integration & Polish Update

## 🔧 Fixed Issues

### Towny Integration Detection

- **Fixed**: Towny integration not being detected on servers with custom forks
- **Fixed**: Integration system now defaults to enabled for backward compatibility
- Works seamlessly with both standard Towny and custom forks
- No config changes required - existing configs work automatically

### Color Code Display

- **Fixed**: Warning messages showing raw color codes (`&6&lLagX &7&l>>&r`) instead of formatted colors
- All warning messages now properly display with beautiful colors
- Consistent color formatting across all plugin messages

### Config Version Management

- **Fixed**: Config version synchronization between code and YAML files
- Build-time replacement ensures version consistency
- Config version 0.2.0 properly synced

## ✨ New Features

### Plugin Integrations System

- New configurable integrations framework in `config.yml`
- Towny integration enabled by default for seamless experience
- Future-ready for additional plugin integrations (map protection, etc.)
- Backward compatible with configs missing the integration section

### Enhanced Integration Manager

- Smarter plugin detection and initialization
- Better defaults for common integrations
- More robust error handling and logging

## 📝 Configuration

New optional config section (automatically uses defaults if missing):

```yaml
integrations:
  towny:
    enabled: true # Default: true
  copyright-plugin:
    enabled: false # Default: false (future feature)
```

## 🔄 Compatibility

- **Minecraft**: 1.20.6+
- **Server**: Paper, Folia
- **Java**: 21
- **Towny**: All versions (including custom forks)

## 📦 Installation

1. Download `LagX-0.7.2.jar`
2. Place in your server's `plugins` folder
3. Restart server (or use `/lagx reload` if updating from 0.7.1)
4. Towny integration works automatically if Towny is installed!

## 🐛 Bug Reports

Found an issue? Report it on our [GitHub Issues](https://github.com/Seristic/LagX/issues)

---

**Note**: This is a polish and bugfix release. If you're upgrading from 0.7.1, your existing config will work without changes. The new `integrations` section is optional - the plugin uses smart defaults when it's missing.
