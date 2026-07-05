package org.holy.unraveling_spells.capability.school;

import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import org.holy.unraveling_spells.Unraveling_spells;

import java.util.HashSet;
import java.util.Set;

public class PlayerSchool {
    Set<ResourceLocation> schools = new HashSet<>();

    public Set<ResourceLocation> getSchools() {
        return schools;
    }

    public void addSchool(ResourceLocation id) {
        this.schools.add(id);
    }

    public void removeSchool(ResourceLocation id) {
        this.schools.remove(id);
    }

    public void copyFrom(PlayerSchool source) {
        this.schools.clear();
        this.schools.addAll(source.getSchools());
    }

    public void saveNBTData(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (ResourceLocation school : schools) {
            list.add(StringTag.valueOf(school.toString()));
        }
        nbt.put("PlayerSchools", list);
    }

    public void loadNBTData(CompoundTag nbt) {
        schools.clear();
        if (nbt.contains("PlayerSchools")) {
            ListTag list = nbt.getList("PlayerSchools", 8);  // 8 = TAG_String
            for (int i = 0; i < list.size(); i++) {
                String schoolStr = list.getString(i);
                try {
                    ResourceLocation schoolId = ResourceLocation.parse(schoolStr);
                    schools.add(schoolId);
                } catch (ResourceLocationException e) {
                    Unraveling_spells.LOGGER.warn("Invalid school ID in NBT: " + schoolStr, e);
                }
            }
        }
    }
}