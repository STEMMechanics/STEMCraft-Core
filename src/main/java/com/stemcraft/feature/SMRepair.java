package com.stemcraft.feature;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class SMRepair extends SMFeature {

    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("REPAIR_USAGE", "Usage: /repair (hand|all) (player)");
        this.plugin.getLanguageManager().registerPhrase("REPAIR_NOT_REPAIRABLE", "Player is not holding anything repairable");
        this.plugin.getLanguageManager().registerPhrase("REPAIR_HAND_REPAIRED", "Item in hand repaired");
        this.plugin.getLanguageManager().registerPhrase("REPAIR_ALL_REPAIRED", "All inventory items repaired");

        String[] aliases = new String[]{};
        String[][] tabCompletion = new String[][]{
            {"repair", "hand", "%player%"},
            {"repair", "all", "%player%"},
        };

        this.plugin.getCommandManager().registerCommand("repair", (sender, command, label, args) -> {
            Player targetPlayer = null;
            Boolean all = false;

            if(args.length < 2) {
                if (sender instanceof Player) {
                    targetPlayer = (Player) sender;
                } else {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_REQ_FROM_CONSOLE");
                    return true;
                }
            } else {
                if (!sender.hasPermission("stemcraft.inventory.repair")) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                    return true;
                }

                if(args.length >= 1) {
                    if(args[0].compareToIgnoreCase("hand") == 0) {
                        all = false;
                    } else if(args[1].compareToIgnoreCase("all") == 0) {
                        all = true;
                    } else {
                        this.plugin.getLanguageManager().sendPhrase(sender, "REPAIR_USAGE");
                        return true;
                    }
                }
                
                if(args.length >= 2) {
                    targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer == null) {
                        this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_NOT_FOUND");
                        return true;
                    }
                } else {
                    targetPlayer = (Player) sender;
                }
            }

            PlayerInventory inventory = targetPlayer.getInventory();
            if(all == false) {
                ItemStack item = inventory.getItemInMainHand();
                if(this.itemRepairable(item) == true) {
                    item = this.itemRepair(item);
                    inventory.setItemInMainHand(item);
                    this.plugin.getLanguageManager().sendPhrase(sender, "REPAIR_HAND_REPAIRED");
                } else {
                    this.plugin.getLanguageManager().sendPhrase(sender, "REPAIR_NOT_REPAIRABLE");
                }
            } else {
                ItemStack[] items = inventory.getContents();

                for(ItemStack item : items) {
                    if(item != null && this.itemRepairable(item) == true) {
                        item = this.itemRepair(item);
                    }
                }

                targetPlayer.updateInventory();
            }

            return true;
        }, aliases, tabCompletion);

        return true;
    }

    private Boolean itemRepairable(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        return (itemMeta instanceof Damageable);
    }
    
    private ItemStack itemRepair(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof Damageable) {
            Damageable damageable = (Damageable)itemMeta;
            damageable.setDamage(0);
            item.setItemMeta(itemMeta);
        }

        return item;
    }
}
