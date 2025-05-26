package com.jsonloader.loader.core.loader;

import java.util.List;

// Define the record for mob drops
public record MobDrop(
    String mob_id,
    List<DropEntry> drops
) {}
