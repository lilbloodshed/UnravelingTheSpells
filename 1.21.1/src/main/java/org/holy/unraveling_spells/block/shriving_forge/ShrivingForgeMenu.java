package org.holy.unraveling_spells.block.shriving_forge;

import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.holy.unraveling_spells.registries.utsMenuRegistry;
import org.jetbrains.annotations.NotNull;

import static org.holy.unraveling_spells.registries.utsBlockRegistry.SHRIVING_FORGE_BLOCK;

public class ShrivingForgeMenu extends AbstractContainerMenu {
    public final ShrivingForgeTile blockEntity;
    private final ContainerData data;
    private final Level level;
    private final Slot resultSlot;

    public ShrivingForgeMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, inventory, requireForge(inventory, buffer), new SimpleContainerData(2));
    }

    public ShrivingForgeMenu(int containerId, Inventory inventory, ShrivingForgeTile blockEntity,
                             ContainerData data) {
        super(utsMenuRegistry.SHRIVING_FORGE_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;
        this.level = inventory.player.level();
        addDataSlots(data);

        addPlayerHotbar(inventory);
        addPlayerInventory(inventory);

        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 62, 30) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ItemRegistry.SCROLL.get());
            }
        });
        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 1, 98, 30) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ItemRegistry.SHRIVING_STONE.get());
            }
        });
        resultSlot = addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 2, 80, 50) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
    }

    private static ShrivingForgeTile requireForge(Inventory inventory, RegistryFriendlyByteBuf buffer) {
        if (inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof ShrivingForgeTile forge) {
            return forge;
        }
        throw new IllegalStateException("Shriving forge block entity is missing");
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public float getProgress() {
        int maxProgress = data.get(1);
        return maxProgress > 0 ? Mth.clamp(data.get(0) / (float) maxProgress, 0.0F, 1.0F) : 0.0F;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copy = sourceStack.copy();
        int playerSlotEnd = 36;
        if (index < playerSlotEnd) {
            if (!moveItemStackTo(sourceStack, playerSlotEnd, playerSlotEnd + 2, false)) return ItemStack.EMPTY;
        } else if (index < playerSlotEnd + 3) {
            if (!moveItemStackTo(sourceStack, 0, playerSlotEnd, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) sourceSlot.set(ItemStack.EMPTY);
        else sourceSlot.setChanged();
        sourceSlot.onTake(player, sourceStack);
        return copy;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot != resultSlot && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, SHRIVING_FORGE_BLOCK.get());
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory inventory) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }
}
