package com.jsonloader.loader.core.loader;

// Define the main record to represent a block definition from JSON, now including the TextureDefinition
public record BlockDefinition(String id, String name, String material, BlockProperties properties, TextureDefinition texture) {}

