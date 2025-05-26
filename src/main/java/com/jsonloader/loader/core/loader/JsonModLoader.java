package com.jsonloader.loader.core.loader;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.jsonloader.loader.JSONloader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
    
    // Lista de mods carregados para referência e comando de listagem
    private static final List<LoadedMod> LOADED_MODS = new ArrayList<>();
    
    /**
     * Retorna a lista de mods carregados.
     */
    public static List<LoadedMod> getLoadedMods() {
        return Collections.unmodifiableList(LOADED_MODS);
    }
    
    /**
     * Limpa a lista de mods carregados.
     */
    public static void clearLoadedMods() {
        LOADED_MODS.clear();
        LOGGER.info("Lista de mods carregados foi limpa");
    }

    /**
     * Carrega todos os mods da pasta 'jsonmods'.
     * Cada subpasta é considerada um mod separado.
     */
    public static void loadAllMods() {
        LOGGER.info("=== INICIANDO CARREGAMENTO DE MODS JSON ===");
        LOGGER.info("Diretório de mods: '{}'", JSONMODS_FOLDER);
        
        // Limpa a lista de mods carregados anteriormente
        clearLoadedMods();
        
        // Verifica se a pasta jsonmods existe, se não, cria
        Path jsonmodsPath = Paths.get(JSONMODS_FOLDER);
        if (!Files.exists(jsonmodsPath)) {
            try {
                Files.createDirectories(jsonmodsPath);
                LOGGER.info("[Diretório] Pasta '{}' não existia e foi criada com sucesso", JSONMODS_FOLDER);
            } catch (IOException e) {
                LOGGER.error("[ERRO CRÍTICO] Falha ao criar pasta '{}': {}", JSONMODS_FOLDER, e.getMessage());
                return;
            }
        }
        
        // Lista todas as subpastas (cada uma é um mod)
        try {
            List<Path> modFolders = Files.list(jsonmodsPath)
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());
            
            if (modFolders.isEmpty()) {
                LOGGER.warn("[Aviso] Nenhum mod encontrado na pasta '{}'. Crie subpastas com arquivos mod.json para adicionar mods.", JSONMODS_FOLDER);
            } else {
                LOGGER.info("[Descoberta] Encontrados {} possíveis mods na pasta '{}'", modFolders.size(), JSONMODS_FOLDER);
            }
            
            // Carrega cada mod individualmente
            int successCount = 0;
            for (Path modFolder : modFolders) {
                boolean success = loadMod(modFolder);
                if (success) {
                    successCount++;
                }
            }
            
            LOGGER.info("=== CARREGAMENTO DE MODS CONCLUÍDO ===");
            LOGGER.info("Total de mods encontrados: {}", modFolders.size());
            LOGGER.info("Mods carregados com sucesso: {}", successCount);
            LOGGER.info("Mods com falha no carregamento: {}", modFolders.size() - successCount);
            
        } catch (IOException e) {
            LOGGER.error("[ERRO CRÍTICO] Falha ao listar mods na pasta '{}': {}", JSONMODS_FOLDER, e.getMessage());
            LOGGER.error("Detalhes da exceção:", e);
        }
    }
    
    /**
     * Carrega um mod específico a partir de sua pasta.
     * @return true se o mod foi carregado com sucesso, false caso contrário
     */
    private static boolean loadMod(Path modFolder) {
        String folderName = modFolder.getFileName().toString();
        
        LOGGER.info("[Mod] Iniciando carregamento do mod na pasta '{}'", folderName);
        
        // Carrega o arquivo mod.json
        ModMetadata metadata = loadModMetadata(modFolder);
        if (metadata == null) {
            LOGGER.error("[ERRO] Falha ao carregar mod da pasta '{}': arquivo mod.json ausente ou inválido", folderName);
            return false;
        }
        
        LOGGER.info("[Mod] Carregando mod: {} ({}) versão {}", metadata.name(), metadata.mod_id(), metadata.version());
        
        // Determina os nomes dos arquivos de recursos
        String blocksFile = DEFAULT_BLOCKS_JSON_FILENAME;
        String itemsFile = DEFAULT_ITEMS_JSON_FILENAME;
        String dropsFile = DEFAULT_DROPS_JSON_FILENAME;
        
        if (metadata.assets() != null) {
            if (metadata.assets().blocks_file() != null) {
                blocksFile = metadata.assets().blocks_file();
                LOGGER.debug("[Config] Arquivo de blocos personalizado: {}", blocksFile);
            }
            if (metadata.assets().items_file() != null) {
                itemsFile = metadata.assets().items_file();
                LOGGER.debug("[Config] Arquivo de itens personalizado: {}", itemsFile);
            }
            if (metadata.assets().drops_file() != null) {
                dropsFile = metadata.assets().drops_file();
                LOGGER.debug("[Config] Arquivo de drops personalizado: {}", dropsFile);
            }
        }
        
        try {
            // Carrega blocos, itens e drops do mod
            List<BlockDefinition> blocks = loadBlocksFromMod(modFolder, blocksFile, metadata.mod_id());
            List<ItemDefinition> items = loadItemsFromMod(modFolder, itemsFile, metadata.mod_id());
            DropsDefinition drops = loadDropsFromMod(modFolder, dropsFile, metadata.mod_id());
            
            // Registra os blocos, itens e drops carregados
            if (!blocks.isEmpty() || !items.isEmpty() || drops != null) {
                boolean success = registerModContent(metadata, blocks, items, drops);
                
                if (success) {
                    // Adiciona o mod à lista de mods carregados
                    LoadedMod loadedMod = new LoadedMod(
                        metadata.mod_id(),
                        metadata.name(),
                        metadata.version(),
                        metadata.description(),
                        metadata.author() != null ? metadata.author() : "Desconhecido",
                        blocks.size(),
                        items.size(),
                        drops != null ? 
                            (drops.block_drops() != null ? drops.block_drops().size() : 0) + 
                            (drops.mob_drops() != null ? drops.mob_drops().size() : 0) : 0,
                        modFolder.toString()
                    );
                    LOADED_MODS.add(loadedMod);
                    LOGGER.info("[Sucesso] Mod {} ({}) versão {} carregado com sucesso!", metadata.name(), metadata.mod_id(), metadata.version());
                    return true;
                } else {
                    LOGGER.error("[ERRO] Falha ao registrar conteúdo do mod {} ({})", metadata.name(), metadata.mod_id());
                    return false;
                }
            } else {
                LOGGER.warn("[Aviso] Mod {} ({}) não contém blocos, itens ou drops para registrar", metadata.name(), metadata.mod_id());
                // Ainda consideramos um sucesso, apenas um mod vazio
                LoadedMod loadedMod = new LoadedMod(
                    metadata.mod_id(),
                    metadata.name(),
                    metadata.version(),
                    metadata.description(),
                    metadata.author() != null ? metadata.author() : "Desconhecido",
                    0, 0, 0,
                    modFolder.toString()
                );
                LOADED_MODS.add(loadedMod);
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("[ERRO CRÍTICO] Exceção ao carregar mod {} ({}): {}", 
                metadata.name(), metadata.mod_id(), e.getMessage());
            LOGGER.error("Detalhes da exceção:", e);
            return false;
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
     * @return true se o registro foi bem-sucedido, false caso contrário
     */
    private static boolean registerModContent(ModMetadata metadata, List<BlockDefinition> blocks, List<ItemDefinition> items, DropsDefinition drops) {
        String modId = metadata.mod_id();
        String modName = metadata.name();
        
        try {
            // Registra creative tabs personalizadas
            if (metadata.creative_tabs() != null && !metadata.creative_tabs().isEmpty()) {
                LOGGER.info("[Registro] Registrando {} abas criativas do mod {} ({})", 
                    metadata.creative_tabs().size(), modName, modId);
                
                int registeredTabs = 0;
                for (CreativeTabDefinition tabDef : metadata.creative_tabs()) {
                    try {
                        boolean success = com.jsonloader.loader.core.init.DynamicCreativeTabManager.registerCreativeTab(modId, tabDef);
                        if (success) {
                            registeredTabs++;
                            LOGGER.info("[Registro] Aba criativa '{}' registrada com sucesso para o mod {}", 
                                tabDef.id(), modId);
                        } else {
                            LOGGER.error("[ERRO] Falha ao registrar aba criativa '{}' para o mod {}", 
                                tabDef.id(), modId);
                        }
                    } catch (Exception e) {
                        LOGGER.error("[ERRO] Falha ao registrar aba criativa '{}' para o mod {}: {}", 
                            tabDef.id(), modId, e.getMessage());
                    }
                }
                
                // Registra todas as abas no EventBus
                boolean tabsRegistered = com.jsonloader.loader.core.init.DynamicCreativeTabManager.registerModTabsToEventBus(
                    modId, net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
                
                LOGGER.info("[Registro] Registradas {} de {} abas criativas do mod {}", 
                    registeredTabs, metadata.creative_tabs().size(), modId);
            }
            
            // Registra blocos
            if (!blocks.isEmpty()) {
                LOGGER.info("[Registro] Registrando {} blocos do mod {} ({})", blocks.size(), modName, modId);
                // Chamamos o método que registra os blocos no Minecraft
                int registeredBlocks = com.jsonloader.loader.core.init.BlockInit.registerBlocks(blocks, modId);
                LOGGER.info("[Registro] Registrados {} de {} blocos do mod {}", registeredBlocks, blocks.size(), modId);
                
                if (registeredBlocks < blocks.size()) {
                    LOGGER.warn("[Aviso] Alguns blocos do mod {} não puderam ser registrados ({} de {})", 
                        modId, blocks.size() - registeredBlocks, blocks.size());
                }
            }
            
            // Registra itens
            if (!items.isEmpty()) {
                LOGGER.info("[Registro] Registrando {} itens do mod {} ({})", items.size(), modName, modId);
                // Chamamos o método que registra os itens no Minecraft
                int registeredItems = com.jsonloader.loader.core.init.ItemInit.registerItems(items, modId);
                LOGGER.info("[Registro] Registrados {} de {} itens do mod {}", registeredItems, items.size(), modId);
                
                if (registeredItems < items.size()) {
                    LOGGER.warn("[Aviso] Alguns itens do mod {} não puderam ser registrados ({} de {})", 
                        modId, items.size() - registeredItems, items.size());
                }
            }
            
            // Registra drops
            if (drops != null && 
                ((drops.block_drops() != null && !drops.block_drops().isEmpty()) || 
                 (drops.mob_drops() != null && !drops.mob_drops().isEmpty()))) {
                LOGGER.info("[Registro] Registrando drops do mod {} ({})", modName, modId);
                // Aqui registramos os drops no sistema
                // Por enquanto, apenas logamos que foram carregados
                // TODO: Implementar registro de drops quando o sistema de drops estiver pronto
                int blockDropsCount = drops.block_drops() != null ? drops.block_drops().size() : 0;
                int mobDropsCount = drops.mob_drops() != null ? drops.mob_drops().size() : 0;
                LOGGER.info("[Registro] Carregados {} drops de blocos e {} drops de mobs do mod {}", 
                    blockDropsCount, mobDropsCount, modId);
            }
            
            return true;
        } catch (Exception e) {
            LOGGER.error("[ERRO CRÍTICO] Falha ao registrar conteúdo do mod {} ({}): {}", 
                modName, modId, e.getMessage());
            LOGGER.error("Detalhes da exceção:", e);
            return false;
        }
    }
}

// LoadedMod foi movido para seu próprio arquivo

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
    ModAssets assets,
    List<CreativeTabDefinition> creative_tabs
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
