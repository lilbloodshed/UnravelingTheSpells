package org.holy.unraveling_spells.registries;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.block.MagicLecternBlock;
import org.holy.unraveling_spells.block.magic_lectern.MagicLecternTile;

import static net.minecraft.world.level.block.state.BlockBehaviour.simpleCodec;

public class utsBlockRegistry {
    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Unraveling_spells.MODID);
    static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Unraveling_spells.MODID);

    public static final DeferredBlock<MagicLecternBlock> MAGIC_LECTERN_BLOCK = BLOCKS.register(
            "magic_lectern",
            () -> new MagicLecternBlock(BlockBehaviour.Properties.of()
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MagicLecternTile>> MAGIC_LECTERN_TILE =
            BLOCK_ENTITIES.register("magic_lectern", () ->
                    BlockEntityType.Builder.of(MagicLecternTile::new, MAGIC_LECTERN_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        REGISTRAR.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        eventBus.addListener(utsBlockRegistry::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                MAGIC_LECTERN_TILE.get(),
                (lectern, side) -> lectern.getItemHandler());
    }

    public static final DeferredRegister<MapCodec<? extends Block>> REGISTRAR = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, Unraveling_spells.MODID);

    public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<MagicLecternBlock>> SIMPLE_CODEC = REGISTRAR.register(
            "simple",
            () -> simpleCodec(MagicLecternBlock::new)
    );
}
