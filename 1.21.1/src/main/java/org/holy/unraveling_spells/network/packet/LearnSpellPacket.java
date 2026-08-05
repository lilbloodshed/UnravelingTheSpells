package org.holy.unraveling_spells.network.packet;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.block.magic_lectern.MagicLecternTile;
import org.holy.unraveling_spells.capability.SpellLearningHelper;
import org.holy.unraveling_spells.config.Configuration;

public record LearnSpellPacket(BlockPos pos, ResourceLocation spellId) implements CustomPacketPayload {
    public static final Type<LearnSpellPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "learn_spell"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LearnSpellPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    LearnSpellPacket::pos,
                    ResourceLocation.STREAM_CODEC,
                    LearnSpellPacket::spellId,
                    LearnSpellPacket::new);

    public static void handle(LearnSpellPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(packet.pos());
        AbstractSpell spell = SpellRegistry.getSpell(packet.spellId());
        if (blockEntity instanceof MagicLecternTile lectern && spell != null) {
            lectern.removeSpellTablets(
                    SpellLearningHelper.isEldritchSpell(spell),
                    Configuration.getSpellScrollCost(packet.spellId()));
        }
    }

    @Override
    public Type<LearnSpellPacket> type() {
        return TYPE;
    }
}
