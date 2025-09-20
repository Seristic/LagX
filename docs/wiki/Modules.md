# Modules

LagX supports simple drop-in modules placed under `plugins/LagX/Modules` (data folder `Modules/` inside the plugin).

Module JAR contents:

- `module.yml` with keys: `name`, `version`, `author`, `main`
- Compiled classes; `main` points to a class extending `com.seristic.hbzcleaner.api.Module`

Module lifecycle:

- Scanned on plugin startup from the `Modules/` directory
- Loaded via isolated `URLClassLoader`
- `onEnable()` called after loading; `onDisable()` on plugin shutdown

Module API (highlights):

- Extend `com.seristic.hbzcleaner.api.Module`
- `onEnable()` / `onDisable()` — lifecycle hooks
- `onCommand(CommandSender sender, String label, String[] args)` — optional command handling hook; return true if consumed
- `getDataFolder()` — returns `plugins/LagX/Modules/<module name>` directory for your data
- `getLogger()` — use plugin logger
- `Module.registerHelp("/yourcmd", "Description")` — add a hoverable help entry into `/lagx help`

Permissions:

- Define your own permissions; the sample `registerHelp` call uses a placeholder `module.permission` node.

Example `module.yml`:

```yaml
name: ExampleModule
version: 1.0.0
author: You
main: your.package.ExampleModule
```
