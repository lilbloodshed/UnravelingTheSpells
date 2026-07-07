package org.holy.unraveling_spells.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.holy.unraveling_spells.Unraveling_spells;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpellConflictManager {
    private static final Map<ResourceLocation, Set<ResourceLocation>> conflictMap = new HashMap<>();
    private static final File configFile;
    private static final File exampleFile;

    static {
        configFile = FMLPaths.CONFIGDIR.get().resolve("unraveling_spells").resolve("conflicts.json").toFile();
        exampleFile = new File(configFile.getParentFile(), "conflicts_example.txt");
        loadConfig();
    }

    public static void loadConfig() {
        ensureConfigDirectory();

        if (!configFile.exists()) {
            createDefaultConfig();
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            JsonObject spells = json != null && json.has("spells") && json.get("spells").isJsonObject()
                    ? json.getAsJsonObject("spells")
                    : new JsonObject();

            conflictMap.clear();

            for (Map.Entry<String, JsonElement> entry : spells.entrySet()) {
                ResourceLocation mainSpell = parseSpellId(entry.getKey());
                if (mainSpell == null || !entry.getValue().isJsonArray()) {
                    continue;
                }

                Set<ResourceLocation> conflicts = new LinkedHashSet<>();
                for (JsonElement conflict : entry.getValue().getAsJsonArray()) {
                    if (!conflict.isJsonPrimitive() || !conflict.getAsJsonPrimitive().isString()) {
                        continue;
                    }

                    ResourceLocation conflictSpell = parseSpellId(conflict.getAsString());
                    if (conflictSpell != null) {
                        conflicts.add(conflictSpell);
                    }
                }

                conflictMap.put(mainSpell, conflicts);
            }

            Unraveling_spells.LOGGER.info("Spell conflict configuration loaded successfully");
        } catch (IOException | JsonParseException | IllegalStateException e) {
            Unraveling_spells.LOGGER.error("Failed to load spell conflict configuration: {}", e.getMessage());
        }
    }

    private static void ensureConfigDirectory() {
        File parent = configFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            Unraveling_spells.LOGGER.error("Failed to create spell conflict config directory: {}", parent.getAbsolutePath());
        }
    }

    private static void createDefaultConfig() {
        ensureConfigDirectory();

        JsonObject json = new JsonObject();
        JsonObject spells = new JsonObject();

        json.add("spells", spells);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(json, writer);
            Unraveling_spells.LOGGER.info("Default spell conflict configuration created: {}", configFile.getAbsolutePath());
        } catch (IOException e) {
            Unraveling_spells.LOGGER.error("Failed to create default spell conflict configuration: {}", e.getMessage());
        }

        String exampleText =
                "----- conflicts.json example -----\n" +
                "If a player has learned modid:spell1, then modid:spell2 and modid:spell3 are blocked from being learned.\n" +
                "However, if a player has learned modid:spell2 or modid:spell3, then modid:spell1 is blocked from being learned.\n" +
                "{\n" +
                "  \"spells\": {\n" +
                "    \"modid:spell1\": [\n" +
                "      \"modid:spell2\",\n" +
                "      \"modid:spell3\"\n" +
                "    ],\n" +
                "    \"modid:senbonzakura_kageyoshi\": [\n" +
                "      \"modid:hihio_zabimaru\"\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        try (FileWriter exampleWriter = new FileWriter(exampleFile)) {
            exampleWriter.write(exampleText);
            Unraveling_spells.LOGGER.info("Example spell conflict configuration created: {}", exampleFile.getAbsolutePath());
        } catch (IOException e) {
            Unraveling_spells.LOGGER.error("Failed to create example spell conflict configuration: {}", e.getMessage());
        }
    }

    private static ResourceLocation parseSpellId(String spellId) {
        try {
            return ResourceLocation.parse(spellId);
        } catch (ResourceLocationException e) {
            Unraveling_spells.LOGGER.warn("Skipping invalid spell id in conflict configuration: {}", spellId);
            return null;
        }
    }

    public static boolean hasConflict(ResourceLocation spell, Collection<ResourceLocation> playerSpells) {
        Set<ResourceLocation> directConflicts = conflictMap.get(spell);

        if (directConflicts != null) {
            for (ResourceLocation conflict : directConflicts) {
                if (playerSpells.contains(conflict)) {
                    return true;
                }
            }
        }

        for (Map.Entry<ResourceLocation, Set<ResourceLocation>> entry : conflictMap.entrySet()) {
            if (entry.getValue().contains(spell) && playerSpells.contains(entry.getKey())) {
                return true;
            }
        }

        return false;
    }

    public static List<ResourceLocation> getConflictSpells(ResourceLocation spell) {
        Set<ResourceLocation> conflicts = new LinkedHashSet<>();
        Set<ResourceLocation> directConflicts = conflictMap.get(spell);

        if (directConflicts != null) {
            conflicts.addAll(directConflicts);
        }

        for (Map.Entry<ResourceLocation, Set<ResourceLocation>> entry : conflictMap.entrySet()) {
            if (entry.getValue().contains(spell)) {
                conflicts.add(entry.getKey());
            }
        }

        return new ArrayList<>(conflicts);
    }
}
