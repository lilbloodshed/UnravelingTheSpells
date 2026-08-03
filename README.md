# Unraveling Spells
Unraveling Spells adds a magic learning system for [Iron's Spells ‘n Spellbooks](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks): the player needs to open schools and spells through the magic department using special scrolls.

The mod makes the progression of magic more meaningful: unexplored spells are hidden and inaccessible, learned ones work as usual. Choose the schools of magic that you want to study in the future. To study spells, you need to create a Magic lectern, put a Spell Scroll in it and study your first spell.

The main advantage of the mod is its extensive customization options. It’s primarily suited for people who create their own modpacks.

In the mod’s configuration files, you can adjust:

*   the max. number of schools that can be learned;
*   which spells are learned by default;
*   how spell conflicts are handled;
*   and other settings.

The mod requires [Iron's Spells ‘n Spellbooks](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks) 3.15.0 or higher

Please report bugs or issues to the [discord addon branch](https://discord.com/channels/1104430139275743293/1476138588650606644) or to the [github page](https://github.com/lilbloodshed/UnravelingTheSpells/issues)!

![unravelingspells](https://cdn.modrinth.com/data/cached_images/d997f9282e1ff4d9db6e59f7117a32943ff207c9.jpeg) ![GUIs](https://cdn.modrinth.com/data/cached_images/8dcf9d9acac4c250444598d040491dcd56bb8b59.jpeg)

[MODRINTH](https://modrinth.com/mod/unraveling-the-spells)
[CURSEFORGE](https://www.curseforge.com/minecraft/mc-mods/iss-unraveling-the-spells)

## Development layout

- `common/` contains loader-independent Java utilities and shared assets.
- `1.20.1/` contains the complete Forge 1.20.1 implementation.
- `1.21.1/` contains the NeoForge 1.21.1 Gradle scaffold and its minimal main
  mod class. Other platform code is intentionally not implemented yet.

Each platform directory is an independent Gradle build with its own wrapper:

```text
1.20.1/gradlew build
1.21.1/gradlew build
```

The root wrapper provides convenience tasks:

```text
gradlew buildForge
gradlew buildNeoForge
gradlew buildAll
```

Forge 1.20.1 targets Java 17. NeoForge 1.21.1 targets Java 21. Do not place
Forge, NeoForge or version-specific Minecraft APIs in `common`.

### IntelliJ IDEA

The repository root is only a build launcher. For complete code navigation,
imports and run configurations, link `1.20.1/build.gradle` as the active
Gradle project. The shared `common` sources are included by its source set.

Link `1.21.1/build.gradle` as a second Gradle project and use Java 21 for it.
Both platform builds can remain linked in the same IDEA window.
