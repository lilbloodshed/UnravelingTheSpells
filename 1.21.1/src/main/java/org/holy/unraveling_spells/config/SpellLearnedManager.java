package org.holy.unraveling_spells.config;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public final class SpellLearnedManager {
    private static final Set<ResourceLocation> learnedSpells = new HashSet<>();

    public static void loadConfig() {
        setDefaultLearnedSpells(Configuration.getLocalDefaultLearnedSpells());
    }

    public static void setDefaultLearnedSpells(Set<ResourceLocation> spells) {
        learnedSpells.clear();
        learnedSpells.addAll(spells);
    }

    public static boolean isSpellDefaultLearned(ResourceLocation spellId) {
        return learnedSpells.contains(spellId);
    }

    public static Set<ResourceLocation> getDefaultLearnedSpells() {
        return new HashSet<>(learnedSpells);
    }

    private SpellLearnedManager() {
    }
}
