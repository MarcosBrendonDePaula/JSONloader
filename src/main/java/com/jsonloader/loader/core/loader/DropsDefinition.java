package com.jsonloader.loader.core.loader;

import java.util.List;

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

// Define the record for a single drop entry
public record DropEntry(
    String item_id,
    int count_min,
    int count_max,
    float chance,
    DropCondition conditions
) {
    // Default constructor with default values
    public DropEntry() {
        this("minecraft:stone", 1, 1, 1.0f, new DropCondition());
    }
}

// Define the record for block drops
public record BlockDrop(
    String block_id,
    List<DropEntry> drops
) {}

// Define the record for mob drops
public record MobDrop(
    String mob_id,
    List<DropEntry> drops
) {}

// Define the main record for all drops configuration
public record DropsDefinition(
    List<BlockDrop> block_drops,
    List<MobDrop> mob_drops
) {}
