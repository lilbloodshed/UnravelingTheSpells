package org.holy.unraveling_spells.events;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.capability.spell.PlayerSpellProvider;

/**
 * Ивент для проверки перед кастом спелла, изучен ли спелл
 */
@Mod.EventBusSubscriber(modid = Unraveling_spells.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpellCastEvents {
    /*
    @SubscribeEvent
    public static void onPreCast(SpellPreCastEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ResourceLocation spellId = SpellRegistry.getSpell(event.getSpellId()).getSpellResource();

        player.getCapability(PlayerSpellProvider.PLAYER_SPELL).ifPresent(cap -> {
            if (!cap.getSpells().contains(spellId)) {
                event.setCanceled(true);
                player.displayClientMessage(
                        Component.translatable(
                                "message.unraveling_spells.spell_not_learned",
                                SpellRegistry.getSpell(spellId).getDisplayName(player)
                        ).withStyle(ChatFormatting.RED),
                        true
                );
            }
        });
    }

     */
}
