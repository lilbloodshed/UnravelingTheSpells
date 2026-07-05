package org.holy.unraveling_spells.mixin_support;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import org.holy.unraveling_spells.Configuration;
import org.holy.unraveling_spells.capability.spell.PlayerSpell;
import org.holy.unraveling_spells.capability.spell.PlayerSpellProvider;
import org.holy.unraveling_spells.config.SpellLearnedManager;
import org.jetbrains.annotations.Nullable;

public final class SpellLearningHelper {
    public static final Style OBFUSCATED_STYLE = Style.EMPTY
            .withObfuscated(true)
            .withFont(ResourceLocation.withDefaultNamespace("alt"));

    private SpellLearningHelper() {
    }

    public static boolean isLearned(AbstractSpell spell, @Nullable Player player) {
        if (spell == null) {
            return false;
        }

        if (isEldritchSpell(spell)) {
            return isIronSpellLearned(spell, player);
        }

        if (SpellLearnedManager.isSpellDefaultLearned(spell.getSpellResource())) {
            return true;
        }

        if (player == null) {
            return true;
        }

        LazyOptional<PlayerSpell> cap = player.getCapability(PlayerSpellProvider.PLAYER_SPELL);
        if (cap.isPresent()) {
            PlayerSpell spellData = cap.orElse(null);
            return spellData != null && spellData.isLearned(spell);
        }

        if (player.level().isClientSide) {
            return ClientMagicData.getSyncedSpellData(player).isSpellLearned(spell);
        }

        return MagicData.getPlayerMagicData(player).getSyncedData().isSpellLearned(spell);
    }

    public static boolean shouldObfuscate(AbstractSpell spell, @Nullable Player player) {
        if (Configuration.SHOW_SPELLS_NAME.get()) return false;
        return !isLearned(spell, player);
    }

    public static boolean isEldritchSpell(AbstractSpell spell) {
        return SchoolRegistry.ELDRITCH.get().equals(spell.getSchoolType());
    }

    public static MutableComponent displayName(AbstractSpell spell, @Nullable Player player) {
        Style style = shouldObfuscate(spell, player) ? OBFUSCATED_STYLE : Style.EMPTY;
        return Component.translatable(spell.getComponentId()).withStyle(style);
    }

    public static CastResult unlearnedCastFailure() {
        return new CastResult(
                CastResult.Type.FAILURE,
                Component.translatable("ui.irons_spellbooks.cast_error_unlearned").withStyle(ChatFormatting.RED)
        );
    }

    private static boolean isIronSpellLearned(AbstractSpell spell, @Nullable Player player) {
        if (player == null) {
            return false;
        }

        if (player.level().isClientSide) {
            return ClientMagicData.getSyncedSpellData(player).isSpellLearned(spell);
        }

        return MagicData.getPlayerMagicData(player).getSyncedData().isSpellLearned(spell);
    }
}
