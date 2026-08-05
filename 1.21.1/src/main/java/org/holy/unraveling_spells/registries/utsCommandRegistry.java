package org.holy.unraveling_spells.registries;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.commands.UTSCommand;

@EventBusSubscriber(modid = Unraveling_spells.MODID)
public final class utsCommandRegistry {
    private utsCommandRegistry() {
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        UTSCommand.register(event.getDispatcher());
    }
}
