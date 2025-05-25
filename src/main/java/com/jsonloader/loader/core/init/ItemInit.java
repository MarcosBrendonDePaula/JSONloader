package com.jsonloader.loader.core.init;

import com.jsonloader.loader.JSONloader; // Updated import
import net.minecraft.world.item.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemInit {
    // DeferredRegister for Items - Updated MODID
    // This register will be populated dynamically by BlockInit when registering BlockItems for JSON blocks.
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, JSONloader.MODID);

    // Removed all example item registrations
}

