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

public class SMVillagerTrading extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getEventManager().registerEvent(InventoryOpenEvent.class, (listener, rawEvent) -> {
            InventoryOpenEvent event = (InventoryOpenEvent)rawEvent;

            if(event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                if (event.getInventory().getType() == InventoryType.MERCHANT) {
                    Merchant merchant = ((MerchantInventory) event.getInventory()).getMerchant();
                    List<MerchantRecipe> recipeList =  new ArrayList<MerchantRecipe>();

                    for (MerchantRecipe recipe : merchant.getRecipes()) {
                        ItemStack result = recipe.getResult();

                        // Replace emeralds with emerald blocks in the ingredients
                        List<ItemStack> ingredients = recipe.getIngredients();
                        List<ItemStack> newIngredients = new ArrayList<>();
                        Material newMaterial = Material.EMERALD_BLOCK;

                        if (result.getType() == Material.ENCHANTED_BOOK) {
                            newMaterial = Material.LAPIS_LAZULI;
                        }

                        for (ItemStack ingredient : ingredients) {
                            if (ingredient.getType() == Material.EMERALD) {
                                newIngredients.add(new ItemStack(newMaterial, ingredient.getAmount()));
                            } else {
                                newIngredients.add(ingredient);
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
