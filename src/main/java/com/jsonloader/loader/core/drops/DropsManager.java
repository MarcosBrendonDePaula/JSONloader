package com.jsonloader.loader.core.drops;

import com.jsonloader.loader.JSONloader;
import com.jsonloader.loader.core.loader.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Mod.EventBusSubscriber(modid = JSONloader.MODID)
public class DropsManager {
    private static final Logger LOGGER = LogManager.getLogger(JSONloader.MODID + " DropsManager");
    private static final Random RANDOM = new Random();
    
    private static DropsDefinition dropsDefinition;
    private static Map<String, BlockDrop> blockDropsMap = new HashMap<>();
    private static Map<String, MobDrop> mobDropsMap = new HashMap<>();
    
    // Initialize the drops manager
    public static void init() {
        LOGGER.info("Initializing DropsManager...");
        dropsDefinition = JsonDropsLoader.loadDropsDefinitions();
        
        // Index block drops by block ID for faster lookup
        if (dropsDefinition.block_drops() != null) {
            for (BlockDrop blockDrop : dropsDefinition.block_drops()) {
                blockDropsMap.put(blockDrop.block_id(), blockDrop);
            }
            LOGGER.info("Indexed {} block drop definitions", blockDropsMap.size());
        }
        
        // Index mob drops by mob ID for faster lookup
        if (dropsDefinition.mob_drops() != null) {
            for (MobDrop mobDrop : dropsDefinition.mob_drops()) {
                mobDropsMap.put(mobDrop.mob_id(), mobDrop);
            }
            LOGGER.info("Indexed {} mob drop definitions", mobDropsMap.size());
        }
    }
    
