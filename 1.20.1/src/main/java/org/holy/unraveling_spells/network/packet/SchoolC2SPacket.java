package org.holy.unraveling_spells.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.resources.ResourceLocation;
import org.holy.unraveling_spells.capability.school.PlayerSchoolProvider;
import org.holy.unraveling_spells.network.ModMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SchoolC2SPacket {
    List<ResourceLocation> schools;

    public SchoolC2SPacket(List<ResourceLocation> schools) {
        this.schools = schools;
    }

    public SchoolC2SPacket(FriendlyByteBuf buf) {
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
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player != null) {
                player.getCapability(PlayerSchoolProvider.PLAYER_SCHOOL).ifPresent(schoolData -> {
                    schoolData.getSchools().clear();
                    schoolData.getSchools().addAll(schools);
                    // Синхронизируем данные с клиентом
                    ModMessages.sendToClients(new SchoolS2CPacket(schools));
                });
            }
        });
        return true;
    }
}
