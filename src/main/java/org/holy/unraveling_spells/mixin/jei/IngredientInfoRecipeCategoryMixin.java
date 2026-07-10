package org.holy.unraveling_spells.mixin.jei;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.capability.SpellLearningHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "mezz.jei.library.plugins.jei.info.IngredientInfoRecipeCategory", remap = false)
public abstract class IngredientInfoRecipeCategoryMixin {
    @Inject(method = "draw(Lmezz/jei/api/recipe/vanilla/IJeiIngredientInfoRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void unraveling_spells$hideUnlearnedScrollInfo(IJeiIngredientInfoRecipe recipe, IRecipeSlotsView recipeSlotsView,
                                                           GuiGraphics guiGraphics, double mouseX, double mouseY,
                                                           CallbackInfo ci) {
        if (Configuration.SHOW_JEI_GUIDE_SPELLS.get()) {
            return;
        }

        AbstractSpell spell = unraveling_spells$getScrollSpell(recipe);
        if (spell == null || SpellLearningHelper.isEldritchSpell(spell)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!SpellLearningHelper.shouldObfuscate(spell, minecraft.player)) {
            return;
        }

        int y = 22;
        int lineCount = Math.max(1, recipe.getDescription().size());
        Component hiddenLine = Component.translatable(String.format("%s.guide", spell.getComponentId()))
                .withStyle(SpellLearningHelper.OBFUSCATED_STYLE);

        for (int i = 0; i < lineCount && y <= 116; i++) {
            guiGraphics.drawString(
                    minecraft.font,
                    Language.getInstance().getVisualOrder(hiddenLine),
                    0,
                    y,
                    0xFF000000,
                    false
            );
            y += minecraft.font.lineHeight + 2;
        }
        ci.cancel();
    }

    private static AbstractSpell unraveling_spells$getScrollSpell(IJeiIngredientInfoRecipe recipe) {
        for (ITypedIngredient<?> typedIngredient : recipe.getIngredients()) {
            Object ingredient = typedIngredient.getIngredient();
            if (!(ingredient instanceof ItemStack stack) || stack.getItem() != io.redspace.ironsspellbooks.registries.ItemRegistry.SCROLL.get()) {
                continue;
            }

            ISpellContainer container = ISpellContainer.get(stack);
            if (container == null || container.isEmpty()) {
                continue;
            }

            SpellData spellData = container.getSpellAtIndex(0);
            if (spellData != null && spellData != SpellData.EMPTY) {
                return spellData.getSpell();
            }
        }
        return null;
    }
}
