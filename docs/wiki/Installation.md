# Installation

Requirements:

- Java 21 runtime
- Paper/Folia 1.21.x (Folia supported)
- Optional: Towny (soft-depend)

Steps:

1. Download the plugin jar (artifact name `LagX-<version>-folia.jar`).
2. Place it in your server's `plugins/` folder (you can keep the filename `HBZCleaner.jar` if preferred).
3. Start the server once to generate `config.yml`.
4. Edit `config.yml` to your needs. See [[Configuration]].
5. Reload with `/lagx reload` or restart the server.

Updating:

- The config includes `auto-update: true`. You can disable it if you manage updates manually.
- When upgrading from HBZCleaner, LagX will auto-migrate any legacy prefix occurrences to "LagX" in `config.yml` on boot.
