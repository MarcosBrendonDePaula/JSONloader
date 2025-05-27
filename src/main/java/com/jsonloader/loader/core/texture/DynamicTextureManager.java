package com.jsonloader.loader.core.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.jsonloader.loader.JSONloader;
import com.jsonloader.loader.core.loader.BlockDefinition;
import com.jsonloader.loader.core.loader.ItemDefinition;
import com.jsonloader.loader.core.loader.TextureDefinition;
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

/**
 * Gerenciador de texturas dinâmicas para o JSONloader.
 * Esta classe é responsável por carregar e registrar texturas dinâmicas
 * a partir de URLs ou strings Base64.
 */
public class DynamicTextureManager {

    private static final Logger LOGGER = LogManager.getLogger(JSONloader.MODID + " DynamicTextureManager");
    // Cache to store loaded dynamic textures to avoid redundant processing
    private static final Map<String, ResourceLocation> textureCache = new ConcurrentHashMap<>();

    /**
     * Processa todas as definições de blocos e registra texturas dinâmicas para aquelas
     * especificadas como 'url' ou 'base64'. Este método deve ser chamado durante a configuração do cliente.
     *
     * @param definitions Lista de definições de blocos carregadas do JSON.
     */
    public static void registerDynamicTextures(List<BlockDefinition> definitions) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        for (BlockDefinition def : definitions) {
            if (def.texture() == null) {
                LOGGER.warn("Bloco {} não possui textura definida", def.id());
                continue;
            }
            
            String textureType = def.texture().type().toLowerCase();
            String textureValue = def.texture().value();
            String blockId = def.id();
            ResourceLocation textureLocation = new ResourceLocation(JSONloader.MODID, "block/" + blockId);

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
    
    /**
     * Processa todas as definições de itens e registra texturas dinâmicas para aquelas
     * especificadas como 'url' ou 'base64'. Este método deve ser chamado durante a configuração do cliente.
     *
     * @param definitions Lista de definições de itens carregadas do JSON.
     */
    public static void registerDynamicItemTextures(List<ItemDefinition> definitions) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        for (ItemDefinition def : definitions) {
            if (def.texture() == null) {
                LOGGER.warn("Item {} não possui textura definida", def.id());
                continue;
            }
            
            String textureType = def.texture().type().toLowerCase();
            String textureValue = def.texture().value();
            String itemId = def.id();
            ResourceLocation textureLocation = new ResourceLocation(JSONloader.MODID, "item/" + itemId);

            // Skip if already processed (e.g., during a reload)
            if (textureCache.containsKey("item_" + itemId)) {
                continue;
            }

            try {
                NativeImage image = null;
                if ("url".equals(textureType)) {
                    LOGGER.info("Attempting to download texture for item '{}' from URL: {}", itemId, textureValue);
                    image = downloadImage(textureValue);
                } else if ("base64".equals(textureType)) {
                    LOGGER.info("Attempting to decode Base64 texture for item '{}'", itemId);
                    image = decodeBase64Image(textureValue);
                }

                if (image != null) {
                    DynamicTexture dynamicTexture = new DynamicTexture(image);
                    textureManager.register(textureLocation, dynamicTexture);
                    textureCache.put("item_" + itemId, textureLocation);
                    LOGGER.info("Successfully registered dynamic texture for item '{}' at {}", itemId, textureLocation);
                } else if (!"local".equals(textureType)) {
                    LOGGER.error("Failed to load dynamic texture for item '{}'. Type: {}, Value: {}", itemId, textureType, textureValue);
                }
            } catch (Exception e) {
                LOGGER.error("Exception occurred while processing dynamic texture for item '{}'. Type: {}, Value: {}", itemId, textureType, textureValue, e);
            }
        }
    }

    /**
     * Baixa uma imagem de uma URL e a converte para NativeImage.
     * 
     * @param urlString A URL da imagem
     * @return A imagem baixada como NativeImage, ou null se ocorrer um erro
     * @throws IOException Se ocorrer um erro ao baixar ou processar a imagem
     */
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

