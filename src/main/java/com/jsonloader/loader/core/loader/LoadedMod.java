package com.jsonloader.loader.core.loader;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Classe que representa um mod carregado com suas estatísticas.
 */
public record LoadedMod(
    String mod_id,
    String name,
    String version,
    String description,
    String author,
    int blocks_count,
    int items_count,
    int drops_count,
    String directory
) {
    /**
     * Retorna uma representação formatada do mod para exibição.
     */
    public Component getFormattedInfo() {
        MutableComponent component = Component.literal("§6" + name + " §7(§b" + mod_id + "§7) §ev" + version)
            .append(Component.literal("\n§7Autor: §f" + author))
            .append(Component.literal("\n§7Descrição: §f" + description))
            .append(Component.literal("\n§7Conteúdo: §a" + blocks_count + " §7blocos, §a" + items_count + " §7itens, §a" + drops_count + " §7drops"));
        return component;
    }
    
    /**
     * Retorna uma representação resumida do mod para listagem.
     */
    public Component getShortInfo() {
        return Component.literal("§6" + name + " §7(§b" + mod_id + "§7) §ev" + version + " §7- §a" + 
            (blocks_count + items_count + drops_count) + " §7elementos");
    }
}
