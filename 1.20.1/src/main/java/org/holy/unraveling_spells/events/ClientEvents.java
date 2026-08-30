package org.holy.unraveling_spells.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.holy.unraveling_spells.Unraveling_spells;
import org.holy.unraveling_spells.block.shriving_forge.ShrivingForgeRenderer;
import org.holy.unraveling_spells.registries.utsBlockRegistry;

@Mod.EventBusSubscriber(modid = Unraveling_spells.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(utsBlockRegistry.SHRIVING_FORGE_TILE.get(), ShrivingForgeRenderer::new);
    }
}
