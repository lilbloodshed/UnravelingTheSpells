package org.holy.unraveling_spells.mixin.jei;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.holy.unraveling_spells.capability.SpellLearningHelper;
import org.holy.unraveling_spells.config.ClientConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Pseudo
@Mixin(targets = "mezz.jei.library.plugins.jei.info.IngredientInfoRecipeCategory", remap = false)
public abstract class IngredientInfoRecipeCategoryMixin {
    @Inject(method = "createRecipeExtras(Lmezz/jei/api/gui/widgets/IRecipeExtrasBuilder;Lmezz/jei/api/recipe/vanilla/IJeiIngredientInfoRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void unraveling_spells$hideUnlearnedScrollInfo(@Coerce Object builder, @Coerce Object recipe,
                                                           @Coerce Object focuses, CallbackInfo ci) {
        if (ClientConfiguration.SHOW_JEI_GUIDE_SPELLS.get()) {
            return;
        }

        AbstractSpell spell = unraveling_spells$getScrollSpell(recipe);
        if (spell == null || !SpellLearningHelper.usesCustomLearning(spell)
                || !SpellLearningHelper.shouldObfuscate(spell, Minecraft.getInstance().player)) {
            return;
        }

        Object scrollBox = unraveling_spells$invoke(builder, "addScrollBoxWidget", 170, 103, 0, 22);
        if (scrollBox != null) {
            unraveling_spells$invoke(scrollBox, "setContents",
                    List.of(Component.translatable("spell.unraveling_spells.not_learned")));
            ci.cancel();
        }
    }

    private static AbstractSpell unraveling_spells$getScrollSpell(Object recipe) {
        Object recipeIngredients = unraveling_spells$invoke(recipe, "getIngredients");
        if (!(recipeIngredients instanceof Iterable<?> ingredients)) {
            return null;
        }

        for (Object typedIngredient : ingredients) {
            Object ingredient = unraveling_spells$invoke(typedIngredient, "getIngredient");
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

    private static Object unraveling_spells$invoke(Object target, String methodName, Object... arguments) {
        if (target == null) {
            return null;
        }

        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != arguments.length) {
                continue;
            }
            try {
                if (!method.canAccess(target)) {
                    method.trySetAccessible();
                }
                return method.invoke(target, arguments);
            } catch (IllegalArgumentException ignored) {
                // A same-name overload with incompatible parameters; try the next one.
            } catch (IllegalAccessException | InvocationTargetException exception) {
                return null;
            }
        }
        return null;
    }
}
