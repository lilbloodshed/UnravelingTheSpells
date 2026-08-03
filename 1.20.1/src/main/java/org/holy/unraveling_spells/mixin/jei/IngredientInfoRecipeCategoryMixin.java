package org.holy.unraveling_spells.mixin.jei;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.holy.unraveling_spells.config.ClientConfiguration;
import org.holy.unraveling_spells.capability.SpellLearningHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Pseudo
@Mixin(targets = "mezz.jei.library.plugins.jei.info.IngredientInfoRecipeCategory", remap = false)
public abstract class IngredientInfoRecipeCategoryMixin {
    @Inject(method = "draw(Lmezz/jei/api/recipe/vanilla/IJeiIngredientInfoRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void unraveling_spells$hideUnlearnedScrollInfo(@Coerce Object recipe, @Coerce Object recipeSlotsView,
                                                           GuiGraphics guiGraphics, double mouseX, double mouseY,
                                                           CallbackInfo ci) {
        if (ClientConfiguration.SHOW_JEI_GUIDE_SPELLS.get()) {
            return;
        }

        AbstractSpell spell = unraveling_spells$getScrollSpell(recipe);
        if (spell == null || !SpellLearningHelper.usesCustomLearning(spell)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!SpellLearningHelper.shouldObfuscate(spell, minecraft.player)) {
            return;
        }

        int y = 22;
        Component hiddenLine = Component.translatable("spell.unraveling_spells.not_learned");

        guiGraphics.drawString(
                minecraft.font,
                Language.getInstance().getVisualOrder(hiddenLine),
                0,
                y,
                0xFF000000,
                false
        );
        ci.cancel();
    }

    private static AbstractSpell unraveling_spells$getScrollSpell(Object recipe) {
        Object recipeIngredients = unraveling_spells$invokeNoArgs(recipe, "getIngredients");
        if (!(recipeIngredients instanceof Iterable<?> ingredients)) {
            return null;
        }

        for (Object typedIngredient : ingredients) {
            Object ingredient = unraveling_spells$invokeNoArgs(typedIngredient, "getIngredient");
            if (!(ingredient instanceof ItemStack stack) || stack.getItem() != ItemRegistry.SCROLL.get()) {
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

    private static Object unraveling_spells$invokeNoArgs(Object target, String methodName) {
        if (target == null) {
            return null;
        }

        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            return null;
        }
    }
}
