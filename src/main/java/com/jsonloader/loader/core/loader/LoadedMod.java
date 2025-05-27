package com.jsonloader.loader.core.loader;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Collections;
import java.util.List;

/**
 * Classe que representa um mod carregado com suas estatísticas.
 */
public record LoadedMod(
    String modId,
    String name,
    String version,
    String description,
    String author,
    String website,
    List<BlockDefinition> blocks,
    List<ItemDefinition> items,
    DropsDefinition drops,
    int dropsCount,
    String directory
) {
    /**
     * Construtor completo para uso em carregamento de mods.
     */
    public LoadedMod(
        String modId,
        String name,
        String version,
        String description,
        String author,
        String website,
        List<BlockDefinition> blocks,
        List<ItemDefinition> items,
        DropsDefinition drops
    ) {
        this(
            modId, 
            name, 
            version, 
            description, 
            author, 
            website,
            blocks != null ? blocks : Collections.emptyList(),
            items != null ? items : Collections.emptyList(),
            drops,
            (drops != null) ? ((drops.block_drops() != null ? drops.block_drops().size() : 0) + 
                (drops.mob_drops() != null ? drops.mob_drops().size() : 0)) : 0,
            ""
        );
    }
    
    /**
     * Retorna uma representação formatada do mod para exibição.
     */
    public Component getFormattedInfo() {
        MutableComponent component = Component.literal("§6" + name + " §7(§b" + modId + "§7) §ev" + version)
            .append(Component.literal("\n§7Autor: §f" + author));
            
        if (website != null && !website.isEmpty()) {
            component.append(Component.literal("\n§7Website: §f" + website));
        }
        
        component.append(Component.literal("\n§7Descrição: §f" + description))
            .append(Component.literal("\n§7Conteúdo: §a" + (blocks != null ? blocks.size() : 0) + 
                " §7blocos, §a" + (items != null ? items.size() : 0) + 
                " §7itens, §a" + dropsCount + " §7drops"));
        return component;
    }
    
    /**
     * Retorna uma representação resumida do mod para listagem.
     */
    public Component getShortInfo() {
        return Component.literal("§6" + name + " §7(§b" + modId + "§7) §ev" + version + " §7- §a" + 
            ((blocks != null ? blocks.size() : 0) + (items != null ? items.size() : 0) + dropsCount) + " §7elementos");
    }
    
    /**
     * Retorna o número de blocos no mod.
     */
    public int blocksCount() {
        return blocks != null ? blocks.size() : 0;
    }
    
    /**
     * Retorna o número de itens no mod.
     */
    public int itemsCount() {
        return items != null ? items.size() : 0;
    }
}
