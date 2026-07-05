package org.holy.unraveling_spells.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.holy.unraveling_spells.capability.school.PlayerSchoolProvider;
import org.holy.unraveling_spells.capability.spell.PlayerSpellProvider;
import org.holy.unraveling_spells.network.ModMessages;

import java.util.ArrayList;
import java.util.function.Supplier;

public class RequestSyncPacket {
    public RequestSyncPacket() {}

    public RequestSyncPacket(FriendlyByteBuf buf) {}


    public void encode(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();

            if (player != null) {
                player.getCapability(PlayerSchoolProvider.PLAYER_SCHOOL).ifPresent(cap -> {
                    ModMessages.sendToPlayer(
                            new SchoolS2CPacket(new ArrayList<>(cap.getSchools())),
                            player
                    );
                });

                player.getCapability(PlayerSpellProvider.PLAYER_SPELL).ifPresent(cap -> {
                    ModMessages.sendToPlayer(
                            new SpellS2CPacket(new ArrayList<>(cap.getSpells())),
                            player
                    );
                });
            }
        });
        return true;
    }
}
