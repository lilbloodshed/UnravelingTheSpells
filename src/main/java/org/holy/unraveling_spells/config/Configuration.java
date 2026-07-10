package org.holy.unraveling_spells.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.holy.unraveling_spells.config.SpellConflictManager;
import org.holy.unraveling_spells.config.SpellLearnedManager;

import java.util.List;

@Mod.EventBusSubscriber
public class Configuration {
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static ForgeConfigSpec.IntValue MAX_SCHOOLS;
    public static ForgeConfigSpec.IntValue MAX_MISTAKES;
    public static ForgeConfigSpec.BooleanValue SHOW_SPELLS_NAME;
    public static ForgeConfigSpec.BooleanValue SHOW_JEI_GUIDE_SPELLS;
    public static ForgeConfigSpec.BooleanValue ENABLED_ANIMATIONS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> LEARNED_SPELLS;


    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        MAX_SCHOOLS = builder
                .comment("Maximum number of learnable schools")
                .defineInRange("max_schools", 3, 1, 100);

        MAX_MISTAKES = builder
                .comment("Maximum number of player's mistakes when creating Spell scrolls")
                .defineInRange("max_mistakes", 3, 1, 6);

        SHOW_SPELLS_NAME = builder
                .comment("Shows the names of spell and its descriptions if this spell is not learned")
                .define("show_spells_name", false);

        SHOW_JEI_GUIDE_SPELLS = builder
                .comment("Shows spell guide descriptions in JEI information pages even if the spell is not learned")
                .define("show_jei_guide_spells", true);

        ENABLED_ANIMATIONS = builder
                .comment("If enabled, AnimLib animations will be displayed in the GUI.\nAnimLib is an optional client-side dependency.")
                .define("enabled_animations", true);

        LEARNED_SPELLS = builder
                .comment(
                        "List of spells that are learned by default.",
                        "Format: namespace:path (e.g., irons_spellbooks:fireball)",
                        "These spells will be marked as learned and cannot be learned again."
                )
                .defineList(
                        "learned_spells",
                        List.of(),
                        obj -> obj instanceof String
                );

        CONFIG_SPEC = builder.build();
    }

    public static void onLoad() {
        SpellConflictManager.loadConfig();
        SpellLearnedManager.loadConfig();
    }

    public static ForgeConfigSpec getConfig() {
        return CONFIG_SPEC;
    }
}
