package com.stemcraft.feature;

import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.StonecuttingRecipe;
import com.stemcraft.STEMCraft;
import com.stemcraft.core.SMBridge;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.config.SMConfig;

public class SMRecipes extends SMFeature {

    /**
     * When feature is enabled
     */
    @Override
    protected Boolean onEnable() {

        // Remove Recipes
        List<String> itemList = SMConfig.main().getStringList("recipes.remove");
        for (String item : itemList) {
            if (item.length() > 0) {
                NamespacedKey namespaceItem = NamespacedKey.fromString(item);

                Bukkit.removeRecipe(namespaceItem);
                STEMCraft.info("Removed recipe " + namespaceItem.getNamespace() + ":" + namespaceItem.getKey());
            }
        }

        // Stonecutter Recipes
        List<String> stonecutterKeys = SMConfig.main().getKeys("recipes.stonecutter");
        for (String stonecutterItem : stonecutterKeys) {
            Material source = Material.getMaterial(stonecutterItem.toUpperCase());

            if (source == null) {
                STEMCraft.info(
                    "Could not add stonecutter recipes for " + stonecutterItem + " as the material was not found");
                continue;
            }

            String transformedStonecutterItem = stonecutterItem.replace(":", "_");
            Map<String, Integer> stonecutterResults =
                SMConfig.main().getIntMap("recipes.stonecutter." + transformedStonecutterItem);

            for (String stonecutterResultItem : stonecutterResults.keySet()) {
                String transformedStonecutterResultItem = stonecutterResultItem.replace(":", "_");
                NamespacedKey key =
                    NamespacedKey.fromString(transformedStonecutterItem + "_to_" + transformedStonecutterResultItem);

                if (Bukkit.getRecipe(key) != null) {
                    Bukkit.removeRecipe(key);
                }

                ItemStack result =
                    SMBridge.newItemStack(stonecutterResultItem, stonecutterResults.get(stonecutterResultItem));
                StonecuttingRecipe recipe = new StonecuttingRecipe(key, result, source);
                Bukkit.addRecipe(recipe);

                STEMCraft.info("Added stonecutter recipe " + stonecutterItem + " to " + stonecutterResultItem);
            }
        }

        return true;
    }
}
