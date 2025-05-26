package com.jsonloader.loader.core.loader;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
 * Procura por mods na pasta 'jsonmods' e carrega seus metadados, blocos, itens e drops.
 */
public class JsonModLoader {
    private static final Logger LOGGER = LogManager.getLogger(JSONloader.MODID + " JsonModLoader");
    private static final Gson GSON = new Gson();
    private static final String JSONMODS_FOLDER = "jsonmods";
    private static final String MOD_JSON_FILENAME = "mod.json";
    private static final String DEFAULT_BLOCKS_JSON_FILENAME = "blocks.json";
    private static final String DEFAULT_ITEMS_JSON_FILENAME = "items.json";
    private static final String DEFAULT_DROPS_JSON_FILENAME = "drops.json";

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
                loadMod(modFolder);
            }
            
        } catch (IOException e) {
            LOGGER.error("Erro ao listar mods na pasta '{}': {}", JSONMODS_FOLDER, e.getMessage());
        }
    }
    
    /**
     * Carrega um mod específico a partir de sua pasta.
     */
    private static void loadMod(Path modFolder) {
        String folderName = modFolder.getFileName().toString();
        
        // Carrega o arquivo mod.json
        ModMetadata metadata = loadModMetadata(modFolder);
        if (metadata == null) {
            LOGGER.error("Falha ao carregar mod da pasta '{}': arquivo mod.json ausente ou inválido", folderName);
            return;
        }
        
        LOGGER.info("Carregando mod: {} ({})", metadata.name(), metadata.mod_id());
        
        // Determina os nomes dos arquivos de recursos
        String blocksFile = DEFAULT_BLOCKS_JSON_FILENAME;
        String itemsFile = DEFAULT_ITEMS_JSON_FILENAME;
        String dropsFile = DEFAULT_DROPS_JSON_FILENAME;
        
        if (metadata.assets() != null) {
            if (metadata.assets().blocks_file() != null) {
                blocksFile = metadata.assets().blocks_file();
            }
            if (metadata.assets().items_file() != null) {
                itemsFile = metadata.assets().items_file();
            }
            if (metadata.assets().drops_file() != null) {
                dropsFile = metadata.assets().drops_file();
            }
        }
        
        // Carrega blocos, itens e drops do mod
        List<BlockDefinition> blocks = loadBlocksFromMod(modFolder, blocksFile, metadata.mod_id());
        List<ItemDefinition> items = loadItemsFromMod(modFolder, itemsFile, metadata.mod_id());
        DropsDefinition drops = loadDropsFromMod(modFolder, dropsFile, metadata.mod_id());
        
        // Registra os blocos, itens e drops carregados
        if (!blocks.isEmpty() || !items.isEmpty() || drops != null) {
            registerModContent(metadata, blocks, items, drops);
        }
    }
    
    /**
     * Carrega os metadados de um mod a partir do arquivo mod.json.
     */
    private static ModMetadata loadModMetadata(Path modFolder) {
        Path modJsonFile = modFolder.resolve(MOD_JSON_FILENAME);
        if (!Files.exists(modJsonFile)) {
            LOGGER.error("Arquivo '{}' não encontrado na pasta {}", MOD_JSON_FILENAME, modFolder.getFileName());
            return null;
        }
        
        try (InputStream inputStream = Files.newInputStream(modJsonFile);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            
            ModMetadata metadata = GSON.fromJson(reader, ModMetadata.class);
            
            if (metadata == null) {
                LOGGER.error("Falha ao analisar mod.json da pasta {}. GSON retornou null.", modFolder.getFileName());
                return null;
            }
            
            // Validação de campos obrigatórios
            if (metadata.mod_id() == null || metadata.mod_id().isEmpty()) {
                LOGGER.error("Campo obrigatório 'mod_id' ausente ou vazio no mod.json da pasta {}", modFolder.getFileName());
                return null;
            }
            
            if (metadata.name() == null || metadata.name().isEmpty()) {
                LOGGER.error("Campo obrigatório 'name' ausente ou vazio no mod.json da pasta {}", modFolder.getFileName());
                return null;
            }
            
            if (metadata.version() == null || metadata.version().isEmpty()) {
                LOGGER.error("Campo obrigatório 'version' ausente ou vazio no mod.json da pasta {}", modFolder.getFileName());
                return null;
            }
            
            if (metadata.description() == null || metadata.description().isEmpty()) {
                LOGGER.error("Campo obrigatório 'description' ausente ou vazio no mod.json da pasta {}", modFolder.getFileName());
                return null;
            }
            
            LOGGER.info("Metadados do mod carregados com sucesso: {} ({})", metadata.name(), metadata.mod_id());
            return metadata;
            
        } catch (JsonSyntaxException e) {
            LOGGER.error("Erro de sintaxe JSON no arquivo mod.json da pasta {}: {}", modFolder.getFileName(), e.getMessage());
            return null;
        } catch (Exception e) {
            LOGGER.error("Erro ao carregar mod.json da pasta {}: {}", modFolder.getFileName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Carrega os blocos de um mod específico.
     */
    private static List<BlockDefinition> loadBlocksFromMod(Path modFolder, String blocksFileName, String modId) {
        Path blocksFile = modFolder.resolve(blocksFileName);
        if (!Files.exists(blocksFile)) {
            LOGGER.info("Arquivo '{}' não encontrado no mod {}", blocksFileName, modId);
            return Collections.emptyList();
        }
        
        try (InputStream inputStream = Files.newInputStream(blocksFile)) {
            return loadBlocksFromInputStream(inputStream, modId);
        } catch (Exception e) {
            LOGGER.error("Erro ao carregar blocos do mod {}: {}", modId, e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Carrega os itens de um mod específico.
     */
    private static List<ItemDefinition> loadItemsFromMod(Path modFolder, String itemsFileName, String modId) {
        Path itemsFile = modFolder.resolve(itemsFileName);
        if (!Files.exists(itemsFile)) {
            LOGGER.info("Arquivo '{}' não encontrado no mod {}", itemsFileName, modId);
            return Collections.emptyList();
        }
        
        try (InputStream inputStream = Files.newInputStream(itemsFile)) {
            return loadItemsFromInputStream(inputStream, modId);
        } catch (Exception e) {
            LOGGER.error("Erro ao carregar itens do mod {}: {}", modId, e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Carrega os drops de um mod específico.
     */
    private static DropsDefinition loadDropsFromMod(Path modFolder, String dropsFileName, String modId) {
        Path dropsFile = modFolder.resolve(dropsFileName);
        if (!Files.exists(dropsFile)) {
            LOGGER.info("Arquivo '{}' não encontrado no mod {}", dropsFileName, modId);
            return new DropsDefinition(Collections.emptyList(), Collections.emptyList());
        }
        
        try (InputStream inputStream = Files.newInputStream(dropsFile)) {
            return loadDropsFromInputStream(inputStream, modId);
        } catch (Exception e) {
            LOGGER.error("Erro ao carregar drops do mod {}: {}", modId, e.getMessage());
            return new DropsDefinition(Collections.emptyList(), Collections.emptyList());
        }
    }
    
    /**
     * Processa o stream de entrada para carregar definições de blocos.
     */
    private static List<BlockDefinition> loadBlocksFromInputStream(InputStream inputStream, String modId) {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<BlockDefinition>>() {}.getType();
            List<BlockDefinition> definitions = GSON.fromJson(reader, listType);
            
            if (definitions == null) {
                LOGGER.error("Falha ao analisar blocks.json do mod {}. GSON retornou null.", modId);
                return Collections.emptyList();
            }
            
            LOGGER.info("Carregados com sucesso {} definições de blocos do mod {}.", definitions.size(), modId);
            definitions.forEach(def -> LOGGER.debug("Carregada definição para bloco ID: {}", def.id()));
            
            return definitions;
        } catch (Exception e) {
            LOGGER.error("Erro durante análise JSON do arquivo blocks.json do mod {}", modId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Processa o stream de entrada para carregar definições de itens.
     */
    private static List<ItemDefinition> loadItemsFromInputStream(InputStream inputStream, String modId) {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<ItemDefinition>>() {}.getType();
            List<ItemDefinition> definitions = GSON.fromJson(reader, listType);
            
            if (definitions == null) {
                LOGGER.error("Falha ao analisar items.json do mod {}. GSON retornou null.", modId);
                return Collections.emptyList();
            }
            
            LOGGER.info("Carregados com sucesso {} definições de itens do mod {}.", definitions.size(), modId);
            definitions.forEach(def -> LOGGER.debug("Carregada definição para item ID: {}", def.id()));
            
            return definitions;
        } catch (Exception e) {
            LOGGER.error("Erro durante análise JSON do arquivo items.json do mod {}", modId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Processa o stream de entrada para carregar definições de drops.
     */
    private static DropsDefinition loadDropsFromInputStream(InputStream inputStream, String modId) {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<DropsDefinition>() {}.getType();
            DropsDefinition definition = GSON.fromJson(reader, type);
            
            if (definition == null) {
                LOGGER.error("Falha ao analisar drops.json do mod {}. GSON retornou null.", modId);
                return new DropsDefinition(Collections.emptyList(), Collections.emptyList());
            }
            
            int blockDropsCount = definition.block_drops() != null ? definition.block_drops().size() : 0;
            int mobDropsCount = definition.mob_drops() != null ? definition.mob_drops().size() : 0;
            
            LOGGER.info("Carregadas com sucesso definições de drops do mod {}: {} drops de blocos e {} drops de mobs.", 
                        modId, blockDropsCount, mobDropsCount);
            
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
            LOGGER.error("Erro durante análise JSON do arquivo drops.json do mod {}", modId, e);
            return new DropsDefinition(Collections.emptyList(), Collections.emptyList());
        }
    }
    
    /**
     * Registra o conteúdo de um mod no sistema.
     */
    private static void registerModContent(ModMetadata metadata, List<BlockDefinition> blocks, List<ItemDefinition> items, DropsDefinition drops) {
        String modId = metadata.mod_id();
        String modName = metadata.name();
        
        // Registra blocos
        if (!blocks.isEmpty()) {
            LOGGER.info("Registrando {} blocos do mod {} ({})", blocks.size(), modName, modId);
            // Aqui chamamos o método que registra os blocos no Minecraft
            // Por exemplo: BlockInit.registerBlocks(blocks, modId);
        }
        
        // Registra itens
        if (!items.isEmpty()) {
            LOGGER.info("Registrando {} itens do mod {} ({})", items.size(), modName, modId);
            // Aqui chamamos o método que registra os itens no Minecraft
            // Por exemplo: ItemInit.registerItems(items, modId);
        }
        
        // Registra drops
        if (drops != null && 
            ((drops.block_drops() != null && !drops.block_drops().isEmpty()) || 
             (drops.mob_drops() != null && !drops.mob_drops().isEmpty()))) {
            LOGGER.info("Registrando drops do mod {} ({})", modName, modId);
            // Aqui chamamos o método que registra os drops no Minecraft
            // Por exemplo: DropsManager.registerDrops(drops, modId);
        }
        
        // Registra o mod no sistema para referência futura
        LOGGER.info("Mod {} ({}) versão {} carregado com sucesso!", modName, modId, metadata.version());
    }
}

/**
 * Classe que representa os metadados de um mod.
 */
record ModMetadata(
    String mod_id,
    String name,
    String version,
    String description,
    String author,
    String website,
    List<ModDependency> dependencies,
    ModAssets assets
) {}

/**
 * Classe que representa uma dependência de mod.
 */
record ModDependency(
    String mod_id,
    String version_required
) {}

/**
 * Classe que representa a configuração de arquivos de recursos.
 */
record ModAssets(
    String blocks_file,
    String items_file,
    String drops_file
) {}
