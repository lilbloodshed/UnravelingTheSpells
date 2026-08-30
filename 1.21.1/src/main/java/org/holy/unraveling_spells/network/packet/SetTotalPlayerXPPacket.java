package org.holy.unraveling_spells.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.client.MagicLecternScreen;

public record SetTotalPlayerXPPacket(int totalExperience) implements CustomPacketPayload {
    public static final Type<SetTotalPlayerXPPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "set_total_player_xp"));
    public static final StreamCodec<ByteBuf, SetTotalPlayerXPPacket> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(SetTotalPlayerXPPacket::new, SetTotalPlayerXPPacket::totalExperience);

    public SetTotalPlayerXPPacket {
        totalExperience = Math.max(0, totalExperience);
    }

    public static void handle(SetTotalPlayerXPPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen instanceof MagicLecternScreen screen) {
                screen.setTotalPlayerXP(packet.totalExperience());
            }
        });
    }

    @Override
    public Type<SetTotalPlayerXPPacket> type() {
        return TYPE;
    }
}
