package org.holy.unraveling_spells.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.holy.unraveling_spells.block.magic_lectern.MagicLecternTile;

import javax.annotation.Nullable;

public class MagicLecternBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING;
    public static final VoxelShape SHAPE_BASE;
    public static final VoxelShape SHAPE_POST;
    public static final VoxelShape SHAPE_COMMON;
    public static final VoxelShape SHAPE_TOP_PLATE;
    public static final VoxelShape SHAPE_COLLISION;
    public static final VoxelShape SHAPE_WEST;
    public static final VoxelShape SHAPE_NORTH;
    public static final VoxelShape SHAPE_EAST;
    public static final VoxelShape SHAPE_SOUTH;

    public MagicLecternBlock() {
        super(Properties.of().strength(2.5F).sound(SoundType.WOOD).noOcclusion());
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH))));
    }

    static {
        FACING = HorizontalDirectionalBlock.FACING;
        SHAPE_BASE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
        SHAPE_POST = Block.box(4.0, 2.0, 4.0, 12.0, 14.0, 12.0);
        SHAPE_COMMON = Shapes.or(SHAPE_BASE, SHAPE_POST);
        SHAPE_TOP_PLATE = Block.box(0.0, 15.0, 0.0, 16.0, 15.0, 16.0);
        SHAPE_COLLISION = Shapes.or(SHAPE_COMMON, SHAPE_TOP_PLATE);
        SHAPE_WEST = Shapes.or(Block.box(1.0, 10.0, 0.0, 5.333333, 14.0, 16.0), new VoxelShape[]{Block.box(5.333333, 12.0, 0.0, 9.666667, 16.0, 16.0), Block.box(9.666667, 14.0, 0.0, 14.0, 18.0, 16.0), SHAPE_COMMON});
        SHAPE_NORTH = Shapes.or(Block.box(0.0, 10.0, 1.0, 16.0, 14.0, 5.333333), new VoxelShape[]{Block.box(0.0, 12.0, 5.333333, 16.0, 16.0, 9.666667), Block.box(0.0, 14.0, 9.666667, 16.0, 18.0, 14.0), SHAPE_COMMON});
        SHAPE_EAST = Shapes.or(Block.box(10.666667, 10.0, 0.0, 15.0, 14.0, 16.0), new VoxelShape[]{Block.box(6.333333, 12.0, 0.0, 10.666667, 16.0, 16.0), Block.box(2.0, 14.0, 0.0, 6.333333, 18.0, 16.0), SHAPE_COMMON});
        SHAPE_SOUTH = Shapes.or(Block.box(0.0, 10.0, 10.666667, 16.0, 14.0, 15.0), new VoxelShape[]{Block.box(0.0, 12.0, 6.333333, 16.0, 16.0, 10.666667), Block.box(0.0, 14.0, 2.0, 16.0, 18.0, 6.333333), SHAPE_COMMON});
    }

    public BlockState getStateForPlacement(BlockPlaceContext placeContext) {
        return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, placeContext.getHorizontalDirection().getOpposite()));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_54543_) {
        p_54543_.add(FACING);
    }

    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    public VoxelShape getOcclusionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return SHAPE_COMMON;
    }

    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    public VoxelShape getCollisionShape(BlockState p_54577_, BlockGetter p_54578_, BlockPos p_54579_, CollisionContext p_54580_) {
        return SHAPE_COLLISION;
    }

    public VoxelShape getShape(BlockState p_54561_, BlockGetter p_54562_, BlockPos p_54563_, CollisionContext p_54564_) {
        switch ((Direction)p_54561_.getValue(FACING)) {
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
            default:
                return SHAPE_COMMON;
        }
    }

    public BlockState rotate(BlockState p_54540_, Rotation p_54541_) {
        return (BlockState)p_54540_.setValue(FACING, p_54541_.rotate((Direction)p_54540_.getValue(FACING)));
    }

    public BlockState mirror(BlockState p_54537_, Mirror p_54538_) {
        return p_54537_.rotate(p_54538_.getRotation((Direction)p_54537_.getValue(FACING)));
    }

    @Override
    public InteractionResult use(BlockState state, Level pLevel, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pos);

            if (entity instanceof MagicLecternTile) {
                NetworkHooks.openScreen(((ServerPlayer) player), (MagicLecternTile) entity, pos);
            } else {
                throw new IllegalStateException("Container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);

            if (blockEntity instanceof MagicLecternTile) {
                ((MagicLecternTile) blockEntity).drops();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MagicLecternTile(pos, state);
    }
}
