package org.holy.unraveling_spells.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.capability.PlayerSpellProvider;
import org.holy.unraveling_spells.client.MagicLecternScreen;

import java.util.List;

public record SpellS2CPacket(List<ResourceLocation> spells) implements CustomPacketPayload {
    public static final Type<SpellS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "spell_s2c"));
    public static final StreamCodec<ByteBuf, SpellS2CPacket> STREAM_CODEC =
            PacketCodecs.RESOURCE_LOCATION_LIST.map(SpellS2CPacket::new, SpellS2CPacket::spells);

    public SpellS2CPacket {
        spells = List.copyOf(spells);
    }

    public static void handle(SpellS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var spellData = PlayerSpellProvider.get(context.player());
            spellData.getSpells().clear();
            spellData.getSpells().addAll(packet.spells());

            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen instanceof MagicLecternScreen screen) {
                screen.SyncSpells(packet.spells());
                screen.onSyncComplete();
            }
        });
    }

    @Override
    public Type<SpellS2CPacket> type() {
        return TYPE;
    }
}
