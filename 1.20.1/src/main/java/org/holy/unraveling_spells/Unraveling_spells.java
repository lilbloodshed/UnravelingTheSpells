package org.holy.unraveling_spells;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.holy.unraveling_spells.client.MagicLecternScreen;
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

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final RegistryObject<SoundEvent> SPELL_LEARN = SOUND_EVENTS.register("spell.learn",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "spell.learn")));
    public static final RegistryObject<SoundEvent> SPELL_FILL = SOUND_EVENTS.register("spell.fill",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "spell.fill")));

    public Unraveling_spells() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        utsItemRegistry.register(bus);
        utsCreativeTabRegistry.register(bus);
        utsBlockRegistry.register(bus);
        utsMenuRegistry.register(bus);
        SOUND_EVENTS.register(bus);

        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.getConfig());
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfiguration.getConfig());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModMessages.register();
            Configuration.onLoad();
        });
    }

    @SuppressWarnings("removal")
    void clientSetup(final FMLClientSetupEvent e) {
        MenuScreens.register(utsMenuRegistry.MAGIC_TABLE_MENU.get(), MagicLecternScreen::new);
    }
}
