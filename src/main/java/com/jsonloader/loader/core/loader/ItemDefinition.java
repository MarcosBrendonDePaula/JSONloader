package com.jsonloader.loader.core.loader;

// Define the record for item definitions from JSON
public record ItemDefinition(
    String id, 
    String name, 
    String type, 
    ItemProperties properties, 
    TextureDefinition texture
) {}
