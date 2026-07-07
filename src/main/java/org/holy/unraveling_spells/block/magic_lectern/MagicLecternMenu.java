package org.holy.unraveling_spells.block.magic_lectern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.holy.unraveling_spells.client.screens.LearningScreen;
import org.holy.unraveling_spells.network.ModMessages;
import org.holy.unraveling_spells.network.packet.LearnSpellPacket;
import org.holy.unraveling_spells.registries.ItemRegistry;
import org.holy.unraveling_spells.registries.MenuRegistry;

import static org.holy.unraveling_spells.registries.BlockRegistry.MAGIC_LECTERN_BLOCK;

/**
 * Many parts of the code are taken from Iron's Spellbooks mod by Iron431
 * (https://github.com/iron431/irons-spells-n-spellbooks)
 */
public class MagicLecternMenu extends AbstractContainerMenu {
    public final MagicLecternTile blockEntity;
    final Level level;
    final Slot tableSlot;
    //final Slot slot1;
    //final Slot slot2;
    //final Slot slot3;
    public MagicLecternMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public MagicLecternMenu(int containerId, Inventory inv, BlockEntity entity) {
        super(MenuRegistry.MAGIC_TABLE_MENU.get(), containerId);

        checkContainerSize(inv, 4);

        blockEntity = (MagicLecternTile) entity;
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        IItemHandler itemHandler = this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().get();

        tableSlot = new SlotItemHandler(itemHandler, 0, 15, 142) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ItemRegistry.SPELL_SCROLL.get());
            }

            @Override
            public boolean isActive() {
                Screen currentScreen = Minecraft.getInstance().screen;
                if (currentScreen instanceof LearningScreen branchScreen) {
                    if (branchScreen.getPage() == 2) {
                        return true;
                    }
                }
                return false;
            }
        };

        /*
        slot1 = new SlotItemHandler(itemHandler, 1, 20, 80) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ItemRegistry.SPELL_SCROLL.get());
            }

            @Override
            public boolean isActive() {
                Screen currentScreen = Minecraft.getInstance().screen;
                if (currentScreen instanceof RuneTableScreen runeScreen) {
                    return true;
                }
                return false;
            }
        };
        slot2 = new SlotItemHandler(itemHandler, 2, 50, 80) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ItemRegistry.SPELL_SCROLL.get());
            }

            @Override
            public boolean isActive() {
                Screen currentScreen = Minecraft.getInstance().screen;
                if (currentScreen instanceof RuneTableScreen runeScreen) {
                    return true;
                }
                return false;
            }
        };
        slot3 = new SlotItemHandler(itemHandler, 3, 70, 80) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ItemRegistry.SPELL_SCROLL.get());
            }

            @Override
            public boolean isActive() {
                Screen currentScreen = Minecraft.getInstance().screen;
                if (currentScreen instanceof RuneTableScreen runeScreen) {
                    return true;
                }
                return false;
            }
        };

         */

        this.addSlot(tableSlot);
        //this.addSlot(slot1);
        //this.addSlot(slot2);
        //this.addSlot(slot3);
    }

    public void tableSlotChange() {
        ItemStack currentStack = tableSlot.getItem();
        if (!currentStack.isEmpty()) {
            currentStack.shrink(1);
            tableSlot.set(currentStack);

            level.playSound(null, blockEntity.getBlockPos(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, .8f, 1.1f);
            ModMessages.sendToServer(new LearnSpellPacket(blockEntity.getBlockPos()));
        }
    }

    public ItemStack getTableSlotItem() {
        return tableSlot.getItem();
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 9;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 3;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 1;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                pPlayer, MAGIC_LECTERN_BLOCK.get());
    }
    private void addPlayerInventory(Inventory playerInventory) {
        final int ROWS = 9;
        final int COLUMNS = 3;
        final int X_OFFSET = -108;
        final int Y_OFFSET = 7-4;
        final int SLOT_SIZE = 18;

        for (int row = 0; row < ROWS; ++row) {
            for (int column = 0; column < COLUMNS; ++column) {
                int slotIndex = column + row * COLUMNS + 9;
                int x = X_OFFSET + column * SLOT_SIZE;
                int y = Y_OFFSET + row * SLOT_SIZE;

                this.addSlot(new Slot(playerInventory, slotIndex, x, y));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 173));
        }
    }
}
