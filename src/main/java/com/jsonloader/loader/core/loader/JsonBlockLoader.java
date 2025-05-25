package com.jsonloader.loader.core.loader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jsonloader.loader.JSONloader; // Updated import
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class JsonBlockLoader {

    // Use the logger from the main mod class or create a dedicated one
    // Corrected: Use MODID from the main class
    private static final Logger LOGGER = LogManager.getLogger(JSONloader.MODID + " JsonBlockLoader");
    private static final Gson GSON = new Gson();
    // Construct the path relative to the classpath root, typically within the JAR's assets
    // Corrected: Use MODID from the main class
    private static final String BLOCKS_JSON_PATH = "/assets/" + JSONloader.MODID + "/blocks.json";

    /**
     * Loads block definitions from the blocks.json file located in the mod's resources.
     * 
     * @return A list of BlockDefinition objects parsed from the JSON, or an empty list if loading fails.
     */
    public static List<BlockDefinition> loadBlockDefinitions() {
        LOGGER.info("Attempting to load block definitions from JSON at path: {}", BLOCKS_JSON_PATH);

        // Use getResourceAsStream which searches the classpath
        try (InputStream inputStream = JsonBlockLoader.class.getResourceAsStream(BLOCKS_JSON_PATH)) {
            
            if (inputStream == null) {
                // Try with context class loader as fallback
                // Corrected path for context class loader (remove leading slash)
                String fallbackPath = BLOCKS_JSON_PATH.startsWith("/") ? BLOCKS_JSON_PATH.substring(1) : BLOCKS_JSON_PATH;
                InputStream fallbackInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fallbackPath);
                 if (fallbackInputStream == null) {
                    LOGGER.error("Could not find blocks.json using class loader or context class loader at path: {}", BLOCKS_JSON_PATH);
                    return Collections.emptyList();
                 } else {
                     LOGGER.warn("Found blocks.json using context class loader, check pathing: {}", BLOCKS_JSON_PATH);
                     return loadFromInputStream(fallbackInputStream);
                 }
            }
            
            return loadFromInputStream(inputStream);

        } catch (Exception e) {
            LOGGER.error("Failed to read or parse blocks.json from path: {}", BLOCKS_JSON_PATH, e);
            return Collections.emptyList();
        }
    }

    private static List<BlockDefinition> loadFromInputStream(InputStream inputStream) {
         try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<BlockDefinition>>() {}.getType();
            List<BlockDefinition> definitions = GSON.fromJson(reader, listType);

            if (definitions == null) {
                LOGGER.error("Failed to parse blocks.json. GSON returned null.");
                return Collections.emptyList();
            }

            LOGGER.info("Successfully loaded {} block definitions from JSON.", definitions.size());
            definitions.forEach(def -> LOGGER.debug("Loaded definition for block ID: {}", def.id()));
            return definitions;
        } catch (Exception e) {
            LOGGER.error("Error occurred during JSON parsing from input stream.", e);
            return Collections.emptyList();
        }
    }
}

