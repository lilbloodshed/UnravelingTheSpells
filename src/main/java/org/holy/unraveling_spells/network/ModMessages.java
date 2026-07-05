package org.holy.unraveling_spells.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.network.packet.*;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(SchoolC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SchoolC2SPacket::new)
                .encoder(SchoolC2SPacket::toBytes)
                .consumerMainThread(SchoolC2SPacket::handle)
                .add();

        net.messageBuilder(SchoolS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SchoolS2CPacket::new)
                .encoder(SchoolS2CPacket::toBytes)
                .consumerMainThread(SchoolS2CPacket::handle)
                .add();

        net.messageBuilder(RequestSyncPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestSyncPacket::new)
                .encoder(RequestSyncPacket::encode)
                .consumerMainThread(RequestSyncPacket::handle)
                .add();

        net.messageBuilder(SpellC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SpellC2SPacket::new)
                .encoder(SpellC2SPacket::toBytes)
                .consumerMainThread(SpellC2SPacket::handle)
                .add();

        net.messageBuilder(SpellS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SpellS2CPacket::new)
                .encoder(SpellS2CPacket::toBytes)
                .consumerMainThread(SpellS2CPacket::handle)
                .add();

        net.messageBuilder(LearnSpellPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(LearnSpellPacket::decode)
                .encoder(LearnSpellPacket::encode)
                .consumerMainThread(LearnSpellPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