    /**
     * Decodifica uma imagem Base64 e a converte para NativeImage.
     * 
     * @param base64String A string Base64 da imagem
     * @return A imagem decodificada como NativeImage, ou null se ocorrer um erro
     * @throws IOException Se ocorrer um erro ao decodificar ou processar a imagem
     */
    private static NativeImage decodeBase64Image(String base64String) throws IOException {
        // Remove data URI prefix if present (e.g., data:image/png;base64,)
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
     * Obtém a ResourceLocation para a textura de um bloco.
     * Para tipos 'local', retorna a localização padrão.
     * Para tipos 'url' ou 'base64', retorna a localização da textura dinâmica em cache.
     *
     * @param definition A definição do bloco.
     * @return A ResourceLocation apropriada para a textura.
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
            return new ResourceLocation(JSONloader.MODID, "block/" + textureName);
        }
    }
    
    /**
     * Obtém a ResourceLocation para a textura de um item.
     * Para tipos 'local', retorna a localização padrão.
     * Para tipos 'url' ou 'base64', retorna a localização da textura dinâmica em cache.
     *
     * @param definition A definição do item.
     * @return A ResourceLocation apropriada para a textura.
     */
    public static ResourceLocation getItemTextureLocation(ItemDefinition definition) {
        String itemId = definition.id();
        String textureType = definition.texture().type().toLowerCase();

        if ("url".equals(textureType) || "base64".equals(textureType)) {
            // Return cached location for dynamic textures
            return textureCache.getOrDefault("item_" + itemId, 
                // Fallback to a default missing texture if not found in cache (should not happen if registration is correct)
                new ResourceLocation("minecraft", "textures/missing_no.png")); 
        } else {
            // Default behavior for 'local' textures
            String textureName = definition.texture().value();
            return new ResourceLocation(JSONloader.MODID, "item/" + textureName);
        }
    }

    /**
     * Gera o conteúdo JSON necessário para blockstate, modelo de bloco e modelo de item dinamicamente.
     * Isso assume um modelo de cubo simples para todos os blocos com texturas dinâmicas.
     *
     * @param definition A definição do bloco.
     * @return Um mapa contendo o conteúdo JSON para blockstate, modelo de bloco e modelo de item.
     */
    public static Map<String, String> generateDynamicBlockResources(BlockDefinition definition) {
        String blockId = definition.id();
        // Use the block ID itself as the texture name within the dynamic registration
        ResourceLocation textureLoc = new ResourceLocation(JSONloader.MODID, "block/" + blockId);

        String blockstateJson = String.format("{\"variants\": {\"\": { \"model\": \"%s:block/%s\" }}}", JSONloader.MODID, blockId);
        // Use minecraft:block/cube_all as parent, but override the 'all' texture
        String blockModelJson = String.format("{\"parent\": \"minecraft:block/cube_all\", \"textures\": {\"all\": \"%s\"}}", textureLoc.toString());
        String itemModelJson = String.format("{\"parent\": \"%s:block/%s\"}", JSONloader.MODID, blockId);

        return Map.of(
            "blockstate", blockstateJson,
            "block_model", blockModelJson,
            "item_model", itemModelJson
        );
    }
    
    /**
     * Gera o conteúdo JSON necessário para o modelo de item dinamicamente.
     *
     * @param definition A definição do item.
     * @return O conteúdo JSON para o modelo de item.
     */
    public static String generateDynamicItemModelJson(ItemDefinition definition) {
        String itemId = definition.id();
        // Use the item ID itself as the texture name within the dynamic registration
        ResourceLocation textureLoc = new ResourceLocation(JSONloader.MODID, "item/" + itemId);

        return String.format("{\"parent\": \"minecraft:item/generated\", \"textures\": {\"layer0\": \"%s\"}}", textureLoc.toString());
    }
}
