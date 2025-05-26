package com.jsonloader.loader.core.init;

import com.jsonloader.loader.JSONloader; // Updated import
import com.jsonloader.loader.core.loader.BlockDefinition;
import com.jsonloader.loader.core.loader.JsonBlockLoader;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.function.Supplier;

public class BlockInit {
    // Corrected logger initialization to use the new MODID
    private static final Logger LOGGER = LogManager.getLogger(JSONloader.MODID + " BlockInit");

    // DeferredRegister for Blocks - Updated MODID
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, JSONloader.MODID);
    // DeferredRegister for BlockItems (associated with Blocks)
    public static final DeferredRegister<Item> ITEMS = ItemInit.ITEMS;

    // Static initializer block to load and register blocks from JSON
    static {
        LOGGER.info("Starting JSON block registration process.");
        List<BlockDefinition> blockDefinitions = JsonBlockLoader.loadBlockDefinitions();

        if (blockDefinitions.isEmpty()) {
            LOGGER.warn("No block definitions found or loaded from JSON. No custom blocks will be registered.");
        } else {
            LOGGER.info("Registering {} blocks defined in JSON...", blockDefinitions.size());
            for (BlockDefinition definition : blockDefinitions) {
                try {
                    registerBlockFromJson(definition);
                    LOGGER.debug("Successfully registered block: {}", definition.id());
                } catch (Exception e) {
                    LOGGER.error("Failed to register block definition with id: {}", definition.id(), e);
                }
            }
            LOGGER.info("Finished JSON block registration.");
        }
    }
    
    /**
     * Registra blocos de um mod externo.
     * @param blocks Lista de definições de blocos
     * @param modId ID do mod para prefixar os blocos
     * @return Número de blocos registrados com sucesso
     */
    public static int registerBlocks(List<BlockDefinition> blocks, String modId) {
        if (blocks == null || blocks.isEmpty()) {
            LOGGER.warn("Nenhum bloco para registrar do mod: {}", modId);
            return 0;
        }
        
        int successCount = 0;
        LOGGER.info("Registrando {} blocos do mod: {}", blocks.size(), modId);
        
        for (BlockDefinition definition : blocks) {
            try {
                // Prefixar o ID do bloco com o ID do mod para evitar conflitos
                String blockId = modId + "_" + definition.id();
                
                // Criar propriedades do bloco
                BlockBehaviour.Properties properties = createBlockProperties(definition);
                
                // Registrar o bloco e seu item correspondente
                Supplier<Block> blockSupplier = () -> new Block(properties);
                RegistryObject<Block> blockObject = BLOCKS.register(blockId, blockSupplier);
                ITEMS.register(blockId, () -> new BlockItem(blockObject.get(), new Item.Properties()));
                
                LOGGER.debug("Bloco registrado com sucesso: {}", blockId);
                successCount++;
            } catch (Exception e) {
                LOGGER.error("Falha ao registrar bloco {} do mod {}: {}", definition.id(), modId, e.getMessage());
            }
        }
        
        LOGGER.info("Registrados com sucesso {} de {} blocos do mod: {}", successCount, blocks.size(), modId);
        return successCount;
    }

    // Helper method to create BlockBehaviour.Properties based on JSON definition
    private static BlockBehaviour.Properties createBlockProperties(BlockDefinition definition) {
        BlockBehaviour.Properties properties;
        String materialStr = definition.material().toLowerCase();

        // Determine base properties from material string using Forge 1.20.1+ methods
        switch (materialStr) {
            case "stone":
                properties = BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE);
                break;
            case "wood":
                properties = BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD);
                break;
            case "metal":
                properties = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL);
                break;
            case "dirt":
                properties = BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).sound(SoundType.GRAVEL);
                break;
            case "sand":
                properties = BlockBehaviour.Properties.of().mapColor(MapColor.SAND).sound(SoundType.SAND);
                break;
            default:
                LOGGER.warn("Unknown material 	'{}' for block 	'{}'	. Defaulting to STONE.", materialStr, definition.id());
                properties = BlockBehaviour.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE);
                break;
        }

        // Apply specific properties from JSON
        properties.strength(definition.properties().hardness(), definition.properties().resistance());

        if (definition.properties().requires_tool()) {
            properties.requiresCorrectToolForDrops();
        }

        return properties;
    }

    // Helper method to register a block and its corresponding BlockItem based on JSON definition
    private static RegistryObject<Block> registerBlockFromJson(BlockDefinition definition) {
        Supplier<Block> blockSupplier = () -> new Block(createBlockProperties(definition));
        RegistryObject<Block> blockObject = BLOCKS.register(definition.id(), blockSupplier);
        ITEMS.register(definition.id(), () -> new BlockItem(blockObject.get(), new Item.Properties()));
        return blockObject;
    }
}

