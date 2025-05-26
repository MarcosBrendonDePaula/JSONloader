package com.jsonloader.loader.core.loader;

// Define the properties for items
public record ItemProperties(
    int max_stack_size,
    int durability,
    FoodProperties food_properties,
    ToolProperties tool_properties
) {
    // Default constructor with default values
    public ItemProperties() {
        this(64, 0, null, null);
    }
    
    // Nested record for food properties
    public record FoodProperties(
        int nutrition,
        float saturation_modifier,
        boolean is_meat,
        boolean can_always_eat,
        EffectProperty[] effects
    ) {
        // Default constructor with default values
        public FoodProperties() {
            this(4, 0.3f, false, false, new EffectProperty[0]);
        }
    }
    
    // Nested record for effect properties
    public record EffectProperty(
        String effect_id,
        int duration,
        int amplifier,
        float probability
    ) {
        // Default constructor with default values
        public EffectProperty() {
            this("minecraft:regeneration", 200, 0, 1.0f);
        }
    }
    
    // Nested record for tool properties
    public record ToolProperties(
        String tier,
        float attack_damage_modifier,
        float attack_speed_modifier,
        float efficiency
    ) {
        // Default constructor with default values
        public ToolProperties() {
            this("iron", 0.0f, 0.0f, 1.0f);
        }
    }
}
