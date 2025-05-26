package com.jsonloader.loader.core.loader;

import java.util.List;

/**
 * Definição de uma aba criativa personalizada para um mod.
 */
public record CreativeTabDefinition(
    String id,
    String title,
    String icon_item,
    String background_texture,
    List<String> search_tags,
    boolean show_search_bar,
    String position_before,
    String position_after
) {
    /**
     * Retorna um ID válido para a aba criativa, combinando o ID do mod com o ID da aba.
     */
    public String getFullId(String modId) {
        return modId + "_" + id;
    }
    
    /**
     * Retorna a chave de tradução para o título da aba.
     */
    public String getTranslationKey(String modId) {
        return "creativetab." + modId + "." + id;
    }
}
