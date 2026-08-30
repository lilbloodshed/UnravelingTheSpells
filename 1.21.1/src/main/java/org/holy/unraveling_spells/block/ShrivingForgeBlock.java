package org.holy.unraveling_spells.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.holy.unraveling_spells.block.shriving_forge.ShrivingForgeTile;
import org.holy.unraveling_spells.registries.utsBlockRegistry;
import org.jetbrains.annotations.Nullable;

import static org.holy.unraveling_spells.registries.utsBlockRegistry.SHRIVINGFORGE_CODEC;

public class ShrivingForgeBlock  extends BaseEntityBlock {
    public static final VoxelShape SHAPE_TABLETOP;
    public static final VoxelShape SHAPE_LEG_1;
    public static final VoxelShape SHAPE_LEG_2;
    public static final VoxelShape SHAPE;
    public ShrivingForgeBlock(Properties properties) {
        super(properties);
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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return SHRIVINGFORGE_CODEC.get();
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        openMenu(level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level,
                                              BlockPos pos, Player player,
                                              net.minecraft.world.InteractionHand hand,
                                              BlockHitResult hitResult) {
        openMenu(level, pos, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void openMenu(Level level, BlockPos pos, Player player) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof ShrivingForgeTile blockentity)) {
            return;
        }

        if (!level.isClientSide) {
            player.openMenu(blockentity, buffer -> buffer.writeBlockPos(pos));
        }
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
