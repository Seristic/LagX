# FAQ

- Why is the plugin called LagX but permissions are `lagx.*`?

  - We rebranded user-facing names to LagX while keeping internal package names and permission nodes for compatibility.

- Do legacy commands still work?

  - Yes. `/hbzcleaner` and `/hbzperf` still work. We recommend using `/lagx` and `/lagxperf` in docs and messages.

- Does this work on Paper as well as Folia?

  - Yes, but it’s optimized for Folia’s scheduler model. Some outputs reference region threads.

- How do I safely clear entities without removing recent death items?

  - Use cc_items/cc_entities via `/lagx clear` or protocols; death-protected items are kept automatically.

- How do I add my own module?
  - Create a JAR with `module.yml` and a class extending `com.seristic.hbzcleaner.api.Module`. Drop it in `plugins/LagX/Modules/` and restart.
