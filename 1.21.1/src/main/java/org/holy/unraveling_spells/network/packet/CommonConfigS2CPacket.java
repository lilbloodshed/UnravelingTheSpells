package org.holy.unraveling_spells.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.client.MagicLecternScreen;
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.config.SpellLearnedManager;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public record CommonConfigS2CPacket(
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
        Map<ResourceLocation, String> uniqueSpellInfo) implements CustomPacketPayload {
    public static final Type<CommonConfigS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "common_config_s2c"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CommonConfigS2CPacket> STREAM_CODEC =
            CustomPacketPayload.codec(CommonConfigS2CPacket::write, CommonConfigS2CPacket::read);

    public CommonConfigS2CPacket {
        schoolsWithoutLearning = Set.copyOf(schoolsWithoutLearning);
        spellScrollCosts = Map.copyOf(spellScrollCosts);
        defaultLearnedSpells = Set.copyOf(defaultLearnedSpells);
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

    public static void handle(CommonConfigS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Configuration.applyServerConfig(
                    packet.maxSchools(),
                    packet.eldritchSchoolLearning(),
                    packet.schoolsWithoutLearning(),
                    packet.defaultSpellScrollCost(),
                    packet.spellScrollCosts(),
                    packet.schoolPriceType(),
                    packet.schoolXpBaseCost(),
                    packet.schoolXpMultiplier(),
                    packet.schoolXpCostGrowth(),
                    packet.schoolXpMinimumLevel(),
                    packet.schoolXpAllowBulkLearning(),
                    packet.schoolXpRespectMaxSchools(),
                    packet.schoolXpCosts(),
                    packet.uniqueSpellInfo());
            SpellLearnedManager.setDefaultLearnedSpells(packet.defaultLearnedSpells());

            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen instanceof MagicLecternScreen screen) {
                screen.SyncCommonConfig();
                screen.onSyncComplete();
            }
        });
    }

    private static CommonConfigS2CPacket read(RegistryFriendlyByteBuf buffer) {
        int maxSchools = buffer.readVarInt();
        boolean eldritchSchoolLearning = buffer.readBoolean();
        Set<ResourceLocation> schoolsWithoutLearning = readResourceLocations(buffer);
        int defaultSpellScrollCost = buffer.readVarInt();

        int costCount = readBoundedSize(buffer);
        Map<ResourceLocation, Integer> spellScrollCosts = new LinkedHashMap<>();
        for (int i = 0; i < costCount; i++) {
            spellScrollCosts.put(buffer.readResourceLocation(), buffer.readVarInt());
        }

        Set<ResourceLocation> defaultLearnedSpells = readResourceLocations(buffer);

        Configuration.LearnType schoolPriceType = buffer.readEnum(Configuration.LearnType.class);
        int schoolXpBaseCost = buffer.readVarInt();
        double schoolXpMultiplier = buffer.readDouble();
        double schoolXpCostGrowth = buffer.readDouble();
        int schoolXpMinimumLevel = buffer.readVarInt();
        boolean schoolXpAllowBulkLearning = buffer.readBoolean();
        boolean schoolXpRespectMaxSchools = buffer.readBoolean();
        Map<ResourceLocation, Integer> schoolXpCosts = readCosts(buffer);
        Map<ResourceLocation, String> uniqueSpellInfo = readTextTemplates(buffer);
        return new CommonConfigS2CPacket(
                maxSchools,
                eldritchSchoolLearning,
                schoolsWithoutLearning,
                defaultSpellScrollCost,
                spellScrollCosts,
                defaultLearnedSpells,
                schoolPriceType,
                schoolXpBaseCost,
                schoolXpMultiplier,
                schoolXpCostGrowth,
                schoolXpMinimumLevel,
                schoolXpAllowBulkLearning,
                schoolXpRespectMaxSchools,
                schoolXpCosts,
                uniqueSpellInfo);
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(maxSchools);
        buffer.writeBoolean(eldritchSchoolLearning);
        writeResourceLocations(buffer, schoolsWithoutLearning);
        buffer.writeVarInt(defaultSpellScrollCost);

        buffer.writeVarInt(spellScrollCosts.size());
        spellScrollCosts.forEach((spellId, cost) -> {
            buffer.writeResourceLocation(spellId);
            buffer.writeVarInt(cost);
        });

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

    private static Set<ResourceLocation> readResourceLocations(RegistryFriendlyByteBuf buffer) {
        int count = readBoundedSize(buffer);
        Set<ResourceLocation> values = new LinkedHashSet<>();
        for (int i = 0; i < count; i++) {
            values.add(buffer.readResourceLocation());
        }
        return values;
    }

    private static void writeResourceLocations(
            RegistryFriendlyByteBuf buffer,
            Set<ResourceLocation> values) {
        buffer.writeVarInt(values.size());
        values.forEach(buffer::writeResourceLocation);
    }

    private static int readBoundedSize(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > PacketCodecs.MAX_KNOWLEDGE_ENTRIES) {
            throw new IllegalArgumentException("Invalid collection size: " + size);
        }
        return size;
    }

    @Override
    public Type<CommonConfigS2CPacket> type() {
        return TYPE;
    }
}
