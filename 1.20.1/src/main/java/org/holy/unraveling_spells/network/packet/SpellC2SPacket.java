package org.holy.unraveling_spells.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.holy.unraveling_spells.capability.spell.PlayerSpellProvider;
import org.holy.unraveling_spells.network.ModMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SpellC2SPacket {
    List<ResourceLocation> spells;

    public SpellC2SPacket(List<ResourceLocation> spells) {
        this.spells = spells;
    }

    public SpellC2SPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        spells = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            spells.add(ResourceLocation.parse(buf.readUtf()));
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(spells.size());

        for (ResourceLocation spell : spells) {
            buf.writeUtf(spell.toString());
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player != null) {
                player.getCapability(PlayerSpellProvider.PLAYER_SPELL).ifPresent(schoolData -> {
                    schoolData.getSpells().clear();
                    schoolData.getSpells().addAll(spells);
                    // Синхронизируем данные с клиентом
                    ModMessages.sendToClients(new SpellS2CPacket(spells));
                });
            }
        });
        return true;
    }
}
