package com.yourname.yourmodid.core.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.yourname.yourmodid.YourModName;
import com.yourname.yourmodid.core.loader.BlockDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicTextureManager {

    private static final Logger LOGGER = LogManager.getLogger(YourModName.MODID + " DynamicTextureManager");
    // Cache to store loaded dynamic textures to avoid redundant processing
    private static final Map<String, ResourceLocation> textureCache = new ConcurrentHashMap<>();

    /**
     * Processes all block definitions and registers dynamic textures for those
     * specified as 'url' or 'base64'. This should be called during client setup.
     *
     * @param definitions List of block definitions loaded from JSON.
     */
    public static void registerDynamicTextures(List<BlockDefinition> definitions) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        for (BlockDefinition def : definitions) {
            String textureType = def.texture().type().toLowerCase();
            String textureValue = def.texture().value();
            String blockId = def.id();
            ResourceLocation textureLocation = new ResourceLocation(YourModName.MODID, "block/" + blockId);

            // Skip if already processed (e.g., during a reload)
            if (textureCache.containsKey(blockId)) {
                continue;
            }

            try {
                NativeImage image = null;
                if ("url".equals(textureType)) {
                    LOGGER.info("Attempting to download texture for '{}' from URL: {}", blockId, textureValue);
                    image = downloadImage(textureValue);
                } else if ("base64".equals(textureType)) {
                    LOGGER.info("Attempting to decode Base64 texture for '{}'", blockId);
                    image = decodeBase64Image(textureValue);
                }

                if (image != null) {
                    DynamicTexture dynamicTexture = new DynamicTexture(image);
                    textureManager.register(textureLocation, dynamicTexture);
                    textureCache.put(blockId, textureLocation);
                    LOGGER.info("Successfully registered dynamic texture for '{}' at {}", blockId, textureLocation);
                } else if (!"local".equals(textureType)) {
                    LOGGER.error("Failed to load dynamic texture for '{}'. Type: {}, Value: {}", blockId, textureType, textureValue);
                }
            } catch (Exception e) {
                LOGGER.error("Exception occurred while processing dynamic texture for '{}'. Type: {}, Value: {}", blockId, textureType, textureValue, e);
            }
        }
    }

    private static NativeImage downloadImage(String urlString) throws IOException {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000); // 5 seconds connection timeout
            connection.setReadTimeout(10000); // 10 seconds read timeout
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
                // Use NativeImage.read to parse the image data directly
                // This is preferred over BufferedImage for Minecraft textures
                return NativeImage.read(inputStream);
            } else {
                LOGGER.error("Failed to download image from {}. HTTP Response Code: {}", urlString, responseCode);
                return null;
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Error closing input stream from URL", e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static NativeImage decodeBase64Image(String base64String) throws IOException {
        // Remove data URI prefix if present (e.g., 
        if (base64String.startsWith("data:image/")) {
            base64String = base64String.substring(base64String.indexOf(",") + 1);
        }

        byte[] imageBytes = Base64.getDecoder().decode(base64String);
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            // Use NativeImage.read for consistency and direct Minecraft compatibility
            return NativeImage.read(inputStream);
        }
    }

    /**
     * Retrieves the ResourceLocation for a block's texture.
     * For 'local' types, it returns the standard location.
     * For 'url' or 'base64' types, it returns the cached dynamic texture location.
     *
     * @param definition The block definition.
     * @return The appropriate ResourceLocation for the texture.
     */
    public static ResourceLocation getTextureLocation(BlockDefinition definition) {
        String blockId = definition.id();
        String textureType = definition.texture().type().toLowerCase();

        if ("url".equals(textureType) || "base64".equals(textureType)) {
            // Return cached location for dynamic textures
            return textureCache.getOrDefault(blockId, 
                // Fallback to a default missing texture if not found in cache (should not happen if registration is correct)
                new ResourceLocation("minecraft", "textures/missing_no.png")); 
        } else {
            // Default behavior for 'local' textures
            String textureName = definition.texture().value();
            return new ResourceLocation(YourModName.MODID, "block/" + textureName);
        }
    }

     /**
     * Generates the necessary blockstate, block model, and item model JSON content dynamically.
     * This assumes a simple cube model for all dynamically textured blocks.
     *
     * @param definition The block definition.
     * @return A map containing the JSON content for blockstate, block model, and item model.
     */
    public static Map<String, String> generateDynamicBlockResources(BlockDefinition definition) {
        String blockId = definition.id();
        // Use the block ID itself as the texture name within the dynamic registration
        ResourceLocation textureLoc = new ResourceLocation(YourModName.MODID, "block/" + blockId);

        String blockstateJson = String.format("{\"variants\": {\"\": { \"model\": \"%s:block/%s\" }}}", YourModName.MODID, blockId);
        // Use minecraft:block/cube_all as parent, but override the 'all' texture
        String blockModelJson = String.format("{\"parent\": \"minecraft:block/cube_all\", \"textures\": {\"all\": \"%s\"}}", textureLoc.toString());
        String itemModelJson = String.format("{\"parent\": \"%s:block/%s\"}", YourModName.MODID, blockId);

        return Map.of(
            "blockstate", blockstateJson,
            "block_model", blockModelJson,
            "item_model", itemModelJson
        );
    }
}

