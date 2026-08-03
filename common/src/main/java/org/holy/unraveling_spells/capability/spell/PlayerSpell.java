package org.holy.unraveling_spells.capability.spell;

import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import org.holy.unraveling_spells.common.knowledge.KnowledgeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class PlayerSpell {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerSpell.class);
    private final KnowledgeSet<ResourceLocation> spells = new KnowledgeSet<>();

    public Set<ResourceLocation> getSpells() {
        return spells.values();
    }

    public boolean isLearned(ResourceLocation spellId) {
        return spells.contains(spellId);
    }

    public void addSpell(ResourceLocation id) {
        spells.add(id);
    }

    public void removeSpell(ResourceLocation id) {
        spells.remove(id);
    }

    public void copyFrom(PlayerSpell source) {
        spells.replaceWith(source.getSpells());
    }


    public void saveNBTData(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (ResourceLocation spell : spells.values()) {
            list.add(StringTag.valueOf(spell.toString()));
        }
        nbt.put("PlayerSpells", list);
    }

    public void loadNBTData(CompoundTag nbt) {
        spells.values().clear();
        if (nbt.contains("PlayerSpells")) {
            ListTag list = nbt.getList("PlayerSpells", 8);
            for (int i = 0; i < list.size(); i++) {
                String spellStr = list.getString(i);
                try {
                    ResourceLocation spellId = ResourceLocation.parse(spellStr);
                    spells.add(spellId);
                } catch (ResourceLocationException e) {
                    LOGGER.warn("Invalid spell ID in NBT: {}", spellStr, e);
                }
            }
        }
    }
}
