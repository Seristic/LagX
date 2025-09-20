# Troubleshooting

Common issues and fixes.

- Command not found or permission denied

  - Ensure you're using `/lagx` and have the relevant `lagx.*` permissions.
  - Check `plugin.yml` loaded aliases: `lagx`, `lagxperf`.

- Protocol warnings not showing

  - Ensure `protocol_warnings.enabled: true` in config.
  - Only players with `lagx.warn` will see warnings.

- Entity limiter isn't doing anything

  - Check `entity_limiter.enabled: true` and the selected `preset_mode`.
  - Run `/lagx limiter status` to confirm state; `/lagx preset info` to review caps.
  - Set `check_interval` higher if too frequent; lower to enforce more often.

- Stacker isn't stacking

  - Ensure `stacker.enabled: true` and add entities to `stacker.stackable_entities`.
  - Use `/lagx stacker stack 50` to force a pass around you.

- Villager optimizer has no effect

  - Confirm `villager_optimization.enabled: true` and thresholds are not too high.
  - Use `/lagx villagers optimize <world>` to run once.

- Performance command errors
  - Requires `lagxperf.use` (or `lagx.performance`). Use `/lagxperf help`.

Logs/Support:

- Check `latest.log` for LagX messages.
- Include config excerpts and the output of `/lagx status` when reporting.
