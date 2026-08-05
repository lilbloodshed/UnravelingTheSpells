package org.holy.unraveling_spells;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.holy.unraveling_spells.capability.PlayerSchoolProvider;
import org.holy.unraveling_spells.capability.PlayerSpellProvider;
import org.holy.unraveling_spells.config.ClientConfiguration;
import org.holy.unraveling_spells.config.Configuration;
import org.holy.unraveling_spells.network.ModMessages;
import org.holy.unraveling_spells.registries.utsBlockRegistry;
import org.holy.unraveling_spells.registries.utsCreativeTabRegistry;
import org.holy.unraveling_spells.registries.utsItemRegistry;
import org.holy.unraveling_spells.registries.utsMenuRegistry;
import org.slf4j.Logger;

@Mod(Unraveling_spells.MODID)
public class Unraveling_spells {
    public static final String MODID = "unraveling_spells";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_LEARN = SOUND_EVENTS.register(
            "spell.learn",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(MODID, "spell.learn")));
    public static final DeferredHolder<SoundEvent, SoundEvent> SPELL_FILL = SOUND_EVENTS.register(
            "spell.fill",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(MODID, "spell.fill")));

    public Unraveling_spells(IEventBus modEventBus, ModContainer modContainer) {
        utsItemRegistry.register(modEventBus);
        utsCreativeTabRegistry.register(modEventBus);
        utsBlockRegistry.register(modEventBus);
        utsMenuRegistry.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        PlayerSchoolProvider.register(modEventBus);
        PlayerSpellProvider.register(modEventBus);
        modEventBus.addListener(ModMessages::register);

        //CONFIG REGISTERING
        modContainer.registerConfig(ModConfig.Type.COMMON, Configuration.CONFIG_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfiguration.CONFIG_SPEC);
        modEventBus.addListener(Configuration::onConfigLoading);
        modEventBus.addListener(Configuration::onConfigReloading);
    }

    
}
