package org.holy.unraveling_spells.block.shriving_forge;

import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.holy.unraveling_spells.registries.utsBlockRegistry;
import org.holy.unraveling_spells.registries.utsItemRegistry;
import org.jetbrains.annotations.Nullable;

public class ShrivingForgeTile extends BlockEntity implements MenuProvider {
    private static final int TOTAL_STORAGE_SLOTS = 128;

    protected final ContainerData data;
    ShrivingForgeMenu menu;
    private int progress = 0;
    private int maxProgress = 200;

    public ShrivingForgeTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(utsBlockRegistry.SHRIVING_FORGE_TILE.get(), pWorldPosition, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> ShrivingForgeTile.this.progress;
                    case 1 -> ShrivingForgeTile.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int v) {
                switch (i) {
                    case 0 -> ShrivingForgeTile.this.progress = v;
                    case 1 -> ShrivingForgeTile.this.maxProgress = v;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_STORAGE_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            updateMenuSlots(slot);
            setChanged();
        }
    };

    private void updateMenuSlots(int slot) {
        if (level == null) return;

        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if(canStart()) {
            increaseCraftingProgress();
            setChanged(level, pos, state);

            if (hasProgressFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    private void resetProgress() {
        progress = 0;
    }

    private boolean canStart() {
        boolean hasCraftingSlot1 = this.itemHandler.getStackInSlot(0).getItem().equals(ItemRegistry.SCROLL.get());
        boolean hasCraftingSlot2 = this.itemHandler.getStackInSlot(1).getItem().equals(ItemRegistry.SHRIVING_STONE.get());
        ItemStack result = new ItemStack(utsItemRegistry.SPELL_SCROLL.get());

        return hasCraftingSlot1 && hasCraftingSlot2 && canInsertAmountIntoResultSlot(result.getCount()) && canInsertItemIntoResultSlot(result.getItem());
    }

    private boolean canInsertItemIntoResultSlot(Item item) {
        return this.itemHandler.getStackInSlot(2).isEmpty() || this.itemHandler.getStackInSlot(2).is(item);
    }

    private boolean canInsertAmountIntoResultSlot(int count) {
        return this.itemHandler.getStackInSlot(2).getCount() + count <= this.itemHandler.getStackInSlot(2).getMaxStackSize();
    }

    private void craftItem() {
        ItemStack result = new ItemStack(utsItemRegistry.SPELL_SCROLL.get());
        this.itemHandler.extractItem(0, 1, false);
        this.itemHandler.extractItem(1, 1, false);

        this.itemHandler.setStackInSlot(2, new ItemStack(result.getItem(),
                this.itemHandler.getStackInSlot(2).getCount() + result.getCount()));
    }

    private boolean hasProgressFinished() {
        return progress >= maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        menu = new ShrivingForgeMenu(containerId, inventory, this, data);
        return menu;
    }

    public void drops() {
        SimpleContainer simpleContainer = new SimpleContainer(itemHandler.getSlots());

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            simpleContainer.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, simpleContainer);
    }

    public ItemStack getRenderScroll() {
        return itemHandler.getStackInSlot(0);
    }

    public ItemStack getRenderStone() {
        return itemHandler.getStackInSlot(1);
    }

    public ItemStack getRenderResult() {
        return itemHandler.getStackInSlot(2);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        if (nbt.contains("inventory", Tag.TAG_COMPOUND)) {
            CompoundTag inventory = nbt.getCompound("inventory").copy();
            inventory.putInt("Size", TOTAL_STORAGE_SLOTS);
            itemHandler.deserializeNBT(registries, inventory);
        }
        progress = nbt.getInt("shriving_forge.progress");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("shriving_forge.progress", progress);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("");
    }
}
