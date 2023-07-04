package com.stemcraft.component;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class ComponentRecipe extends SMComponent {
    // Chest
    NamespacedKey key = new NamespacedKey("minecraft", "chest");
    ItemStack chest = new ItemStack(Material.CHEST);
    ShapedRecipe customChestRecipe = new ShapedRecipe(key, chest);
        
    // Set the ingredients for the recipe
    customChestRecipe.shape("SLS", "SIS", "SLS");
    customChestRecipe.setIngredient('S', Material.STRIPPED_WOOD);
    customChestRecipe.setIngredient('L', Material.STRIPPED_LOG);
    customChestRecipe.setIngredient('I', Material.IRON_INGOT);
    
    // Add the recipe to the server
    Bukkit.addRecipe(customChestRecipe);
}
