package org.holy.unraveling_spells.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.capability.PlayerSchoolProvider;
import org.holy.unraveling_spells.network.ModMessages;

import java.util.List;

public record SchoolC2SPacket(List<ResourceLocation> schools) implements CustomPacketPayload {
    public static final Type<SchoolC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "school_c2s"));
    public static final StreamCodec<ByteBuf, SchoolC2SPacket> STREAM_CODEC =
            PacketCodecs.RESOURCE_LOCATION_LIST.map(SchoolC2SPacket::new, SchoolC2SPacket::schools);

    public SchoolC2SPacket {
        schools = List.copyOf(schools);
    }

    public static void handle(SchoolC2SPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        var schoolData = PlayerSchoolProvider.get(player);
        schoolData.getSchools().clear();
        schoolData.getSchools().addAll(packet.schools());
        ModMessages.sendToPlayer(new SchoolS2CPacket(packet.schools()), player);
    }

    @Override
    public Type<SchoolC2SPacket> type() {
        return TYPE;
    }
}
