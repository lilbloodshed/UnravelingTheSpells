package org.holy.unraveling_spells.capability.school;

import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import org.holy.unraveling_spells.common.knowledge.KnowledgeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class PlayerSchool {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerSchool.class);
    private final KnowledgeSet<ResourceLocation> schools = new KnowledgeSet<>();

    public Set<ResourceLocation> getSchools() {
        return schools.values();
    }

    public void addSchool(ResourceLocation id) {
        schools.add(id);
    }

    public void removeSchool(ResourceLocation id) {
        schools.remove(id);
    }

    public void copyFrom(PlayerSchool source) {
        schools.replaceWith(source.getSchools());
    }

    public void saveNBTData(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (ResourceLocation school : schools.values()) {
            list.add(StringTag.valueOf(school.toString()));
        }
        nbt.put("PlayerSchools", list);
    }

    public void loadNBTData(CompoundTag nbt) {
        schools.values().clear();
        if (nbt.contains("PlayerSchools")) {
            ListTag list = nbt.getList("PlayerSchools", 8);  // 8 = TAG_String
            for (int i = 0; i < list.size(); i++) {
                String schoolStr = list.getString(i);
                try {
                    ResourceLocation schoolId = ResourceLocation.parse(schoolStr);
                    schools.add(schoolId);
                } catch (ResourceLocationException e) {
                    LOGGER.warn("Invalid school ID in NBT: {}", schoolStr, e);
                }
            }
        }
    }
}
