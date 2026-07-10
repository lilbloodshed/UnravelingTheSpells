package org.holy.unraveling_spells.config;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class SpellLearnedManager {
    private static final Set<ResourceLocation> learnedSpells = new HashSet<>();

    public static void loadConfig() {
        learnedSpells.clear();
        for (String spellId : Configuration.LEARNED_SPELLS.get()) {
            try {
                ResourceLocation location = new ResourceLocation(spellId);
                learnedSpells.add(location);
            } catch (Exception e) {
                System.err.println("Invalid spell ID in config: " + spellId);
            }
        }
    }

    public static boolean isSpellDefaultLearned(ResourceLocation spellId) {
        return learnedSpells.contains(spellId);
    }

    public static Set<ResourceLocation> getDefaultLearnedSpells() {
        return new HashSet<>(learnedSpells);
    }
}
