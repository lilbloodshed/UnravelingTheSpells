package org.holy.unraveling_spells.commands;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.holy.unraveling_spells.Unraveling_spells;

@Mod.EventBusSubscriber(
        modid = Unraveling_spells.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class ModCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        SpellCommand.register(event.getDispatcher());
        SchoolCommand.register(event.getDispatcher());
    }
}

