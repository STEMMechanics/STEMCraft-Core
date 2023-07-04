package com.stemcraft.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;

public class ComponentRecipe extends SMComponent {
    public void load() {
        NamespacedKey key = new NamespacedKey("minecraft", "chest");
        
        Bukkit.removeRecipe(key);

        ItemStack chest = new ItemStack(Material.CHEST);
        ShapedRecipe customChestRecipe = new ShapedRecipe(key, chest);

        List<Material> strippedWoodMaterials = new ArrayList<>();
        List<Material> strippedLogMaterials = new ArrayList<>();
        for (Material material : Material.values()) {
            if (material.name().endsWith("_WOOD")) {
                strippedWoodMaterials.add(material);
            } else if (material.name().endsWith("_LOG")) {
                strippedLogMaterials.add(material);
            }
        }

        customChestRecipe.shape("SLS", "SIS", "SLS");
        List<Material> strippedWoodList = Arrays.asList(Material.STRIPPED_ACACIA_WOOD, Material.STRIPPED_BIRCH_WOOD, Material.STRIPPED_CHERRY_WOOD, Material.STRIPPED_DARK_OAK_WOOD, Material.STRIPPED_JUNGLE_WOOD, Material.STRIPPED_MANGROVE_WOOD, Material.STRIPPED_OAK_WOOD, Material.STRIPPED_SPRUCE_WOOD);
        List<Material> strippedLogList = Arrays.asList(Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_BIRCH_LOG, Material.STRIPPED_CHERRY_LOG, Material.STRIPPED_DARK_OAK_LOG, Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG);

        MaterialChoice strippedWood = new RecipeChoice.MaterialChoice(strippedWoodList);
        MaterialChoice strippedLog = new RecipeChoice.MaterialChoice(strippedLogList);

        customChestRecipe.setIngredient('S', strippedWood);
        customChestRecipe.setIngredient('L', strippedLog);
        customChestRecipe.setIngredient('I', Material.IRON_INGOT);

        Bukkit.addRecipe(customChestRecipe);
    }
}
