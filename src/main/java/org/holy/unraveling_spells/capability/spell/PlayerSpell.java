package org.holy.unraveling_spells.capability.spell;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import org.holy.unraveling_spells.Unraveling_spells;

import java.util.HashSet;
import java.util.Set;

public class PlayerSpell {
    Set<ResourceLocation> spells = new HashSet<>();

    public Set<ResourceLocation> getSpells() {
        return spells;
    }

    public boolean isLearned(AbstractSpell spell) {
        return spells.contains(spell.getSpellResource());
    }

    public void addSpell(ResourceLocation id) {
        this.spells.add(id);
    }

    public void removeSpell(ResourceLocation id) {
        this.spells.remove(id);
    }

    public void copyFrom(PlayerSpell source) {
        this.spells.clear();
        this.spells.addAll(source.getSpells());
    }


    public void saveNBTData(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (ResourceLocation spell : spells) {
            list.add(StringTag.valueOf(spell.toString()));
        }
        nbt.put("PlayerSpells", list);
    }

    public void loadNBTData(CompoundTag nbt) {
        spells.clear();
        if (nbt.contains("PlayerSpells")) {
            ListTag list = nbt.getList("PlayerSpells", 8);
            for (int i = 0; i < list.size(); i++) {
                String spellStr = list.getString(i);
                try {
                    ResourceLocation spellId = ResourceLocation.parse(spellStr);
                    spells.add(spellId);
                } catch (ResourceLocationException e) {
                    Unraveling_spells.LOGGER.warn("Invalid spell ID in NBT: " + spellStr, e);
                }
            }
        }
    }
}
