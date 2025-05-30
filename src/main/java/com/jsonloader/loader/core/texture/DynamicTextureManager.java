package com.jsonloader.loader.core.texture;

import com.jsonloader.loader.JSONloader;
import com.jsonloader.loader.core.loader.BlockDefinition;
import com.jsonloader.loader.core.loader.ItemDefinition;
import com.jsonloader.loader.core.loader.TextureDefinition;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;

/**
 * Gerenciador de texturas dinâmicas para mods JSON.
 * Esta classe processa e registra texturas para blocos e itens definidos em JSON.
 */
public class DynamicTextureManager {
    private static final Logger LOGGER = LogManager.getLogger(JSONloader.MODID + " TextureManager");
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/jsonloader_textures";

    /**
     * Inicializa o gerenciador de texturas dinâmicas.
     */
    public static void initialize() {
        // Cria o diretório temporário se não existir
        File tempDir = new File(TEMP_DIR);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
            LOGGER.info("Diretório temporário para texturas criado: {}", TEMP_DIR);
        }
    }

    /**
     * Registra texturas dinâmicas para blocos.
     * @param blockDefinitions Lista de definições de blocos
     */
    public static void registerDynamicTextures(List<BlockDefinition> blockDefinitions) {
        LOGGER.info("Registrando texturas dinâmicas para {} blocos", blockDefinitions.size());
        
        for (BlockDefinition block : blockDefinitions) {
            try {
                if (block.texture() != null && block.texture().value() != null && !block.texture().value().isEmpty()) {
                    processTexture(block.id(), block.texture(), "block");
                }
            } catch (Exception e) {
                LOGGER.error("Erro ao processar textura para o bloco {}: {}", block.id(), e.getMessage());
            }
        }
    }

    /**
     * Registra texturas dinâmicas para itens.
     * @param itemDefinitions Lista de definições de itens
     */
    public static void registerDynamicItemTextures(List<ItemDefinition> itemDefinitions) {
        LOGGER.info("Registrando texturas dinâmicas para {} itens", itemDefinitions.size());
        
        for (ItemDefinition item : itemDefinitions) {
            try {
                if (item.texture() != null && item.texture().value() != null && !item.texture().value().isEmpty()) {
                    processTexture(item.id(), item.texture(), "item");
                }
            } catch (Exception e) {
                LOGGER.error("Erro ao processar textura para o item {}: {}", item.id(), e.getMessage());
            }
        }
    }

    /**
     * Processa uma textura com base em sua definição.
     * @param id ID do bloco ou item
     * @param texture Definição da textura
     * @param type Tipo (block ou item)
     */
    private static void processTexture(String id, TextureDefinition texture, String type) {
        String textureType = texture.type().toLowerCase();
        String textureValue = texture.value();
        
        // Extrai o modId do id (assumindo formato modid_itemname)
        String modId = JSONloader.MODID; // Valor padrão
        if (id.contains("_")) {
            modId = id.substring(0, id.indexOf("_"));
        }
        
        // Limpa o ID para remover o prefixo do mod, se presente
        String cleanId = id;
        if (id.startsWith(modId + "_")) {
            cleanId = id.substring(modId.length() + 1);
        }
        
        LOGGER.info("Processando textura {} para {}: {} (tipo: {})", textureType, type, cleanId, textureType);
        
        switch (textureType) {
            case "base64":
                processBase64Texture(modId, cleanId, textureValue, type);
                break;
            case "url":
                processUrlTexture(modId, cleanId, textureValue, type);
                break;
            case "local":
                // Texturas locais são gerenciadas pelo sistema de recursos do Minecraft
                LOGGER.info("Textura local para {} {}: {}", type, cleanId, textureValue);
                break;
            default:
                LOGGER.warn("Tipo de textura desconhecido para {} {}: {}", type, cleanId, textureType);
                break;
        }
    }

    /**
     * Processa uma textura codificada em Base64.
     * @param modId ID do mod
     * @param id ID do bloco ou item
     * @param base64Value Valor Base64 da textura
     * @param type Tipo (block ou item)
     */
    private static void processBase64Texture(String modId, String id, String base64Value, String type) {
        try {
            // Decodifica a string Base64
            byte[] imageData = Base64.getDecoder().decode(base64Value);
            
            // Salva a imagem no diretório temporário
            String texturePath = String.format("%s/%s/textures/%s/%s.png", TEMP_DIR, modId, type, id);
            File textureFile = new File(texturePath);
            textureFile.getParentFile().mkdirs();
            
            // Converte os bytes em uma imagem e salva
            try (InputStream is = new ByteArrayInputStream(imageData)) {
                BufferedImage image = ImageIO.read(is);
                if (image != null) {
                    ImageIO.write(image, "PNG", textureFile);
                    LOGGER.info("Textura Base64 salva em: {}", texturePath);
                } else {
                    LOGGER.error("Falha ao decodificar imagem Base64 para {} {}", type, id);
                }
            }
            
            // Notifica o gerenciador de resource pack para incluir esta textura
            DynamicResourcePackManager.clearModResources(modId);
            
        } catch (IllegalArgumentException e) {
            LOGGER.error("Dados Base64 inválidos para {} {}: {}", type, id, e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Erro de I/O ao processar textura Base64 para {} {}: {}", type, id, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Erro ao processar textura Base64 para {} {}: {}", type, id, e.getMessage());
        }
    }

    /**
     * Processa uma textura a partir de uma URL.
     * @param modId ID do mod
     * @param id ID do bloco ou item
     * @param urlValue URL da textura
     * @param type Tipo (block ou item)
     */
    private static void processUrlTexture(String modId, String id, String urlValue, String type) {
        try {
            // Cria o diretório para a textura
            String texturePath = String.format("%s/%s/textures/%s/%s.png", TEMP_DIR, modId, type, id);
            File textureFile = new File(texturePath);
            textureFile.getParentFile().mkdirs();
            
            // Baixa a imagem da URL
            URL url = new URL(urlValue);
            try (InputStream is = url.openStream()) {
                Files.copy(is, textureFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Textura URL baixada para: {}", texturePath);
            }
            
            // Notifica o gerenciador de resource pack para incluir esta textura
            DynamicResourcePackManager.clearModResources(modId);
            
        } catch (IOException e) {
            LOGGER.error("Erro de I/O ao baixar textura URL para {} {}: {}", type, id, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Erro ao processar textura URL para {} {}: {}", type, id, e.getMessage());
        }
    }
}
