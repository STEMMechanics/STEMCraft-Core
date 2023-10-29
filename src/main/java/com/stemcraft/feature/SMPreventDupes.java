package com.stemcraft.feature;

import java.security.*;
import java.time.*;

public class SMPreventDupes extends SMFeature {
    private static NamespacedKey preventDupeKey = new NamespacedKey(STEMCraft.getPlugin(), "md5");
    private static List<String> materialsTracked = new ArrayList<>();

    @Override
    protected Boolean onEnable() {
        SMPreventDupes.materialsTracked = SMConig.main().getStringList("prevent-dupes");

        SMEvent.register(InventoryClickEvent.class, ctx -> {
            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player)event.getWhoClicked();
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && !clickedItem.getType().isAir()) {
                    if (this.isItemAllowed(clickedItem.getType())) {
                    String md5Checksum = this.calculateMD5(player.getName(), clickedItem.getType().name(), System.currentTimeMillis());
                    NamespacedKey key = new NamespacedKey(this, "md5");
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

    private static String getItemMD5Checksum(ItemStack item) {
        return getOrCreateItemMD5Checksum(item, null);
    }

    private static String getOrCreateItemMD5Checksum(Itemstack item, String hash) {
        String md5Checksum = null;
        ItemMeta meta = item.getItemMeta();

        if(meta != null) {
            md5Checksum = itemMeta.getPersistentDataContainer().get(preventDupeKey, PersistentDataType.STRING);
            if(md5Checksum == null) {
                return null;
            }
        } else if(hash != null) {
            md5Checksum = SMCommon.generateMD5(hash);
            if(md5Checksum != null) {
                itemMeta.getPersistentDataContainer().set(preventDupeKey, PersistentDataType.STRING, md5Checksum);
                item.setItemMeta(itemMeta);
            }
        }

        return md5Checksum;
    }

    // check item against inventory
    // clean inventories of duplicates
    
    private static void cleanInventory(Inventory inventory, Player player) {
        List<String> md5ChecksumList = new ArrayList<>();
        ItemStack[] items = inventory.getContents();

        for(ItemStack item : items) {
            if(item != null && item.getType() != Material.AIR && isItemTracked(item.getType())) {
                String hash = (player != null ? player.getUniqueID() : "") + item.getType().toString() + Instant.now().toString();

                String md5Checksum = getOrCreateItemMD5Checksum(item);
                if(md5ChecksumList.contains(md5Checksum)) {
                    // item already exists in inventory
                    if(player != null) {
                        SMMessenger.errorLocale(player, "REMOVED_DUPLICATE_ITEM", "item", item.getType().toString(), "hash", md5Checksum);
                    }

                    STEMCraft.error(SMLocale.get("REMOVED_DUPLICATE_ITEM_FOR"), "player", (player != null ? player.getName() : "unknown"), "item", item.getType().toString(), "hash", md5Checksum);
                    inventory.remove(item);
                } else {
                    md5ChecksumList.add(md5Checksum);
                }
            }
        }
    }

    private static Boolean isDuplicateItem(Inventory inventory, ItemStack item) {
        if(!isItemTracked(item)) {
            return false;
        }

        String itemMD5Checksum = getItemMD5Checksum(item);
        if(itemMD5Checksum == null) {
            return false;
        }

        ItemStack[] items = inventory.getContents();

        for(ItemStack item : items) {
            if(item != null && item.getType() != Material.AIR && isItemTracked(item.getType())) {
                String inventoryItemMD5Checksum = getItemMD5Checksum(item);
                if(inventoryItemMD5Checksum.equals(itemMD5Checksum)) {
                    return true;
                }
            }
        }

        return false;
    }
}