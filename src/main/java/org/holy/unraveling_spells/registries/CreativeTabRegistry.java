package org.holy.unraveling_spells.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.holy.unraveling_spells.Unraveling_spells;

@Mod.EventBusSubscriber(modid = Unraveling_spells.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreativeTabRegistry {
    static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Unraveling_spells.MODID);

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }

    public static final RegistryObject<CreativeModeTab> MOD_TAB = TABS.register("material_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + Unraveling_spells.MODID + ".material_tab"))
            .icon(() -> new ItemStack(ItemRegistry.SPELL_SCROLL.get()))
            .displayItems((enabledFeatures, entries) -> {
                entries.accept(ItemRegistry.MAGIC_LECTERN_ITEM.get());
                entries.accept(ItemRegistry.SPELL_SCROLL.get());
                entries.accept(ItemRegistry.OBLIVION_SCROLL.get());
            })
            .build());
}
