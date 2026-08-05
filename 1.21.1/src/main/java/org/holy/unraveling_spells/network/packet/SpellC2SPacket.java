package org.holy.unraveling_spells.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.capability.PlayerSpellProvider;
import org.holy.unraveling_spells.network.ModMessages;

import java.util.List;

public record SpellC2SPacket(List<ResourceLocation> spells) implements CustomPacketPayload {
    public static final Type<SpellC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "spell_c2s"));
    public static final StreamCodec<ByteBuf, SpellC2SPacket> STREAM_CODEC =
            PacketCodecs.RESOURCE_LOCATION_LIST.map(SpellC2SPacket::new, SpellC2SPacket::spells);

    public SpellC2SPacket {
        spells = List.copyOf(spells);
    }

    public static void handle(SpellC2SPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        var spellData = PlayerSpellProvider.get(player);
        spellData.getSpells().clear();
        spellData.getSpells().addAll(packet.spells());
        ModMessages.sendToPlayer(new SpellS2CPacket(packet.spells()), player);
    }

    @Override
    public Type<SpellC2SPacket> type() {
        return TYPE;
    }
}
