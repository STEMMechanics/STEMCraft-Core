package com.stemcraft.listener.inventory;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
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
            }
        }
    }
}
