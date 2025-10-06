# Command Usage & Tab Completion Audit

## Commands Registered in CommandManager

Based on `CommandManager.java`:

### Basic Commands
1. **help** (aliases: h)
2. **ping** (aliases: p)
3. **tps**
4. **ram**
5. **gc**
6. **info** (aliases: i)
7. **status** (aliases: master, m)
8. **protocol** (aliases: pr)

### Utility Commands
9. **clear** (aliases: c)
10. **count** (aliases: ct)
11. **world** (aliases: w)
12. **unload** (aliases: u)

### Feature Commands
13. **stacker** (aliases: stack)
14. **entities** (aliases: ent)
15. **limiter** (aliases: lim)
16. **towny** (aliases: town)
17. **preset**
18. **villagers** (aliases: optimize)
19. **reload** (aliases: rl)
20. **warnings** (aliases: warn)
21. **mapart** (aliases: map)

## Tab Completions in HBZTabCompleter

Currently offers (first argument):
- help, h
- master, m
- ram
- info, i
- world, w
- gc
- tps
- status
- modules, mo
- protocol, pr
- unload, u
- ping, p
- clear, c
- count, ct
- preset, presets
- entities
- limiter
- villagers
- optimize
- towny, town
- stacker, stack
- reload, rl
- warnings, warn
- test

## Issues Found

### Missing from Tab Completer:
1. **ent** (alias for entities)
2. **lim** (alias for limiter)
3. **mapart** / **map** (map art command)

### Extra in Tab Completer (not in CommandManager):
1. **modules** / **mo** - This command doesn't exist!
2. **test** - This command doesn't exist!
3. **presets** (should just be "preset")

### Inconsistent Aliases:
- Tab completer has both "preset" and "presets", should just be "preset"

## Next Steps:
1. ✅ Remove: modules, mo, test, presets
2. ✅ Add: ent, lim, mapart, map
3. ✅ Verify each command's sub-argument tab completions match their getUsage() and getTabCompletions()
4. ✅ Update plugin.yml usage string to be comprehensive
