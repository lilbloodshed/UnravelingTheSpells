package org.holy.unraveling_spells.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.holy.unraveling_spells.client.MagicLecternScreen;

import java.util.function.Supplier;

public class SetTotalPlayerXPPacket {
    private final int totalExperience;

    public SetTotalPlayerXPPacket(int totalExperience) {
        this.totalExperience = Math.max(0, totalExperience);
    }

    public SetTotalPlayerXPPacket(FriendlyByteBuf buf) {
        totalExperience = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(totalExperience);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen instanceof MagicLecternScreen screen) {
                screen.setTotalPlayerXP(totalExperience);
            }
        });
        return true;
    }
}
