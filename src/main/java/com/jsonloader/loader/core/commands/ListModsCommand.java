package com.jsonloader.loader.core.commands;

import com.jsonloader.loader.JSONloader;
import com.jsonloader.loader.core.loader.JsonModLoader;
import com.jsonloader.loader.core.loader.LoadedMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Comando para listar mods JSON carregados.
 */
public class ListModsCommand {
    private static final Logger LOGGER = LogManager.getLogger(JSONloader.MODID + " ListModsCommand");

    /**
     * Registra o comando no dispatcher.
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LOGGER.info("[Comando] Registrando comando /jsonmods");
        
        dispatcher.register(
            Commands.literal("jsonmods")
                .requires(source -> source.hasPermission(0)) // Nível 0 = todos os jogadores
                .executes(ListModsCommand::listAllMods)
                .then(Commands.literal("list")
                    .executes(ListModsCommand::listAllMods))
                .then(Commands.literal("info")
                    .then(Commands.argument("mod_id", StringArgumentType.word())
                        .executes(context -> showModInfo(context, StringArgumentType.getString(context, "mod_id")))))
                .then(Commands.literal("reload")
                    .requires(source -> source.hasPermission(2)) // Nível 2 = operadores
                    .executes(ListModsCommand::reloadMods))
                .then(Commands.literal("count")
                    .executes(ListModsCommand::countMods))
        );
        
        LOGGER.info("[Comando] Comando /jsonmods registrado com sucesso");
    }
    
    /**
     * Lista todos os mods carregados.
     */
    private static int listAllMods(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<LoadedMod> mods = JsonModLoader.getLoadedMods();
        
        if (mods.isEmpty()) {
            source.sendFailure(Component.literal("§cNenhum mod JSON carregado."));
            return 0;
        }
        
        source.sendSuccess(() -> Component.literal("§2=== Mods JSON Carregados (" + mods.size() + ") ==="), false);
        
        for (int i = 0; i < mods.size(); i++) {
            final int index = i;
            final LoadedMod mod = mods.get(i);
            source.sendSuccess(() -> Component.literal("§7" + (index + 1) + ". ").append(mod.getShortInfo()), false);
        }
        
        source.sendSuccess(() -> Component.literal("§2=== Use /jsonmods info <mod_id> para mais detalhes ==="), false);
        
        LOGGER.info("[Comando] Listados {} mods para {}", mods.size(), source.getTextName());
        return mods.size();
    }
    
    /**
     * Mostra informações detalhadas sobre um mod específico.
     */
    private static int showModInfo(CommandContext<CommandSourceStack> context, String modId) {
        CommandSourceStack source = context.getSource();
        List<LoadedMod> mods = JsonModLoader.getLoadedMods();
        
        LoadedMod mod = mods.stream()
                .filter(m -> m.mod_id().equalsIgnoreCase(modId))
                .findFirst()
                .orElse(null);
        
        if (mod == null) {
            source.sendFailure(Component.literal("§cMod não encontrado: " + modId));
            LOGGER.warn("[Comando] Usuário {} tentou acessar informações do mod inexistente: {}", 
                source.getTextName(), modId);
            return 0;
        }
        
        source.sendSuccess(() -> Component.literal("§2=== Detalhes do Mod ==="), false);
        source.sendSuccess(() -> mod.getFormattedInfo(), false);
        source.sendSuccess(() -> Component.literal("§2====================="), false);
        
        LOGGER.info("[Comando] Exibidas informações do mod {} para {}", modId, source.getTextName());
        return 1;
    }
    
    /**
     * Recarrega todos os mods.
     */
    private static int reloadMods(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        source.sendSuccess(() -> Component.literal("§6Recarregando mods JSON..."), false);
        LOGGER.info("[Comando] Recarregamento de mods solicitado por {}", source.getTextName());
        
        // Limpa a lista de mods carregados
        JsonModLoader.clearLoadedMods();
        
        // Recarrega todos os mods
        JsonModLoader.loadAllMods();
        
        List<LoadedMod> mods = JsonModLoader.getLoadedMods();
        
        source.sendSuccess(() -> Component.literal("§aRecarregamento concluído! " + mods.size() + " mods carregados."), false);
        LOGGER.info("[Comando] Recarregamento concluído, {} mods carregados", mods.size());
        
        return mods.size();
    }
    
    /**
     * Conta os mods carregados e exibe estatísticas.
     */
    private static int countMods(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        List<LoadedMod> mods = JsonModLoader.getLoadedMods();
        
        if (mods.isEmpty()) {
            source.sendFailure(Component.literal("§cNenhum mod JSON carregado."));
            return 0;
        }
        
        int totalBlocks = mods.stream().mapToInt(LoadedMod::blocks_count).sum();
        int totalItems = mods.stream().mapToInt(LoadedMod::items_count).sum();
        int totalDrops = mods.stream().mapToInt(LoadedMod::drops_count).sum();
        
        source.sendSuccess(() -> Component.literal("§2=== Estatísticas de Mods JSON ==="), false);
        source.sendSuccess(() -> Component.literal("§7Total de mods: §a" + mods.size()), false);
        source.sendSuccess(() -> Component.literal("§7Total de blocos: §a" + totalBlocks), false);
        source.sendSuccess(() -> Component.literal("§7Total de itens: §a" + totalItems), false);
        source.sendSuccess(() -> Component.literal("§7Total de drops: §a" + totalDrops), false);
        source.sendSuccess(() -> Component.literal("§7Total de elementos: §a" + (totalBlocks + totalItems + totalDrops)), false);
        
        LOGGER.info("[Comando] Estatísticas exibidas para {}: {} mods, {} blocos, {} itens, {} drops", 
            source.getTextName(), mods.size(), totalBlocks, totalItems, totalDrops);
        
        return mods.size();
    }
}
