package com.stemcraft.listener.inventory;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import de.tr7zw.nbtapi.NBT;

public class InventoryClickListener implements Listener {
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().getGameMode() == GameMode.SURVIVAL) {
            if ((event.getInventory() instanceof AnvilInventory)) {

                // Do not increase experiance costs on anvils
                ItemStack item = event.getCurrentItem();
                if (item != null && item.getType() != Material.AIR) {
                    NBT.modify(item, nbt -> {
                        nbt.removeKey("RepairCost");
                    });
                }
            } else if (event.getInventory().getType() == InventoryType.MERCHANT) {
                event.getWhoClicked().sendMessage("merchant");
                Merchant merchant = ((MerchantInventory) event.getInventory()).getMerchant();

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
                }
            }
        }
    }
}
