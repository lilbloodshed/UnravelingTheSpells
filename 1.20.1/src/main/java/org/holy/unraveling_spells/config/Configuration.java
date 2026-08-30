package org.holy.unraveling_spells.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.holy.unraveling_spells.common.config.CostOverrideParser;

import java.util.LinkedHashSet;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber
public class Configuration {
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static ForgeConfigSpec.IntValue MAX_SCHOOLS;
    public static ForgeConfigSpec.BooleanValue ENABLE_ELDRITCH_SCHOOL_LEARNING;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> SCHOOLS_WITHOUT_LEARNING;
    public static ForgeConfigSpec.IntValue DEFAULT_SPELL_SCROLL_COST;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> SPELL_SCROLL_COSTS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> LEARNED_SPELLS;
    public static ForgeConfigSpec.EnumValue<LearnType> SCHOOLS_PRICE_TYPE;
    public static ForgeConfigSpec.IntValue XP_BASE_COST;
    public static ForgeConfigSpec.DoubleValue PRICE_XP_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue XP_COST_GROWTH;
    public static ForgeConfigSpec.IntValue XP_MINIMUM_LEVEL;
    public static ForgeConfigSpec.BooleanValue XP_ALLOW_BULK_LEARNING;
    public static ForgeConfigSpec.BooleanValue XP_RESPECT_MAX_SCHOOLS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> SCHOOL_XP_COSTS;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> UNIQUE_SPELL_INFO;

    private static volatile Integer syncedMaxSchools;
    private static volatile Boolean syncedEldritchSchoolLearning;
    private static volatile Set<ResourceLocation> syncedSchoolsWithoutLearning;
    private static volatile Integer syncedDefaultSpellScrollCost;
    private static volatile Map<ResourceLocation, Integer> syncedSpellScrollCosts;
    private static volatile LearnType syncedSchoolPriceType;
    private static volatile Integer syncedXpBaseCost;
    private static volatile Double syncedXpMultiplier;
    private static volatile Double syncedXpCostGrowth;
    private static volatile Integer syncedXpMinimumLevel;
    private static volatile Boolean syncedXpAllowBulkLearning;
    private static volatile Boolean syncedXpMaxSchools;
    private static volatile Map<ResourceLocation, Integer> syncedSchoolXpCosts;
    private static volatile Map<ResourceLocation, String> syncedUniqueSpellInfo;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("general");

