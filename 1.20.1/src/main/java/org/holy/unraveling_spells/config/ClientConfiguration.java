package org.holy.unraveling_spells.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfiguration {
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final ForgeConfigSpec.BooleanValue SHOW_SPELLS_NAME;
    public static final ForgeConfigSpec.BooleanValue SHOW_JEI_GUIDE_SPELLS;
    public static final ForgeConfigSpec.BooleanValue ENABLED_ANIMATIONS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        SHOW_SPELLS_NAME = builder
                .comment("Shows the names of spell and its descriptions if this spell is not learned")
                .define("show_spells_name", false);

        SHOW_JEI_GUIDE_SPELLS = builder
                .comment("Shows spell guide descriptions in JEI information pages even if the spell is not learned")
                .define("show_jei_guide_spells", true);

        ENABLED_ANIMATIONS = builder
                .comment("If enabled, AnimLib animations will be displayed in the GUI.\nAnimLib is an optional client-side dependency.")
                .define("enabled_animations", true);

        CONFIG_SPEC = builder.build();
    }

    private ClientConfiguration() {
    }

    public static ForgeConfigSpec getConfig() {
        return CONFIG_SPEC;
    }
}
