package org.holy.unraveling_spells.network.packet;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.holy.unraveling_spells.block.magic_lectern.MagicLecternTile;
import org.holy.unraveling_spells.capability.SpellLearningHelper;
import org.holy.unraveling_spells.config.Configuration;

import java.util.function.Supplier;

public class LearnSpellPacket {
    final BlockPos pos;
    final ResourceLocation spellId;

    public LearnSpellPacket(BlockPos pos, ResourceLocation spellId) {
        this.pos = pos;
        this.spellId = spellId;
    }

    public static void encode(LearnSpellPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeResourceLocation(packet.spellId);
    }

    public static LearnSpellPacket decode(FriendlyByteBuf buffer) {
        return new LearnSpellPacket(buffer.readBlockPos(), buffer.readResourceLocation());
    }

    public static void handle(LearnSpellPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            Level level = player.level();
            BlockEntity blockEntity = level.getBlockEntity(packet.pos);

            AbstractSpell spell = SpellRegistry.getSpell(packet.spellId);
            if (blockEntity instanceof MagicLecternTile magicTableTile && spell != null) {
                magicTableTile.removeSpellTablets(
                        SpellLearningHelper.isEldritchSpell(spell),
                        Configuration.getSpellScrollCost(packet.spellId)
                );
            }
        });
        context.get().setPacketHandled(true);
    }
}
