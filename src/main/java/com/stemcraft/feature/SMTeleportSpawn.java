package com.stemcraft.feature;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SMTeleportSpawn extends SMFeature {
    @Override
    protected Boolean onEnable() {
        this.plugin.getLanguageManager().registerPhrase("TPSPAWN", "Teleported to the world spawn");
        this.plugin.getLanguageManager().registerPhrase("TPSPAWN_FOR", "Teleported %PLAYER_NAME% to the world spawn");
        this.plugin.getLanguageManager().registerPhrase("TPSPAWN_BY", "Teleported to the world spawn by %PLAYER_NAME%");

        String commandName = "tpspawn";
        String[] aliases = new String[]{};
        String[][] tabCompletions = new String[][]{
            {"teleportspawn", "%player%"},
        };

        this.plugin.getCommandManager().registerCommand(commandName, (sender, command, label, args) -> {
            Player targetPlayer = null;

            if (!sender.hasPermission("stemcraft.teleport.spawn") && !sender.hasPermission("stemcraft.teleport.spawn.other")) {
                this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                return true;
            }

            if(args.length < 1) {
                if (sender instanceof Player) {
                    targetPlayer = (Player) sender;
                } else {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_REQ_FROM_CONSOLE");
                    return true;
                }
            } else {
                if (!sender.hasPermission("stemcraft.teleport.spawn.other")) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_NO_PERMISSION");
                    return true;
                }

                targetPlayer = Bukkit.getPlayer(args[0]);
                if (targetPlayer == null) {
                    this.plugin.getLanguageManager().sendPhrase(sender, "CMD_PLAYER_NOT_FOUND");
                    return true;
                }
            }
            
            targetPlayer.teleport(targetPlayer.getWorld().getSpawnLocation());

            if(targetPlayer == sender) {
                this.plugin.getLanguageManager().sendPhrase(sender, "TPSPAWN");
            } else {
                HashMap<String, String> replacements = new HashMap<>();
                        
                replacements.put("PLAYER_NAME", targetPlayer.getName());
                this.plugin.getLanguageManager().sendPhrase(sender, "TPSPAWN_FOR", replacements);

                replacements.put("PLAYER_NAME", sender.getName());
                this.plugin.getLanguageManager().sendPhrase(sender, "TPSPAWN_BY", replacements);
            }

            return true;
        }, aliases, tabCompletions);

        return true;
    }
}
