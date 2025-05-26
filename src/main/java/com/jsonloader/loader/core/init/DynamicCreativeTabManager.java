package com.jsonloader.loader.core.init;

import com.jsonloader.loader.JSONloader;
import com.jsonloader.loader.core.loader.CreativeTabDefinition;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public class DynamicCreativeTabManager {
    // Mapa para armazenar os DeferredRegister de cada mod
    private static final Map<String, DeferredRegister<CreativeModeTab>> MOD_TABS_REGISTERS = new HashMap<>();
    
    // Mapa para armazenar os RegistryObject de cada tab
    private static final Map<String, RegistryObject<CreativeModeTab>> MOD_TABS = new HashMap<>();
    
    /**
     * Registra uma aba criativa personalizada para um mod.
     * 
     * @param modId ID do mod
     * @param tabDef Definição da aba criativa
     * @return true se o registro foi bem-sucedido, false caso contrário
     */
    public static boolean registerCreativeTab(String modId, CreativeTabDefinition tabDef) {
        try {
            // Obtém ou cria o DeferredRegister para o mod
            DeferredRegister<CreativeModeTab> register = MOD_TABS_REGISTERS.computeIfAbsent(
                modId, 
                id -> DeferredRegister.create(Registries.CREATIVE_MODE_TAB, id)
            );
            
            // Determina o item de ícone (usa tijolo como padrão se não especificado)
            String iconItem = tabDef.icon_item() != null ? tabDef.icon_item() : "minecraft:bricks";
            
            // Registra a aba criativa
            String tabId = tabDef.id();
            RegistryObject<CreativeModeTab> tab = register.register(tabId,
                () -> CreativeModeTab.builder()
                    .title(Component.translatable(tabDef.getTranslationKey(modId)))
                    .icon(() -> new ItemStack(Items.BRICKS)) // Temporariamente usa tijolo, depois será substituído pelo item correto
                    .displayItems((parameters, output) -> {
                        // Adiciona todos os itens do mod à aba
                        ItemInit.getModItems(modId).forEach(output::accept);
                    })
                    .build()
            );
            
            // Armazena a referência para uso posterior
            MOD_TABS.put(modId + ":" + tabId, tab);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Registra todas as abas criativas de um mod no EventBus.
     * 
     * @param modId ID do mod
     * @param eventBus EventBus do Forge
     * @return true se o registro foi bem-sucedido, false caso contrário
     */
    public static boolean registerModTabsToEventBus(String modId, net.minecraftforge.eventbus.api.IEventBus eventBus) {
        DeferredRegister<CreativeModeTab> register = MOD_TABS_REGISTERS.get(modId);
        if (register != null) {
            register.register(eventBus);
            return true;
        }
        return false;
    }
    
    /**
     * Obtém uma aba criativa registrada.
     * 
     * @param modId ID do mod
     * @param tabId ID da aba
     * @return RegistryObject da aba criativa, ou null se não encontrada
     */
    public static RegistryObject<CreativeModeTab> getCreativeTab(String modId, String tabId) {
        return MOD_TABS.get(modId + ":" + tabId);
    }
    
    /**
     * Registra todas as abas criativas de todos os mods no EventBus principal.
     * 
     * @param eventBus EventBus principal do Forge
     */
    public static void registerAllTabsToEventBus(net.minecraftforge.eventbus.api.IEventBus eventBus) {
        for (DeferredRegister<CreativeModeTab> register : MOD_TABS_REGISTERS.values()) {
            register.register(eventBus);
        }
    }
}
