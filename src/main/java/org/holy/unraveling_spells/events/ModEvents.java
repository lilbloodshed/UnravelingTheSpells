package org.holy.unraveling_spells.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.capability.school.PlayerSchool;
import org.holy.unraveling_spells.capability.school.PlayerSchoolProvider;
import org.holy.unraveling_spells.capability.spell.PlayerSpell;
import org.holy.unraveling_spells.capability.spell.PlayerSpellProvider;

@Mod.EventBusSubscriber(modid = Unraveling_spells.MODID)
public class ModEvents {
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerSchool.class);
        event.register(PlayerSpell.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerSchoolProvider.PLAYER_SCHOOL).isPresent()) {
                event.addCapability(new ResourceLocation(Unraveling_spells.MODID, "properties_school"), new PlayerSchoolProvider());
            }
            if (!event.getObject().getCapability(PlayerSpellProvider.PLAYER_SPELL).isPresent()) {
                event.addCapability(new ResourceLocation(Unraveling_spells.MODID, "properties_spell"), new PlayerSpellProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer newServerPlayer) {
            boolean keepEverything = !event.isWasDeath();
            //Persist summon timers across death
            event.getOriginal().reviveCaps();

            PlayerSchool oldSchoolCap = event.getOriginal().getCapability(PlayerSchoolProvider.PLAYER_SCHOOL).orElse(null);
            PlayerSpell oldSpellCap = event.getOriginal().getCapability(PlayerSpellProvider.PLAYER_SPELL).orElse(null);

            PlayerSchool newSchoolCap = event.getEntity().getCapability(PlayerSchoolProvider.PLAYER_SCHOOL).orElse(null);
            PlayerSpell newSpellCap = event.getEntity().getCapability(PlayerSpellProvider.PLAYER_SPELL).orElse(null);

            if (newSchoolCap != null && oldSchoolCap != null) {
                newSchoolCap.copyFrom(oldSchoolCap);
            }
            if (newSpellCap != null && oldSpellCap != null) {
                newSpellCap.copyFrom(oldSpellCap);
            }
            event.getOriginal().invalidateCaps();
        }
    }
}
