package org.holy.unraveling_spells.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfiguration {
    public static final ModConfigSpec CONFIG_SPEC;
    public static final ModConfigSpec.BooleanValue SHOW_SPELLS_NAME;
    public static final ModConfigSpec.BooleanValue SHOW_JEI_GUIDE_SPELLS;
    public static final ModConfigSpec.BooleanValue ENABLED_ANIMATIONS;
    public static final ModConfigSpec.EnumValue<GUItype> GUI_TYPE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        SHOW_SPELLS_NAME = builder
                .comment("Shows the names of spell and its descriptions if this spell is not learned")
                .define("show_spells_name", false);

        SHOW_JEI_GUIDE_SPELLS = builder
                .comment("Shows spell guide descriptions in JEI information pages even if the spell is not learned")
                .define("show_jei_guide_spells", true);

        ENABLED_ANIMATIONS = builder
                .comment("If enabled, AnimLib animations will be displayed in the GUI.\nAnimLib is an optional client-side dependency.")
                .define("enabled_animations", true);

        GUI_TYPE = builder
                .comment("The parameter is responsible for changing the appearance of the Magic lectern's GUI.")
                .defineEnum("gui_type", GUItype.CLASSIC);

        CONFIG_SPEC = builder.build();
    }

    public enum GUItype {
        CLASSIC,
        LARGER
    }

    private ClientConfiguration() {
    }

    public static ModConfigSpec getConfig() {
        return CONFIG_SPEC;
    }
}
