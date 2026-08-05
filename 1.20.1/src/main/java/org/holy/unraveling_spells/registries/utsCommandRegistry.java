package org.holy.unraveling_spells.registries;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.commands.UTSCommand;

@Mod.EventBusSubscriber(modid = Unraveling_spells.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class utsCommandRegistry {
    private utsCommandRegistry() {
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        UTSCommand.register(event.getDispatcher());
    }
}
