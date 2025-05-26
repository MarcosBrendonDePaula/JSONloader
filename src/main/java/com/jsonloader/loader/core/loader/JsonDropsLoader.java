package com.jsonloader.loader.core.loader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jsonloader.loader.JSONloader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class JsonDropsLoader {

    private static final Logger LOGGER = LogManager.getLogger(JSONloader.MODID + " JsonDropsLoader");
    private static final Gson GSON = new Gson();
    private static final String DROPS_JSON_PATH = "/assets/" + JSONloader.MODID + "/drops.json";

    /**
     * Loads drops definitions from the drops.json file located in the mod's resources.
     * 
     * @return A DropsDefinition object parsed from the JSON, or an empty definition if loading fails.
     */
    public static DropsDefinition loadDropsDefinitions() {
        LOGGER.info("Attempting to load drops definitions from JSON at path: {}", DROPS_JSON_PATH);

        try (InputStream inputStream = JsonDropsLoader.class.getResourceAsStream(DROPS_JSON_PATH)) {
            
            if (inputStream == null) {
                // Try with context class loader as fallback
                String fallbackPath = DROPS_JSON_PATH.startsWith("/") ? DROPS_JSON_PATH.substring(1) : DROPS_JSON_PATH;
                InputStream fallbackInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fallbackPath);
                 if (fallbackInputStream == null) {
                    LOGGER.error("Could not find drops.json using class loader or context class loader at path: {}", DROPS_JSON_PATH);
                    return new DropsDefinition(Collections.emptyList(), Collections.emptyList());
                 } else {
                     LOGGER.warn("Found drops.json using context class loader, check pathing: {}", DROPS_JSON_PATH);
                     return loadFromInputStream(fallbackInputStream);
                 }
            }
            
            return loadFromInputStream(inputStream);

        } catch (Exception e) {
            LOGGER.error("Failed to read or parse drops.json from path: {}", DROPS_JSON_PATH, e);
            return new DropsDefinition(Collections.emptyList(), Collections.emptyList());
        }
    }

    private static DropsDefinition loadFromInputStream(InputStream inputStream) {
         try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<DropsDefinition>() {}.getType();
            DropsDefinition definition = GSON.fromJson(reader, type);

            if (definition == null) {
                LOGGER.error("Failed to parse drops.json. GSON returned null.");
                return new DropsDefinition(Collections.emptyList(), Collections.emptyList());
            }

            int blockDropsCount = definition.block_drops() != null ? definition.block_drops().size() : 0;
            int mobDropsCount = definition.mob_drops() != null ? definition.mob_drops().size() : 0;
            
            LOGGER.info("Successfully loaded drops definitions from JSON: {} block drops and {} mob drops.", 
                        blockDropsCount, mobDropsCount);
            
            if (definition.block_drops() != null) {
                definition.block_drops().forEach(blockDrop -> 
                    LOGGER.debug("Loaded drop definition for block ID: {}", blockDrop.block_id()));
            }
            
            if (definition.mob_drops() != null) {
                definition.mob_drops().forEach(mobDrop -> 
                    LOGGER.debug("Loaded drop definition for mob ID: {}", mobDrop.mob_id()));
            }
            
            return definition;
        } catch (Exception e) {
            LOGGER.error("Error occurred during JSON parsing from input stream.", e);
            return new DropsDefinition(Collections.emptyList(), Collections.emptyList());
        }
    }
}
