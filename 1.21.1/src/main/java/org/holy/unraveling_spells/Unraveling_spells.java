package org.holy.unraveling_spells;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.holy.unraveling_spells.config.ClientConfiguration;
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.registries.utsBlockRegistry;
import org.holy.unraveling_spells.registries.utsCreativeTabRegistry;
import org.holy.unraveling_spells.registries.utsItemRegistry;
import org.holy.unraveling_spells.registries.utsMenuRegistry;
import org.slf4j.Logger;

@Mod(Unraveling_spells.MODID)
public class Unraveling_spells {
    public static final String MODID = "unraveling_spells";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Unraveling_spells(IEventBus modEventBus, ModContainer modContainer) {
        utsItemRegistry.register(modEventBus);
        utsCreativeTabRegistry.register(modEventBus);
        utsBlockRegistry.register(modEventBus);
        utsMenuRegistry.register(modEventBus);

        //CONFIG REGISTERING
        modContainer.registerConfig(ModConfig.Type.COMMON, Configuration.CONFIG_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfiguration.CONFIG_SPEC);
        modEventBus.addListener(Configuration::onConfigLoading);
        modEventBus.addListener(Configuration::onConfigReloading);
    }

    
}
