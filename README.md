<p align="center"><img src="https://github.com/STEMMechanics/.github/blob/main/stemcraft-sky-logo.jpg?raw=true" width="666" height="198"></p>

STEMMechanics brings creativity to life by developing engaging resources and programs. Powered by open source by an amazing community.

## Participating in the community

🫂 We aim to create a welcoming and inclusive community. Please note that STEMMechanics open source projects are governed by our [code of conduct](code-of-conduct.md).

## Contributing

We welcome all contributions that are in the spirit of the STEMCraft server. All contributions should be in their own branch with the appropriate prefixes:

**Bug fix**: hotfix/<name>\
**Feature**: feature/<name>

Contributions must:

-   Have a clear description of their purpose
-   Clear documentation of the variables and methods
-   Use the STEMCraft framework where possible

When your contribution is ready to be merged, create a Pull Request and once ready, it will be merged into the main branch at the approriate time (this may not be straight away depending on release schedules).

## Changes

### 1.1

-   Added ability to customize stonecutter recipes in the configuration
-   Graves will now spawn in the nearest safe location on land. If there is no suitable location, your items will just be dropped
-   Coordination action and boss bar now available using `/coord` and `/coordbar`
-   Trader villagers randomally spawn in survival worlds and keep a running tally of trades
-   Tons of helper methods for coding features
-   Tab completion shows options that contain the text instead of starts with
-   Added `display-version` config option to override MOTD version
-   Added /toolreset command to clear toolstats data on item
-   Added values feature for item pricing
-   Added workbench commands /anvil, /cartographytable, /grindstone, /loom, /smithingtable, /stonecutter
-   Restores remaining player night vision duration when disabling command
-   Added a simple persistent saving feature for other features to utilize
-   Added the /enchant command that add or removes enchantments from player items
-   Added the /tptop command that teleports a player upwards to the next safe location.

### 1.0.2

-   Added ability to link world inventories for nether/the end.

### 1.0.1

-   Internal code refactoring
-   Fix shift click crafting tool stats
-   Plugin version announced in server console on load
-   Player heads only drop in survival PVP
-   Waystones will now deactivate/reactive correctly when using pistons
-   A player will now teleport to the nearest safe location when entering a workshop
-   Workshop permissions and gamemode changes now work correctly

### 1.0.0

-   Rewrite of the framework to make adding features easier
-   When clicking a waystone, if the nearest does not have a safe location, instead of giving up, it will try other waystones

### 0.4.2

-   Added Restrictive Creative which disables players interacting, dropping and picking items unless they have the `stemcraft.creative.override` permission.
-   Added `/seen <player>` command requiring the `stemcraft.seen` permission
-   Moved from custom YAML framework to [BoostedYAML](https://github.com/dejvokep/boosted-yaml)
-   Added `/repair (<player>)` command to any repair in the main hand of a player
-   `/back` will now teleport a player to their last death location
-   Added this.plugin.DebugLog(String s) to output debug strings (if enabled in config)

### 0.3.2

-   Remove the usage of the System.Out method and instead use the Bukkit Logger methods

### 0.3.1

-   Fix players not being able to interact due to bug in the disable spawn eggs code

## Get in touch!

Learn more about what we're doing at [stemmechanics.com.au](https://stemmechanics.com.au).

👋 [@STEMMechanics](https://twitter.com/STEMMechanics)
