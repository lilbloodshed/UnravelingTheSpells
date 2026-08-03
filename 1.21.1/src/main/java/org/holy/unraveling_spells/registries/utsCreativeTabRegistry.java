package org.holy.unraveling_spells.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.holy.unraveling_spells.Unraveling_spells;

import java.util.function.Supplier;

public class utsCreativeTabRegistry {
    static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Unraveling_spells.MODID);

    public static final Supplier<CreativeModeTab> MOD_TAB = TABS.register("material_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + Unraveling_spells.MODID + ".material_tab"))
            .icon(() -> new ItemStack(utsItemRegistry.SPELL_SCROLL.get()))
            .displayItems((enabledFeatures, entries) -> {
                entries.accept(utsItemRegistry.MAGIC_LECTERN_ITEM.get());
                entries.accept(utsItemRegistry.SPELL_SCROLL.get());
                entries.accept(utsItemRegistry.OBLIVION_SCROLL.get());
            })
            .build()
    );

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
