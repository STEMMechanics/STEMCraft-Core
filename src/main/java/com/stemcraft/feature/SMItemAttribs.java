package com.stemcraft.feature;

import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import com.stemcraft.core.event.SMEvent;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;

public class SMItemAttribs extends SMFeature {
    private final NamespacedKey destroyOnDrop = new NamespacedKey(STEMCraft.getPlugin(), "destroy-on-drop");
    private final NamespacedKey noDrop = new NamespacedKey(STEMCraft.getPlugin(), "no-drop");

    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        SMEvent.register(PlayerDropItemEvent.class, (ctx) -> {
            PlayerDropItemEvent event = (PlayerDropItemEvent) ctx.event;
            ItemStack item = event.getItemDrop().getItemStack();
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                if (meta.getPersistentDataContainer().has(destroyOnDrop, PersistentDataType.BYTE)) {
                    event.getItemDrop().remove();
                }

                if (meta.getPersistentDataContainer().has(noDrop, PersistentDataType.BYTE)) {
                    event.setCancelled(true);
                }
            }
        });

        return true;
    }

    /**
     * Adds an attribute to the ItemStack with the given key and value.
     *
     * @param item The ItemStack to modify.
     * @param key The key for the attribute.
     * @param value The value for the attribute.
     */
    public static void addAttrib(ItemStack item, String key, String value) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey namespacedKey = new NamespacedKey(STEMCraft.getPlugin(), key);
            meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
            item.setItemMeta(meta);
        }
    }

    /**
     * Checks if the ItemStack has an attribute with the given key.
     *
     * @param item The ItemStack to check.
     * @param key The key for the attribute.
     * @return true if the attribute exists, false otherwise.
     */
    public static boolean hasAttrib(ItemStack item, String key) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey namespacedKey = new NamespacedKey(STEMCraft.getPlugin(), key);
            PersistentDataContainer container = meta.getPersistentDataContainer();
            return container.has(namespacedKey, PersistentDataType.STRING);
        }
        return false;
    }

    /**
     * Removes an attribute from the ItemStack with the given key.
     *
     * @param item The ItemStack to modify.
     * @param key The key for the attribute to remove.
     */
    public static void removeAttrib(ItemStack item, String key) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey namespacedKey = new NamespacedKey(STEMCraft.getPlugin(), key);
            meta.getPersistentDataContainer().remove(namespacedKey);
            item.setItemMeta(meta);
        }
    }

    /**
     * Retrieves an attribute from the ItemStack with the given key.
     *
     * @param item The ItemStack to check.
     * @param key The key for the attribute.
     * @param typeClass The class of the type you're expecting (String.class, Byte.class, etc.).
     * @return The value of the attribute, or null if not found or if the type doesn't match.
     */
    public static <T, Z> T getAttrib(ItemStack item, String key, Class<T> typeClass) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey namespacedKey = new NamespacedKey(STEMCraft.getPlugin(), key);
            PersistentDataType<Z, T> type = getDataTypeForClass(typeClass);
            if (type != null) {
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (container.has(namespacedKey, type)) {
                    return container.get(namespacedKey, type);
                }
            }
        }
        return null;
    }

    /**
     * Determines the PersistentDataType based on the class provided.
     *
     * @param typeClass The class of the type (String.class, Byte.class, etc.).
     * @return The corresponding PersistentDataType, or null if the class is unsupported.
     */
    @SuppressWarnings("unchecked")
    private static <T, Z> PersistentDataType<Z, T> getDataTypeForClass(Class<T> typeClass) {
        if (typeClass == String.class) {
            return (PersistentDataType<Z, T>) PersistentDataType.STRING;
        } else if (typeClass == Byte.class) {
            return (PersistentDataType<Z, T>) PersistentDataType.BYTE;
        } else if (typeClass == Integer.class) {
            return (PersistentDataType<Z, T>) PersistentDataType.INTEGER;
        } else if (typeClass == Double.class) {
            return (PersistentDataType<Z, T>) PersistentDataType.DOUBLE;
        } else if (typeClass == Float.class) {
            return (PersistentDataType<Z, T>) PersistentDataType.FLOAT;
        }
        // Add more types if needed
        return null;
    }
}
