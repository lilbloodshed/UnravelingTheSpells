package org.holy.unraveling_spells.block.magic_lectern;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.holy.unraveling_spells.registries.utsItemRegistry;
import org.holy.unraveling_spells.registries.utsMenuRegistry;
import org.holy.unraveling_spells.network.ModMessages;
import org.holy.unraveling_spells.network.packet.LearnSpellPacket;

import static org.holy.unraveling_spells.registries.utsBlockRegistry.MAGIC_LECTERN_BLOCK;

public class MagicLecternMenu extends AbstractContainerMenu {
    public final MagicLecternTile blockEntity;

    public MagicLecternMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(containerId, inventory, requireLectern(inventory, data));
    }

    public MagicLecternMenu(int containerId, Inventory inventory, MagicLecternTile blockEntity) {
        super(utsMenuRegistry.MAGIC_LECTERN_MENU.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(new SlotItemHandler(blockEntity.getItemHandler(), 0, 15, 142) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(utsItemRegistry.SPELL_SCROLL.get());
            }

            @Override
            public boolean isActive() {
                return false;
            }
        });
    }

    private static MagicLecternTile requireLectern(Inventory inventory, RegistryFriendlyByteBuf data) {
        if (inventory.player.level().getBlockEntity(data.readBlockPos()) instanceof MagicLecternTile lectern) {
            return lectern;
        }
        throw new IllegalStateException("Magic lectern block entity is missing");
    }

    public void tableSlotChange(ResourceLocation spellId, boolean eldritch, int amount) {
        boolean consumed = eldritch
                ? blockEntity.consumeEldritchManuscripts(amount)
                : blockEntity.consumeSpellScrolls(amount);
        if (consumed) {
            blockEntity.getLevel().playSound(
                    null,
                    blockEntity.getBlockPos(),
                    SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
                    SoundSource.BLOCKS,
                    0.8F,
                    1.1F);
            ModMessages.sendToServer(new LearnSpellPacket(blockEntity.getBlockPos(), spellId));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player,
                MAGIC_LECTERN_BLOCK.get());
    }
}
