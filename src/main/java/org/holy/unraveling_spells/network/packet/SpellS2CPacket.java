package org.holy.unraveling_spells.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.holy.unraveling_spells.capability.spell.PlayerSpellProvider;
import org.holy.unraveling_spells.client.screens.LearningScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SpellS2CPacket {
    List<ResourceLocation> spells;

    public SpellS2CPacket(List<ResourceLocation> spells) {
        this.spells = spells;
    }

    public SpellS2CPacket(FriendlyByteBuf buf) {
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
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null) {
                player.getCapability(PlayerSpellProvider.PLAYER_SPELL).ifPresent(cap -> {
                    cap.getSpells().clear();
                    cap.getSpells().addAll(spells);
                });

                // Находим экран и вызываем onSyncComplete
                Screen currentScreen = Minecraft.getInstance().screen;
                if (currentScreen instanceof LearningScreen branchScreen) {
                    branchScreen.SyncSpell();
                    branchScreen.onSyncComplete();
                }
            }
        });
        return true;
    }
}
