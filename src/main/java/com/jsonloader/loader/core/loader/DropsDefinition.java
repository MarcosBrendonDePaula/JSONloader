package com.jsonloader.loader.core.loader;

import java.util.List;

// Define the main record for all drops configuration
public record DropsDefinition(
    List<BlockDrop> block_drops,
    List<MobDrop> mob_drops
) {}
