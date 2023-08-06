package com.stemcraft.feature;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

public class SMRemoveRecipes extends SMFeature {
    @Override
    protected Boolean onEnable() {
        String value = this.plugin.getConfigManager().getConfig().registerValue("remove-recipes", "[]", "Remove the following receipes from the game");

        if(value.startsWith("[") && value.endsWith("]")) {
            String cleanedInput = value.replaceAll("\\[|\\]", "");
            String[] itemList = cleanedInput.split(",");

            for (int i = 0; i < itemList.length; i++) {
                String item = itemList[i].trim();

                NamespacedKey namespaceItem = NamespacedKey.fromString(item);

                Bukkit.removeRecipe(namespaceItem);
                System.out.println("Removed recipe " + namespaceItem.getNamespace() + ":" + namespaceItem.getKey());
            }
        } else {
            System.out.println("Config option remove-recipes is invalid");
        }

        return true;
    }
}
