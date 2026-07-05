package org.holy.unraveling_spells.config;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.holy.unraveling_spells.Unraveling_spells;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SpellConflictManager {
    private static final Map<ResourceLocation, Set<ResourceLocation>> conflictMap = new HashMap<>();
    private static final File configFile;
    private static final File exampleFile;

    static {
        configFile = new File("./config/unraveling_spells/conflicts.json");
        exampleFile = new File(configFile.getParentFile(), "conflicts_example.txt");
        loadConfig();
    }

    public static void loadConfig() {
        if (!configFile.exists()) {
            createDefaultConfig();
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            JsonObject spells = json.getAsJsonObject("spells");

            conflictMap.clear();

            for (Map.Entry<String, JsonElement> entry : spells.entrySet()) {
                ResourceLocation mainSpell = new ResourceLocation(entry.getKey());
                Set<ResourceLocation> conflicts = new HashSet<>();

                for (JsonElement conflict : entry.getValue().getAsJsonArray()) {
                    conflicts.add(new ResourceLocation(conflict.getAsString()));
                }

                conflictMap.put(mainSpell, conflicts);
            }

            Unraveling_spells.LOGGER.info("Spell conflict configuration loaded successfully");
        } catch (IOException e) {
            Unraveling_spells.LOGGER.error("Failed to load spell conflict configuration: {}", e.getMessage());
        }
    }

    private static void createDefaultConfig() {
        JsonObject json = new JsonObject();
        JsonObject spells = new JsonObject();

        json.add("spells", spells);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(json, writer);
            Unraveling_spells.LOGGER.info("Default spell conflict configuration created with pretty formatting");
        } catch (IOException e) {
            Unraveling_spells.LOGGER.error("Failed to create default spell conflict configuration: {}", e.getMessage());
        }

        String exampleText =
                "----- conflict.json example -----\n" +
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
            Unraveling_spells.LOGGER.info("Example configuration file conflicts.txt created");
        } catch (IOException e) {
            Unraveling_spells.LOGGER.error("Failed to create example configuration file conflicts.txt: {}", e.getMessage());
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
        List<ResourceLocation> conflicts = new ArrayList<>();
        Set<ResourceLocation> directConflicts = conflictMap.get(spell);

        if (directConflicts != null) {
            conflicts.addAll(directConflicts);
        }

        for (Map.Entry<ResourceLocation, Set<ResourceLocation>> entry : conflictMap.entrySet()) {
            if (entry.getValue().contains(spell)) {
                conflicts.add(entry.getKey());
            }
        }

        return conflicts;
    }
}
