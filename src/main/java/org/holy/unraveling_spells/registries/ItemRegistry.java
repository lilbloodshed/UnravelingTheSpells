package org.holy.unraveling_spells.registries;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.item.OblivionScrollItem;
import org.holy.unraveling_spells.item.SpellScrollItem;

public class ItemRegistry {
    static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Unraveling_spells.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static final RegistryObject<Item> SPELL_SCROLL = ITEMS.register("spell_scroll",
            () -> new SpellScrollItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));

    public static final RegistryObject<Item> OBLIVION_SCROLL = ITEMS.register("oblivion_scroll",
            () -> new OblivionScrollItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> MAGIC_LECTERN_ITEM = ITEMS.register("magic_lectern", () -> new BlockItem(BlockRegistry.MAGIC_LECTERN_BLOCK.get(), new Item.Properties()));
}
