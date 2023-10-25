package com.stemcraft.feature;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.config.SMConfig;

public class SMRemoveRecipes extends SMFeature {

    /**
     * When feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        List<String> itemList = SMConfig.main().getStringList("remove-recipes");

        for (String item : itemList) {
            if(item.length() > 0) {
                NamespacedKey namespaceItem = NamespacedKey.fromString(item);

                Bukkit.removeRecipe(namespaceItem);
                STEMCraft.info("Removed recipe " + namespaceItem.getNamespace() + ":" + namespaceItem.getKey());
            }
        }

        return true;
    }
}
