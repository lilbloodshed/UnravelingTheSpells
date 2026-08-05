package org.holy.unraveling_spells.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.events.ModEvents;

public record RequestSyncPacket() implements CustomPacketPayload {
    public static final Type<RequestSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "request_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSyncPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestSyncPacket());

    public static void handle(RequestSyncPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ModEvents.syncPlayerKnowledge(player);
        }
    }

    @Override
    public Type<RequestSyncPacket> type() {
        return TYPE;
    }
}
