package org.holy.unraveling_spells.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.registries.ItemRegistry;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class UnravelingJeiPlugin implements IModPlugin {
    private static final ResourceLocation PLUGIN_UID = ResourceLocation.fromNamespaceAndPath(Unraveling_spells.MODID, "jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        registration.addItemStackInfo(
                new ItemStack(ItemRegistry.MAGIC_LECTERN_ITEM.get()),
                Component.translatable("jei.unraveling_spells.magic_lectern.description")
        );
        registration.addItemStackInfo(
                new ItemStack(ItemRegistry.SPELL_SCROLL.get()),
                Component.translatable("jei.unraveling_spells.spell_scroll.description")
        );
        registration.addItemStackInfo(
                new ItemStack(ItemRegistry.OBLIVION_SCROLL.get()),
                Component.translatable("jei.unraveling_spells.oblivion_scroll.description")
        );
    }
}
