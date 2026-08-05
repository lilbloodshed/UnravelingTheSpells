package org.holy.unraveling_spells.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.holy.unraveling_spells.network.packet.CommonConfigS2CPacket;
import org.holy.unraveling_spells.network.packet.LearnSpellPacket;
import org.holy.unraveling_spells.network.packet.RequestSyncPacket;
import org.holy.unraveling_spells.network.packet.SchoolC2SPacket;
import org.holy.unraveling_spells.network.packet.SchoolS2CPacket;
import org.holy.unraveling_spells.network.packet.SpellC2SPacket;
import org.holy.unraveling_spells.network.packet.SpellS2CPacket;

public final class ModMessages {
    private static final String NETWORK_VERSION = "1";

    private ModMessages() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);

        registrar.playToServer(SchoolC2SPacket.TYPE, SchoolC2SPacket.STREAM_CODEC, SchoolC2SPacket::handle);
        registrar.playToClient(SchoolS2CPacket.TYPE, SchoolS2CPacket.STREAM_CODEC, SchoolS2CPacket::handle);
        registrar.playToServer(RequestSyncPacket.TYPE, RequestSyncPacket.STREAM_CODEC, RequestSyncPacket::handle);
        registrar.playToServer(SpellC2SPacket.TYPE, SpellC2SPacket.STREAM_CODEC, SpellC2SPacket::handle);
        registrar.playToClient(SpellS2CPacket.TYPE, SpellS2CPacket.STREAM_CODEC, SpellS2CPacket::handle);
        registrar.playToServer(LearnSpellPacket.TYPE, LearnSpellPacket.STREAM_CODEC, LearnSpellPacket::handle);
        registrar.playToClient(CommonConfigS2CPacket.TYPE, CommonConfigS2CPacket.STREAM_CODEC, CommonConfigS2CPacket::handle);
    }

    public static void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }

    public static void sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void sendToClients(CustomPacketPayload message) {
        PacketDistributor.sendToAllPlayers(message);
    }
}
