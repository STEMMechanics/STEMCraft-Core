package com.stemcraft.feature;

import java.security.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMCommon;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.SMLocale;
import com.stemcraft.core.SMMessenger;
import com.stemcraft.core.config.SMConfig;
import com.stemcraft.core.event.SMEvent;

public class SMPreventDupes extends SMFeature {
    private static NamespacedKey preventDupeKey = new NamespacedKey(STEMCraft.getPlugin(), "md5");
    private static List<String> materialsTracked = new ArrayList<>();

    @Override
    protected Boolean onEnable() {
        SMPreventDupes.materialsTracked = SMConfig.main().getStringList("prevent-dupes");

        SMEvent.register(InventoryClickEvent.class, ctx -> {
            if (ctx.event.getWhoClicked() instanceof Player) {
                Player player = (Player) ctx.event.getWhoClicked();
                ItemStack clickedItem = ctx.event.getCurrentItem();
                if (clickedItem != null && !clickedItem.getType().isAir()) {
                    if (SMPreventDupes.isItemTracked(clickedItem.getType())) {
                        String md5Checksum = SMCommon.generateMD5(
                            SMPreventDupes.generateHash(player, clickedItem.getType()));
                        NamespacedKey key = new NamespacedKey(STEMCraft.getPlugin(), "md5");
                        ItemMeta itemMeta = clickedItem.getItemMeta();
                        if (itemMeta == null) {
                            return;
                        }

                        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, md5Checksum);
                        clickedItem.setItemMeta(itemMeta);
                    }
                }
            }
        });

        return true;
    }

    /**
     * Is this item being tracked by the feature.
     * 
     * @param itemType The material to check
     * @return If the item is being tracked
     */
    private static Boolean isItemTracked(Material itemType) {
        return SMCommon.listContainsIgnoreCase(materialsTracked, itemType.toString());
    }

    private static String generateHash(Player player, Material material) {
        return (player == null ? "" : player.getName()) + material.name() + System.currentTimeMillis();
    }

    private static String getItemMD5Checksum(ItemStack item) {
        return getOrCreateItemMD5Checksum(item, null);
    }

    private static String getOrCreateItemMD5Checksum(ItemStack item, String hash) {
        String md5Checksum = null;
        ItemMeta itemMeta = item.getItemMeta();

        if (itemMeta != null) {
            md5Checksum = itemMeta.getPersistentDataContainer().get(preventDupeKey, PersistentDataType.STRING);
            if (md5Checksum == null) {
                return null;
            } else if (hash != null) {
                md5Checksum = SMCommon.generateMD5(hash);
                if (md5Checksum != null) {
                    itemMeta.getPersistentDataContainer().set(preventDupeKey, PersistentDataType.STRING, md5Checksum);
                    item.setItemMeta(itemMeta);
                }
            }
        }

        return md5Checksum;
    }

    // check item against inventory
    // clean inventories of duplicates

    private static void cleanInventory(Inventory inventory, Player player) {
        List<String> md5ChecksumList = new ArrayList<>();
        ItemStack[] items = inventory.getContents();

        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR && isItemTracked(item.getType())) {
                String hash = generateHash(player, item.getType());

                String md5Checksum = getOrCreateItemMD5Checksum(item, hash);
                if (md5ChecksumList.contains(md5Checksum)) {
                    // item already exists in inventory
                    if (player != null) {
                        SMMessenger.errorLocale(player, "REMOVED_DUPLICATE_ITEM", "item", item.getType().toString(),
                            "hash", md5Checksum);
                    }

                    STEMCraft.warning(SMLocale.get("REMOVED_DUPLICATE_ITEM_FOR"), "player",
                        (player != null ? player.getName() : "unknown"), "item", item.getType().toString(), "hash",
                        md5Checksum);
                    inventory.remove(item);
                } else {
                    md5ChecksumList.add(md5Checksum);
                }
            }
        }
    }

    private static Boolean isDuplicateItem(Inventory inventory, ItemStack item) {
        if (!isItemTracked(item.getType())) {
            return false;
        }

        String itemMD5Checksum = getItemMD5Checksum(item);
        if (itemMD5Checksum == null) {
            return false;
        }

        ItemStack[] inventoryList = inventory.getContents();

        for (ItemStack inventoryItem : inventoryList) {
            if (inventoryItem != null && inventoryItem.getType() != Material.AIR
                && isItemTracked(inventoryItem.getType())) {
                String inventoryItemMD5Checksum = getItemMD5Checksum(inventoryItem);
                if (inventoryItemMD5Checksum.equals(itemMD5Checksum)) {
                    return true;
                }
            }
        }

        return false;
    }
}
