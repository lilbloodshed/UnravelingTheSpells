package org.holy.unraveling_spells.events;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.config.SpellLearnedManager;

@EventBusSubscriber(modid = Unraveling_spells.MODID, value = Dist.CLIENT)
public final class ClientConfigEvents {
    private ClientConfigEvents() {
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        Configuration.clearServerConfig();
        SpellLearnedManager.loadConfig();
    }
}
