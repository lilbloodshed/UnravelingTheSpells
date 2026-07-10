package io.redspace.ironsspellbooks.unravelingspellmixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.holy.unraveling_spells.capability.SpellLearningHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSpell.class)
public abstract class AbstractSpellMixin {
    @Inject(method = "isLearned", at = @At("HEAD"), cancellable = true, remap = false)
    private void unraveling_spells$isLearned(Player player, CallbackInfoReturnable<Boolean> cir) {
        AbstractSpell spell = (AbstractSpell) (Object) this;
        if (SpellLearningHelper.isEldritchSpell(spell)) {
            return;
        }
        cir.setReturnValue(SpellLearningHelper.isLearned(spell, player));
    }

    @Inject(method = "obfuscateStats", at = @At("HEAD"), cancellable = true, remap = false)
    private void unraveling_spells$obfuscateStats(@Nullable Player player, CallbackInfoReturnable<Boolean> cir) {
        AbstractSpell spell = (AbstractSpell) (Object) this;
        if (SpellLearningHelper.isEldritchSpell(spell)) {
            return;
        }
        cir.setReturnValue(SpellLearningHelper.shouldObfuscate(spell, player));
    }

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true, remap = false)
    private void unraveling_spells$getDisplayName(Player player, CallbackInfoReturnable<MutableComponent> cir) {
        AbstractSpell spell = (AbstractSpell) (Object) this;
        if (SpellLearningHelper.isEldritchSpell(spell)) {
            return;
        }
        cir.setReturnValue(SpellLearningHelper.displayName(spell, player));
    }

    @Inject(method = "canBeCastedBy", at = @At("HEAD"), cancellable = true, remap = false)
    private void unraveling_spells$canBeCastedBy(int spellLevel, CastSource castSource, MagicData playerMagicData, Player player, CallbackInfoReturnable<CastResult> cir) {
        AbstractSpell spell = (AbstractSpell) (Object) this;
        if (SpellLearningHelper.isEldritchSpell(spell)) {
            return;
        }
        if (!SpellLearningHelper.isLearned(spell, player)) {
            cir.setReturnValue(SpellLearningHelper.unlearnedCastFailure());
        }
    }
}
