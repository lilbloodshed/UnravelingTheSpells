package org.holy.unraveling_spells.events;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.capability.PlayerSchoolProvider;
import org.holy.unraveling_spells.capability.PlayerSpellProvider;
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.network.ModMessages;
import org.holy.unraveling_spells.network.packet.CommonConfigS2CPacket;
import org.holy.unraveling_spells.network.packet.SchoolS2CPacket;
import org.holy.unraveling_spells.network.packet.SetTotalPlayerXPPacket;
import org.holy.unraveling_spells.network.packet.SpellS2CPacket;
import org.holy.unraveling_spells.registries.utsItemRegistry;

import java.util.ArrayList;

@EventBusSubscriber(modid = Unraveling_spells.MODID)
public final class ModEvents {
    private ModEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        syncPlayerKnowledge(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawned(PlayerEvent.PlayerRespawnEvent event) {
        syncPlayerKnowledge(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncPlayerKnowledge(event.getEntity());
    }

    public static void syncPlayerKnowledge(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ModMessages.sendToPlayer(CommonConfigS2CPacket.fromServerConfig(), serverPlayer);
        ModMessages.sendToPlayer(new SetTotalPlayerXPPacket(serverPlayer.totalExperience), serverPlayer);
        ModMessages.sendToPlayer(
                new SchoolS2CPacket(new ArrayList<>(PlayerSchoolProvider.get(serverPlayer).getSchools())),
                serverPlayer);
        ModMessages.sendToPlayer(
                new SpellS2CPacket(new ArrayList<>(PlayerSpellProvider.get(serverPlayer).getSpells())),
                serverPlayer);
    }
    @SubscribeEvent
    public static void onEldritchManuscriptUse(PlayerInteractEvent.RightClickItem event) {
        boolean eldritchLearningReplaced =
                Configuration.isEldritchSchoolLearningEnabled()
                        || Configuration.isSchoolLearningDisabled(SchoolRegistry.ELDRITCH.get().getId());
        if (event.getLevel().isClientSide()
                || !eldritchLearningReplaced
                || !event.getItemStack().is(ItemRegistry.ELDRITCH_PAGE.get())) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    @SubscribeEvent
    public static void onEldritchManuscriptTooltip(ItemTooltipEvent event) {
        if (Configuration.isEldritchSchoolLearningEnabled()
                && event.getItemStack().is(ItemRegistry.ELDRITCH_PAGE.get())) {
            event.getToolTip().add(Component.translatable(
                            "item.unraveling_spells.eldritch_manuscript.magic_lectern")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
