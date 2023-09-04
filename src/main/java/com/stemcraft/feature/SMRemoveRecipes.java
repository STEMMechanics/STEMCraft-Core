package com.stemcraft.feature;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

public class SMRemoveRecipes extends SMFeature {
    @Override
    protected Boolean onEnable() {
        List<String> itemList = this.plugin.getConfigManager().getConfig().registerStringList("remove-recipes", null, "Remove the following receipes from the game");

        for (String item : itemList) {
            if(item.length() > 0) {
                NamespacedKey namespaceItem = NamespacedKey.fromString(item);

                Bukkit.removeRecipe(namespaceItem);
                this.plugin.getLogger().info("Removed recipe " + namespaceItem.getNamespace() + ":" + namespaceItem.getKey());
            }
        }

        return true;
    }
}
