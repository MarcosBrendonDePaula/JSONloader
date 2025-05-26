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
import java.util.List;

public class JsonItemLoader {

    private static final Logger LOGGER = LogManager.getLogger(JSONloader.MODID + " JsonItemLoader");
    private static final Gson GSON = new Gson();
    private static final String ITEMS_JSON_PATH = "/assets/" + JSONloader.MODID + "/items.json";

    /**
     * Loads item definitions from the items.json file located in the mod's resources.
     * 
     * @return A list of ItemDefinition objects parsed from the JSON, or an empty list if loading fails.
     */
    public static List<ItemDefinition> loadItemDefinitions() {
        LOGGER.info("Attempting to load item definitions from JSON at path: {}", ITEMS_JSON_PATH);

        try (InputStream inputStream = JsonItemLoader.class.getResourceAsStream(ITEMS_JSON_PATH)) {
            
            if (inputStream == null) {
                // Try with context class loader as fallback
                String fallbackPath = ITEMS_JSON_PATH.startsWith("/") ? ITEMS_JSON_PATH.substring(1) : ITEMS_JSON_PATH;
                InputStream fallbackInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fallbackPath);
                 if (fallbackInputStream == null) {
                    LOGGER.error("Could not find items.json using class loader or context class loader at path: {}", ITEMS_JSON_PATH);
                    return Collections.emptyList();
                 } else {
                     LOGGER.warn("Found items.json using context class loader, check pathing: {}", ITEMS_JSON_PATH);
                     return loadFromInputStream(fallbackInputStream);
                 }
            }
            
            return loadFromInputStream(inputStream);

        } catch (Exception e) {
            LOGGER.error("Failed to read or parse items.json from path: {}", ITEMS_JSON_PATH, e);
            return Collections.emptyList();
        }
    }

    private static List<ItemDefinition> loadFromInputStream(InputStream inputStream) {
         try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<ItemDefinition>>() {}.getType();
            List<ItemDefinition> definitions = GSON.fromJson(reader, listType);

            if (definitions == null) {
                LOGGER.error("Failed to parse items.json. GSON returned null.");
                return Collections.emptyList();
            }

            LOGGER.info("Successfully loaded {} item definitions from JSON.", definitions.size());
            definitions.forEach(def -> LOGGER.debug("Loaded definition for item ID: {}", def.id()));
            return definitions;
        } catch (Exception e) {
            LOGGER.error("Error occurred during JSON parsing from input stream.", e);
            return Collections.emptyList();
        }
    }
}
