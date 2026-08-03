package org.holy.unraveling_spells.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import org.holy.unraveling_spells.capability.school.PlayerSchool;
import org.holy.unraveling_spells.capability.school.PlayerSchoolProvider;
import org.holy.unraveling_spells.capability.spell.PlayerSpell;
import org.holy.unraveling_spells.capability.spell.PlayerSpellProvider;
import org.holy.unraveling_spells.network.ModMessages;
import org.holy.unraveling_spells.network.packet.SchoolS2CPacket;
import org.holy.unraveling_spells.network.packet.SpellS2CPacket;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OblivionScrollItem extends Item {
    public OblivionScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }

        if (player.isShiftKeyDown()) {
            boolean succesfulClear = false;

            if (player instanceof ServerPlayer serverPlayer) {
                LazyOptional<PlayerSchool> capSchool = serverPlayer.getCapability(PlayerSchoolProvider.PLAYER_SCHOOL);
                LazyOptional<PlayerSpell> capSpell = serverPlayer.getCapability(PlayerSpellProvider.PLAYER_SPELL);

                PlayerSchool schoolData = capSchool.orElse(null);
                PlayerSpell spellData = capSpell.orElse(null);

                if (schoolData == null || spellData == null) {
                    player.sendSystemMessage(Component.literal("Error data"));
                }

                schoolData.getSchools().clear();
                spellData.getSpells().clear();

                ModMessages.sendToPlayer(
                        new SchoolS2CPacket(new ArrayList<>(schoolData.getSchools())),
                        serverPlayer
                );
                ModMessages.sendToPlayer(
                        new SpellS2CPacket(new ArrayList<>(spellData.getSpells())),
                        serverPlayer
                );

                succesfulClear = true;
            }

            if (succesfulClear) {
                player.sendSystemMessage(Component.translatable("item.unraveling_spells.oblivion_scroll.use"));
                stack.shrink(1);
                level.playSound(null, player.blockPosition(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 1f, 1.1f);
            }
        } else {
            player.sendSystemMessage(Component.translatable("item.unraveling_spells.oblivion_scroll.confirm").withStyle(ChatFormatting.RED));
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> list, TooltipFlag p_41424_) {
        list.add(Component.translatable("item.unraveling_spells.oblivion_scroll.description")
                .withStyle(ChatFormatting.GRAY));
    }
}