        MAX_SCHOOLS = builder
                .comment("Maximum number of learnable schools.",
                        "Set to 0 to disable this limit.")
                .defineInRange("max_schools", 3, 0, 100);

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
                        "Format: \"namespace:path\" (e.g., \"irons_spellbooks:fire\")"
                )
                .defineList(
                        "schools_without_learning",
                        List.of(),
                        value -> value instanceof String id
                                && ResourceLocation.tryParse(id) != null
                );

        LEARNED_SPELLS = builder
                .comment(
                        "List of spells that are learned by default.",
                        "Format: \"namespace:path\" (e.g., \"irons_spellbooks:fireball\")",
                        "These spells will be marked as learned and cannot be learned again."
                )
                .defineList(
                        "learned_spells",
                        List.of(),
                        obj -> obj instanceof String
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
                        "Format: \"namespace:spell_id=amount\" (e.g., \"irons_spellbooks:fireball=3\").",
                        "The amount must be between 1 and 64."
                )
                .defineList(
                        "spell_scroll_costs",
                        List.of(),
                        Configuration::isValidSpellScrollCost
                );

        builder.pop();

        builder.push("types");

        SCHOOLS_PRICE_TYPE = builder
                .comment("How schools are learned: DEFAULT requires selecting every available school slot; XP spends player experience.")
                .defineEnum("school_price_type", LearnType.DEFAULT);

        builder.push("xp");

        XP_RESPECT_MAX_SCHOOLS = builder
                .comment("When \"school_price_type\" is XP, determines whether max_schools limits XP school learning.",
                        "max_schools = 0 always disables the limit.")
                .define("xp_respect_max_schools", false);

        XP_BASE_COST = builder
                .comment("Base total XP cost of one school when \"school_price_type\" is XP.",
                        "Set to 0 to make XP learning free.")
                .defineInRange("xp_base_cost", 120, 0, 1_000_000);

        PRICE_XP_MULTIPLIER = builder
                .comment("Global multiplier applied to every XP school cost when \"school_price_type\" is XP.")
                .defineInRange("price_xp_multiplier", 1.0, 0.0, 100.0);

        XP_COST_GROWTH = builder
                .comment("Multiplier applied for every school the player already knows.",
                        "1.0 keeps all schools at the same price; 1.25 makes each next school 25% more expensive.")
                .defineInRange("xp_cost_growth", 1.15, 0.0, 100.0);

        XP_MINIMUM_LEVEL = builder
                .comment("Minimum vanilla XP level required to buy a school with XP. 0 disables the level requirement.")
                .defineInRange("xp_minimum_level", 0, 0, 1_000_000);

        XP_ALLOW_BULK_LEARNING = builder
                .comment("Allows selecting and buying several schools at once with XP.",
                        "Their cost still increases in a deterministic order.")
                .define("xp_allow_bulk_learning", true);

        SCHOOL_XP_COSTS = builder
                .comment("Optional per-school base XP cost overrides.",
                        "Format: \"namespace:school_id=amount\" (e.g., \"irons_spellbooks:fire=200\").",
                        "Overrides are still affected by \"xp_cost_multiplier\" and \"xp_cost_growth\".")
                .defineList("school_xp_costs", List.of(), Configuration::isValidSchoolXpCost);

        builder.pop();
        builder.pop();

        builder.push("additional");

        UNIQUE_SPELL_INFO = builder
                .comment(
                        "Custom text templates for spell descriptions in the Magic Lectern GUI.",
                        "Format: \"namespace:spell_id=template\".",
                        "Use {guide} to insert the spell's original guide text at that exact position.",
                        "Example: \"irons_spellbooks:fireball=Your text\\n{guide}\".",
                        "A template without {guide} replaces the original guide completely."
                )
                .defineList("unique_spell_info", List.of(), Configuration::isValidUniqueSpellInfo);

        CONFIG_SPEC = builder.build();
    }

    public enum LearnType {
        DEFAULT,
        XP
    }

    public static void onLoad() {
        SpellConflictManager.loadConfig();
        SpellLearnedManager.loadConfig();
    }

    public static ForgeConfigSpec getConfig() {
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

    public static LearnType getSchoolPriceType() {
        LearnType syncedValue = syncedSchoolPriceType;
        return syncedValue != null ? syncedValue : SCHOOLS_PRICE_TYPE.get();
    }

    public static int getXpMinimumLevel() {
        Integer syncedValue = syncedXpMinimumLevel;
        return syncedValue != null ? syncedValue : XP_MINIMUM_LEVEL.get();
    }

    public static boolean isSchoolXpBulkLearningAllowed() {
        Boolean syncedValue = syncedXpAllowBulkLearning;
        return syncedValue != null ? syncedValue : XP_ALLOW_BULK_LEARNING.get();
    }

    public static boolean isMaxSchoolsLimitEnabled() {
        if (getMaxSchools() == 0) {
            return false;
        }
        return getSchoolPriceType() != LearnType.XP || isSchoolXpMaxSchoolsLimitEnabled();
    }

    public static boolean isSchoolXpMaxSchoolsLimitEnabled() {
        Boolean syncedValue = syncedXpMaxSchools;
        return syncedValue != null ? syncedValue : XP_RESPECT_MAX_SCHOOLS.get();
    }

    public static int getSchoolXpCost(ResourceLocation schoolId, int knownSchoolCount) {
        Map<ResourceLocation, Integer> costs = syncedSchoolXpCosts;
        int configuredBase = costs != null
                ? costs.getOrDefault(schoolId, getXpBaseCost())
                : getLocalSchoolXpCosts().getOrDefault(schoolId, getXpBaseCost());
        double multiplier = syncedXpMultiplier != null ? syncedXpMultiplier : PRICE_XP_MULTIPLIER.get();
        double growth = syncedXpCostGrowth != null ? syncedXpCostGrowth : XP_COST_GROWTH.get();
        return Math.max(0, (int) Math.round(configuredBase * multiplier * Math.pow(growth, Math.max(0, knownSchoolCount))));
    }

    public static int getSchoolXpCost(Collection<ResourceLocation> schoolIds, int knownSchoolCount) {
        List<ResourceLocation> orderedSchools = new ArrayList<>(schoolIds);
        orderedSchools.sort(Comparator.comparing(ResourceLocation::toString));
        int total = 0;
        for (int index = 0; index < orderedSchools.size(); index++) {
            total = Math.addExact(total, getSchoolXpCost(orderedSchools.get(index), knownSchoolCount + index));
        }
        return total;
    }

    private static int getXpBaseCost() {
        Integer syncedValue = syncedXpBaseCost;
        return syncedValue != null ? syncedValue : XP_BASE_COST.get();
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

    public static Map<ResourceLocation, Integer> getLocalSchoolXpCosts() {
        return CostOverrideParser.parse(SCHOOL_XP_COSTS.get(), ResourceLocation::tryParse);
    }

    public static String getUniqueSpellInfo(ResourceLocation spellId) {
        if (spellId == null) {
            return null;
        }
        Map<ResourceLocation, String> syncedValues = syncedUniqueSpellInfo;
        return syncedValues != null
                ? syncedValues.get(spellId)
                : getLocalUniqueSpellInfo().get(spellId);
    }

    public static Map<ResourceLocation, String> getLocalUniqueSpellInfo() {
        Map<ResourceLocation, String> templates = new java.util.LinkedHashMap<>();
        for (String entry : UNIQUE_SPELL_INFO.get()) {
            int separator = entry.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            ResourceLocation spellId = ResourceLocation.tryParse(entry.substring(0, separator).trim());
            if (spellId != null) {
                templates.put(spellId, entry.substring(separator + 1).replace("\\n", "\n"));
            }
        }
        return templates;
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
            Map<ResourceLocation, Integer> spellScrollCosts,
            LearnType schoolPriceType,
            int schoolXpBaseCost,
            double schoolXpMultiplier,
            double schoolXpCostGrowth,
            int schoolXpMinimumLevel,
            boolean schoolXpAllowBulkLearning,
            boolean schoolXpRespectMaxSchools,
            Map<ResourceLocation, Integer> schoolXpCosts,
            Map<ResourceLocation, String> uniqueSpellInfo) {
        syncedMaxSchools = maxSchools;
        syncedEldritchSchoolLearning = eldritchSchoolLearning;
        syncedSchoolsWithoutLearning = Set.copyOf(schoolsWithoutLearning);
        syncedDefaultSpellScrollCost = defaultSpellScrollCost;
        syncedSpellScrollCosts = Map.copyOf(spellScrollCosts);
        syncedSchoolPriceType = schoolPriceType;
        syncedXpBaseCost = schoolXpBaseCost;
        syncedXpMultiplier = schoolXpMultiplier;
        syncedXpCostGrowth = schoolXpCostGrowth;
        syncedXpMinimumLevel = schoolXpMinimumLevel;
        syncedXpAllowBulkLearning = schoolXpAllowBulkLearning;
        syncedXpMaxSchools = schoolXpRespectMaxSchools;
        syncedSchoolXpCosts = Map.copyOf(schoolXpCosts);
        syncedUniqueSpellInfo = Map.copyOf(uniqueSpellInfo);
    }

    public static void clearServerConfig() {
        syncedMaxSchools = null;
        syncedEldritchSchoolLearning = null;
        syncedSchoolsWithoutLearning = null;
        syncedDefaultSpellScrollCost = null;
        syncedSpellScrollCosts = null;
        syncedSchoolPriceType = null;
        syncedXpBaseCost = null;
        syncedXpMultiplier = null;
        syncedXpCostGrowth = null;
        syncedXpMinimumLevel = null;
        syncedXpAllowBulkLearning = null;
        syncedXpMaxSchools = null;
        syncedSchoolXpCosts = null;
        syncedUniqueSpellInfo = null;
    }

    private static boolean isValidUniqueSpellInfo(Object value) {
        if (!(value instanceof String entry)) {
            return false;
        }
        int separator = entry.indexOf('=');
        return separator > 0
                && ResourceLocation.tryParse(entry.substring(0, separator).trim()) != null;
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

    private static boolean isValidSchoolXpCost(Object value) {
        if (!(value instanceof String entry)) return false;
        int separator = entry.lastIndexOf('=');
        if (separator <= 0 || separator == entry.length() - 1
                || ResourceLocation.tryParse(entry.substring(0, separator).trim()) == null) return false;
        try {
            int cost = Integer.parseInt(entry.substring(separator + 1).trim());
            return cost >= 0 && cost <= 1_000_000;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
