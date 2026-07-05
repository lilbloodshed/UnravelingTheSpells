package org.holy.unraveling_spells.registries;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.block.MagicLecternBlock;
import org.holy.unraveling_spells.block.magic_lectern.MagicLecternTile;

public class BlockRegistry {
    static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Unraveling_spells.MODID);
    static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Unraveling_spells.MODID);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }

    public static final RegistryObject<Block> MAGIC_LECTERN_BLOCK = BLOCKS.register("magic_lectern", MagicLecternBlock::new);

    public static final RegistryObject<BlockEntityType<MagicLecternTile>> MAGIC_TABLE_TILE = BLOCK_ENTITIES.register("magic_lectern",
            () -> BlockEntityType.Builder.of(MagicLecternTile::new, MAGIC_LECTERN_BLOCK.get()).build(null));
}
