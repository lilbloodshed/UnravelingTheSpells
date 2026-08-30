package org.holy.unraveling_spells.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.holy.unraveling_spells.block.shriving_forge.ShrivingForgeTile;
import org.holy.unraveling_spells.registries.utsBlockRegistry;
import org.jetbrains.annotations.Nullable;

public class ShrivingForgeBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE_TABLETOP;
    public static final VoxelShape SHAPE_LEG_1;
    public static final VoxelShape SHAPE_LEG_2;
    public static final VoxelShape SHAPE;
    public ShrivingForgeBlock() {
        super(Properties.copy(Blocks.ENCHANTING_TABLE).sound(SoundType.NETHERITE_BLOCK).noOcclusion().requiresCorrectToolForDrops());
    }

    static {
        SHAPE_TABLETOP = Block.box(0.0, 11.0, 0.0, 16.0, 15.0, 16.0);
        SHAPE_LEG_1 = Block.box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0);
        SHAPE_LEG_2 = Block.box(4.0, 4.0, 4.0, 12.0, 11.0, 12.0);
        SHAPE = Shapes.or(SHAPE_LEG_1, new VoxelShape[]{SHAPE_LEG_2, SHAPE_TABLETOP});
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof ShrivingForgeTile blockentity)) {
            throw new IllegalStateException("Container provider is missing!");
        }

        if (!level.isClientSide()) {
            NetworkHooks.openScreen((ServerPlayer) player, blockentity, pos);
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ShrivingForgeTile(blockPos, blockState);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);

            if (blockEntity instanceof ShrivingForgeTile) {
                ((ShrivingForgeTile) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(entityType, utsBlockRegistry.SHRIVING_FORGE_TILE.get(),
                (level1, blockPos, blockState, tile) -> tile.tick(level1, blockPos, blockState));
    }
}
