package org.holy.unraveling_spells.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.resources.ResourceLocation;
import org.holy.unraveling_spells.capability.school.PlayerSchoolProvider;
import org.holy.unraveling_spells.client.screens.LearningScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SchoolS2CPacket {
    List<ResourceLocation> schools;

    public SchoolS2CPacket(List<ResourceLocation> schools) {
        this.schools = schools;
    }

    public SchoolS2CPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        schools = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            schools.add(ResourceLocation.parse(buf.readUtf()));
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(schools.size());

        for (ResourceLocation school : schools) {
            buf.writeUtf(school.toString());
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player != null) {
                player.getCapability(PlayerSchoolProvider.PLAYER_SCHOOL).ifPresent(cap -> {
                    cap.getSchools().clear();
                    cap.getSchools().addAll(schools);
                });

                // Находим экран и вызываем onSyncComplete
                Screen currentScreen = Minecraft.getInstance().screen;

                if (currentScreen instanceof LearningScreen branchScreen) {
                    branchScreen.SyncSchool();
                    branchScreen.onSyncComplete();
                }
            }
        });
        return true;
    }
}