    // Event handler for block drops
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState() == null || event.getPlayer() == null) {
            return;
        }
        
        BlockState state = event.getState();
        Block block = state.getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        
        if (blockId == null) {
            return;
        }
        
        String blockIdStr = blockId.toString();
        BlockDrop blockDrop = blockDropsMap.get(blockIdStr);
        
        if (blockDrop == null || blockDrop.drops() == null || blockDrop.drops().isEmpty()) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack tool = player.getMainHandItem();
        
        // Process each drop entry for this block
        for (DropEntry dropEntry : blockDrop.drops()) {
            if (shouldDropItem(dropEntry, tool, player)) {
                int count = calculateDropCount(dropEntry, tool);
                if (count > 0) {
                    // Schedule the drop for after the block is broken
                    // We can't directly add drops here as the block isn't broken yet
                    scheduleBlockDrop(event, dropEntry.item_id(), count);
                }
            }
        }
    }
    
    // Helper method to schedule block drops
    private static void scheduleBlockDrop(BlockEvent.BreakEvent event, String itemId, int count) {
        // In a real implementation, you'd use a more sophisticated approach
        // For simplicity, we're just logging the scheduled drop
        LOGGER.debug("Scheduled drop of {}x {} for block at {}", 
                    count, itemId, event.getPos());
        
        // In a real implementation, you might use a map to store scheduled drops
        // and then handle them in a separate event like BlockEvent.HarvestDropsEvent
    }
    
    // Event handler for mob drops
    @SubscribeEvent
    public static void onEntityDrop(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        Entity killer = event.getSource().getEntity();
        
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityId == null) {
            return;
        }
        
        String entityIdStr = entityId.toString();
        MobDrop mobDrop = mobDropsMap.get(entityIdStr);
        
        if (mobDrop == null || mobDrop.drops() == null || mobDrop.drops().isEmpty()) {
            return;
        }
        
        boolean isPlayerKill = killer instanceof Player;
        ItemStack weapon = isPlayerKill ? ((Player)killer).getMainHandItem() : ItemStack.EMPTY;
        
        // Process each drop entry for this mob
        for (DropEntry dropEntry : mobDrop.drops()) {
            if (shouldDropItemFromMob(dropEntry, weapon, isPlayerKill)) {
                int count = calculateMobDropCount(dropEntry, weapon);
                if (count > 0) {
                    addEntityDrop(event, dropEntry.item_id(), count);
                }
            }
        }
    }
    
    // Helper method to add entity drops
    private static void addEntityDrop(LivingDropsEvent event, String itemId, int count) {
        // In a real implementation, you'd create an ItemEntity and add it to the drops
        // For simplicity, we're just logging the drop
        LOGGER.debug("Added drop of {}x {} for entity {}", 
                    count, itemId, event.getEntity().getName().getString());
        
        // In a real implementation, you'd do something like:
        // ResourceLocation itemRL = new ResourceLocation(itemId);
        // Item item = ForgeRegistries.ITEMS.getValue(itemRL);
        // if (item != null) {
        //     ItemStack stack = new ItemStack(item, count);
        //     ItemEntity itemEntity = new ItemEntity(event.getEntity().level, 
        //                                           event.getEntity().getX(), 
        //                                           event.getEntity().getY(), 
        //                                           event.getEntity().getZ(), 
        //                                           stack);
        //     event.getDrops().add(itemEntity);
        // }
    }
    
    // Helper method to determine if an item should drop from a block
    private static boolean shouldDropItem(DropEntry dropEntry, ItemStack tool, Player player) {
        if (RANDOM.nextFloat() > dropEntry.chance()) {
            return false;
        }
        
        DropCondition conditions = dropEntry.conditions();
        if (conditions == null) {
            return true;
        }
        
        // Check silk touch condition
        boolean hasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;
        if (conditions.requires_silk_touch() && !hasSilkTouch) {
            return false;
        }
        
        // Check tool requirement
        if (conditions.requires_tool() && tool.isEmpty()) {
            return false;
        }
        
        // Check tool tier (simplified implementation)
        if (conditions.requires_tool() && !conditions.min_tool_tier().isEmpty()) {
            if (!isToolTierSufficient(tool, conditions.min_tool_tier())) {
                return false;
            }
        }
        
        return true;
    }
    
    // Helper method to determine if an item should drop from a mob
    private static boolean shouldDropItemFromMob(DropEntry dropEntry, ItemStack weapon, boolean isPlayerKill) {
        if (RANDOM.nextFloat() > dropEntry.chance()) {
            return false;
        }
        
        DropCondition conditions = dropEntry.conditions();
        if (conditions == null) {
            return true;
        }
        
        // Check player kill condition
        if (conditions.requires_player_kill() && !isPlayerKill) {
            return false;
        }
        
        // Check fire aspect condition
        boolean hasFireAspect = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FIRE_ASPECT, weapon) > 0;
        if (conditions.requires_fire_aspect() && !hasFireAspect) {
            return false;
        }
        
        return true;
    }
    
    // Helper method to calculate the number of items to drop from a block
    private static int calculateDropCount(DropEntry dropEntry, ItemStack tool) {
        int baseCount = RANDOM.nextInt(dropEntry.count_max() - dropEntry.count_min() + 1) + dropEntry.count_min();
        
        // Apply fortune bonus if applicable
        DropCondition conditions = dropEntry.conditions();
        if (conditions != null && conditions.fortune_multiplier() > 0) {
            int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
            if (fortuneLevel > 0) {
                float bonus = fortuneLevel * conditions.fortune_multiplier();
                baseCount = Math.round(baseCount * (1.0f + bonus));
            }
        }
        
        return baseCount;
    }
    
    // Helper method to calculate the number of items to drop from a mob
    private static int calculateMobDropCount(DropEntry dropEntry, ItemStack weapon) {
        int baseCount = RANDOM.nextInt(dropEntry.count_max() - dropEntry.count_min() + 1) + dropEntry.count_min();
        
        // Apply looting bonus if applicable
        DropCondition conditions = dropEntry.conditions();
        if (conditions != null && conditions.looting_multiplier() > 0) {
            int lootingLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, weapon);
            if (lootingLevel > 0) {
                float bonus = lootingLevel * conditions.looting_multiplier();
                baseCount = Math.round(baseCount * (1.0f + bonus));
            }
        }
        
        return baseCount;
    }
    
    // Helper method to check if a tool's tier is sufficient
    private static boolean isToolTierSufficient(ItemStack tool, String minTier) {
        // This is a simplified implementation
        // In a real mod, you'd use the tool's harvest level or tier
        
        // Check if the tool can perform a digging action
        boolean canDig = tool.canPerformAction(ToolActions.PICKAXE_DIG) || 
                         tool.canPerformAction(ToolActions.AXE_DIG) || 
                         tool.canPerformAction(ToolActions.SHOVEL_DIG);
        
        if (!canDig) {
            return false;
        }
        
        // For simplicity, we're just checking the tool material based on the item's registry name
        // In a real implementation, you'd use the proper Forge tier system
        String toolId = ForgeRegistries.ITEMS.getKey(tool.getItem()).toString().toLowerCase();
        
        switch (minTier.toLowerCase()) {
            case "wood":
                return true; // Any tool is at least as good as wood
            case "stone":
                return !toolId.contains("wooden");
            case "iron":
                return !toolId.contains("wooden") && !toolId.contains("stone");
            case "diamond":
                return !toolId.contains("wooden") && !toolId.contains("stone") && !toolId.contains("iron") && !toolId.contains("golden");
            case "netherite":
                return toolId.contains("netherite");
            default:
                return true;
        }
    }
}
