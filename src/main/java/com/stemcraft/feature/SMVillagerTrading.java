package com.stemcraft.feature;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import com.stemcraft.core.SMFeature;
import com.stemcraft.core.event.SMEvent;

/**
 * Increases the costs of buying from villagers
 */
public class SMVillagerTrading extends SMFeature {
    
    /**
     * When the feature is enabled
     */
    @Override
    protected Boolean onEnable() {
        SMEvent.register(InventoryOpenEvent.class, ctx -> {
            if(ctx.event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                if (ctx.event.getInventory().getType() == InventoryType.MERCHANT) {
                    Merchant merchant = ((MerchantInventory) ctx.event.getInventory()).getMerchant();
                    List<MerchantRecipe> recipeList =  new ArrayList<MerchantRecipe>();

                    for (MerchantRecipe recipe : merchant.getRecipes()) {
                        ItemStack result = recipe.getResult();

                        // Replace emeralds with emerald blocks in the ingredients
                        List<ItemStack> ingredients = recipe.getIngredients();
                        List<ItemStack> newIngredients = new ArrayList<>();
                        Material newMaterial = Material.EMERALD_BLOCK;

                        // Enchanted books results, the ingredient should be Lapis instead
                        if (result.getType() == Material.ENCHANTED_BOOK) {
                            newMaterial = Material.LAPIS_LAZULI;
                        }

                        for (ItemStack ingredient : ingredients) {
                            if (ingredient.getType() == Material.EMERALD) {
                                newIngredients.add(new ItemStack(newMaterial, ingredient.getAmount()));
                            } else {
                                newIngredients.add(ingredient);
                            }

                            // If the result is emerald, change to iron ingot
                            if (result.getType() == Material.EMERALD) {
                                newIngredients.add(new ItemStack(Material.IRON_INGOT, result.getAmount()));
                            }

                        }

                        recipe.setIngredients(newIngredients);
                        recipeList.add(recipe);
                    }

                    merchant.setRecipes(recipeList);
                }
            }
        });

        return true;
    }
}
