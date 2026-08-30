package org.holy.unraveling_spells.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.holy.unraveling_spells.client.MagicLecternScreen;
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
    private final Configuration.LearnType schoolPriceType;
    private final int schoolXpBaseCost;
    private final double schoolXpMultiplier;
    private final double schoolXpCostGrowth;
    private final int schoolXpMinimumLevel;
    private final boolean schoolXpAllowBulkLearning;
    private final boolean schoolXpRespectMaxSchools;
    private final Map<ResourceLocation, Integer> schoolXpCosts;
    private final Map<ResourceLocation, String> uniqueSpellInfo;

    public CommonConfigS2CPacket(
            int maxSchools,
            boolean eldritchSchoolLearning,
            Set<ResourceLocation> schoolsWithoutLearning,
            int defaultSpellScrollCost,
            Map<ResourceLocation, Integer> spellScrollCosts,
            Set<ResourceLocation> defaultLearnedSpells,
            Configuration.LearnType schoolPriceType,
            int schoolXpBaseCost,
            double schoolXpMultiplier,
            double schoolXpCostGrowth,
            int schoolXpMinimumLevel,
            boolean schoolXpAllowBulkLearning,
            boolean schoolXpRespectMaxSchools,
            Map<ResourceLocation, Integer> schoolXpCosts,
            Map<ResourceLocation, String> uniqueSpellInfo) {
        this.maxSchools = maxSchools;
        this.eldritchSchoolLearning = eldritchSchoolLearning;
        this.schoolsWithoutLearning = new LinkedHashSet<>(schoolsWithoutLearning);
        this.defaultSpellScrollCost = defaultSpellScrollCost;
        this.spellScrollCosts = new LinkedHashMap<>(spellScrollCosts);
        this.defaultLearnedSpells = new LinkedHashSet<>(defaultLearnedSpells);
        this.schoolPriceType = schoolPriceType;
        this.schoolXpBaseCost = schoolXpBaseCost;
        this.schoolXpMultiplier = schoolXpMultiplier;
        this.schoolXpCostGrowth = schoolXpCostGrowth;
        this.schoolXpMinimumLevel = schoolXpMinimumLevel;
        this.schoolXpAllowBulkLearning = schoolXpAllowBulkLearning;
        this.schoolXpRespectMaxSchools = schoolXpRespectMaxSchools;
        this.schoolXpCosts = new LinkedHashMap<>(schoolXpCosts);
        this.uniqueSpellInfo = new LinkedHashMap<>(uniqueSpellInfo);
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
        schoolPriceType = buffer.readEnum(Configuration.LearnType.class);
        schoolXpBaseCost = buffer.readVarInt();
        schoolXpMultiplier = buffer.readDouble();
        schoolXpCostGrowth = buffer.readDouble();
        schoolXpMinimumLevel = buffer.readVarInt();
        schoolXpAllowBulkLearning = buffer.readBoolean();
        schoolXpRespectMaxSchools = buffer.readBoolean();
        schoolXpCosts = readCosts(buffer);
        uniqueSpellInfo = readTextTemplates(buffer);
    }

    public static CommonConfigS2CPacket fromServerConfig() {
        return new CommonConfigS2CPacket(
                Configuration.MAX_SCHOOLS.get(),
                Configuration.ENABLE_ELDRITCH_SCHOOL_LEARNING.get(),
                Configuration.getLocalSchoolsWithoutLearning(),
                Configuration.DEFAULT_SPELL_SCROLL_COST.get(),
                Configuration.getLocalSpellScrollCosts(),
                Configuration.getLocalDefaultLearnedSpells(),
                Configuration.SCHOOLS_PRICE_TYPE.get(),
                Configuration.XP_BASE_COST.get(),
                Configuration.PRICE_XP_MULTIPLIER.get(),
                Configuration.XP_COST_GROWTH.get(),
                Configuration.XP_MINIMUM_LEVEL.get(),
                Configuration.XP_ALLOW_BULK_LEARNING.get(),
                Configuration.XP_RESPECT_MAX_SCHOOLS.get(),
                Configuration.getLocalSchoolXpCosts(),
                Configuration.getLocalUniqueSpellInfo()
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
        buffer.writeEnum(schoolPriceType);
        buffer.writeVarInt(schoolXpBaseCost);
        buffer.writeDouble(schoolXpMultiplier);
        buffer.writeDouble(schoolXpCostGrowth);
        buffer.writeVarInt(schoolXpMinimumLevel);
        buffer.writeBoolean(schoolXpAllowBulkLearning);
        buffer.writeBoolean(schoolXpRespectMaxSchools);
        writeCosts(buffer, schoolXpCosts);
        writeTextTemplates(buffer, uniqueSpellInfo);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            Configuration.applyServerConfig(
                    maxSchools,
                    eldritchSchoolLearning,
                    schoolsWithoutLearning,
                    defaultSpellScrollCost,
                    spellScrollCosts,
                    schoolPriceType,
                    schoolXpBaseCost,
                    schoolXpMultiplier,
                    schoolXpCostGrowth,
                    schoolXpMinimumLevel,
                    schoolXpAllowBulkLearning,
                    schoolXpRespectMaxSchools,
                    schoolXpCosts,
                    uniqueSpellInfo
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

    private static Map<ResourceLocation, Integer> readCosts(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        Map<ResourceLocation, Integer> costs = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) costs.put(buffer.readResourceLocation(), buffer.readVarInt());
        return costs;
    }

    private static void writeCosts(FriendlyByteBuf buffer, Map<ResourceLocation, Integer> costs) {
        buffer.writeVarInt(costs.size());
        for (Map.Entry<ResourceLocation, Integer> entry : costs.entrySet()) {
            buffer.writeResourceLocation(entry.getKey());
            buffer.writeVarInt(entry.getValue());
        }
    }

    private static Map<ResourceLocation, String> readTextTemplates(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        Map<ResourceLocation, String> templates = new LinkedHashMap<>();
        for (int i = 0; i < count; i++) {
            templates.put(buffer.readResourceLocation(), buffer.readUtf(32_767));
        }
        return templates;
    }

    private static void writeTextTemplates(FriendlyByteBuf buffer, Map<ResourceLocation, String> templates) {
        buffer.writeVarInt(templates.size());
        for (Map.Entry<ResourceLocation, String> entry : templates.entrySet()) {
            buffer.writeResourceLocation(entry.getKey());
            buffer.writeUtf(entry.getValue(), 32_767);
        }
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
