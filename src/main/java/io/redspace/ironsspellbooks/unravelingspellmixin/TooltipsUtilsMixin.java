package io.redspace.ironsspellbooks.unravelingspellmixin;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.util.TooltipsUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import org.holy.unraveling_spells.capability.SpellLearningHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(TooltipsUtils.class)
public abstract class TooltipsUtilsMixin {
    @Inject(method = "createSpellDescriptionTooltip", at = @At("HEAD"), cancellable = true, remap = false)
    private static void unraveling_spells$createSpellDescriptionTooltip(AbstractSpell spell, Font font, CallbackInfoReturnable<List<FormattedCharSequence>> cir) {
        Player player = Minecraft.getInstance().player;
        if (SpellLearningHelper.isEldritchSpell(spell)) {
            return;
        }

        if (!SpellLearningHelper.shouldObfuscate(spell, player)) {
            return;
        }

        List<FormattedCharSequence> tooltip = new ArrayList<>();
        tooltip.add(FormattedCharSequence.forward(
                Component.translatable(spell.getComponentId()).getString(),
                SpellLearningHelper.OBFUSCATED_STYLE.withUnderlined(true)
        ));
        tooltip.addAll(font.split(
                Component.translatable(String.format("%s.guide", spell.getComponentId()))
                        .withStyle(SpellLearningHelper.OBFUSCATED_STYLE),
                180
        ));
        cir.setReturnValue(tooltip);
    }

    @Inject(method = "getStyleFor", at = @At("HEAD"), cancellable = true, remap = false)
    private static void unraveling_spells$getStyleFor(Player player, AbstractSpell spell, CallbackInfoReturnable<Style> cir) {
        if (SpellLearningHelper.isEldritchSpell(spell)) {
            return;
        }

        if (SpellLearningHelper.shouldObfuscate(spell, player)) {
            cir.setReturnValue(SpellLearningHelper.OBFUSCATED_STYLE);
        }
    }
}
