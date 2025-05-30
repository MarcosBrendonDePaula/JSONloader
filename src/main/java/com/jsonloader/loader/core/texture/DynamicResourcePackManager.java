package com.jsonloader.loader.core.texture;

import com.google.common.collect.ImmutableMap;
import com.jsonloader.loader.JSONloader;
import com.jsonloader.loader.core.loader.LoadedMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Gerenciador de resource packs dinâmicos para texturas de mods JSON.
 * Esta classe cria e registra um resource pack virtual que contém todas as texturas
 * extraídas dos mods JSON carregados.
 */
@Mod.EventBusSubscriber(modid = JSONloader.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DynamicResourcePackManager implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogManager.getLogger(JSONloader.MODID + " ResourcePack");
    private static final String PACK_ID = "jsonloader:dynamic_resources";
    private static final Path TEMP_DIR = new File(System.getProperty("java.io.tmpdir"), "jsonloader_resources").toPath();
    private static final Map<String, Map<String, byte[]>> TEXTURE_CACHE = new HashMap<>();

    /**
     * Inicializa o gerenciador de resource pack dinâmico.
     */
    public static void initialize() {
        LOGGER.info("[ResourcePack] Inicializando gerenciador de resource pack dinâmico");
        
        // Cria o diretório temporário se não existir
        try {
            Files.createDirectories(TEMP_DIR);
            LOGGER.info("[ResourcePack] Diretório temporário criado: {}", TEMP_DIR);
        } catch (IOException e) {
            LOGGER.error("[ResourcePack] Erro ao criar diretório temporário: {}", e.getMessage());
        }
    }

    /**
     * Registra o resource pack dinâmico no evento AddPackFindersEvent.
     * Este método é chamado automaticamente pelo Forge durante a inicialização.
     * @param event O evento de registro de resource packs
     */
    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            LOGGER.info("[ResourcePack] Registrando resource pack dinâmico para JSONloader");
            
            // Registra o resource pack
            event.addRepositorySource((packConsumer) -> {
                Pack pack = Pack.create(
                    PACK_ID,                                // ID do pack
                    net.minecraft.network.chat.Component.literal("JSONloader Dynamic Resources"), // Nome visível
                    true,                                   // Required (obrigatório)
                    (packId) -> new DynamicPackResources(packId), // ResourcesSupplier
                    new Pack.Info(
                        net.minecraft.network.chat.Component.literal("JSONloader Dynamic Resources"),
                        9,                                  // Format version (9 para 1.20.1)
                        FeatureFlags.DEFAULT_FLAGS          // Feature flags (padrão)
                    ),
                    PackType.CLIENT_RESOURCES,              // Tipo do pack
                    Pack.Position.TOP,                      // Posição na lista
                    false,                                  // Fixed (fixo)
                    PackSource.DEFAULT                      // Fonte do pack
                );
                
                if (pack != null) {
                    packConsumer.accept(pack);
                    LOGGER.info("[ResourcePack] Resource pack dinâmico registrado com sucesso");
                } else {
                    LOGGER.error("[ResourcePack] Falha ao criar resource pack dinâmico");
                }
            });
        }
    }

    /**
     * Implementação de PackResources para o resource pack dinâmico.
     */
    private static class DynamicPackResources extends AbstractPackResources {
        
        public DynamicPackResources(String packId) {
            super(packId, true); // true = é um pack obrigatório
        }
        
        @Nullable
        @Override
        public IoSupplier<InputStream> getRootResource(String... paths) {
            // Este método é chamado para recursos na raiz do pack, como pack.mcmeta
            if (paths.length == 1 && paths[0].equals("pack.mcmeta")) {
                String packMeta = "{\"pack\":{\"description\":\"JSONloader Dynamic Resources\",\"pack_format\":9}}";
                return () -> new ByteArrayInputStream(packMeta.getBytes());
            }
            return null;
        }

        @Nullable
        @Override
        public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
            if (packType != PackType.CLIENT_RESOURCES) {
                return null;
            }
            
            String namespace = location.getNamespace();
            String path = location.getPath();
            
            // Verifica se a textura está no cache
            if (TEXTURE_CACHE.containsKey(namespace) && TEXTURE_CACHE.get(namespace).containsKey(path)) {
                byte[] data = TEXTURE_CACHE.get(namespace).get(path);
                return () -> new ByteArrayInputStream(data);
            }
            
            // Tenta carregar do sistema de arquivos temporário
            Path filePath = TEMP_DIR.resolve(namespace).resolve(path);
            if (Files.exists(filePath)) {
                try {
                    return () -> Files.newInputStream(filePath);
                } catch (Exception e) {
                    LOGGER.error("[ResourcePack] Erro ao abrir arquivo {}: {}", filePath, e.getMessage());
                    return null;
                }
            }
            
            return null;
        }

        @Override
        public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
            if (packType != PackType.CLIENT_RESOURCES) {
                return;
            }
            
            // Lista recursos do cache
            if (TEXTURE_CACHE.containsKey(namespace)) {
                for (String resourcePath : TEXTURE_CACHE.get(namespace).keySet()) {
                    if (resourcePath.startsWith(path)) {
                        ResourceLocation location = ResourceLocation.parse(namespace + ":" + resourcePath);
                        resourceOutput.accept(location, 
                            () -> new ByteArrayInputStream(TEXTURE_CACHE.get(namespace).get(resourcePath)));
                    }
                }
            }
            
            // Lista recursos do sistema de arquivos temporário
            Path namespacePath = TEMP_DIR.resolve(namespace);
            Path resourcePath = namespacePath.resolve(path);
            if (Files.exists(resourcePath) && Files.isDirectory(resourcePath)) {
                try {
                    Files.walk(resourcePath)
                        .filter(Files::isRegularFile)
                        .forEach(file -> {
                            String relativePath = namespacePath.relativize(file).toString().replace('\\', '/');
                            ResourceLocation location = ResourceLocation.parse(namespace + ":" + relativePath);
                            try {
                                resourceOutput.accept(
                                    location,
                                    () -> Files.newInputStream(file)
                                );
                            } catch (Exception e) {
                                LOGGER.error("[ResourcePack] Erro ao listar recurso {}: {}", 
                                    relativePath, e.getMessage());
                            }
                        });
                } catch (Exception e) {
                    LOGGER.error("[ResourcePack] Erro ao listar recursos em {}: {}", 
                        resourcePath, e.getMessage());
                }
            }
        }

        @Override
        public Set<String> getNamespaces(PackType packType) {
            if (packType != PackType.CLIENT_RESOURCES) {
                return Collections.emptySet();
            }
            
            Set<String> namespaces = new HashSet<>(TEXTURE_CACHE.keySet());
            
            // Adiciona namespaces do sistema de arquivos temporário
            try {
                if (Files.exists(TEMP_DIR)) {
                    Files.list(TEMP_DIR)
                        .filter(Files::isDirectory)
                        .forEach(dir -> namespaces.add(dir.getFileName().toString()));
                }
            } catch (Exception e) {
                LOGGER.error("[ResourcePack] Erro ao listar namespaces: {}", e.getMessage());
            }
            
            return namespaces;
        }

        @Override
        public void close() {
            // Nada a fazer aqui
        }
    }

    /**
     * Processa as texturas de um mod carregado e as adiciona ao resource pack dinâmico.
     * @param mod O mod carregado
     */
    public static void processModTextures(LoadedMod mod) {
        String modId = mod.modId();
        LOGGER.info("[ResourcePack] Processando texturas para o mod: {}", modId);
        
        // Processa texturas de blocos
        if (mod.blocks() != null && !mod.blocks().isEmpty()) {
            mod.blocks().forEach(block -> {
                try {
                    if (block.texture() != null && block.texture().value() != null && !block.texture().value().isEmpty()) {
                        processBlockTexture(modId, block.id(), block.texture().type(), block.texture().value());
                        LOGGER.info("[ResourcePack] Textura do bloco {} processada com sucesso", block.id());
                    }
                } catch (Exception e) {
                    LOGGER.error("[ResourcePack] Erro ao processar textura do bloco {}: {}", 
                        block.id(), e.getMessage());
                }
            });
        }
        
        // Processa texturas de itens
        if (mod.items() != null && !mod.items().isEmpty()) {
            mod.items().forEach(item -> {
                try {
                    if (item.texture() != null && item.texture().value() != null && !item.texture().value().isEmpty()) {
                        processItemTexture(modId, item.id(), item.texture().type(), item.texture().value());
                        LOGGER.info("[ResourcePack] Textura do item {} processada com sucesso", item.id());
                    }
                } catch (Exception e) {
                    LOGGER.error("[ResourcePack] Erro ao processar textura do item {}: {}", 
                        item.id(), e.getMessage());
                }
            });
        }
        
        LOGGER.info("[ResourcePack] Texturas processadas com sucesso para o mod: {}", modId);
    }

    /**
     * Processa a textura de um bloco e a adiciona ao resource pack dinâmico.
     * @param modId O ID do mod
     * @param blockId O ID do bloco
     * @param textureType O tipo da textura (base64, url, local)
     * @param textureValue O valor da textura (base64, url, caminho local)
     */
    private static void processBlockTexture(String modId, String blockId, String textureType, String textureValue) {
        // Corrigido: Remover prefixo do mod_id do blockId para evitar duplicação
        String cleanBlockId = blockId.startsWith(modId + "_") ? blockId.substring(modId.length() + 1) : blockId;
        
        if ("base64".equalsIgnoreCase(textureType)) {
            // Decodifica a textura Base64
            byte[] textureData = Base64.getDecoder().decode(textureValue);
            
            // Adiciona a textura ao cache
            addTextureToCache(modId, "textures/block/" + cleanBlockId + ".png", textureData);
            
            // Salva a textura no sistema de arquivos temporário
            saveTextureToFile(modId, "textures/block/" + cleanBlockId + ".png", textureData);
            
            // Gera e adiciona os arquivos de modelo e blockstate
            generateBlockModelFiles(modId, cleanBlockId);
        } else if ("url".equalsIgnoreCase(textureType)) {
            // Baixa a textura da URL
            byte[] textureData = downloadTexture(textureValue);
            
            // Adiciona a textura ao cache
            addTextureToCache(modId, "textures/block/" + cleanBlockId + ".png", textureData);
            
            // Salva a textura no sistema de arquivos temporário
            saveTextureToFile(modId, "textures/block/" + cleanBlockId + ".png", textureData);
            
            // Gera e adiciona os arquivos de modelo e blockstate
            generateBlockModelFiles(modId, cleanBlockId);
        } else if ("local".equalsIgnoreCase(textureType)) {
            // Não é necessário processar texturas locais, pois elas já estão no resource pack do mod
            LOGGER.debug("[ResourcePack] Textura local para o bloco {}: {}", cleanBlockId, textureValue);
        } else {
            LOGGER.warn("[ResourcePack] Tipo de textura desconhecido para o bloco {}: {}", cleanBlockId, textureType);
        }
    }

    /**
     * Processa a textura de um item e a adiciona ao resource pack dinâmico.
     * @param modId O ID do mod
     * @param itemId O ID do item
     * @param textureType O tipo da textura (base64, url, local)
     * @param textureValue O valor da textura (base64, url, caminho local)
     */
    private static void processItemTexture(String modId, String itemId, String textureType, String textureValue) {
        // Corrigido: Remover prefixo do mod_id do itemId para evitar duplicação
        String cleanItemId = itemId.startsWith(modId + "_") ? itemId.substring(modId.length() + 1) : itemId;
        
        if ("base64".equalsIgnoreCase(textureType)) {
            // Decodifica a textura Base64
            byte[] textureData = Base64.getDecoder().decode(textureValue);
            
            // Adiciona a textura ao cache
            addTextureToCache(modId, "textures/item/" + cleanItemId + ".png", textureData);
            
            // Salva a textura no sistema de arquivos temporário
            saveTextureToFile(modId, "textures/item/" + cleanItemId + ".png", textureData);
            
            // Gera e adiciona o arquivo de modelo do item
            generateItemModelFile(modId, cleanItemId);
        } else if ("url".equalsIgnoreCase(textureType)) {
            // Baixa a textura da URL
            byte[] textureData = downloadTexture(textureValue);
            
            // Adiciona a textura ao cache
            addTextureToCache(modId, "textures/item/" + cleanItemId + ".png", textureData);
            
            // Salva a textura no sistema de arquivos temporário
            saveTextureToFile(modId, "textures/item/" + cleanItemId + ".png", textureData);
            
            // Gera e adiciona o arquivo de modelo do item
            generateItemModelFile(modId, cleanItemId);
        } else if ("local".equalsIgnoreCase(textureType)) {
            // Não é necessário processar texturas locais, pois elas já estão no resource pack do mod
            LOGGER.debug("[ResourcePack] Textura local para o item {}: {}", cleanItemId, textureValue);
        } else {
            LOGGER.warn("[ResourcePack] Tipo de textura desconhecido para o item {}: {}", cleanItemId, textureType);
        }
    }

    /**
     * Adiciona uma textura ao cache.
     * @param namespace O namespace da textura (geralmente o ID do mod)
     * @param path O caminho da textura
     * @param data Os dados da textura
     */
    private static void addTextureToCache(String namespace, String path, byte[] data) {
        TEXTURE_CACHE.computeIfAbsent(namespace, k -> new HashMap<>()).put(path, data);
    }

    /**
     * Salva uma textura no sistema de arquivos temporário.
     * @param namespace O namespace da textura (geralmente o ID do mod)
     * @param path O caminho da textura
     * @param data Os dados da textura
     */
    private static void saveTextureToFile(String namespace, String path, byte[] data) {
        try {
            Path filePath = TEMP_DIR.resolve(namespace).resolve(path);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, data);
        } catch (IOException e) {
            LOGGER.error("[ResourcePack] Erro ao salvar textura {}: {}", path, e.getMessage());
        }
    }

    /**
     * Baixa uma textura de uma URL.
     * @param urlString A URL da textura
     * @return Os dados da textura
     */
    private static byte[] downloadTexture(String urlString) {
        try {
            URL url = new URL(urlString);
            try (InputStream in = url.openStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                return out.toByteArray();
            }
        } catch (IOException e) {
            LOGGER.error("[ResourcePack] Erro ao baixar textura da URL {}: {}", urlString, e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Gera os arquivos de modelo e blockstate para um bloco.
     * @param modId O ID do mod
     * @param blockId O ID do bloco (já limpo, sem prefixo do mod)
     */
    private static void generateBlockModelFiles(String modId, String blockId) {
        // Gera o arquivo blockstate
        String blockstatePath = "blockstates/" + blockId + ".json";
        String blockstateJson = String.format(
            "{\"variants\":{\"\":{\"model\":\"%s:block/%s\"}}}",
            modId, blockId
        );
        addTextureToCache(modId, blockstatePath, blockstateJson.getBytes());
        saveTextureToFile(modId, blockstatePath, blockstateJson.getBytes());
        
        // Gera o arquivo de modelo do bloco
        String blockModelPath = "models/block/" + blockId + ".json";
        String blockModelJson = String.format(
            "{\"parent\":\"minecraft:block/cube_all\",\"textures\":{\"all\":\"%s:block/%s\"}}",
            modId, blockId
        );
        addTextureToCache(modId, blockModelPath, blockModelJson.getBytes());
        saveTextureToFile(modId, blockModelPath, blockModelJson.getBytes());
        
        // Gera o arquivo de modelo do item do bloco
        String itemModelPath = "models/item/" + blockId + ".json";
        String itemModelJson = String.format(
            "{\"parent\":\"%s:block/%s\"}",
            modId, blockId
        );
        addTextureToCache(modId, itemModelPath, itemModelJson.getBytes());
        saveTextureToFile(modId, itemModelPath, itemModelJson.getBytes());
        
        LOGGER.info("[ResourcePack] Arquivos de modelo e blockstate gerados para o bloco {}:{}", modId, blockId);
    }

    /**
     * Gera o arquivo de modelo para um item.
     * @param modId O ID do mod
     * @param itemId O ID do item (já limpo, sem prefixo do mod)
     */
    private static void generateItemModelFile(String modId, String itemId) {
        String itemModelPath = "models/item/" + itemId + ".json";
        String itemModelJson = String.format(
            "{\"parent\":\"minecraft:item/generated\",\"textures\":{\"layer0\":\"%s:item/%s\"}}",
            modId, itemId
        );
        addTextureToCache(modId, itemModelPath, itemModelJson.getBytes());
        saveTextureToFile(modId, itemModelPath, itemModelJson.getBytes());
        
        LOGGER.info("[ResourcePack] Arquivo de modelo gerado para o item {}:{}", modId, itemId);
    }

    /**
     * Limpa o cache de texturas e os arquivos temporários para um mod específico.
     * Isso força a regeneração de todos os recursos na próxima vez que o mod for carregado.
     * @param modId O ID do mod
     */
    public static void clearModResources(String modId) {
        LOGGER.info("[ResourcePack] Limpando recursos do mod: {}", modId);
        
        // Remove do cache
        TEXTURE_CACHE.remove(modId);
        
        // Remove os arquivos temporários
        Path modPath = TEMP_DIR.resolve(modId);
        if (Files.exists(modPath)) {
            try {
                Files.walk(modPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            LOGGER.error("[ResourcePack] Erro ao excluir arquivo {}: {}", path, e.getMessage());
                        }
                    });
                LOGGER.info("[ResourcePack] Recursos do mod {} limpos com sucesso", modId);
            } catch (IOException e) {
                LOGGER.error("[ResourcePack] Erro ao limpar recursos do mod {}: {}", modId, e.getMessage());
            }
        }
    }

    /**
     * Limpa todos os recursos de todos os mods.
     * Isso força a regeneração de todos os recursos na próxima vez que os mods forem carregados.
     */
    public static void clearAllResources() {
        LOGGER.info("[ResourcePack] Limpando todos os recursos");
        
        // Limpa o cache
        TEXTURE_CACHE.clear();
        
        // Remove todos os arquivos temporários
        if (Files.exists(TEMP_DIR)) {
            try {
                Files.walk(TEMP_DIR)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            LOGGER.error("[ResourcePack] Erro ao excluir arquivo {}: {}", path, e.getMessage());
                        }
                    });
                LOGGER.info("[ResourcePack] Todos os recursos limpos com sucesso");
            } catch (IOException e) {
                LOGGER.error("[ResourcePack] Erro ao limpar todos os recursos: {}", e.getMessage());
            }
        }
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        // Este método é chamado quando os recursos são recarregados
        LOGGER.info("[ResourcePack] Recursos recarregados");
    }
}
