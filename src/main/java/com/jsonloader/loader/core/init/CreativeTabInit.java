package com.jsonloader.loader.core.init;

import com.jsonloader.loader.JSONloader; // Updated import
import com.jsonloader.loader.core.loader.CreativeTabDefinition;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CreativeTabInit {
    private static final Logger LOGGER = LogManager.getLogger(JSONloader.MODID + " CreativeTabInit");
    
    // DeferredRegister for CreativeModeTabs - Updated MODID
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JSONloader.MODID);

    // Mapa para armazenar as abas criativas registradas por mod
    private static final Map<String, Map<String, RegistryObject<CreativeModeTab>>> MOD_TABS = new HashMap<>();

    // Renamed tab for dynamically loaded blocks
    public static final RegistryObject<CreativeModeTab> YOUR_TAB = CREATIVE_MODE_TABS.register("dynamic_blocks_tab",
            () -> CreativeModeTab.builder()
                    // Updated translation key to use new MODID
                    .title(Component.translatable("creativetab.jsonloader.dynamic_blocks_tab")) 
                    .icon(() -> new ItemStack(Items.BRICKS))
                    .withTabsBefore(CreativeModeTabs.BUILDING_BLOCKS)
                    .displayItems((parameters, output) -> {
                        // Dynamically add all registered items from ItemInit
                        ItemInit.ITEMS.getEntries().forEach(itemRegistryObject -> {
                            try {
                                output.accept(itemRegistryObject.get());
                            } catch (Exception e) {
                                LOGGER.error("Erro ao adicionar item {} à aba criativa: {}", 
                                    itemRegistryObject.getId(), e.getMessage());
                            }
                        });
                    })
                    .build());
                    
    /**
     * Registra uma aba criativa dinâmica a partir de uma definição JSON.
     * @param tabDefinition Definição da aba criativa
     * @param modId ID do mod
     * @return true se a aba foi registrada com sucesso, false caso contrário
     */
    public static boolean registerDynamicCreativeTab(CreativeTabDefinition tabDefinition, String modId) {
        try {
            String tabId = modId + "_" + tabDefinition.id();
            String iconItemId = tabDefinition.icon_item();
            
            LOGGER.info("Registrando aba criativa dinâmica: {} para o mod {}", tabId, modId);
            
            // Criar o builder da aba criativa
            CreativeModeTab.Builder tabBuilder = CreativeModeTab.builder()
                .title(Component.translatable("creativetab." + modId + "." + tabDefinition.id()))
                .displayItems((parameters, output) -> {
                    // Adicionar todos os itens do mod à aba com tratamento de exceção
                    ItemInit.getModItems(modId).forEach(item -> {
                        try {
                            output.accept(item);
                        } catch (Exception e) {
                            LOGGER.error("Erro ao adicionar item à aba criativa {}: {}", tabId, e.getMessage());
                        }
                    });
                });
            
            // Configurar o ícone da aba com tratamento de segurança
            tabBuilder.icon(() -> {
                try {
                    // Verificar se o iconItemId está definido
                    if (iconItemId == null || iconItemId.isEmpty()) {
                        LOGGER.warn("Ícone não especificado para a aba {}. Usando item padrão.", tabId);
                        return new ItemStack(Items.BARRIER);
                    }
                    
                    // Prefixar com modId se necessário
                    String fullIconId = iconItemId.contains(":") ? iconItemId : modId + ":" + iconItemId;
                    
                    // Procurar o item no registro
                    Optional<RegistryObject<net.minecraft.world.item.Item>> iconItem = ItemInit.ITEMS.getEntries().stream()
                        .filter(entry -> entry.getId().toString().equals(fullIconId) || 
                                         entry.getId().toString().equals(JSONloader.MODID + ":" + iconItemId))
                        .findFirst();
                    
                    if (iconItem.isPresent()) {
                        try {
                            return new ItemStack(iconItem.get().get());
                        } catch (Exception e) {
                            LOGGER.error("Erro ao obter item {} para ícone da aba {}: {}", 
                                fullIconId, tabId, e.getMessage());
                            return new ItemStack(Items.BARRIER);
                        }
                    } else {
                        LOGGER.warn("Ícone {} não encontrado para a aba {}. Usando item padrão.", 
                            fullIconId, tabId);
                        return new ItemStack(Items.BARRIER);
                    }
                } catch (Exception e) {
                    LOGGER.error("Erro ao configurar ícone para a aba {}: {}", tabId, e.getMessage());
                    return new ItemStack(Items.BARRIER);
                }
            });
            
            // Configurar posição da aba
            try {
                if (tabDefinition.position_before() != null && !tabDefinition.position_before().isEmpty()) {
                    tabBuilder.withTabsBefore(net.minecraft.resources.ResourceLocation.parse(tabDefinition.position_before()));
                } else if (tabDefinition.position_after() != null && !tabDefinition.position_after().isEmpty()) {
                    tabBuilder.withTabsAfter(net.minecraft.resources.ResourceLocation.parse(tabDefinition.position_after()));
                }
            } catch (Exception e) {
                LOGGER.error("Erro ao configurar posição da aba {}: {}", tabId, e.getMessage());
            }
            
            // Registrar a aba
            RegistryObject<CreativeModeTab> tabObject = CREATIVE_MODE_TABS.register(tabId, () -> tabBuilder.build());
            
            // Armazenar a aba no mapa para referência futura
            MOD_TABS.computeIfAbsent(modId, k -> new HashMap<>()).put(tabDefinition.id(), tabObject);
            
            LOGGER.info("Aba criativa {} registrada com sucesso para o mod {}", tabId, modId);
            return true;
        } catch (Exception e) {
            LOGGER.error("Falha ao registrar aba criativa para o mod {}: {}", modId, e.getMessage());
            LOGGER.debug("Detalhes da exceção:", e);
            return false;
        }
    }
    
    /**
     * Obtém uma aba criativa registrada para um mod específico.
     * @param modId ID do mod
     * @param tabId ID da aba
     * @return RegistryObject da aba criativa, ou null se não encontrada
     */
    public static RegistryObject<CreativeModeTab> getModTab(String modId, String tabId) {
        Map<String, RegistryObject<CreativeModeTab>> modTabs = MOD_TABS.get(modId);
        if (modTabs != null) {
            return modTabs.get(tabId);
        }
        return null;
    }
}
