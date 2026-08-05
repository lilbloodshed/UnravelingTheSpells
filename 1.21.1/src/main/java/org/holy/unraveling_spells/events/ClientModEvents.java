package org.holy.unraveling_spells.events;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.client.MagicLecternScreen;
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.config.SpellLearnedManager;
import org.holy.unraveling_spells.registries.utsMenuRegistry;

@EventBusSubscriber(modid = Unraveling_spells.MODID, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(utsMenuRegistry.MAGIC_LECTERN_MENU.get(), MagicLecternScreen::new);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        Configuration.clearServerConfig();
        SpellLearnedManager.loadConfig();
    }
}
