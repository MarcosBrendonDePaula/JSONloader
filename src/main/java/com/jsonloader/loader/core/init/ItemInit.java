package com.jsonloader.loader.core.init;

import com.jsonloader.loader.JSONloader;
import com.jsonloader.loader.core.loader.ItemDefinition;
import com.jsonloader.loader.core.loader.ItemProperties;
import com.jsonloader.loader.core.loader.JsonItemLoader;
import com.jsonloader.loader.core.texture.DynamicTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ItemInit {
    private static final Logger LOGGER = LogManager.getLogger(JSONloader.MODID + " ItemInit");
    
    // DeferredRegister for Items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, JSONloader.MODID);

    // Static initializer block to load and register items from JSON
    static {
        LOGGER.info("Starting JSON item registration process.");
        List<ItemDefinition> itemDefinitions = JsonItemLoader.loadItemDefinitions();

        if (itemDefinitions.isEmpty()) {
            LOGGER.warn("No item definitions found or loaded from JSON. No custom items will be registered.");
        } else {
            LOGGER.info("Registering {} items defined in JSON...", itemDefinitions.size());
            for (ItemDefinition definition : itemDefinitions) {
                try {
                    registerItemFromJson(definition);
                    LOGGER.debug("Successfully registered item: {}", definition.id());
                } catch (Exception e) {
                    LOGGER.error("Failed to register item definition with id: {}", definition.id(), e);
                }
            }
            LOGGER.info("Finished JSON item registration.");
        }
    }
    
    /**
     * Registra itens de um mod externo.
     * @param items Lista de definições de itens
     * @param modId ID do mod para prefixar os itens
     * @return Número de itens registrados com sucesso
     */
    public static int registerItems(List<ItemDefinition> items, String modId) {
        if (items == null || items.isEmpty()) {
            LOGGER.warn("Nenhum item para registrar do mod: {}", modId);
            return 0;
        }
        
        int successCount = 0;
        LOGGER.info("Registrando {} itens do mod: {}", items.size(), modId);
        
        for (ItemDefinition definition : items) {
            try {
                // Prefixar o ID do item com o ID do mod para evitar conflitos
                String itemId = modId + "_" + definition.id();
                
                // Criar o item baseado na definição
                Supplier<Item> itemSupplier = () -> createItemFromDefinition(definition);
                ITEMS.register(itemId, itemSupplier);
                
                LOGGER.debug("Item registrado com sucesso: {}", itemId);
                successCount++;
            } catch (Exception e) {
                LOGGER.error("Falha ao registrar item {} do mod {}: {}", definition.id(), modId, e.getMessage());
            }
        }
        
        LOGGER.info("Registrados com sucesso {} de {} itens do mod: {}", successCount, items.size(), modId);
        return successCount;
    }

    // Helper method to register an item based on JSON definition
    private static RegistryObject<Item> registerItemFromJson(ItemDefinition definition) {
        Supplier<Item> itemSupplier = () -> createItemFromDefinition(definition);
        return ITEMS.register(definition.id(), itemSupplier);
    }

    // Helper method to create an Item instance based on the definition
    private static Item createItemFromDefinition(ItemDefinition definition) {
        Item.Properties properties = createItemProperties(definition);
        
        // Create the appropriate item type based on the definition
        switch (definition.type().toLowerCase()) {
            case "food":
                return new Item(properties);
            case "tool_sword":
                return createSword(definition, properties);
            case "tool_pickaxe":
                return createPickaxe(definition, properties);
            case "tool_axe":
                return createAxe(definition, properties);
            case "tool_shovel":
                return createShovel(definition, properties);
            case "tool_hoe":
                return createHoe(definition, properties);
            case "basic":
            default:
                return new Item(properties);
        }
    }

    // Helper method to create Item.Properties based on JSON definition
    private static Item.Properties createItemProperties(ItemDefinition definition) {
        Item.Properties properties = new Item.Properties();
        
        // Set max stack size
        if (definition.properties() != null && definition.properties().max_stack_size() > 0) {
            properties = properties.stacksTo(definition.properties().max_stack_size());
        }
        
        // Set durability for tools
        if (definition.properties() != null && definition.properties().durability() > 0) {
            properties = properties.durability(definition.properties().durability());
        }
        
        // Set food properties if applicable
        if ("food".equalsIgnoreCase(definition.type()) && definition.properties() != null && 
            definition.properties().food_properties() != null) {
            properties = properties.food(createFoodProperties(definition.properties().food_properties()));
        }
        
        return properties;
    }

    // Helper method to create FoodProperties from JSON definition
    private static FoodProperties createFoodProperties(ItemProperties.FoodProperties foodProps) {
        FoodProperties.Builder builder = new FoodProperties.Builder()
            .nutrition(foodProps.nutrition())
            .saturationMod(foodProps.saturation_modifier());
        
        if (foodProps.is_meat()) {
            builder.meat();
        }
        
        if (foodProps.can_always_eat()) {
            builder.alwaysEat();
        }
        
        // Add effects if defined
        if (foodProps.effects() != null) {
            Arrays.stream(foodProps.effects()).forEach(effect -> {
                try {
                    // For simplicity, we're using a fixed set of effects
                    // In a more complete implementation, you'd parse the effect_id string
                    MobEffectInstance effectInstance = new MobEffectInstance(
                        getMobEffectFromId(effect.effect_id()),
                        effect.duration(),
                        effect.amplifier()
                    );
                    builder.effect(effectInstance, effect.probability());
                } catch (Exception e) {
                    LOGGER.error("Failed to add effect {} to food item", effect.effect_id(), e);
                }
            });
        }
        
        return builder.build();
    }
    
    // Helper method to get MobEffect from string ID (simplified version)
    private static net.minecraft.world.effect.MobEffect getMobEffectFromId(String effectId) {
        // This is a simplified implementation - in a real mod, you'd use the registry to look up effects
        switch (effectId.toLowerCase()) {
            case "minecraft:speed": return MobEffects.MOVEMENT_SPEED;
            case "minecraft:slowness": return MobEffects.MOVEMENT_SLOWDOWN;
            case "minecraft:haste": return MobEffects.DIG_SPEED;
            case "minecraft:mining_fatigue": return MobEffects.DIG_SLOWDOWN;
            case "minecraft:strength": return MobEffects.DAMAGE_BOOST;
            case "minecraft:instant_health": return MobEffects.HEAL;
            case "minecraft:instant_damage": return MobEffects.HARM;
            case "minecraft:jump_boost": return MobEffects.JUMP;
            case "minecraft:nausea": return MobEffects.CONFUSION;
            case "minecraft:regeneration": return MobEffects.REGENERATION;
            case "minecraft:resistance": return MobEffects.DAMAGE_RESISTANCE;
            case "minecraft:fire_resistance": return MobEffects.FIRE_RESISTANCE;
            case "minecraft:water_breathing": return MobEffects.WATER_BREATHING;
            case "minecraft:invisibility": return MobEffects.INVISIBILITY;
            case "minecraft:blindness": return MobEffects.BLINDNESS;
            case "minecraft:night_vision": return MobEffects.NIGHT_VISION;
            case "minecraft:hunger": return MobEffects.HUNGER;
            case "minecraft:weakness": return MobEffects.WEAKNESS;
            case "minecraft:poison": return MobEffects.POISON;
            case "minecraft:wither": return MobEffects.WITHER;
            case "minecraft:health_boost": return MobEffects.HEALTH_BOOST;
            case "minecraft:absorption": return MobEffects.ABSORPTION;
            case "minecraft:saturation": return MobEffects.SATURATION;
            case "minecraft:glowing": return MobEffects.GLOWING;
            case "minecraft:levitation": return MobEffects.LEVITATION;
            case "minecraft:luck": return MobEffects.LUCK;
            case "minecraft:unluck": return MobEffects.UNLUCK;
            case "minecraft:slow_falling": return MobEffects.SLOW_FALLING;
            case "minecraft:conduit_power": return MobEffects.CONDUIT_POWER;
            case "minecraft:dolphins_grace": return MobEffects.DOLPHINS_GRACE;
            case "minecraft:bad_omen": return MobEffects.BAD_OMEN;
            case "minecraft:hero_of_the_village": return MobEffects.HERO_OF_THE_VILLAGE;
            default: return MobEffects.REGENERATION; // Default fallback
        }
    }
    
    // Helper methods to create tools based on JSON definition
    private static SwordItem createSword(ItemDefinition definition, Item.Properties properties) {
        Tier tier = getTierFromString(definition.properties().tool_properties().tier());
        float attackDamage = 3.0f + definition.properties().tool_properties().attack_damage_modifier();
        float attackSpeed = -2.4f + definition.properties().tool_properties().attack_speed_modifier();
        
        return new SwordItem(tier, (int)attackDamage, attackSpeed, properties);
    }
    
    private static PickaxeItem createPickaxe(ItemDefinition definition, Item.Properties properties) {
        Tier tier = getTierFromString(definition.properties().tool_properties().tier());
        int attackDamage = 1;
        float attackSpeed = -2.8f + definition.properties().tool_properties().attack_speed_modifier();
        
        // Custom PickaxeItem implementation to allow instantiation (since constructor is protected)
        return new PickaxeItem(tier, attackDamage, attackSpeed, properties) {};
    }
    
    private static AxeItem createAxe(ItemDefinition definition, Item.Properties properties) {
        Tier tier = getTierFromString(definition.properties().tool_properties().tier());
        float attackDamage = 5.0f + definition.properties().tool_properties().attack_damage_modifier();
        float attackSpeed = -3.0f + definition.properties().tool_properties().attack_speed_modifier();
        
        // Custom AxeItem implementation to allow instantiation (since constructor is protected)
        return new AxeItem(tier, attackDamage, attackSpeed, properties) {};
    }
    
    private static ShovelItem createShovel(ItemDefinition definition, Item.Properties properties) {
        Tier tier = getTierFromString(definition.properties().tool_properties().tier());
        float attackDamage = 1.5f + definition.properties().tool_properties().attack_damage_modifier();
        float attackSpeed = -3.0f + definition.properties().tool_properties().attack_speed_modifier();
        
        return new ShovelItem(tier, attackDamage, attackSpeed, properties);
    }
    
    private static HoeItem createHoe(ItemDefinition definition, Item.Properties properties) {
        Tier tier = getTierFromString(definition.properties().tool_properties().tier());
        int attackDamage = -2;
        float attackSpeed = -1.0f + definition.properties().tool_properties().attack_speed_modifier();
        
        // Custom HoeItem implementation to allow instantiation (since constructor is protected)
        return new HoeItem(tier, attackDamage, attackSpeed, properties) {};
    }
    
    // Helper method to get Tier from string
    private static Tier getTierFromString(String tierString) {
        switch (tierString.toLowerCase()) {
            case "wood": return Tiers.WOOD;
            case "stone": return Tiers.STONE;
            case "iron": return Tiers.IRON;
            case "gold": return Tiers.GOLD;
            case "diamond": return Tiers.DIAMOND;
            case "netherite": return Tiers.NETHERITE;
            default: return Tiers.IRON; // Default fallback
        }
    }
}
