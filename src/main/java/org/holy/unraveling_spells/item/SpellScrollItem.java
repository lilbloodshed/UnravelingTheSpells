package org.holy.unraveling_spells.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpellScrollItem extends Item {
    public SpellScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> list, TooltipFlag p_41424_) {
        list.add(Component.translatable("item.unraveling_spells.spell_scroll.description")
                .withStyle(ChatFormatting.GRAY));
        list.add(Component.translatable("item.unraveling_spells.spell_scroll.description2")
                .withStyle(ChatFormatting.GRAY));
    }
}
