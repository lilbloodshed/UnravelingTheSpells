package org.holy.unraveling_spells.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.holy.unraveling_spells.block.magic_lectern.MagicLecternTile;

import java.util.function.Supplier;

public class LearnSpellPacket {
    final BlockPos pos;

    public LearnSpellPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(LearnSpellPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static LearnSpellPacket decode(FriendlyByteBuf buffer) {
        return new LearnSpellPacket(buffer.readBlockPos());
    }

    public static void handle(LearnSpellPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            Level level = player.level();
            BlockEntity blockEntity = level.getBlockEntity(packet.pos);

            if (blockEntity instanceof MagicLecternTile magicTableTile) {
                magicTableTile.removeSpellTablet();
            }
        });
        context.get().setPacketHandled(true);
    }
}
