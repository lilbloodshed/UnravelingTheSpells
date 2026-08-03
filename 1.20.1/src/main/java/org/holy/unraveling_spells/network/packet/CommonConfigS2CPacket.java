package org.holy.unraveling_spells.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.holy.unraveling_spells.client.screens.MagicLecternScreen;
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.config.SpellLearnedManager;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class CommonConfigS2CPacket {
    private final int maxSchools;
    private final boolean eldritchSchoolLearning;
    private final Set<ResourceLocation> schoolsWithoutLearning;
    private final int defaultSpellScrollCost;
    private final Map<ResourceLocation, Integer> spellScrollCosts;
    private final Set<ResourceLocation> defaultLearnedSpells;

    public CommonConfigS2CPacket(
            int maxSchools,
            boolean eldritchSchoolLearning,
            Set<ResourceLocation> schoolsWithoutLearning,
            int defaultSpellScrollCost,
            Map<ResourceLocation, Integer> spellScrollCosts,
            Set<ResourceLocation> defaultLearnedSpells) {
        this.maxSchools = maxSchools;
        this.eldritchSchoolLearning = eldritchSchoolLearning;
        this.schoolsWithoutLearning = new LinkedHashSet<>(schoolsWithoutLearning);
        this.defaultSpellScrollCost = defaultSpellScrollCost;
        this.spellScrollCosts = new LinkedHashMap<>(spellScrollCosts);
        this.defaultLearnedSpells = new LinkedHashSet<>(defaultLearnedSpells);
    }

    public CommonConfigS2CPacket(FriendlyByteBuf buffer) {
        maxSchools = buffer.readVarInt();
        eldritchSchoolLearning = buffer.readBoolean();
        schoolsWithoutLearning = readResourceLocations(buffer);
        defaultSpellScrollCost = buffer.readVarInt();

        int costCount = buffer.readVarInt();
        spellScrollCosts = new LinkedHashMap<>();
        for (int i = 0; i < costCount; i++) {
            spellScrollCosts.put(
                    buffer.readResourceLocation(),
                    buffer.readVarInt()
            );
        }

        defaultLearnedSpells = readResourceLocations(buffer);
    }

    public static CommonConfigS2CPacket fromServerConfig() {
        return new CommonConfigS2CPacket(
                Configuration.MAX_SCHOOLS.get(),
                Configuration.ENABLE_ELDRITCH_SCHOOL_LEARNING.get(),
                Configuration.getLocalSchoolsWithoutLearning(),
                Configuration.DEFAULT_SPELL_SCROLL_COST.get(),
                Configuration.getLocalSpellScrollCosts(),
                Configuration.getLocalDefaultLearnedSpells()
        );
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(maxSchools);
        buffer.writeBoolean(eldritchSchoolLearning);
        writeResourceLocations(buffer, schoolsWithoutLearning);
        buffer.writeVarInt(defaultSpellScrollCost);

        buffer.writeVarInt(spellScrollCosts.size());
        for (Map.Entry<ResourceLocation, Integer> entry : spellScrollCosts.entrySet()) {
            buffer.writeResourceLocation(entry.getKey());
            buffer.writeVarInt(entry.getValue());
        }

        writeResourceLocations(buffer, defaultLearnedSpells);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Configuration.applyServerConfig(
                    maxSchools,
                    eldritchSchoolLearning,
                    schoolsWithoutLearning,
                    defaultSpellScrollCost,
                    spellScrollCosts
            );
            SpellLearnedManager.setDefaultLearnedSpells(defaultLearnedSpells);

            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen instanceof MagicLecternScreen screen) {
                screen.SyncCommonConfig();
                screen.onSyncComplete();
            }
        });
        return true;
    }

    private static Set<ResourceLocation> readResourceLocations(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        Set<ResourceLocation> values = new LinkedHashSet<>();
        for (int i = 0; i < count; i++) {
            values.add(buffer.readResourceLocation());
        }
        return values;
    }

    private static void writeResourceLocations(
            FriendlyByteBuf buffer,
            Set<ResourceLocation> values) {
        buffer.writeVarInt(values.size());
        for (ResourceLocation value : values) {
            buffer.writeResourceLocation(value);
        }
    }
}
