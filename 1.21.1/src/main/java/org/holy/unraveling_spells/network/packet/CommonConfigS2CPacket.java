package org.holy.unraveling_spells.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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
        Set<ResourceLocation> defaultLearnedSpells) implements CustomPacketPayload {
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
                Configuration.getLocalDefaultLearnedSpells());
    }

    public static void handle(CommonConfigS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Configuration.applyServerConfig(
                    packet.maxSchools(),
                    packet.eldritchSchoolLearning(),
                    packet.schoolsWithoutLearning(),
                    packet.defaultSpellScrollCost(),
                    packet.spellScrollCosts());
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
        return new CommonConfigS2CPacket(
                maxSchools,
                eldritchSchoolLearning,
                schoolsWithoutLearning,
                defaultSpellScrollCost,
                spellScrollCosts,
                defaultLearnedSpells);
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
