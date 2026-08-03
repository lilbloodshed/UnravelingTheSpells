package org.holy.unraveling_spells.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.holy.unraveling_spells.common.config.CostOverrideParser;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Configuration {
    public static final ModConfigSpec CONFIG_SPEC;
    public static final ModConfigSpec.IntValue MAX_SCHOOLS;
    public static final ModConfigSpec.BooleanValue ENABLE_ELDRITCH_SCHOOL_LEARNING;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SCHOOLS_WITHOUT_LEARNING;
    public static final ModConfigSpec.IntValue DEFAULT_SPELL_SCROLL_COST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SPELL_SCROLL_COSTS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> LEARNED_SPELLS;

    private static volatile Integer syncedMaxSchools;
    private static volatile Boolean syncedEldritchSchoolLearning;
    private static volatile Set<ResourceLocation> syncedSchoolsWithoutLearning;
    private static volatile Integer syncedDefaultSpellScrollCost;
    private static volatile Map<ResourceLocation, Integer> syncedSpellScrollCosts;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        MAX_SCHOOLS = builder
                .comment("Maximum number of learnable schools")
                .defineInRange("max_schools", 3, 1, 100);

        ENABLE_ELDRITCH_SCHOOL_LEARNING = builder
                .comment(
                        "Allows the Eldritch school and its spells to be learned in the Magic Lectern.",
                        "When enabled, the Eldritch Manuscript no longer opens its original research GUI."
                )
                .define("enable_eldritch_school_learning", false);

        SCHOOLS_WITHOUT_LEARNING = builder
                .comment(
                        "Schools whose spells do not require learning.",
                        "These schools are hidden from school selection and their spells can always be cast.",
                        "Format: namespace:path (e.g., irons_spellbooks:fire)"
                )
                .defineList(
                        "schools_without_learning",
                        List.of(),
                        value -> value instanceof String id
                                && ResourceLocation.tryParse(id) != null
                );

        DEFAULT_SPELL_SCROLL_COST = builder
                .comment(
                        "Default number of Spell Scrolls required to learn a spell.",
                        "Eldritch spells use the same configured amount of Eldritch Manuscripts."
                )
                .defineInRange("default_spell_scroll_cost", 1, 1, 64);

        SPELL_SCROLL_COSTS = builder
                .comment(
                        "Per-spell learning cost overrides.",
                        "Format: namespace:spell_id=amount (e.g., irons_spellbooks:fireball=3).",
                        "The amount must be between 1 and 64."
                )
                .defineList(
                        "spell_scroll_costs",
                        List.of(),
                        Configuration::isValidSpellScrollCost
                );

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

    public static void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == CONFIG_SPEC) {
            onLoad();
        }
    }

    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == CONFIG_SPEC) {
            onLoad();
        }
    }

    public static ModConfigSpec getConfig() {
        return CONFIG_SPEC;
    }

    public static int getMaxSchools() {
        Integer syncedValue = syncedMaxSchools;
        return syncedValue != null ? syncedValue : MAX_SCHOOLS.get();
    }

    public static boolean isEldritchSchoolLearningEnabled() {
        Boolean syncedValue = syncedEldritchSchoolLearning;
        return syncedValue != null ? syncedValue : ENABLE_ELDRITCH_SCHOOL_LEARNING.get();
    }

    public static int getDefaultSpellScrollCost() {
        Integer syncedValue = syncedDefaultSpellScrollCost;
        return syncedValue != null ? syncedValue : DEFAULT_SPELL_SCROLL_COST.get();
    }

    public static boolean isSchoolLearningDisabled(ResourceLocation schoolId) {
        if (schoolId == null) {
            return false;
        }

        Set<ResourceLocation> syncedValues = syncedSchoolsWithoutLearning;
        return syncedValues != null
                ? syncedValues.contains(schoolId)
                : getLocalSchoolsWithoutLearning().contains(schoolId);
    }

    public static int getSpellScrollCost(ResourceLocation spellId) {
        Map<ResourceLocation, Integer> syncedCosts = syncedSpellScrollCosts;
        if (syncedCosts != null) {
            return syncedCosts.getOrDefault(spellId, getDefaultSpellScrollCost());
        }

        if (spellId != null) {
            Integer configuredCost = getLocalSpellScrollCosts().get(spellId);
            if (configuredCost != null) {
                return configuredCost;
            }
        }

        return getDefaultSpellScrollCost();
    }

    public static Set<ResourceLocation> getLocalSchoolsWithoutLearning() {
        Set<ResourceLocation> schools = new LinkedHashSet<>();
        for (String entry : SCHOOLS_WITHOUT_LEARNING.get()) {
            ResourceLocation schoolId = ResourceLocation.tryParse(entry.trim());
            if (schoolId != null) {
                schools.add(schoolId);
            }
        }
        return schools;
    }

    public static Map<ResourceLocation, Integer> getLocalSpellScrollCosts() {
        return CostOverrideParser.parse(
                SPELL_SCROLL_COSTS.get(),
                ResourceLocation::tryParse
        );
    }

    public static Set<ResourceLocation> getLocalDefaultLearnedSpells() {
        Set<ResourceLocation> spells = new LinkedHashSet<>();
        for (String entry : LEARNED_SPELLS.get()) {
            ResourceLocation spellId = ResourceLocation.tryParse(entry.trim());
            if (spellId != null) {
                spells.add(spellId);
            }
        }
        return spells;
    }

    public static void applyServerConfig(
            int maxSchools,
            boolean eldritchSchoolLearning,
            Set<ResourceLocation> schoolsWithoutLearning,
            int defaultSpellScrollCost,
            Map<ResourceLocation, Integer> spellScrollCosts) {
        syncedMaxSchools = maxSchools;
        syncedEldritchSchoolLearning = eldritchSchoolLearning;
        syncedSchoolsWithoutLearning = Set.copyOf(schoolsWithoutLearning);
        syncedDefaultSpellScrollCost = defaultSpellScrollCost;
        syncedSpellScrollCosts = Map.copyOf(spellScrollCosts);
    }

    public static void clearServerConfig() {
        syncedMaxSchools = null;
        syncedEldritchSchoolLearning = null;
        syncedSchoolsWithoutLearning = null;
        syncedDefaultSpellScrollCost = null;
        syncedSpellScrollCosts = null;
    }

    private static boolean isValidSpellScrollCost(Object value) {
        if (!(value instanceof String entry)) {
            return false;
        }

        int separator = entry.lastIndexOf('=');
        if (separator <= 0 || separator == entry.length() - 1
                || ResourceLocation.tryParse(entry.substring(0, separator).trim()) == null) {
            return false;
        }

        try {
            int cost = Integer.parseInt(entry.substring(separator + 1).trim());
            return cost >= 1 && cost <= 64;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private Configuration() {
    }
}
