package org.holy.unraveling_spells.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.holy.unraveling_spells.Unraveling_spells;

/**
 * ВРЕМЕННЫЙ КЛАСС ДЛЯ БИНДА СКРИНА
 */
@Mod.EventBusSubscriber(modid = Unraveling_spells.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyMapping {
    public static final String IRONS_SPELLBOOKS_REIMAGINED_CATEGORY = "key." + Unraveling_spells.MODID + ".category";
    public static final net.minecraft.client.KeyMapping openKey = new net.minecraft.client.KeyMapping("key." + Unraveling_spells.MODID + ".open_branch", KeyConflictContext.IN_GAME,
            InputConstants.getKey(InputConstants.KEY_P, -1), IRONS_SPELLBOOKS_REIMAGINED_CATEGORY);

    @SubscribeEvent
    public static void registerKeyMappingsEvent(RegisterKeyMappingsEvent event) {
        event.register(openKey);
    }
}