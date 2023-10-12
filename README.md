<p align="center"><img src="https://github.com/STEMMechanics/STEMCraft/blob/main/docs/stemcraft-sky-logo.jpg?raw=true" width="666" height="198"></p>

STEMMechanics brings creativity to life by developing engaging resources and programs. Powered by open source by an amazing community.

## Participating in the community

🫂 We aim to create a welcoming and inclusive community. Please note that STEMMechanics open source projects are governed by our [code of conduct](code-of-conduct.md).

## Changes

### 1.0.0

-   Rewrite of the framework to make adding features easier
-   When clicking a waystone, if the nearest does not have a safe location, instead of giving up, it will try other waystones

### 0.4.2

-   Added Restrictive Creative which disables players interacting, dropping and picking items unless they have the `stemcraft.creative.override` permission.
-   Added `/seen <player>` command requiring the `stemcraft.seen` permission.
-   Moved from custom YAML framework to [BoostedYAML](https://github.com/dejvokep/boosted-yaml)
-   Added /repair command
-   `/back` will now teleport a player to their last death location
-   Added this.plugin.DebugLog(String s) to output debug strings (if enabled in config)

### 0.3.2

-   Remove the usage of the System.Out method and instead use the Bukkit Logger methods

### 0.3.1

-   Fix players not being able to interact due to bug in the disable spawn eggs code

## Get in touch!

Learn more about what we're doing at [stemmechanics.com.au](https://stemmechanics.com.au).

👋 [@STEMMechanics](https://twitter.com/STEMMechanics)

-   Drop Mob heads when killed by a player in survival
