package org.holy.unraveling_spells.block.magic_lectern;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.registries.utsBlockRegistry;
import org.holy.unraveling_spells.registries.utsItemRegistry;
import org.jetbrains.annotations.Nullable;

public class MagicLecternTile extends BlockEntity implements MenuProvider {
    public static final int MAX_STORED_SCROLLS = 64;
    public static final int MAX_STORED_ELDRITCH_MANUSCRIPTS = 64;
    private static final int STORAGE_SLOTS_PER_ITEM = 4;
    private static final int SCROLL_STORAGE_START = 0;
    private static final int ELDRITCH_STORAGE_START =
            SCROLL_STORAGE_START + STORAGE_SLOTS_PER_ITEM;
    private static final int TOTAL_STORAGE_SLOTS = STORAGE_SLOTS_PER_ITEM * 2;
    private static final int ITEMS_PER_SLOT = MAX_STORED_SCROLLS / STORAGE_SLOTS_PER_ITEM;

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_STORAGE_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            updateMenuSlots(slot);
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return ITEMS_PER_SLOT;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot >= SCROLL_STORAGE_START
                    && slot < SCROLL_STORAGE_START + STORAGE_SLOTS_PER_ITEM) {
                return stack.is(utsItemRegistry.SPELL_SCROLL.get());
            }
            return slot >= ELDRITCH_STORAGE_START
                    && slot < ELDRITCH_STORAGE_START + STORAGE_SLOTS_PER_ITEM
                    && Configuration.isEldritchSchoolLearningEnabled()
                    && stack.is(ItemRegistry.ELDRITCH_PAGE.get());
        }
    };

    public MagicLecternTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(utsBlockRegistry.MAGIC_LECTERN_TILE.get(), pWorldPosition, pBlockState);
    }

    private void updateMenuSlots(int slot) {
        if (level == null) return;

        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    public ItemStack getStackInSlot(int slot) {
        return itemHandler.getStackInSlot(slot);
    }

    public int getStoredScrollCount() {
        return getStoredItemCount(
                utsItemRegistry.SPELL_SCROLL.get(),
                SCROLL_STORAGE_START,
                MAX_STORED_SCROLLS);
    }

    public int getStoredEldritchManuscriptCount() {
        return getStoredItemCount(
                io.redspace.ironsspellbooks.registries.ItemRegistry.ELDRITCH_PAGE.get(),
                ELDRITCH_STORAGE_START,
                MAX_STORED_ELDRITCH_MANUSCRIPTS);
    }

    public int insertSpellScrolls(ItemStack source) {
        return insertStoredItems(
                source,
                utsItemRegistry.SPELL_SCROLL.get(),
                SCROLL_STORAGE_START,
                getStoredScrollCount(),
                MAX_STORED_SCROLLS);
    }

    public int insertEldritchManuscripts(ItemStack source) {
        if (!Configuration.isEldritchSchoolLearningEnabled()) {
            return 0;
        }
        return insertStoredItems(
                source,
                io.redspace.ironsspellbooks.registries.ItemRegistry.ELDRITCH_PAGE.get(),
                ELDRITCH_STORAGE_START,
                getStoredEldritchManuscriptCount(),
                MAX_STORED_ELDRITCH_MANUSCRIPTS);
    }

    public ItemStack extractSpellScrolls(int amount) {
        return extractStoredItems(
                utsItemRegistry.SPELL_SCROLL.get(),
                SCROLL_STORAGE_START,
                amount,
                getStoredScrollCount());
    }

    public ItemStack extractEldritchManuscripts(int amount) {
        return extractStoredItems(
                io.redspace.ironsspellbooks.registries.ItemRegistry.ELDRITCH_PAGE.get(),
                ELDRITCH_STORAGE_START,
                amount,
                getStoredEldritchManuscriptCount());
    }

    private int getStoredItemCount(net.minecraft.world.item.Item item, int firstSlot, int maximum) {
        int count = 0;
        int lastSlot = firstSlot + STORAGE_SLOTS_PER_ITEM;
        for (int slot = firstSlot; slot < lastSlot; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return Math.min(count, maximum);
    }

    private int insertStoredItems(ItemStack source, net.minecraft.world.item.Item item,
                                  int firstSlot, int storedCount, int maximum) {
        if (source.isEmpty() || !source.is(item)) {
            return 0;
        }

        int insertable = Math.min(source.getCount(), maximum - storedCount);
        ItemStack remaining = source.copy();
        remaining.setCount(insertable);
        int lastSlot = firstSlot + STORAGE_SLOTS_PER_ITEM;

        for (int slot = firstSlot; slot < lastSlot && !remaining.isEmpty(); slot++) {
            remaining = itemHandler.insertItem(slot, remaining, false);
        }

        return insertable - remaining.getCount();
    }

    private ItemStack extractStoredItems(net.minecraft.world.item.Item item, int firstSlot,
                                         int amount, int storedCount) {
        int toExtract = Math.min(Math.max(amount, 0), storedCount);
        int extractedCount = 0;
        int lastSlot = firstSlot + STORAGE_SLOTS_PER_ITEM;

        for (int slot = firstSlot; slot < lastSlot && extractedCount < toExtract; slot++) {
            ItemStack extracted = itemHandler.extractItem(slot, toExtract - extractedCount, false);
            extractedCount += extracted.getCount();
        }

        return extractedCount == 0
                ? ItemStack.EMPTY
                : new ItemStack(item, extractedCount);
    }

    public ItemStack getFirstStoredScroll() {
        for (int slot = SCROLL_STORAGE_START;
             slot < SCROLL_STORAGE_START + STORAGE_SLOTS_PER_ITEM;
             slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    public boolean consumeSpellScrolls(int amount) {
        int required = Math.max(1, amount);
        return getStoredScrollCount() >= required
                && extractSpellScrolls(required).getCount() == required;
    }

    public boolean consumeEldritchManuscripts(int amount) {
        int required = Math.max(1, amount);
        return getStoredEldritchManuscriptCount() >= required
                && extractEldritchManuscripts(required).getCount() == required;
    }

    public void removeSpellTablets(boolean eldritch, int amount) {
        boolean consumed = eldritch
                ? consumeEldritchManuscripts(amount)
                : consumeSpellScrolls(amount);
        if (consumed) {
            setChanged();

            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
                setBlockDirty();
            }
        }
    }

    private void setBlockDirty() {
        if (level != null) {
            level.setBlock(getBlockPos(), getBlockState(), 3);
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MagicLecternMenu(containerId, inventory, this);
    }

    public void drops() {
        SimpleContainer simpleContainer = new SimpleContainer(itemHandler.getSlots());

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            simpleContainer.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, simpleContainer);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        if (nbt.contains("inventory", Tag.TAG_COMPOUND)) {
            CompoundTag inventory = nbt.getCompound("inventory").copy();
            inventory.putInt("Size", TOTAL_STORAGE_SLOTS);
            itemHandler.deserializeNBT(registries, inventory);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", itemHandler.serializeNBT(registries));
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
