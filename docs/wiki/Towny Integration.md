# Towny Integration

LagX optionally integrates with Towny to respect protected regions during cleanups.

Notes:

- The plugin has a soft-depend on Towny. If Towny is not present, the integration is disabled automatically.
- A bypass permission `hbzcleaner.towny.bypass` exists for administrative overrides.

Commands:

- `/lagx towny status` — Show if Towny is detected and version
- `/lagx towny info` — For players: show current location's protection and town name

Permissions:

- `hbzcleaner.towny`
- `hbzcleaner.towny.bypass`
