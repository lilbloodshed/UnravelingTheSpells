# Common sources

`src/main/java` contains loader-independent Java classes shared by both builds.
Keep Forge, NeoForge and Minecraft-version-specific APIs out of this directory.
The player knowledge data classes are shared; capability providers and event
registration remain platform-specific.

`src/main/resources` contains assets that are valid for both Minecraft versions.
Loader metadata, mixin configurations, recipes, loot tables and `pack.mcmeta`
remain in the version-specific projects.
