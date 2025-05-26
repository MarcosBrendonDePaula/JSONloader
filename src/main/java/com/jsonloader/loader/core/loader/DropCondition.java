package com.jsonloader.loader.core.loader;

// Define the record for drop conditions from JSON
public record DropCondition(
    boolean requires_silk_touch,
    boolean requires_tool,
    String min_tool_tier,
    float fortune_multiplier,
    boolean requires_player_kill,
    boolean requires_fire_aspect,
    float looting_multiplier
) {
    // Default constructor with default values
    public DropCondition() {
        this(false, false, "wood", 0.0f, false, false, 0.0f);
    }
}
