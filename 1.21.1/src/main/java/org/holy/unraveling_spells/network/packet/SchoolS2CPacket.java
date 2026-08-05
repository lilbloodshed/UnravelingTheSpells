package org.holy.unraveling_spells.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.capability.PlayerSchoolProvider;
import org.holy.unraveling_spells.client.MagicLecternScreen;

import java.util.List;

public record SchoolS2CPacket(List<ResourceLocation> schools) implements CustomPacketPayload {
    public static final Type<SchoolS2CPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "school_s2c"));
    public static final StreamCodec<ByteBuf, SchoolS2CPacket> STREAM_CODEC =
            PacketCodecs.RESOURCE_LOCATION_LIST.map(SchoolS2CPacket::new, SchoolS2CPacket::schools);

    public SchoolS2CPacket {
        schools = List.copyOf(schools);
    }

    public static void handle(SchoolS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            var schoolData = PlayerSchoolProvider.get(context.player());
            schoolData.getSchools().clear();
            schoolData.getSchools().addAll(packet.schools());

            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen instanceof MagicLecternScreen screen) {
                screen.SyncSchools(packet.schools());
                screen.onSyncComplete();
            }
        });
    }

    @Override
    public Type<SchoolS2CPacket> type() {
        return TYPE;
    }
}
