package com.jsonloader.loader.core.loader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jsonloader.loader.JSONloader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Carregador principal para mods JSON externos.
 * Procura por mods na pasta 'jsonmods' e carrega seus blocos, itens e drops.
 */
public class JsonModLoader {
    private static final Logger LOGGER = LogManager.getLogger(JSONloader.MODID + " JsonModLoader");
    private static final Gson GSON = new Gson();
    private static final String JSONMODS_FOLDER = "jsonmods";
    private static final String BLOCKS_JSON_FILENAME = "blocks.json";
    private static final String ITEMS_JSON_FILENAME = "items.json";
    private static final String DROPS_JSON_FILENAME = "drops.json";

    /**
     * Carrega todos os mods da pasta 'jsonmods'.
     * Cada subpasta é considerada um mod separado.
     */
    public static void loadAllMods() {
        LOGGER.info("Iniciando carregamento de mods da pasta '{}'", JSONMODS_FOLDER);
        
        // Verifica se a pasta jsonmods existe, se não, cria
        Path jsonmodsPath = Paths.get(JSONMODS_FOLDER);
        if (!Files.exists(jsonmodsPath)) {
            try {
                Files.createDirectories(jsonmodsPath);
                LOGGER.info("Pasta '{}' criada com sucesso", JSONMODS_FOLDER);
            } catch (IOException e) {
                LOGGER.error("Falha ao criar pasta '{}': {}", JSONMODS_FOLDER, e.getMessage());
                return;
            }
        }
        
        // Lista todas as subpastas (cada uma é um mod)
        try {
            List<Path> modFolders = Files.list(jsonmodsPath)
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());
            
            LOGGER.info("Encontrados {} mods na pasta '{}'", modFolders.size(), JSONMODS_FOLDER);
            
            // Carrega cada mod individualmente
            for (Path modFolder : modFolders) {
                String modName = modFolder.getFileName().toString();
                LOGGER.info("Carregando mod: {}", modName);
                
                // Carrega blocos, itens e drops do mod
                List<BlockDefinition> blocks = loadBlocksFromMod(modFolder);
                List<ItemDefinition> items = loadItemsFromMod(modFolder);
                DropsDefinition drops = loadDropsFromMod(modFolder);
                
                // Registra os blocos, itens e drops carregados
                if (!blocks.isEmpty() || !items.isEmpty() || drops != null) {
                    registerModContent(modName, blocks, items, drops);
                }
            }
            
        } catch (IOException e) {
            LOGGER.error("Erro ao listar mods na pasta '{}': {}", JSONMODS_FOLDER, e.getMessage());
        }
    }
    
    /**
     * Carrega os blocos de um mod específico.
     */
    private static List<BlockDefinition> loadBlocksFromMod(Path modFolder) {
        Path blocksFile = modFolder.resolve(BLOCKS_JSON_FILENAME);
        if (!Files.exists(blocksFile)) {
            LOGGER.info("Arquivo '{}' não encontrado no mod {}", BLOCKS_JSON_FILENAME, modFolder.getFileName());
            return Collections.emptyList();
        }
        
        try (InputStream inputStream = Files.newInputStream(blocksFile)) {
            return loadBlocksFromInputStream(inputStream, modFolder.getFileName().toString());
        } catch (Exception e) {
            LOGGER.error("Erro ao carregar blocos do mod {}: {}", modFolder.getFileName(), e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Carrega os itens de um mod específico.
     */
    private static List<ItemDefinition> loadItemsFromMod(Path modFolder) {
        Path itemsFile = modFolder.resolve(ITEMS_JSON_FILENAME);
        if (!Files.exists(itemsFile)) {
            LOGGER.info("Arquivo '{}' não encontrado no mod {}", ITEMS_JSON_FILENAME, modFolder.getFileName());
            return Collections.emptyList();
        }
        
        try (InputStream inputStream = Files.newInputStream(itemsFile)) {
            return loadItemsFromInputStream(inputStream, modFolder.getFileName().toString());
        } catch (Exception e) {
            LOGGER.error("Erro ao carregar itens do mod {}: {}", modFolder.getFileName(), e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Carrega os drops de um mod específico.
     */
    private static DropsDefinition loadDropsFromMod(Path modFolder) {
        Path dropsFile = modFolder.resolve(DROPS_JSON_FILENAME);
        if (!Files.exists(dropsFile)) {
            LOGGER.info("Arquivo '{}' não encontrado no mod {}", DROPS_JSON_FILENAME, modFolder.getFileName());
            return new DropsDefinition(Collections.emptyList(), Collections.emptyList());
        }
        
        try (InputStream inputStream = Files.newInputStream(dropsFile)) {
            return loadDropsFromInputStream(inputStream, modFolder.getFileName().toString());
        } catch (Exception e) {
            LOGGER.error("Erro ao carregar drops do mod {}: {}", modFolder.getFileName(), e.getMessage());
            return new DropsDefinition(Collections.emptyList(), Collections.emptyList());
        }
    }
    
    /**
     * Processa o stream de entrada para carregar definições de blocos.
     */
    private static List<BlockDefinition> loadBlocksFromInputStream(InputStream inputStream, String modName) {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<BlockDefinition>>() {}.getType();
            List<BlockDefinition> definitions = GSON.fromJson(reader, listType);
            
            if (definitions == null) {
                LOGGER.error("Falha ao analisar blocks.json do mod {}. GSON retornou null.", modName);
                return Collections.emptyList();
            }
            
            LOGGER.info("Carregados com sucesso {} definições de blocos do mod {}.", definitions.size(), modName);
            definitions.forEach(def -> LOGGER.debug("Carregada definição para bloco ID: {}", def.id()));
            
            return definitions;
        } catch (Exception e) {
            LOGGER.error("Erro durante análise JSON do arquivo blocks.json do mod {}", modName, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Processa o stream de entrada para carregar definições de itens.
     */
    private static List<ItemDefinition> loadItemsFromInputStream(InputStream inputStream, String modName) {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<ItemDefinition>>() {}.getType();
            List<ItemDefinition> definitions = GSON.fromJson(reader, listType);
            
            if (definitions == null) {
                LOGGER.error("Falha ao analisar items.json do mod {}. GSON retornou null.", modName);
                return Collections.emptyList();
            }
            
            LOGGER.info("Carregados com sucesso {} definições de itens do mod {}.", definitions.size(), modName);
            definitions.forEach(def -> LOGGER.debug("Carregada definição para item ID: {}", def.id()));
            
            return definitions;
        } catch (Exception e) {
            LOGGER.error("Erro durante análise JSON do arquivo items.json do mod {}", modName, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Processa o stream de entrada para carregar definições de drops.
     */
    private static DropsDefinition loadDropsFromInputStream(InputStream inputStream, String modName) {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<DropsDefinition>() {}.getType();
            DropsDefinition definition = GSON.fromJson(reader, type);
            
            if (definition == null) {
                LOGGER.error("Falha ao analisar drops.json do mod {}. GSON retornou null.", modName);
                return new DropsDefinition(Collections.emptyList(), Collections.emptyList());
            }
            
            int blockDropsCount = definition.block_drops() != null ? definition.block_drops().size() : 0;
            int mobDropsCount = definition.mob_drops() != null ? definition.mob_drops().size() : 0;
            
            LOGGER.info("Carregadas com sucesso definições de drops do mod {}: {} drops de blocos e {} drops de mobs.", 
                        modName, blockDropsCount, mobDropsCount);
            
            if (definition.block_drops() != null) {
                definition.block_drops().forEach(blockDrop -> 
                    LOGGER.debug("Carregada definição de drop para bloco ID: {}", blockDrop.block_id()));
            }
            
            if (definition.mob_drops() != null) {
                definition.mob_drops().forEach(mobDrop -> 
                    LOGGER.debug("Carregada definição de drop para mob ID: {}", mobDrop.mob_id()));
            }
            
            return definition;
        } catch (Exception e) {
            LOGGER.error("Erro durante análise JSON do arquivo drops.json do mod {}", modName, e);
            return new DropsDefinition(Collections.emptyList(), Collections.emptyList());
        }
    }
    
    /**
     * Registra o conteúdo de um mod no sistema.
     */
    private static void registerModContent(String modName, List<BlockDefinition> blocks, List<ItemDefinition> items, DropsDefinition drops) {
        // Aqui chamamos os métodos de registro existentes
        // Podemos adicionar prefixo do mod nos IDs para evitar conflitos
        
        // Registra blocos
        if (!blocks.isEmpty()) {
            LOGGER.info("Registrando {} blocos do mod {}", blocks.size(), modName);
            // Aqui chamamos o método que registra os blocos no Minecraft
            // Por exemplo: BlockInit.registerBlocks(blocks);
        }
        
        // Registra itens
        if (!items.isEmpty()) {
            LOGGER.info("Registrando {} itens do mod {}", items.size(), modName);
            // Aqui chamamos o método que registra os itens no Minecraft
            // Por exemplo: ItemInit.registerItems(items);
        }
        
        // Registra drops
        if (drops != null && 
            ((drops.block_drops() != null && !drops.block_drops().isEmpty()) || 
             (drops.mob_drops() != null && !drops.mob_drops().isEmpty()))) {
            LOGGER.info("Registrando drops do mod {}", modName);
            // Aqui chamamos o método que registra os drops no Minecraft
            // Por exemplo: DropsManager.registerDrops(drops);
        }
    }
}
