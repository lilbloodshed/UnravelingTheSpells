package org.holy.unraveling_spells.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.client.KeyMapping;

@Mod.EventBusSubscriber(modid = Unraveling_spells.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (KeyMapping.openKey.consumeClick()) {
            // minecraft.setScreen(new RunesScreenTest());
        }
    }
}
