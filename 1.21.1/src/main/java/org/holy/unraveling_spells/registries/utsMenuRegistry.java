package org.holy.unraveling_spells.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.block.magic_lectern.MagicLecternMenu;

public final class utsMenuRegistry {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Unraveling_spells.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MagicLecternMenu>> MAGIC_LECTERN_MENU =
            MENUS.register("magic_lectern", () -> IMenuTypeExtension.create(MagicLecternMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
