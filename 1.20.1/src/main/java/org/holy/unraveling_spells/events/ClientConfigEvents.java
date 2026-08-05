package org.holy.unraveling_spells.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.config.SpellLearnedManager;

@Mod.EventBusSubscriber(modid = Unraveling_spells.MODID, value = Dist.CLIENT)
public final class ClientConfigEvents {
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        Configuration.clearServerConfig();
        SpellLearnedManager.loadConfig();
    }
}
