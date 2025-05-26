package com.jsonloader.loader.core.loader;

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
