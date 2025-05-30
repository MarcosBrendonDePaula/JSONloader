package com.jsonloader.loader.core.loader;

import java.util.List;

/**
 * Classe que representa os metadados de um mod JSON.
 * Contém informações como ID, nome, versão, descrição, autor, etc.
 */
public record ModMetadata(
    String mod_id,
    String name,
    String version,
    String description,
    String author,
    String website,
    List<String> dependencies,
    AssetsConfig assets,
    List<CreativeTabDefinition> creative_tabs
) {
    /**
     * Configuração de arquivos de recursos do mod.
     */
    public record AssetsConfig(
        String blocks_file,
        String items_file,
        String drops_file
    ) {}
}
