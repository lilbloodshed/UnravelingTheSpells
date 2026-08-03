package org.holy.unraveling_spells.registries;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.item.OblivionScrollItem;
import org.holy.unraveling_spells.item.SpellScrollItem;

import java.util.function.Supplier;

public class utsItemRegistry {
    static final DeferredRegister.Items ITEMS =  DeferredRegister.createItems(Unraveling_spells.MODID);

    public static final Supplier<Item> SPELL_SCROLL = ITEMS.registerItem("spell_scroll",
            SpellScrollItem::new,
            new Item.Properties().stacksTo(16).rarity(Rarity.RARE));

    public static final Supplier<Item> OBLIVION_SCROLL =  ITEMS.registerItem("oblivion_scroll",
            OblivionScrollItem::new,
            new Item.Properties().stacksTo(16).rarity(Rarity.EPIC));

    public static final DeferredItem<BlockItem> MAGIC_LECTERN_ITEM = ITEMS.registerSimpleBlockItem(
            "magic_lectern",
            utsBlockRegistry.MAGIC_LECTERN_BLOCK,
            new Item.Properties()
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
