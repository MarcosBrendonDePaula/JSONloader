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
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
                    createPackResources(),                  // Supplier do PackResources
                    new Pack.Info(
                        net.minecraft.network.chat.Component.literal("JSONloader Dynamic Resources"),
                        1,                                  // Format version
                        net.minecraft.network.chat.Component.literal("Texturas dinâmicas para mods JSON")
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
     * Cria o supplier de PackResources para o resource pack dinâmico.
     * @return Um supplier que fornece o PackResources do resource pack dinâmico
     */
    private static Supplier<PackResources> createPackResources() {
        return () -> new AbstractPackResources(PACK_ID) {
            @Override
            protected IoSupplier<InputStream> getResource(String resourcePath) {
                // Extrai o namespace e o caminho relativo
                String[] parts = resourcePath.split("/", 2);
                if (parts.length != 2) {
                    return null;
                }
                
                String namespace = parts[0];
                String path = parts[1];
                
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
                    } catch (IOException e) {
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
                            resourceOutput.accept(new ResourceLocation(namespace, resourcePath), 
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
                                try {
                                    resourceOutput.accept(
                                        new ResourceLocation(namespace, relativePath),
                                        () -> Files.newInputStream(file)
                                    );
                                } catch (IOException e) {
                                    LOGGER.error("[ResourcePack] Erro ao listar recurso {}: {}", 
                                        relativePath, e.getMessage());
                                }
                            });
                    } catch (IOException e) {
                        LOGGER.error("[ResourcePack] Erro ao listar recursos em {}: {}", 
                            resourcePath, e.getMessage());
                    }
                }
            }

            @Override
            public void close() {
                // Nada a fazer aqui
            }

            @Override
            public Collection<ResourceLocation> getResources(PackType type, String namespace, String path, Predicate<ResourceLocation> filter) {
                List<ResourceLocation> resources = new ArrayList<>();
                
                // Adiciona recursos do cache
                if (TEXTURE_CACHE.containsKey(namespace)) {
                    for (String resourcePath : TEXTURE_CACHE.get(namespace).keySet()) {
                        if (resourcePath.startsWith(path)) {
                            ResourceLocation location = new ResourceLocation(namespace, resourcePath);
                            if (filter.test(location)) {
                                resources.add(location);
                            }
                        }
                    }
                }
                
                // Adiciona recursos do sistema de arquivos temporário
                Path namespacePath = TEMP_DIR.resolve(namespace);
                Path resourcePath = namespacePath.resolve(path);
                if (Files.exists(resourcePath) && Files.isDirectory(resourcePath)) {
                    try {
                        Files.walk(resourcePath)
                            .filter(Files::isRegularFile)
                            .forEach(file -> {
                                String relativePath = namespacePath.relativize(file).toString().replace('\\', '/');
                                ResourceLocation location = new ResourceLocation(namespace, relativePath);
                                if (filter.test(location)) {
                                    resources.add(location);
                                }
                            });
                    } catch (IOException e) {
                        LOGGER.error("[ResourcePack] Erro ao listar recursos em {}: {}", 
                            resourcePath, e.getMessage());
                    }
                }
                
                return resources;
            }

            @Override
            public Set<String> getNamespaces(PackType type) {
                Set<String> namespaces = new HashSet<>(TEXTURE_CACHE.keySet());
                
                // Adiciona namespaces do sistema de arquivos temporário
                try {
                    if (Files.exists(TEMP_DIR)) {
                        Files.list(TEMP_DIR)
                            .filter(Files::isDirectory)
                            .forEach(dir -> namespaces.add(dir.getFileName().toString()));
                    }
                } catch (IOException e) {
                    LOGGER.error("[ResourcePack] Erro ao listar namespaces: {}", e.getMessage());
                }
                
                return namespaces;
            }

            @Override
            public Map<ResourceLocation, IoSupplier<InputStream>> listResources(PackType packType, String namespace, String path, int maxDepth, Predicate<ResourceLocation> filter) {
                Map<ResourceLocation, IoSupplier<InputStream>> resources = new HashMap<>();
                
                // Adiciona recursos do cache
                if (TEXTURE_CACHE.containsKey(namespace)) {
                    for (String resourcePath : TEXTURE_CACHE.get(namespace).keySet()) {
                        if (resourcePath.startsWith(path)) {
                            ResourceLocation location = new ResourceLocation(namespace, resourcePath);
                            if (filter.test(location)) {
                                byte[] data = TEXTURE_CACHE.get(namespace).get(resourcePath);
                                resources.put(location, () -> new ByteArrayInputStream(data));
                            }
                        }
                    }
                }
                
                // Adiciona recursos do sistema de arquivos temporário
                Path namespacePath = TEMP_DIR.resolve(namespace);
                Path resourcePath = namespacePath.resolve(path);
                if (Files.exists(resourcePath) && Files.isDirectory(resourcePath)) {
                    try {
                        Files.walk(resourcePath, maxDepth)
                            .filter(Files::isRegularFile)
                            .forEach(file -> {
                                String relativePath = namespacePath.relativize(file).toString().replace('\\', '/');
                                ResourceLocation location = new ResourceLocation(namespace, relativePath);
                                if (filter.test(location)) {
                                    resources.put(location, () -> {
                                        try {
                                            return Files.newInputStream(file);
                                        } catch (IOException e) {
                                            LOGGER.error("[ResourcePack] Erro ao abrir recurso {}: {}", 
                                                relativePath, e.getMessage());
                                            return null;
                                        }
                                    });
                                }
                            });
                    } catch (IOException e) {
                        LOGGER.error("[ResourcePack] Erro ao listar recursos em {}: {}", 
                            resourcePath, e.getMessage());
                    }
                }
                
                return resources;
            }
        };
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
                    if (block.texture() != null && !block.texture().isEmpty()) {
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
                    if (item.texture() != null && !item.texture().isEmpty()) {
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
     * @throws IOException Se ocorrer um erro ao processar a textura
     */
    private static void processBlockTexture(String modId, String blockId, String textureType, String textureValue) throws IOException {
        if ("base64".equalsIgnoreCase(textureType)) {
            // Decodifica a textura Base64
            byte[] textureData = Base64.getDecoder().decode(textureValue);
            
            // Adiciona a textura ao cache
            addTextureToCache(modId, "textures/block/" + blockId + ".png", textureData);
            
            // Salva a textura no sistema de arquivos temporário
            saveTextureToFile(modId, "textures/block/" + blockId + ".png", textureData);
            
            // Gera e adiciona os arquivos de modelo e blockstate
            generateBlockModelFiles(modId, blockId);
        } else if ("url".equalsIgnoreCase(textureType)) {
            // Baixa a textura da URL
            byte[] textureData = downloadTexture(textureValue);
            
            // Adiciona a textura ao cache
            addTextureToCache(modId, "textures/block/" + blockId + ".png", textureData);
            
            // Salva a textura no sistema de arquivos temporário
            saveTextureToFile(modId, "textures/block/" + blockId + ".png", textureData);
            
            // Gera e adiciona os arquivos de modelo e blockstate
            generateBlockModelFiles(modId, blockId);
        } else if ("local".equalsIgnoreCase(textureType)) {
            // Não é necessário processar texturas locais, pois elas já estão no resource pack do mod
            LOGGER.debug("[ResourcePack] Textura local para o bloco {}: {}", blockId, textureValue);
        } else {
            LOGGER.warn("[ResourcePack] Tipo de textura desconhecido para o bloco {}: {}", blockId, textureType);
        }
    }

    /**
     * Processa a textura de um item e a adiciona ao resource pack dinâmico.
     * @param modId O ID do mod
     * @param itemId O ID do item
     * @param textureType O tipo da textura (base64, url, local)
     * @param textureValue O valor da textura (base64, url, caminho local)
     * @throws IOException Se ocorrer um erro ao processar a textura
     */
    private static void processItemTexture(String modId, String itemId, String textureType, String textureValue) throws IOException {
        if ("base64".equalsIgnoreCase(textureType)) {
            // Decodifica a textura Base64
            byte[] textureData = Base64.getDecoder().decode(textureValue);
            
            // Adiciona a textura ao cache
            addTextureToCache(modId, "textures/item/" + itemId + ".png", textureData);
            
            // Salva a textura no sistema de arquivos temporário
            saveTextureToFile(modId, "textures/item/" + itemId + ".png", textureData);
            
            // Gera e adiciona o arquivo de modelo do item
            generateItemModelFile(modId, itemId);
        } else if ("url".equalsIgnoreCase(textureType)) {
            // Baixa a textura da URL
            byte[] textureData = downloadTexture(textureValue);
            
            // Adiciona a textura ao cache
            addTextureToCache(modId, "textures/item/" + itemId + ".png", textureData);
            
            // Salva a textura no sistema de arquivos temporário
            saveTextureToFile(modId, "textures/item/" + itemId + ".png", textureData);
            
            // Gera e adiciona o arquivo de modelo do item
            generateItemModelFile(modId, itemId);
        } else if ("local".equalsIgnoreCase(textureType)) {
            // Não é necessário processar texturas locais, pois elas já estão no resource pack do mod
            LOGGER.debug("[ResourcePack] Textura local para o item {}: {}", itemId, textureValue);
        } else {
            LOGGER.warn("[ResourcePack] Tipo de textura desconhecido para o item {}: {}", itemId, textureType);
        }
    }

    /**
     * Adiciona uma textura ao cache de texturas.
     * @param modId O ID do mod
     * @param path O caminho da textura
     * @param data Os dados da textura
     */
    private static void addTextureToCache(String modId, String path, byte[] data) {
        TEXTURE_CACHE.computeIfAbsent(modId, k -> new HashMap<>()).put(path, data);
        LOGGER.debug("[ResourcePack] Textura adicionada ao cache: {}:{}", modId, path);
    }

    /**
     * Salva uma textura no sistema de arquivos temporário.
     * @param modId O ID do mod
     * @param path O caminho da textura
     * @param data Os dados da textura
     * @throws IOException Se ocorrer um erro ao salvar a textura
     */
    private static void saveTextureToFile(String modId, String path, byte[] data) throws IOException {
        Path filePath = TEMP_DIR.resolve(modId).resolve(path);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, data);
        LOGGER.debug("[ResourcePack] Textura salva em: {}", filePath);
    }

    /**
     * Baixa uma textura de uma URL.
     * @param url A URL da textura
     * @return Os dados da textura
     * @throws IOException Se ocorrer um erro ao baixar a textura
     */
    private static byte[] downloadTexture(String url) throws IOException {
        try (InputStream inputStream = new java.net.URL(url).openStream()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }

    /**
     * Gera e adiciona os arquivos de modelo e blockstate para um bloco.
     * @param modId O ID do mod
     * @param blockId O ID do bloco
     * @throws IOException Se ocorrer um erro ao gerar os arquivos
     */
    private static void generateBlockModelFiles(String modId, String blockId) throws IOException {
        // Gera o arquivo blockstate
        String blockstateJson = String.format(
            "{\"variants\":{\"\":{\"model\":\"%s:block/%s\"}}}", 
            modId, blockId
        );
        byte[] blockstateData = blockstateJson.getBytes();
        addTextureToCache(modId, "blockstates/" + blockId + ".json", blockstateData);
        saveTextureToFile(modId, "blockstates/" + blockId + ".json", blockstateData);
        
        // Gera o arquivo de modelo do bloco
        String blockModelJson = String.format(
            "{\"parent\":\"minecraft:block/cube_all\",\"textures\":{\"all\":\"%s:block/%s\"}}", 
            modId, blockId
        );
        byte[] blockModelData = blockModelJson.getBytes();
        addTextureToCache(modId, "models/block/" + blockId + ".json", blockModelData);
        saveTextureToFile(modId, "models/block/" + blockId + ".json", blockModelData);
        
        // Gera o arquivo de modelo do item do bloco
        String itemModelJson = String.format(
            "{\"parent\":\"%s:block/%s\"}", 
            modId, blockId
        );
        byte[] itemModelData = itemModelJson.getBytes();
        addTextureToCache(modId, "models/item/" + blockId + ".json", itemModelData);
        saveTextureToFile(modId, "models/item/" + blockId + ".json", itemModelData);
    }

    /**
     * Gera e adiciona o arquivo de modelo para um item.
     * @param modId O ID do mod
     * @param itemId O ID do item
     * @throws IOException Se ocorrer um erro ao gerar o arquivo
     */
    private static void generateItemModelFile(String modId, String itemId) throws IOException {
        // Gera o arquivo de modelo do item
        String itemModelJson = String.format(
            "{\"parent\":\"minecraft:item/generated\",\"textures\":{\"layer0\":\"%s:item/%s\"}}", 
            modId, itemId
        );
        byte[] itemModelData = itemModelJson.getBytes();
        addTextureToCache(modId, "models/item/" + itemId + ".json", itemModelData);
        saveTextureToFile(modId, "models/item/" + itemId + ".json", itemModelData);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        // Este método é chamado quando os recursos são recarregados
        LOGGER.info("[ResourcePack] Recursos recarregados");
    }
}
