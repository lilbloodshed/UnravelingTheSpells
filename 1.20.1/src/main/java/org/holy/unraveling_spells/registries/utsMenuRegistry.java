package org.holy.unraveling_spells.registries;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.block.magic_lectern.MagicLecternMenu;
import org.holy.unraveling_spells.block.shriving_forge.ShrivingForgeMenu;

public class utsMenuRegistry {
    static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Unraveling_spells.MODID);

    public static void register(IEventBus eventBus){
        MENUS.register(eventBus);
    }
    static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static final RegistryObject<MenuType<MagicLecternMenu>> MAGIC_TABLE_MENU = registerMenuType(MagicLecternMenu::new,"magic_table_menu");

    public static final RegistryObject<MenuType<ShrivingForgeMenu>> SHRIVING_FORGE_MENU = registerMenuType(ShrivingForgeMenu::new, "shriving_forge_menu");
}