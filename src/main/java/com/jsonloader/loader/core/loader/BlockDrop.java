package com.jsonloader.loader.core.loader;

import java.util.List;

// Define the record for block drops
public record BlockDrop(
    String block_id,
    List<DropEntry> drops
) {}
