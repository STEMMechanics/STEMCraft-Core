package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMBridge;
import com.stemcraft.core.SMDependency;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.event.SMEvent;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;

/**
 * ItemsAdder plugin support
 */
public class SMItemsAdder extends SMFeature {
    private static String dependantName = "ItemsAdder";
    private static String dependantClazz = "dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent";
    private static Boolean dependantReady = false;

    /**
     * Called when the feature is requested to be loaded.
     * 
     * @return If the feature loaded successfully.
     */
    @Override
    public Boolean onLoad() {
        if (!super.onLoad()) {
            return false;
        }

        if (Bukkit.getPluginManager().getPlugin(dependantName) == null) {
            STEMCraft.warning(dependantName + " is not loaded. Features requiring this plugin won't be available");
        }

        return true;
    }

    /**
     * Called when the feature is requested to be enabled.
     * 
     * @return If the feature enabled successfully.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Boolean onEnable() {
        try {
            Class<? extends Event> eventClass = (Class<? extends Event>) Class.forName(dependantClazz);

            SMEvent.register(eventClass, ctx -> {
                dependantReady = true;
                SMDependency.setDependencyReady(dependantName);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        SMBridge.registerParserProvider("itemsadder", (id, string, player) -> {
            return formatString(player, string);
        });

        SMBridge.registerItemStackGlobalProvider("itemsadder", (option, ctx) -> {
            if ("get".equals(option)) {
                return newItemStack(ctx.id + ":" + ctx.name, ctx.quantity);
            } else if ("name".equals(option)) {
                return getMaterialName(ctx.itemStack);
            } else if ("displayname".equals(option)) {
                return getMaterialDisplayName(ctx.itemStack);
            } else if ("list".equals(option)) {
                List<String> items = new ArrayList<>();

                Set<String> namespaces = CustomStack.getNamespacedIdsInRegistry();
                for (String item : namespaces) {
                    if (!item.startsWith("_")) {
                        items.add(item);
                    }
                }

                return items;
            }

            return null;
        });


        return true;
    }

    /**
     * Returns if the ItemsAdder plugin is loaded and ready.
     * 
     * @return If the plugin is ready.
     */
    public static Boolean isItemsAdderReady() {
        return dependantReady;
    }

    /**
     * Create a new item stack using the custom item stack from the ItemsAdder plugin.
     * 
     * @param name The material name.
     * @param quantity The itemstack quantity.
     * @return The itemstack or null if failed.
     */
    public static ItemStack newItemStack(String name, int quantity) {
        if (!dependantReady) {
            return null;
        }

        CustomStack customStack = CustomStack.getInstance(name);
        if (customStack != null) {
            ItemStack itemStack = customStack.getItemStack();
            itemStack.setAmount(quantity);

            return itemStack;
        }

        return null;
    }

    /**
     * Get the material name from an ItemStack.
     * 
     * @param item The itemstack to identify.
     * @return The material name or null.
     */
    public static String getMaterialName(ItemStack item) {
        CustomStack customStack = CustomStack.byItemStack(item);
        if (customStack != null) {
            return customStack.getNamespace();
        }

        return null;
    }

    /**
     * Get the material name from an ItemStack.
     * 
     * @param item The itemstack to identify.
     * @return The material name or null.
     */
    public static String getMaterialDisplayName(ItemStack item) {
        CustomStack customStack = CustomStack.byItemStack(item);
        if (customStack != null) {
            return customStack.getDisplayName();
        }

        return null;
    }

    /**
     * Format a string using the ItemsAdder plugin.
     * 
     * @param player The player related to the string.
     * @param string The string to format.
     * @return The formatted string.
     */
    public static String formatString(Player player, String string) {
        if (dependantReady) {
            if (player != null) {
                return FontImageWrapper.replaceFontImages(string);
            } else {
                return FontImageWrapper.replaceFontImages(player, string);
            }
        }

        return string;
    }
}
