package com.stemcraft.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.stemcraft.core.config.SMConfig;

public class SMItemLore {
    private static final Map<String, Function<ItemStack, List<String>>> loreSuppliers = new HashMap<>();

    public static void register(String id, Function<ItemStack, List<String>> supplier) {
        loreSuppliers.put(id, supplier);
    }

    public static void updateLore(ItemStack item) {
        List<String> loreOrder = SMConfig.main().getStringList("item-lore");
        List<String> lore = new ArrayList<>();
        Boolean hasLore = false;

        for(String loreId : loreOrder) {
            if(loreId.isEmpty()) {
                lore.add("");
            } else {
                if(loreSuppliers.containsKey(loreId)) {
                    List<String> result = loreSuppliers.get(loreId).apply(item);

                    if (result != null && !result.isEmpty()) {
                        lore.addAll(result);
                        hasLore = true;
                    }
                }
            }
        }

        if(hasLore) {
            ItemMeta meta = item.getItemMeta();
            meta.setLore(SMCommon.colorizeAll(lore));
            item.setItemMeta(meta);
        }
    }
}
