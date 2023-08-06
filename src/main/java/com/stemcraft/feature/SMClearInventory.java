package com.stemcraft.feature;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class SMClearInventory extends SMFeature {

    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("INV_CLEARED", "Inventory cleared");
        this.plugin.getLanguageManager().registerPhrase("INV_CLEARED_FOR", "Inventory cleared for %PLAYER_NAME%");
        this.plugin.getLanguageManager().registerPhrase("INV_CLEARED_BY", "Inventory cleared by %PLAYER_NAME%");

        String[] aliases = new String[]{"clearinv", "ci"};
        String[][] tabCompletion = new String[][]{
            {"clearinventory", "%player%"},
        };

        this.plugin.getCommandManager().registerCommand("clearinventory", (sender, command, label, args) -> {
            Player targetPlayer = null;

            if(args.length < 1) {
                if (sender instanceof Player) {
                    targetPlayer = (Player) sender;
                } else {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_REQ_FROM_CONSOLE");
                    return true;
                }
            } else {
                if (!sender.hasPermission("stemcraft.inventory.clear.other")) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                    return true;
                }

                targetPlayer = Bukkit.getPlayer(args[0]);
                if (targetPlayer == null) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_NOT_FOUND");
                    return true;
                }
            }

            PlayerInventory inventory = targetPlayer.getInventory();
            if(this.plugin.getFeatureManager().featureEnabled("GameModeInventories")) {
                SMGameModeInventories gmiInventories = (SMGameModeInventories)this.plugin.getFeatureManager().getFeature("SMGameModeInventories");
                
                gmiInventories.SaveInventory(targetPlayer, "Player cleared inventory");
            }

            inventory.clear();
            inventory.setArmorContents(null);


            if(targetPlayer == sender) {
                this.plugin.getLanguageManager().sendPhrase(sender, "INV_CLEARED");
            } else {
                HashMap<String, String> replacements = new HashMap<>();

                replacements.put("PLAYER_NAME", targetPlayer.getName());
                this.plugin.getLanguageManager().sendPhrase(sender, "INV_CLEARED_FOR", replacements);

                replacements.put("PLAYER_NAME", sender.getName());
                targetPlayer.sendMessage(this.plugin.getLanguageManager().getPhrase("INV_CLEARED_BY", replacements));
            }

            return true;
        }, aliases, tabCompletion);

        return true;
    }
}
