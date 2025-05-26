package com.jsonloader.loader;

import com.mojang.logging.LogUtils;
import com.jsonloader.loader.core.init.BlockInit;
import com.jsonloader.loader.core.init.CreativeTabInit;
import com.jsonloader.loader.core.init.ItemInit;
import com.jsonloader.loader.core.loader.BlockDefinition;
import com.jsonloader.loader.core.loader.JsonBlockLoader;
import com.jsonloader.loader.core.loader.JsonItemLoader;
import com.jsonloader.loader.core.loader.JsonDropsLoader;
import com.jsonloader.loader.core.loader.JsonModLoader;
import com.jsonloader.loader.core.texture.DynamicTextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;

// Update Mod annotation with the new MODID
@Mod(JSONloader.MODID)
public class JSONloader { // Renamed class
    // Define mod id in a common place for everything to reference
    public static final String MODID = "jsonloader"; // Updated MODID
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public JSONloader() { // Updated constructor name
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Registers
        BlockInit.BLOCKS.register(modEventBus);
        ItemInit.ITEMS.register(modEventBus);
        CreativeTabInit.CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to vanilla creative tabs event listener
        modEventBus.addListener(this::addItemsToVanillaCreativeTabs);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP FOR {}!", MODID);
        
        // Carregar mods da pasta 'jsonmods'
        event.enqueueWork(() -> {
            LOGGER.info("Iniciando carregamento de mods externos da pasta 'jsonmods'...");
            JsonModLoader.loadAllMods();
            LOGGER.info("Carregamento de mods externos concluído.");
            
            // Ainda carregamos os mods internos para compatibilidade
            LOGGER.info("Carregando definições internas de blocos, itens e drops...");
            JsonBlockLoader.loadBlockDefinitions();
            JsonItemLoader.loadItemDefinitions();
            JsonDropsLoader.loadDropsDefinitions();
            LOGGER.info("Carregamento de definições internas concluído.");
        });
    }

    // Add items to creative tabs
    private void addItemsToVanillaCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        // Add dynamically registered block items to the custom creative tab
        if (event.getTabKey() == CreativeTabInit.YOUR_TAB.getKey()) { // Use the correct tab key
            ItemInit.ITEMS.getEntries().forEach(itemRegistryObject -> {
                LOGGER.debug("Adding item to creative tab: {}", itemRegistryObject.getId());
                event.accept(itemRegistryObject.get());
            });
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting for {}!", MODID);
    }

    // Client-specific setup
    // Update Mod.EventBusSubscriber with the new MODID
    @Mod.EventBusSubscriber(modid = JSONloader.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP FOR {}!", JSONloader.MODID); // Use static MODID reference

            // --- Dynamic Texture Registration ---
            event.enqueueWork(() -> {
                LOGGER.info("Enqueueing dynamic texture registration...");
                List<BlockDefinition> definitions = JsonBlockLoader.loadBlockDefinitions();
                if (!definitions.isEmpty()) {
                    DynamicTextureManager.registerDynamicTextures(definitions);
                    LOGGER.info("Dynamic texture registration task submitted.");
                } else {
                    LOGGER.warn("No block definitions loaded for dynamic texture registration.");
                }
            });
        }
    }
}
