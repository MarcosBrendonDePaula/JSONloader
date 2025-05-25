package com.jsonloader.loader.core.init;

import com.jsonloader.loader.JSONloader; // Updated import
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTabInit {
    // DeferredRegister for CreativeModeTabs - Updated MODID
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JSONloader.MODID);

    // Renamed tab for dynamically loaded blocks
    public static final RegistryObject<CreativeModeTab> YOUR_TAB = CREATIVE_MODE_TABS.register("dynamic_blocks_tab",
            () -> CreativeModeTab.builder()
                    // Updated translation key to use new MODID
                    .title(Component.translatable("creativetab.jsonloader.dynamic_blocks_tab")) 
                    .icon(() -> new ItemStack(Items.BRICKS))
                    .withTabsBefore(CreativeModeTabs.BUILDING_BLOCKS)
                    .displayItems((parameters, output) -> {
                        // Dynamically add all registered items from ItemInit
                        ItemInit.ITEMS.getEntries().forEach(itemRegistryObject -> output.accept(itemRegistryObject.get()));
                    })
                    .build());
}

