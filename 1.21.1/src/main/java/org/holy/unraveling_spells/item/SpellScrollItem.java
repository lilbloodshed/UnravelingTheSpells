package org.holy.unraveling_spells.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SpellScrollItem extends Item {
    public SpellScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.unraveling_spells.spell_scroll.description")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.unraveling_spells.spell_scroll.description2")
                .withStyle(ChatFormatting.GRAY));
    }
}
