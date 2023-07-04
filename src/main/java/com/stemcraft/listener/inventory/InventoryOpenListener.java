package com.stemcraft.listener.inventory;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import com.stemcraft.component.ComponentLockdown;

public class InventoryOpenListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (ComponentLockdown.blockedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.YELLOW + "You are required to enter the login code before you can interact");
            return;
        }

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
    }
}
