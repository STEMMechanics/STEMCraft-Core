package com.stemcraft.feature;

import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.stemcraft.core.SMBridge;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.config.SMConfig;

public class SMCustomItems extends SMFeature {
    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        SMBridge.registerItemStackGlobalProvider("smcustomitems", (option, ctx) -> {
            if ("get".equals(option)) {
                return newItemStack(ctx.id + ":" + ctx.name, ctx.quantity);
            } else if ("name".equals(option)) {
                return getMaterialName(ctx.itemStack);
            } else if ("displayname".equals(option)) {
                return getMaterialDisplayName(ctx.itemStack);
            } else if ("list".equals(option)) {
                List<String> list = SMConfig.main().getKeys("custom_items");
                return list;
            }

            return null;
        });

        return true;
    }

    /**
     * Create a new item stack using the custom item stack from the ItemsAdder plugin.
     * 
     * @param name The material name.
     * @param quantity The itemstack quantity.
     * @return The itemstack or null if failed.
     */
    public static ItemStack newItemStack(String name, int quantity) {
        if (SMConfig.main().contains("custom_items." + name)) {
            String vanillaItemName = SMConfig.main().getString("custom_items." + name + ".item", "stick");
            ItemStack itemStack = SMBridge.newMinecraftItemStack(vanillaItemName, quantity);
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                String displayName = SMConfig.main().getString("custom_items." + name + ".display_name", null);
                Integer customModelData = SMConfig.main().getInt("custom_items." + name + ".custom_model_data", null);

                if (displayName != null) {
                    meta.setDisplayName(displayName);
                }

                if (customModelData != null) {
                    meta.setCustomModelData(customModelData);
                }

                itemStack.setItemMeta(meta);
            }

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
        List<String> list = SMConfig.main().getKeys("custom_items");
        String itemMaterial = item.getType().name();

        for (String listItem : list) {
            String listItemMaterial = SMConfig.main().getString("custom_items." + listItem + ".item", null);

            if (listItemMaterial.toLowerCase().replace("minecraft:", "").equals(itemMaterial)) {
                Integer listItemCustomModelData =
                    SMConfig.main().getInt("custom_items." + listItem + ".custom_model_data", null);

                if (listItemCustomModelData != null) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        if (meta.getCustomModelData() == listItemCustomModelData) {
                            return listItem;
                        }
                    }
                }
            }
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
        List<String> list = SMConfig.main().getKeys("custom_items");
        String itemMaterial = item.getType().name();

        for (String listItem : list) {
            String listItemMaterial = SMConfig.main().getString("custom_items." + listItem + ".item", null);

            if (listItemMaterial.toLowerCase().replace("minecraft:", "").equals(itemMaterial)) {
                Integer listItemCustomModelData =
                    SMConfig.main().getInt("custom_items." + listItem + ".custom_model_data", null);

                if (listItemCustomModelData != null) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        if (meta.getCustomModelData() == listItemCustomModelData) {
                            String displayName =
                                SMConfig.main().getString("custom_items." + listItem + ".display_name", null);
                            if (displayName != null) {
                                return displayName;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }
}
